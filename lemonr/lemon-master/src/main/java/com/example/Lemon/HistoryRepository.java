package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface HistoryRepository extends CrudRepository<History, Integer> {
    ArrayList<History> findBySeriesid(Integer seriesId);
}