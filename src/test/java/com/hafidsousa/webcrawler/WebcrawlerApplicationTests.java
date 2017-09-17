package com.hafidsousa.webcrawler;

import com.hafidsousa.webcrawler.controller.CrawlerController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebcrawlerApplicationTests {

    private static final Logger LOG = LoggerFactory.getLogger(WebcrawlerApplicationTests.class);

    @Autowired
    private CrawlerController crawlerController;

    private WebTestClient webTestClient;

    private WebClient webClient = WebClient.create();

    @Before
    public void setUp() throws Exception {

        webTestClient = WebTestClient.bindToController(crawlerController).build();

    }

    @Test
    public void contextLoads() {

    }

    @Test
    public void TestWebClient() {

        webTestClient.get().uri("/v1/deep-crawling/reactive?url=http://psy-lob-saw.blogspot.com.au")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
                .expectBody(String.class)
                .returnResult();


//        StepVerifier.create(result.getResponseBody())
//                .expectNextCount(3)
////                .consumeNextWith(p -> assertEquals("John", p.getName()))
//                .thenCancel()
//                .verify();
    }

    @Test
    public void testFlatMap() {

        Flux.just("red", "white", "blue")
                .log()
                .flatMap(
                        value ->
                                Mono.just(value.toUpperCase())
                                        .subscribeOn(Schedulers.parallel()), 3
                )
                .subscribe(value -> {
                    LOG.info("Consumed: " + value);
                });
    }

    @Test
    public void testRecursion() {

        final int maxPage = 15;

        Flux.create(subscriber -> getInteger(1, 5, maxPage, subscriber)).subscribe();
    }

    private void getInteger(final int pageStart, final int pageSize, final int maxPageNum, FluxSink<? super Integer> subscriber) {

        Flux.range(pageStart, pageSize)
                .log()
                .doOnComplete(
                        () -> {
                            int newPageStart = pageStart + pageSize;
                            if (newPageStart >= maxPageNum)
                            {
                                subscriber.complete();
                            }
                            else
                            {
                                getInteger(newPageStart, pageSize, maxPageNum, subscriber);
                            }
                        }
                ).subscribe(subscriber::next);
    }
}
