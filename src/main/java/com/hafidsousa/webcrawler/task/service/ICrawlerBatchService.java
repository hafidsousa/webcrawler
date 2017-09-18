package com.hafidsousa.webcrawler.task.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.hafidsousa.webcrawler.model.WebsiteModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.net.URI;
import java.util.Map;

/**
 * @author Hafid Ferreira Sousa
 */
public interface ICrawlerBatchService {

    Flux<WebsiteModel> getWebsites(WebsiteModel seedWebsite, WebsiteModel parentWebsite, URI currentUrl, Integer depth);

    Map<String, AttributeValue> getNodes(WebsiteModel websiteModel);
}
