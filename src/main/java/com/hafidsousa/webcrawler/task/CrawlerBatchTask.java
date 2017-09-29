package com.hafidsousa.webcrawler.task;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.hafidsousa.webcrawler.config.Utils;
import com.hafidsousa.webcrawler.model.EStatus;
import com.hafidsousa.webcrawler.model.Profiles;
import com.hafidsousa.webcrawler.model.WebsiteModel;
import com.hafidsousa.webcrawler.task.service.ICrawlerBatchService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hafid Ferreira Sousa
 */
@Service
public class CrawlerBatchTask implements ICrawlerBatchTask {

    public CrawlerBatchTask(AmazonDynamoDBAsync dynamoDBAsync, ICrawlerBatchService crawlerBatchService) {

        this.dynamoDBAsync = dynamoDBAsync;
        this.crawlerBatchService = crawlerBatchService;
    }

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerBatchTask.class);

    private AmazonDynamoDBAsync dynamoDBAsync;

    private ICrawlerBatchService crawlerBatchService;

    @Override
    @JmsListener(destination = "CRAWL_WEBSITES")
    public void getDeepCrawling(String urlString) throws URISyntaxException {

        LOG.info("Received " + urlString);

        WebsiteModel seedWebsite = new WebsiteModel();
        seedWebsite.setUrl(urlString);

        putItemResultMono(urlString, EStatus.PROCESSING, null, null).block();

        WebsiteModel result = crawlerBatchService.getWebsites(
                seedWebsite,
                null,
                new URI(urlString),
                0
        ).blockLast();

        if (result != null) {
            putItemResultMono(urlString, EStatus.COMPLETED, result.getTitle(), result).block();
        }
    }

    private Mono<Map<String, AttributeValue>> putItemResultMono(
            String seedUrl,
            EStatus status,
            String title,
            WebsiteModel websiteModel
    ) {

        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(Utils.table.websites);

        Map<String, AttributeValue> newWebsite = new HashMap<>();

        if (Objects.nonNull(websiteModel)) newWebsite = crawlerBatchService.getNodes(websiteModel);
        newWebsite.put(Utils.params.url, new AttributeValue(seedUrl));
        newWebsite.put(Utils.params.status, new AttributeValue(status.name()));
        if (StringUtils.isNotEmpty(title)) newWebsite.put(Utils.params.title, new AttributeValue(title));

        putItemRequest.setItem(newWebsite);

        return Mono.fromFuture(
                Utils.makeCompletableFuture(
                        dynamoDBAsync.putItemAsync(putItemRequest)))
                .doOnError((throwable -> LOG.error(Utils.error.failed_dynamo_put, seedUrl)))
                .doOnSuccess((a) -> LOG.info(Utils.success.saved_dynamo, String.format("%s [%s]", seedUrl, status)))
                .map(((result) -> putItemRequest.getItem()));
    }
}
