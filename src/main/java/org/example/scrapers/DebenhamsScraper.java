package org.example.scrapers;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.database.Database;
import org.example.entitys.Product;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DebenhamsScraper extends BaseScraper {

    private Database database;
    private final String BASE_URL = "https://www.debenhams.com";

    public DebenhamsScraper(Database database) {
        this.database = database;
    }

    @Override
    public void scrapeCategory(String categoryUrl) {
        try{
            driver.get(categoryUrl);

            // Wait for the product elements to be present
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-test-id=product-card]")));

            // Get the page source and parse it using Jsoup
            String pageSource = driver.getPageSource();
            Document document = Jsoup.parse(pageSource);

            // Selector 1: Using data-test-id attribute
            Elements productCards = document.select("div[data-test-id=product-card]");

            // Process each product card
            for (Element productCard : productCards) {
                // Find the nested 'a' tag with href attribute
                Element linkElement = productCard.selectFirst("a[href]");
                if (linkElement != null) {
                    String href = linkElement.attr("href");
                    String fullHref = BASE_URL + href;

                    // You can now load this URL or further process it
                    scrapeProduct(fullHref);
                    break;
                } else {
                    System.out.println("No link found for product card.");
                }
            }

        } catch (Exception e) {
            System.err.println("An error occurred while scraping the category: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }


    @Override
    public void scrapeProduct(String productUrl) {
        try {


            Document productDocument = getDocument(productUrl);

            if (productDocument == null) {
                System.err.println("Failed to fetch the product page for URL: " + productUrl);
                return;
            }
            Element scriptElement = productDocument.selectFirst("script[type=application/ld+json]");

            if (scriptElement != null) {
                String jsonData = scriptElement.html();


                JsonElement jsonElement = JsonParser.parseString(jsonData);

                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();

                    for (JsonElement element : jsonArray) {
                        if (element.isJsonObject()) {
                            JsonObject jsonObject = element.getAsJsonObject();
                            extractProductData(jsonObject);
                        }
                    }
                } else if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    extractProductData(jsonObject);
                } else {
                    System.err.println("The extracted data is not a valid JSON structure.");
                }
            } else {
                System.err.println("No product found for URL: " + productUrl);
            }
        } catch ( Exception e){
            System.err.println("An error occurred while scraping the product: " + e.getMessage());
            e.printStackTrace();
        }

    }


private void extractProductData(JsonObject jsonObject) {
    try {
        if ("Product".equals(jsonObject.get("@type").getAsString())) {
            // Extract relevant information
            String description = jsonObject.get("description").getAsString();
            String color = jsonObject.get("color").getAsString();
            String name = jsonObject.get("name").getAsString();
            String sku = jsonObject.get("sku").getAsString();
            double price = jsonObject.getAsJsonObject("offers").get("price").getAsDouble();
            String currency = jsonObject.getAsJsonObject("offers").get("priceCurrency").getAsString();

            Product p = new Product();
            p.setSku(sku);
            p.setPrice(price);
            p.setTitle(name);
            p.setCategory(description);

            try{
                database.saveProduct(p);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    } catch (Exception e) {
        System.err.println("An error occurred while extracting product data: " + e.getMessage());
        e.printStackTrace();
    }
}
}
