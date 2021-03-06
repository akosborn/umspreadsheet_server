package com.umspreadsheet.wormblog;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WormBlogRepository extends CrudRepository<WormBlogPost, Long>
{
    List<WormBlogPost> findAllByOrderByPostedOnDesc();
}
