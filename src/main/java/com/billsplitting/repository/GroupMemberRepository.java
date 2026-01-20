package com.billsplitting.repository;

import com.billsplitting.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByGroupId(Long groupId);
    
    Optional<GroupMember> findByGroupIdAndMemberName(Long groupId, String memberName);
    
    @Query("SELECT m FROM GroupMember m WHERE m.group.name = :groupName")
    List<GroupMember> findByGroupName(@Param("groupName") String groupName);
    
    @Query("SELECT m FROM GroupMember m WHERE m.group.name = :groupName AND m.memberName = :memberName")
    Optional<GroupMember> findByGroupNameAndMemberName(@Param("groupName") String groupName, @Param("memberName") String memberName);
    
    boolean existsByGroupIdAndMemberName(Long groupId, String memberName);
    
    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);
}