package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface ComicRepository extends CrudRepository<Comic, Integer> {
    List<Comic> findBySeriesid(Integer seriesid);
}