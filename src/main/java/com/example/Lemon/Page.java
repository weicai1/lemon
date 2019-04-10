package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity // This tells Hibernate to make a table out of this class
public class Page {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer comicid;

    private String path;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getComicid() {
        return comicid;
    }

    public void setComicid(Integer comicid) {
        this.comicid = comicid;
    }

    public String getPath() {
        return path;
    }

    public void setpath(String path) {
        this.path = path;
    }

}