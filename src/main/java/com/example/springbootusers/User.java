package com.example.springbootusers;

public class User {

    String username;
    String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    User(String username, String password){
        this.username = username;
        this.password = password;
    }
}
