package com.beautypro.Objects;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopItem {
    String id;
    String categoryId;
    double price;
    String productDetails;
    String productName;
    int stock;
    Long thumbnail;
    HashMap<String, Object> rating;

    public ShopItem() {
    }

    public ShopItem(String id, String categoryId, double price, String productDetails, String productName, int stock, Long thumbnail, HashMap<String, Object> rating) {
        this.id = id;
        this.categoryId = categoryId;
        this.price = price;
        this.productDetails = productDetails;
        this.productName = productName;
        this.stock = stock;
        this.thumbnail = thumbnail;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
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
