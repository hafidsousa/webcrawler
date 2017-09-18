package com.hafidsousa.webcrawler.task.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hafidsousa.webcrawler.config.Utils;
import com.hafidsousa.webcrawler.model.WebsiteModel;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Hafid Ferreira Sousa
 */
@Service
public class CrawlerBatchService implements ICrawlerBatchService {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerBatchService.class);

    private ConcurrentHashMap<String, WebsiteModel> urls;

    @Value("${max.depth}")
    private Integer MAX_DEPTH;

    @Override
    public Flux<WebsiteModel> getWebsites(
            WebsiteModel seedWebsite,
            WebsiteModel parentWebsite,
            URI currentUrl,
            Integer depth
    ) {

        if (depth == 0) urls = new ConcurrentHashMap<>();

        if (depth > MAX_DEPTH) return Flux.just(seedWebsite);

        return Flux.merge(
                Flux.empty(),
                Flux.from(this.getBody(currentUrl))
                        .flatMap((body) -> getWebsiteModel(seedWebsite, parentWebsite, currentUrl.toString(), body, depth))
                        .flatMap((website) -> getParsedUrl(website, seedWebsite.getUrl(), depth))
                        .flatMap((website) -> {
                            LOG.info(Utils.success.log_recursion, depth, seedWebsite.getUrl(), website.getUrl());
                            try
                            {
                                urls.put(website.getUrl(), website);
                                return getWebsites(seedWebsite, website.getParent(), new URI(website.getUrl()), depth + 1);

                            }
                            catch (URISyntaxException e)
                            {
                                LOG.error(e.getMessage());
                                return Flux.empty();
                            }
                        })
        );
    }

    @Override
    public Map<String, AttributeValue> getNodes(WebsiteModel websiteModel) {

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String string = mapper.writeValueAsString(websiteModel);
            Item item = new Item().withJSON(Utils.params.nodes, string);
            return InternalUtils.toAttributeValues(item);
        }
        catch (JsonProcessingException e)
        {
            LOG.error(e.getMessage());
        }

        return new HashMap<>();
    }

    private Mono<String> getBody(URI currentUrl) {

        return WebClient.create().get()
                .uri(currentUrl)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .filter(clientResponse -> clientResponse.statusCode() == HttpStatus.OK)
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .doOnError(throwable -> LOG.error(Utils.error.failed_get_website, currentUrl))
                .onErrorResume(throwable -> Mono.never());
    }

    private Flux<WebsiteModel> getWebsiteModel(WebsiteModel seedWebsite, WebsiteModel parentWebsite, String currentUrl, String content, Integer depth) {

        return Mono.just(content)
                .flatMapIterable((body) -> {
                    Document document = Jsoup.parse(body, currentUrl);
                    Elements elements = document.select(Utils.document.links);

                    WebsiteModel currentWebsite = getCurrentWebsite(
                            seedWebsite,
                            parentWebsite,
                            currentUrl,
                            depth,
                            document
                    );

                    Set<WebsiteModel> nodes = elements.stream()
                            .map(
                                    (link) -> {
                                        WebsiteModel childWebsite = new WebsiteModel();
                                        String linkUrl = link.attr(Utils.document.link_url);
                                        childWebsite.setUrl(linkUrl);
                                        childWebsite.setParent(depth == 0 ? seedWebsite : currentWebsite);
                                        return childWebsite;
                                    })
                            .collect(Collectors.toSet());

                    currentWebsite.setNodes(nodes);

                    return nodes;

                });
    }

    private Mono<WebsiteModel> getParsedUrl(WebsiteModel currentWebsite, String seedUrl, Integer depth) {

        return Mono.just(currentWebsite)
                .flatMap((website) -> filter(seedUrl, website, depth))
                .map((website) -> {
                    website.setUrl(StringUtils.split(website.getUrl(), "?")[0]);
                    return website;
                })
                .map((website) -> {
                    website.setUrl(StringUtils.split(website.getUrl(), "#")[0]);
                    return website;
                })
                .filter(website -> !urls.containsKey(website.getUrl()));
    }

    private Mono<WebsiteModel> filter(String seedUrl, WebsiteModel currentWebsite, Integer depth) {

        return Mono.just(currentWebsite)
                .filter(website -> StringUtils.isNotEmpty(website.getUrl()) && (website.getUrl().startsWith(seedUrl) || depth == 0))
                .filter(website -> !StringUtils.startsWith(website.getUrl(), seedUrl + "/search"))
                .filter(website -> StringUtils.startsWith(website.getUrl(), "http"));
    }

    private WebsiteModel getCurrentWebsite(
            WebsiteModel seedWebsite,
            WebsiteModel parentWebsite,
            String currentUrl,
            Integer depth,
            Document document
    ) {

        WebsiteModel currentWebsite = urls.get(currentUrl);

        if (currentWebsite == null)
        {
            currentWebsite = new WebsiteModel();
        }

        if (depth == 0)
        {
            seedWebsite.setTitle(document.title());
            seedWebsite.setUrl(currentUrl);
        }
        else
        {
            currentWebsite.setTitle(document.title());
            currentWebsite.setUrl(currentUrl);
            parentWebsite.getNodes().add(currentWebsite);
        }

        return currentWebsite;
    }
}
