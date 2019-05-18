package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;
import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface FavoritecomicRepository extends CrudRepository<Favoritecomic, Integer> {
    List<Favoritecomic> findBySeriesidAndUserid(Integer seriesId, Integer userId);
    List<Favoritecomic> findByUserid(Integer userId);
}