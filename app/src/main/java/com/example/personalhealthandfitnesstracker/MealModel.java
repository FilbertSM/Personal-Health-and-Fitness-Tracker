package com.example.personalhealthandfitnesstracker;

public class MealModel {
    private String mealName;
    private int calorie;
    private int weight;

    public MealModel(String mealName, int calorie, int weight) {
        this.mealName = mealName;
        this.calorie = calorie;
        this.weight = weight;
    }

    public MealModel() {
        // Firestore requires a no-argument constructor
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public int getCalories() {
        return calorie;
    }

    public void setCalories(int calorie) {
        this.calorie = calorie;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
