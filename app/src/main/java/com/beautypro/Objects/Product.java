package com.beautypro.Objects;

import java.util.ArrayList;
import java.util.HashMap;

public class Product {
    String productId;
    int quantity;
    String productName;
    String productDetails;
    double price;
    Long thumbnail;
    HashMap<String, Object> rating;

    public Product() {
    }

    public Product(String productId, int quantity, String productName, String productDetails, double price, Long thumbnail, HashMap<String, Object> rating) {
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.productDetails = productDetails;
        this.price = price;
        this.thumbnail = thumbnail;
        this.rating = rating;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Long thumbnail) {
        this.thumbnail = thumbnail;
    }

    public HashMap<String, Object> getRating() {
        return rating;
    }

    public void setRating(HashMap<String, Object> rating) {
        this.rating = rating;
    }
}
