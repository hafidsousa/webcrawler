package com.hafidsousa.webcrawler.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URISyntaxException;

/**
 * @author Hafid Ferreira Sousa
 */
public interface ICrawlerBatchService {

    void getDeepCrawling(String requestJSON) throws URISyntaxException, JsonProcessingException;
}
