package com.example.Lemon;

import org.springframework.data.repository.CrudRepository;
import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface SubscribedauthorRepository extends CrudRepository<Subscribedauthor, Integer> {
    List<Subscribedauthor> findByAuthoridAndUserid(Integer authorId, Integer userid);
    List<Subscribedauthor> findByUserid(Integer userid);
    List<Subscribedauthor> findByAuthorid(Integer authorid);
}