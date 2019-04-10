package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity // This tells Hibernate to make a table out of this class
public class Follow {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer followeeid;

    private Integer followerid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFolloweeid() {
        return followeeid;
    }

    public void setFolloweeid(Integer followeeid) {
        this.followeeid = followeeid;
    }

    public Integer getFollowerid() {
        return followerid;
    }

    public void setFollowerid(Integer followerid) {
        this.followerid = followerid;
    }
}