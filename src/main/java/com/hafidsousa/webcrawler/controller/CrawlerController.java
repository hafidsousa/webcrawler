package com.hafidsousa.webcrawler.controller;

import com.hafidsousa.webcrawler.service.ICrawlerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Hafid Ferreira Sousa
 */
@RestController
@RequestMapping("v1")
public class CrawlerController {

    private ICrawlerService crawlerService;

    public CrawlerController(ICrawlerService crawlerService) {

        this.crawlerService = crawlerService;
    }

    @GetMapping(value = "/deep-crawling/reactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> getDeepCrawlingReactive(
            @RequestParam(value = "url") String url
    ) {

        return crawlerService.getWebsites(url);
    }
}
