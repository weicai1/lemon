package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity // This tells Hibernate to make a table out of this class
public class Comic {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer seriesid;

    private Integer indexs;

    private boolean publish;

    public boolean getPublish(){
        return publish;
    }


    public void setPublish(boolean publish){
        this.publish=publish;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeriesid() {
        return seriesid;
    }

    public void setSeriesid(Integer seriesid) {
        this.seriesid = seriesid;
    }

    public Integer getIndexs(){ return indexs; }

    public void setIndex(Integer indexs){ this.indexs =indexs ; }


}