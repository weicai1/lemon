package com.example.Lemon;

import net.bytebuddy.TypeCache;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface ComicRepository extends CrudRepository<Comic, Integer> {
    List<Comic> findBySeriesid(Integer seriesid);
    List<Comic> findBySeriesid(Integer seriesid, Sort sort);
    Comic getById(Integer id);
}