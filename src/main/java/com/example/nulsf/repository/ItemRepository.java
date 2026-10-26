package com.example.nulsf.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.nulsf.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByStatusIgnoreCase(String status);

    List<Item> findByStatusInIgnoreCase(List<String> statuses);

    List<Item> findByCategoryIgnoreCase(String category);

    List<Item> findByReportedByOrderByItemIdDesc(Integer reportedBy);

    long countByStatusIgnoreCase(String status);

    @Query("SELECT i FROM Item i WHERE " +
           "(:keyword IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(i.category) = LOWER(:category)) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(i.status) = LOWER(:status)) " +
           "ORDER BY i.itemId DESC")
    List<Item> searchItems(@Param("keyword") String keyword, 
                           @Param("category") String category, 
                           @Param("status") String status);
}
