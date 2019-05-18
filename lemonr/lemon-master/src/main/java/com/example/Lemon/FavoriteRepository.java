package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;
import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface FavoriteRepository extends CrudRepository<Favorite, Integer> {
    List<Favorite> findBySeriesidAndUserid(Integer seriesId, Integer userId);
    List<Favorite> findByUserid(Integer userId);
    List<Favorite> findBySeriesid(Integer seriesId);
}