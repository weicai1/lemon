package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface PageRepository extends CrudRepository<Page, Integer> {
    List<Page> findByComicidAndIndexs(Integer comicId,Integer index);
    List<Page> findByComicid(Integer comicId);
}