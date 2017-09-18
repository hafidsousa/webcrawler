package com.hafidsousa.webcrawler.task;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.hafidsousa.webcrawler.model.EStatus;
import com.hafidsousa.webcrawler.model.Profiles;
import com.hafidsousa.webcrawler.model.WebsiteModel;
import com.hafidsousa.webcrawler.service.BaseServiceTest;
import com.hafidsousa.webcrawler.task.service.ICrawlerBatchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import javax.jms.TextMessage;
import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Hafid Ferreira Sousa
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(Profiles.MOCK_ENVIRONMENT)
//@Import(TestConfig.class)
public class CrawlerBatchTaskTest extends BaseServiceTest {

    @MockBean
    JmsTemplate mockJmsTemplate;

    @Autowired
    private ICrawlerBatchTask crawlerBatchTask;

    @MockBean
    private ICrawlerBatchService crawlerBatchService;

    @MockBean
    private AmazonDynamoDBAsync amazonDynamoDBAsync;

    @MockBean
    private AmazonSQSAsync amazonSQSAsync;

    @Before
    public void setUp() throws Exception {

        TextMessage message = mock(TextMessage.class);
        given(this.mockJmsTemplate.getMessageConverter()).willReturn(new SimpleMessageConverter());
        given(this.mockJmsTemplate.receiveSelected(anyString())).willReturn(message);
    }

    @Test
    public void getDeepCrawling() throws Exception {

        String url = "http://localhost:8089/example";

        WebsiteModel seedWebsite = new WebsiteModel();
        seedWebsite.setUrl(url);

        Flux<WebsiteModel> flux = Flux.just(seedWebsite);

        given(crawlerBatchService.getWebsites(
                seedWebsite,
                null,
                new URI(url),
                0
        )).willReturn(flux);

        given(this.amazonDynamoDBAsync.putItemAsync(this.putItemRequest(url, EStatus.PROCESSING)))
                .willReturn(this.putItemResult(url, EStatus.PROCESSING));

        given(this.amazonDynamoDBAsync.putItemAsync(this.putItemRequest(url, EStatus.COMPLETED)))
                .willReturn(this.putItemResult(url, EStatus.COMPLETED));

        crawlerBatchTask.getDeepCrawling(url);

    }
}
