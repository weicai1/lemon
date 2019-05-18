package com.example.Lemon;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Blob;
import java.util.Comparator;
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

    private Long updatetime;

    private boolean publish;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUpdatetime(){return this.updatetime;}

    public void setUpdatetime(Long updateTime){this.updatetime = updateTime;}

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

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getDescriptioin() {
        return description;
    }

    public boolean getPublish(){
        return publish;
    }

    public void setPublish(boolean publish){
        this.publish=publish;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Comparator<Series> SeriesSubscriptionComparator
            = new Comparator<Series>() {

        public int compare(Series serie1, Series serie2) {

            Integer sub1 = serie1.getSubnumber();
            Integer sub2 = serie2.getSubnumber();
            return sub2.compareTo(sub1);
        }
    };

    public static Comparator<Series> SeriesIdComparator
            = new Comparator<Series>() {

        public int compare(Series serie1, Series serie2) {

            Integer id1 = serie1.getId();
            Integer id2 = serie2.getId();
            return id2.compareTo(id1);
        }
    };

    public static Comparator<Series> SeriesRateComparator
            = new Comparator<Series>() {

        public int compare(Series serie1, Series serie2) {

            Double rate1 = serie1.getRate();
            Double rate2 = serie2.getRate();
            //ascending order
//            return fruitName1.compareTo(fruitName2);

            //descending order
            return rate2.compareTo(rate1);
        }

    };
}