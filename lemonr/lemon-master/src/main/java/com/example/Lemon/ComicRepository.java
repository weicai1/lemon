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
    List<Comic> findBySeriesidAndIndexs(Integer seriesid,Integer indexs);
    List<Comic> findBySeriesidOrderByIndexs(Integer seriesid);
    Comic getById(Integer id);
    List<Comic> findBySeriesidAndPublish(Integer seriesid,Boolean publish);
    List<Comic> findByPublish(Boolean publish);
    List<Comic>  findBySeriesidAndPublishAndIdNot(Integer seriesid,Boolean publish, Integer id);
    List<Comic>  findBySeriesidAndIndexsGreaterThan(Integer seriesid, Integer id);
}