package org.example.scrapers;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.example.database.Database;
import org.example.entitys.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AsosScraper extends BaseScraper {

    private Database database;
    private static final String BASE_URL = "https://www.asos.com/";

    public AsosScraper(Database database) {
        this.database = database;
    }

    @Override
    public void initialiazeDriver() {
         driver = new ChromeDriver();
         driver.manage().window().maximize(); // Asos needs maximise window screen to show navigation bar
    }

    public void getAllMensProducts(){
//        String mens_url = BASE_URL + "men/";
//        Document document = getDocument(mens_url);
//        Map<String,String> l = getSubcategoryLinks(document,"MEN");
//        Document document = getDocument("https://www.asos.com/men/jeans/cat/?cid=4208#nlid=mw|clothing|shop+by+product|jeans");
        scrapeCategory("https://www.asos.com/men/jeans/cat/?cid=4208#nlid=mw|clothing|shop+by+product|jeans");

    }

    private Map<String, String> getSubcategoryLinks(Document document, String navHeading) {
        Elements navLinks = document.select("a.R5kwVNg.ZHWKoMf.leavesden3.ByM_HVJ.TYb4J9A");
        getFilteredLinks(navLinks, navHeading.toLowerCase());
        return null;
    }

    public Map<String,String> getFilteredLinks(Elements navLinks, String heading){
        Map<String, String> categoryMap = new HashMap<>();
        for (Element link : navLinks) {
            String href = link.attr("href");
            String category = link.text();
            if (link.classNames().size() == 5 && href.contains("/"+heading+"/")&& !isBroaderLink(href) && !isBroaderCategory(category)) {
                categoryMap.put(category, href);
            }
        }
        return categoryMap;
    }

    private Boolean isBroaderCategory(String category){
        String[] broaderCategories = {"new in", "clothing", "view all", "latest drop", "top rated" , "%","new","a-z","shipped from","sale"};
        for (String broaderCategory : broaderCategories) {
            if (category.toLowerCase().contains(broaderCategory)) {
                return true;
            }
        }
        return false;
    }

    private Boolean isBroaderLink(String link){
        String[] broaderCategories = {"new+in","a-to-z"};
        for(String broaderCategory: broaderCategories){
            if(link.toLowerCase().contains(broaderCategory)){
                return true;
            }
        }
        return false;

    }


    public void getAllWomensProducts(){
        String women_url = BASE_URL + "women/";
//        String women_url = "https://www.asos.com/women/ctas/usa-online-fashion-13/cat/?cid=16661";
        scrapeLandingPage(women_url);
    }

    public void scrapeLandingPage(String landingURL){
        Document document = getDocument(landingURL);
        System.out.print(document);

        // Add an explicit wait
        try {
            Thread.sleep(5000);  // Adjust the sleep duration as needed
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Elements productTypeElements = document.getElementsByClass("c2oEXGw");
        System.out.println(productTypeElements);

    }

    @Override
    public void scrapeCategory(String categoryUrl) {
        driver.get(categoryUrl);
        Document document = getDocument(categoryUrl);
        Elements allProducts = new Elements();




        while (true) {
            try {
//                    Thread.sleep(5000);
                WebElement loadMoreButton = driver.findElement(By.cssSelector("a.loadButton_wWQ3F[data-auto-id=\"loadMoreProducts\"]"));

                // Scroll into view
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
                Elements products = document.getElementsByClass("productTile_U0clN");
                allProducts.addAll(products);
                loadMoreButton.click();

                // Wait for new content to load
                Thread.sleep(2000); // Adjust the sleep time as needed
            } catch (Exception e) {
                System.out.println("The button non longer exists");
                // Break the loop if the button is no longer found
                break;
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//
////        System.out.println(document);
//
//        // Take a screenshot
//        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//
//        // Save the screenshot to a file
//        File destinationFile = new File("screenshot.png");
//        try {
//            FileUtils.copyFile(screenshot, destinationFile);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println("Screenshot saved: " + destinationFile.getAbsolutePath());
//
//        System.out.println(document);

        System.out.println(allProducts.size());
//
//        for (Element product : products) {
//            String productUrl = product.select("a").attr("href");
//
//
//            // Convert to absolute URL if necessary
//            productUrl = makeAbsoluteUrl(categoryUrl, productUrl);
//
//
//            // Direct call to scrapeProduct for debugging
//            scrapeProduct(productUrl);
//
//            // Using executor to submit tasks
//            //executor.submit(() -> scrapeProduct(productUrl));
//
//        }
    }

    @Override
    public void scrapeProduct(String productUrl) {
        try {

            Document productDocument = getDocument(productUrl);
            System.out.println("productDocument: "+ productDocument);

//            if (productDocument == null) {
//                System.err.println("Failed to fetch the product page for URL: " + productUrl);
//                return;
//            }
//            Element scriptElement = productDocument.getElementById("split-structured-data");
//            System.out.println(scriptElement);
//
//
//
//            if(scriptElement == null){
//                System.err.println("No script found for " + productUrl);
//                return;
//            }
//
//
//            String jsonLd = scriptElement.html();
//            JsonObject productData = parseJsonLd(jsonLd);
//
//            String title = productData.get("name").getAsString();
//            String sku = productData.get("sku").getAsString();
//            String category = productData.get("description").getAsString(); // Assuming category is part of description
//            JsonObject offers = productData.getAsJsonObject("offers");
//            double price = offers.get("lowPrice").getAsDouble();
//
//            if (title == null || sku == null || price <= 0.0) {
//                System.err.println("Missing required product data for URL: " + productUrl);
//                System.out.println("Title: " + title);
//                System.out.println("SKU: " + sku);
//                System.out.println("Price: " + price);
//                return;
//            }
//            Product product = new Product();
//            product.setSku(sku);
//            product.setTitle(title);
//            product.setCategory(category);
//            product.setPrice(price);
//
//            try {
//                database.saveProduct(product);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        } catch (Exception e) {
            System.err.println("An Error occured while scraping product: " + e.getMessage());
            e.printStackTrace();

        }
    }


}
