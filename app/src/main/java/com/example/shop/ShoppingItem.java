package com.example.shop;

public class ShoppingItem {
    private String name;
    private String info;
    private String price;
    private float ratedInfo;
    private final int imageResource;

    public ShoppingItem(int imageResource, String info, String name, String price, float ratedInfo) {
        this.imageResource = imageResource;
        this.info = info;
        this.name = name;
        this.price = price;
        this.ratedInfo = ratedInfo;
    }

    public String getName() { return name; }
    public String getInfo() { return info; }
    public String getPrice() { return price; }
    public float getRatedInfo() { return ratedInfo; }
    public int getImageResource() { return imageResource; }

}