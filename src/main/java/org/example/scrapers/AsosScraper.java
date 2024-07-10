package org.example.scrapers;

import com.google.gson.JsonObject;
import org.example.database.Database;
import org.example.entitys.Product;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;

public class AsosScraper extends BaseScraper {

    private Database database;

    public AsosScraper(Database database) {
        this.database = database;
    }

    @Override
    public void scrapeCategory(String categoryUrl) {
        Document document = getDocument(categoryUrl);

        Elements products = document.getElementsByClass("productTile_U0clN");

        for (Element product : products) {
            String productUrl = product.select("a").attr("href");


            // Convert to absolute URL if necessary
            productUrl = makeAbsoluteUrl(categoryUrl, productUrl);


            // Direct call to scrapeProduct for debugging
            scrapeProduct(productUrl);

            // Using executor to submit tasks
            //executor.submit(() -> scrapeProduct(productUrl));

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
            Element scriptElement = productDocument.getElementById("split-structured-data");
            System.out.println(scriptElement);



            if(scriptElement == null){
                System.err.println("No script found for " + productUrl);
                return;
            }


            String jsonLd = scriptElement.html();
            JsonObject productData = parseJsonLd(jsonLd);

            String title = productData.get("name").getAsString();
            String sku = productData.get("sku").getAsString();
            String category = productData.get("description").getAsString(); // Assuming category is part of description
            JsonObject offers = productData.getAsJsonObject("offers");
            double price = offers.get("lowPrice").getAsDouble();

            if (title == null || sku == null || price <= 0.0) {
                System.err.println("Missing required product data for URL: " + productUrl);
                System.out.println("Title: " + title);
                System.out.println("SKU: " + sku);
                System.out.println("Price: " + price);
                return;
            }
            Product product = new Product();
            product.setSku(sku);
            product.setTitle(title);
            product.setCategory(category);
            product.setPrice(price);

            try {
                database.saveProduct(product);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("An Error occured while scraping product: " + e.getMessage());
            e.printStackTrace();

        }
    }


}
