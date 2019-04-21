package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Blob;

@Entity // This tells Hibernate to make a table out of this class
public class Series {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer chapternumber;

    private Integer authorid;

    private String name;

    private Integer subnumber;

    private Blob cover;

    private Double rate;

    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChapternumber() {
        return chapternumber;
    }

    public void setChapternumber(Integer chapternumber) {
        this.chapternumber = chapternumber;
    }

    public Integer getAuthorid() {
        return authorid;
    }

    public void setAuthorid(Integer authorid) {
        this.authorid = authorid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSubnumber() {
        return subnumber;
    }

    public void setSubnumber(Integer subnumber) {
        this.subnumber = subnumber;
    }

    public Blob getCover() {
        return cover;
    }

    public void setCover(Blob cover) {
        this.cover = cover;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double subnumber) {
        this.rate = rate;
    }

    public String getDescriptioin() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}