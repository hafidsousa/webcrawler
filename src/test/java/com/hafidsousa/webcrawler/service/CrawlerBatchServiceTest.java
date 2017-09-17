package com.hafidsousa.webcrawler.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Hafid Ferreira Sousa
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlerBatchServiceTest {

    @Autowired
    private ICrawlerBatchService crawlerBatchService;

    @Test
    public void getDeepCrawling() throws Exception {

        crawlerBatchService.getDeepCrawling("https://spring.io");
    }

    @Test
    public void getWebsites() throws Exception {

    }


}
