package com.example.personalhealthandfitnesstracker;

public class HydrationModel {
    private int waterIntake; // Amount of water intake
    private int date; // Date of the record
    private String note; // Any additional notes

    // Constructor with parameters
    public HydrationModel(int waterIntake, int date, String note) {
        this.waterIntake = waterIntake;
        this.date = date;
        this.note = note;
    }

    // No-argument constructor required for Firestore
    public HydrationModel() {
        // Firestore requires a no-argument constructor
    }

    // Getter for water intake
    public int getWaterIntake() {
        return waterIntake;
    }

    // Setter for water intake
    public void setWaterIntake(int waterIntake) {
        this.waterIntake = waterIntake;
    }

    // Getter for date
    public int getDate() {
        return date;
    }

    // Setter for date
    public void setDate(int date) {
        this.date = date;
    }

    // Getter for note
    public String getNote() {
        return note;
    }

    // Setter for note
    public void setNote(String note) {
        this.note = note;
    }
}
