package com.hafidsousa.webcrawler.task;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URISyntaxException;

/**
 * @author Hafid Ferreira Sousa
 */
public interface ICrawlerBatchTask {

    void getDeepCrawling(String requestJSON) throws URISyntaxException, JsonProcessingException;
}
