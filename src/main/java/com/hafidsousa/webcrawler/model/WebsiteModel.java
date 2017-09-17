package com.hafidsousa.webcrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hafid Ferreira Sousa
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebsiteModel {

    private String url;

    private String title;

    private String body;

    private Set<WebsiteModel> nodes = new HashSet<>();

    @JsonIgnore
    private WebsiteModel parent;

    public WebsiteModel() {

    }

    public WebsiteModel(String url) {

        this.url = url;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public Set<WebsiteModel> getNodes() {

        return nodes;
    }

    public void setNodes(Set<WebsiteModel> nodes) {

        this.nodes = nodes;
    }

    public WebsiteModel getParent() {

        return parent;
    }

    public void setParent(WebsiteModel parent) {

        this.parent = parent;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("WebsiteModel{");
        sb.append("url='").append(url).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", nodes=").append(nodes);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebsiteModel that = (WebsiteModel) o;

        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {

        return url != null ? url.hashCode() : 0;
    }
}
