package org.example;

import org.example.database.Database;
import org.example.scrapers.AsosScraper;
import org.example.scrapers.BaseScraper;
import org.example.scrapers.DebenhamsScraper;
import org.example.scrapers.Scraper;

import java.sql.SQLException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
            Database database = new Database();


            Scraper asosScraper = new AsosScraper(database);
            Scraper debenhamsScraper = new DebenhamsScraper(database);

            // URLs to scrape
            String asosCategoryUrl = "https://www.asos.com/men/new-in/cat/?cid=27110";
            String debenhamsCategoryURL = "https://www.debenhams.com/categories/mens-trousers";



//             Execute in parallel
            Arrays.asList(asosScraper, debenhamsScraper).parallelStream().forEach(scraper -> {
                scraper.scrapeCategory(asosCategoryUrl);
                scraper.scrapeCategory(debenhamsCategoryURL);
                ((BaseScraper) debenhamsScraper).shutdown();
                ((BaseScraper) asosScraper).shutdown();
            });

            // Shutdown scrapers
            ((BaseScraper) asosScraper).shutdown();
            ((BaseScraper) debenhamsScraper).shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
