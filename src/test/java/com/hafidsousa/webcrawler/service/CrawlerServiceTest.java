package com.hafidsousa.webcrawler.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.hafidsousa.webcrawler.config.Utils;
import com.hafidsousa.webcrawler.model.EStatus;
import com.hafidsousa.webcrawler.model.Profiles;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Hafid Ferreira Sousa
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(value = Profiles.MOCK_ENVIRONMENT)
public class CrawlerServiceTest extends BaseServiceTest {

    @Autowired
    private ICrawlerService crawlerService;

    @MockBean
    private AmazonDynamoDBAsync amazonDynamoDBAsync;

    @MockBean
    private AmazonSQSAsync sqs;

    @Value("${aws.sqs.endpoint}")
    private String SQS_ENDPOINT;

    /**
     * If Website exists returns record from Database
     *
     * @throws Exception
     */
    @Test
    public void testGetExistingWebsites() throws Exception {

        String url = "http://example.com";

        given(this.amazonDynamoDBAsync.getItemAsync(this.getItemRequest(url)))
                .willReturn(this.getItemResult(url));

        Mono<Map<String, Object>> source = crawlerService.getWebsites(url);

        StepVerifier.create(source)
                .expectNext(getMockedWebsites(url))
                .verifyComplete();

        verify(this.amazonDynamoDBAsync, times(1)).getItemAsync(this.getItemRequest(url));
    }


    /**
     * If Website doesn't exist:
     * - Add entry to Database and send Message to Queue for processing
     *
     * @throws Exception
     */
    @Test
    public void testGetNonExistingWebsites() throws Exception {

        String url = "http://example.com";

        given(this.amazonDynamoDBAsync.getItemAsync(this.getItemRequest(url)))
                .willReturn(this.getNullItemResult());

        given(this.amazonDynamoDBAsync.putItemAsync(this.putItemRequest(url, EStatus.NEW)))
                .willReturn(this.putItemResult(url, EStatus.NEW));

        given(this.sqs.sendMessageAsync(this.getSendMessageRequest(url)))
                .willReturn(this.getSendMessageResult());

        Mono<Map<String, Object>> source = crawlerService.getWebsites(url);

        StepVerifier.create(source)
                .expectNext(getMockedWebsites(url))
                .verifyComplete();

        verify(this.amazonDynamoDBAsync, times(1)).getItemAsync(this.getItemRequest(url));
        verify(this.amazonDynamoDBAsync, times(1)).putItemAsync(this.putItemRequest(url, EStatus.NEW));
        verify(this.sqs, times(1)).sendMessageAsync(this.getSendMessageRequest(url));
    }

    private Map<String, Object> getMockedWebsites(String url) {

        Map<String, Object> response = new HashMap<>();
        response.put("url", url);
        response.put("status", "NEW");

        return response;
    }

    private GetItemRequest getItemRequest(String url) {

        GetItemRequest request = new GetItemRequest();
        request.setTableName(Utils.table.websites);
        HashMap<String, AttributeValue> key = new HashMap<>();
        key.put(Utils.params.url, new AttributeValue(url));
        request.setKey(key);

        return request;
    }

    private Future<GetItemResult> getItemResult(String url) {

        GetItemResult getItemResult = new GetItemResult();
        HashMap<String, AttributeValue> result = new HashMap<>();
        result.put(Utils.params.url, new AttributeValue(url));
        result.put(Utils.params.status, new AttributeValue(EStatus.NEW.name()));
        getItemResult.setItem(result);

        return ConcurrentUtils.constantFuture(getItemResult);
    }

    private Future<GetItemResult> getNullItemResult() {

        GetItemResult getItemResult = new GetItemResult();

        return ConcurrentUtils.constantFuture(getItemResult);
    }

    private SendMessageRequest getSendMessageRequest(String url) {

        return new SendMessageRequest(
                SQS_ENDPOINT,
                url
        );
    }

    private Future<SendMessageResult> getSendMessageResult() {

        SendMessageResult sendMessageResult = new SendMessageResult();

        return ConcurrentUtils.constantFuture(sendMessageResult);
    }
}
