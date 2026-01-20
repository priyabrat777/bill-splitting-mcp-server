package com.billsplitting.repository;

import com.billsplitting.entity.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
    
    Optional<ExpenseGroup> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT g FROM ExpenseGroup g LEFT JOIN FETCH g.members WHERE g.name = :name")
    Optional<ExpenseGroup> findByNameWithMembers(@Param("name") String name);
    
    @Query("SELECT g FROM ExpenseGroup g LEFT JOIN FETCH g.expenses WHERE g.name = :name")
    Optional<ExpenseGroup> findByNameWithExpenses(@Param("name") String name);
    
    @Query("SELECT g FROM ExpenseGroup g LEFT JOIN FETCH g.members LEFT JOIN FETCH g.expenses WHERE g.name = :name")
    Optional<ExpenseGroup> findByNameWithMembersAndExpenses(@Param("name") String name);
}