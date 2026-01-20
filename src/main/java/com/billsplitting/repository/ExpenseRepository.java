package com.billsplitting.repository;

import com.billsplitting.entity.Expense;
import com.billsplitting.entity.SplitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByGroupId(Long groupId);
    
    @Query("SELECT e FROM Expense e WHERE e.group.name = :groupName ORDER BY e.createdAt DESC")
    List<Expense> findByGroupNameOrderByCreatedAtDesc(@Param("groupName") String groupName);
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.splits WHERE e.id = :expenseId")
    Optional<Expense> findByIdWithSplits(@Param("expenseId") Long expenseId);
    
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.createdAt BETWEEN :startDate AND :endDate")
    List<Expense> findByGroupIdAndDateRange(@Param("groupId") Long groupId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.group.id = :groupId")
    BigDecimal getTotalExpensesByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.group.name = :groupName")
    BigDecimal getTotalExpensesByGroupName(@Param("groupName") String groupName);
    
    @Query("SELECT e FROM Expense e WHERE e.paidByMember.id = :memberId")
    List<Expense> findByPaidByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT e FROM Expense e WHERE e.splitType = :splitType")
    List<Expense> findBySplitType(@Param("splitType") SplitType splitType);
}