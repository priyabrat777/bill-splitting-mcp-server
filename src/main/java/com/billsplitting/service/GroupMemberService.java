package com.billsplitting.service;

import com.billsplitting.entity.ExpenseGroup;
import com.billsplitting.entity.GroupMember;
import com.billsplitting.exception.DuplicateEntityException;
import com.billsplitting.exception.MemberNotFoundException;
import com.billsplitting.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseGroupService expenseGroupService;

    @Autowired
    public GroupMemberService(GroupMemberRepository groupMemberRepository, 
                             ExpenseGroupService expenseGroupService) {
        this.groupMemberRepository = groupMemberRepository;
        this.expenseGroupService = expenseGroupService;
    }

    public GroupMember addMember(String groupName, String memberName) {
        ExpenseGroup group = expenseGroupService.getGroupByName(groupName);
        
        if (groupMemberRepository.existsByGroupIdAndMemberName(group.getId(), memberName)) {
            throw new DuplicateEntityException("Member '" + memberName + "' already exists in group '" + groupName + "'");
        }
        
        GroupMember member = new GroupMember(group, memberName);
        return groupMemberRepository.save(member);
    }

    public void removeMember(String groupName, String memberName) {
        GroupMember member = getMemberByGroupNameAndMemberName(groupName, memberName);
        groupMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<GroupMember> listMembers(String groupName) {
        return groupMemberRepository.findByGroupName(groupName);
    }

    @Transactional(readOnly = true)
    public GroupMember getMemberByGroupNameAndMemberName(String groupName, String memberName) {
        return groupMemberRepository.findByGroupNameAndMemberName(groupName, memberName)
                .orElseThrow(() -> new MemberNotFoundException(
                    "Member '" + memberName + "' not found in group '" + groupName + "'"));
    }

    @Transactional(readOnly = true)
    public boolean memberExists(String groupName, String memberName) {
        ExpenseGroup group = expenseGroupService.getGroupByName(groupName);
        return groupMemberRepository.existsByGroupIdAndMemberName(group.getId(), memberName);
    }

    @Transactional(readOnly = true)
    public long getMemberCount(String groupName) {
        ExpenseGroup group = expenseGroupService.getGroupByName(groupName);
        return groupMemberRepository.countByGroupId(group.getId());
    }
}