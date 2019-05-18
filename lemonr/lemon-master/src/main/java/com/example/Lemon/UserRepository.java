package com.example.Lemon;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByName(String name);
    User getById(Integer id);
}