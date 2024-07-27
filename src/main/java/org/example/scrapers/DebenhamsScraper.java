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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

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

            // Keep clicking the "Load More" button until it disappears
            while (true) {
                try {
//                    Thread.sleep(5000);
                    WebElement loadMoreButton = driver.findElement(By.cssSelector("[data-test-id='pagination-load-more']"));

                    // Scroll into view
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
//                    System.out.println("The button exists");
                    loadMoreButton.click();
//                    System.out.println("WE CLICKED THE BUTTON");
                    // Wait for new content to load
                    Thread.sleep(2000); // Adjust the sleep time as needed
                } catch (Exception e) {
                    System.out.println("The button non longer exists");
                    // Break the loop if the button is no longer found
                    break;
                }
            }

            // Wait for the product elements to be present
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-test-id=product-card]")));

            // Get the page source and parse it using Jsoup
            String pageSource = driver.getPageSource();
            Document document = Jsoup.parse(pageSource);

            // Selector 1: Using data-test-id attribute
            Elements productCards = document.select("div[data-test-id=product-card]");
            System.out.println(productCards.size());


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

    public void getAllProducts(){
        getAllMensProducts();
        getAllWomensProducts();

    }

    public void getAllWomensProducts() {
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String>  l = getSubcategoryLinks(document,"WOMENS");
    }

    public void getAllKidsProducts(){
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String>  l = getSubcategoryLinks(document,"KIDS");
    }


    @Override
    public void getAllMensProducts() {
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String> l = getSubcategoryLinks(document,"MENS");
    }

    public void getAllHomeProducts(){
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String> l = getSubcategoryLinks(document,"HOME");
    }

    public void getAllBeautyProducts(){
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String>  l = getSubcategoryLinks(document,"BEAUTY");
    }

    public void getAllBrandsProducts(){
        driver.get(BASE_URL);
        Document document = getDocument(BASE_URL);
        Map<String,String> l = getSubcategoryLinks(document,"BRANDS");
    }

    // get subCategoryLinks is getting all links that are in a with that value inside it.
    // It gets all main naigation links and passes that to the filtered links function
    public Map<String,String> getSubcategoryLinks(Document document, String navHeading){
        Elements navLinks = document.select("a[data-test-id^='desktop-nav:']");
        return getFilteredLinks(navLinks, navHeading.toLowerCase());
    }

    // This then sorts through those and gets the only ones that have the url pattern with it
    public Map<String,String> getFilteredLinks(Elements elements, String urlPattern) {
      List<String> filteredLinks = new ArrayList<>();
      Map<String, String> categoryMap = new HashMap<>();

        for (Element element : elements) {
            String href = element.attr("href");
            String[] parts = href.split("[/-]");
            System.out.println(href);

            for (int i = 0; i < parts.length; i++) {
                if (urlPattern.equals(parts[i])) {
                    filteredLinks.add(href);

                    // Get the rest of the list and join with "-"
                    String category = String.join("-", java.util.Arrays.copyOfRange(parts, i + 1, parts.length));
                    categoryMap.put(category, href);

                    break; // Exit the loop once the pattern is found
                }
            }
        }
        return categoryMap;
    }

    public void scrapeLinks(Map<String,String> links){
        for (Map.Entry<String, String> entry : links.entrySet()) {
            String fullUrl = BASE_URL + entry.getValue();
            System.out.println(fullUrl);
//            scrapeCategory(fullUrl);
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
