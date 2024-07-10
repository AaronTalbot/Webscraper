package org.example.scrapers;

public interface Scraper {

    void scrapeCategory(String categoryUrl);
    void scrapeProduct(String productUrl);
}
