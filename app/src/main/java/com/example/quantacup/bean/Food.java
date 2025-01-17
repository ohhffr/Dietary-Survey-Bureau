package com.example.quantacup.bean;

public class Food {
    private int id;
    private String foodName;
    private String picture;
    private int calories;
    private FoodCategory foodCategory;

    public Food(int id, String foodName, String picture, int calories, FoodCategory foodCategory) {
        this.id = id;
        this.foodName = foodName;
        this.picture = picture;
        this.calories = calories;
        this.foodCategory = foodCategory;
    }

    public Food(String foodName, int calories) {
        this.foodName = foodName;
        this.calories = calories;
    }

    public Food(String foodName, String picture, int calories) {
        this.foodName = foodName;
        this.picture = picture;
        this.calories = calories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public FoodCategory getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(FoodCategory foodCategory) {
        this.foodCategory = foodCategory;
    }
}
