package com.hafidsousa.webcrawler.service;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Hafid Ferreira Sousa
 */
public interface ICrawlerService {

    Mono<Map<String, Object>> getWebsites(String url);

}
