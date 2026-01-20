package com.billsplitting.repository;

import com.billsplitting.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    
    List<ExpenseSplit> findByExpenseId(Long expenseId);
    
    List<ExpenseSplit> findByMemberId(Long memberId);
    
    @Query("SELECT s FROM ExpenseSplit s WHERE s.expense.id = :expenseId AND s.member.id = :memberId")
    ExpenseSplit findByExpenseIdAndMemberId(@Param("expenseId") Long expenseId, @Param("memberId") Long memberId);
    
    @Query("SELECT SUM(s.amount) FROM ExpenseSplit s WHERE s.member.id = :memberId")
    BigDecimal getTotalOwedByMember(@Param("memberId") Long memberId);
    
    @Query("SELECT SUM(s.amount) FROM ExpenseSplit s WHERE s.member.group.id = :groupId AND s.member.id = :memberId")
    BigDecimal getTotalOwedByMemberInGroup(@Param("groupId") Long groupId, @Param("memberId") Long memberId);
    
    @Query("SELECT s FROM ExpenseSplit s WHERE s.member.group.name = :groupName AND s.member.memberName = :memberName")
    List<ExpenseSplit> findByGroupNameAndMemberName(@Param("groupName") String groupName, @Param("memberName") String memberName);
    
    void deleteByExpenseId(Long expenseId);
}