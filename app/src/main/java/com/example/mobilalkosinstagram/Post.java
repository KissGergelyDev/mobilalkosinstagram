package com.example.mobilalkosinstagram;

public class Post {
    private String id;
    private String username;
    private String description;
    private String imageUrl;
    private String location;

    // Kötelező üres konstruktor Firestore-hoz
    public Post() {
    }

    // Kényelmes konstruktor manuális példányosításhoz
    public Post(String username, String description, String imageUrl, String location) {
        this.username = username;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    // Getterek és setterek
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
