package com.example.Lemon;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface SeriesRepository extends CrudRepository<Series, Integer> {
        List<Series> findByName(String name);
        Series getById(Integer id);
        List<Series> findByAuthorid(Integer authorid);
        //List<Series> findByAuthoridAndUpdatetimeBefore(Integer authorid,String timestamp);
        List<Series> findByAuthoridAndUpdatetimeAfter(Integer authorid,Long timestamp);
        List<Series> findByPublish(Boolean publish);
        List<Series> findByAuthoridAndPublish(Integer authorid,Boolean publish);
}