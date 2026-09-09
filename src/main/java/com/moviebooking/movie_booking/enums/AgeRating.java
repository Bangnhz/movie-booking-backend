package com.moviebooking.movie_booking.enums;

public enum AgeRating {
    P("Phổ biến", "#22c55e"),
    K("Dành cho trẻ em", "#3b82f6"),
    T13("13+", "#eab308"),
    T16("16+", "#f97316"),
    T18("18+", "#ef4444");

    private final String description;
    private final String color;

    AgeRating(String description, String color) {
        this.description = description;
        this.color = color;
    }
    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }
}