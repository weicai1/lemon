package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Blob;

@Entity // This tells Hibernate to make a table out of this class
public class Page {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer comicid;

    private Integer indexs;

    private Blob content;

    private String draft;

    private boolean publish;

    public boolean getPublish(){
        return publish;
    }


    public void setPublish(boolean publish){
        this.publish=publish;
    }

    public String getDraft(){
        return draft;
    }
    public void setDraft(String draft){
        this.draft=draft;
    }

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

    public Integer getIndexs(){ return indexs; }

    public void setIndexs(Integer indexs){ this.indexs =indexs ; }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

}