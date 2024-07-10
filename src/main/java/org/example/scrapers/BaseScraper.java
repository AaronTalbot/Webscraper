package org.example.scrapers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;



import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public abstract class BaseScraper implements Scraper {
    protected WebDriver driver;
    protected ExecutorService executor;

    public BaseScraper() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\aaron\\Development\\Webscraper\\src\\main\\java\\org\\example\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();

        // Initialize ChromeDriver
        driver = new ChromeDriver(options);
    }

    protected Document getDocument(String url) {
        driver.get(url);
        return Jsoup.parse(driver.getPageSource());
    }

    public void shutdown() {
        driver.quit();
        executor.shutdown();
    }

    public static JsonObject parseJsonLd(String jsonLd) {
        Gson gson = new Gson();
        return gson.fromJson(jsonLd, JsonObject.class);
    }

    public String makeAbsoluteUrl(String baseUrl, String relativeUrl) {
        try {
            URL base = new URL(baseUrl);
            URL absolute = new URL(base, relativeUrl);
            return absolute.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return relativeUrl; // Return as-is if URL is malformed
        }
    }
}