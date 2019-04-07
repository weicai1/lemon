package com.example.Lemon;


import org.springframework.context.annotation.Bean;


public class User {
    private int id;
    private String name;
    private String password;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }
}
