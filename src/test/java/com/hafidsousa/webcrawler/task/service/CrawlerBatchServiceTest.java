package com.hafidsousa.webcrawler.task.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hafidsousa.webcrawler.TestUtils;
import com.hafidsousa.webcrawler.model.Profiles;
import com.hafidsousa.webcrawler.model.WebsiteModel;
import com.hafidsousa.webcrawler.service.BaseServiceTest;
import com.hafidsousa.webcrawler.service.ICrawlerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Hafid Ferreira Sousa
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(Profiles.MOCK_ENVIRONMENT)
public class CrawlerBatchServiceTest extends BaseServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Autowired
    private ICrawlerBatchService crawlerBatchService;

    @MockBean
    private AmazonDynamoDBAsync amazonDynamoDBAsync;

    @MockBean
    private ICrawlerService crawlerService;

    @Before
    public void setUp() throws Exception {

        stubFor(get(urlEqualTo("/example"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "text/html")
                                            .withBody(TestUtils.seedBody)));

        stubFor(get(urlEqualTo("/example/inner1"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "text/html")
                                            .withBody(TestUtils.inner1)));

        stubFor(get(urlEqualTo("/example/inner1/inner11"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "text/html")
                                            .withBody(TestUtils.inner11)));

        stubFor(get(urlEqualTo("/example/inner1/inner11/inner111"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "text/html")
                                            .withBody(TestUtils.inner111)));
    }

    @Test
    public void getWebsites() throws Exception {

        String url = "http://localhost:8089/example";

        WebsiteModel seedWebsite = new WebsiteModel();
        seedWebsite.setUrl(url);

        Flux<WebsiteModel> flux = crawlerBatchService.getWebsites(
                seedWebsite,
                null,
                new URI(url),
                0
        );

        flux.blockLast();

        assertEquals(1, seedWebsite.getNodes().size());
        assertEquals("Seed URL", seedWebsite.getTitle());

        ArrayList<WebsiteModel> first = new ArrayList<>(seedWebsite.getNodes());
        assertEquals(1, first.size());
        assertEquals("Inner 1", first.get(0).getTitle());

        ArrayList<WebsiteModel> second = new ArrayList<>(first.get(0).getNodes());
        assertEquals(1, second.size());
        assertEquals("Inner 11", second.get(0).getTitle());

        ArrayList<WebsiteModel> third = new ArrayList<>(second.get(0).getNodes());
        assertEquals(1, third.size());
        assertEquals("Inner 111", third.get(0).getTitle());

        ArrayList<WebsiteModel> fourth = new ArrayList<>(third.get(0).getNodes());
        assertEquals(0, fourth.size());

    }

    @Test
    public void getNodes() throws Exception {

        String url = "http://localhost:8089/example";

        WebsiteModel seedWebsite = new WebsiteModel();
        seedWebsite.setUrl(url);

        WebsiteModel node1 = new WebsiteModel();
        node1.setUrl("http://localhost:8089/example/inner1");

        seedWebsite.getNodes().add(node1);

        Map<String, AttributeValue> nodes = crawlerBatchService.getNodes(seedWebsite);
    }
}
