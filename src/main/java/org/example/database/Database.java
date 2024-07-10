package org.example.database;

import org.example.entitys.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {


    private Connection connection;

    public Database() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE products (" +
                    "SKU VARCHAR(255), " +
                    "title VARCHAR(255), " +
                    "category VARCHAR(255), " +
                    "price DECIMAL(10, 2))";
            stmt.execute(sql);
        }
    }

    public void saveProduct(Product p) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format("INSERT INTO products (SKU, title, category, price) VALUES ('%s', '%s', '%s', %.2f)",
                    p.getSku(), p.getTitle(), p.getCategory(), p.getPrice());
            stmt.execute(sql);
        }
    }
}
