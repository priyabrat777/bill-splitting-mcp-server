package com.billsplitting.service;

import com.billsplitting.entity.ExpenseGroup;
import com.billsplitting.exception.DuplicateEntityException;
import com.billsplitting.exception.GroupNotFoundException;
import com.billsplitting.repository.ExpenseGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExpenseGroupService {

    private final ExpenseGroupRepository expenseGroupRepository;

    @Autowired
    public ExpenseGroupService(ExpenseGroupRepository expenseGroupRepository) {
        this.expenseGroupRepository = expenseGroupRepository;
    }

    public ExpenseGroup createGroup(String name, String description) {
        if (expenseGroupRepository.existsByName(name)) {
            throw new DuplicateEntityException("Group with name '" + name + "' already exists");
        }
        
        ExpenseGroup group = new ExpenseGroup(name, description);
        return expenseGroupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<ExpenseGroup> listGroups() {
        return expenseGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ExpenseGroup getGroupByName(String name) {
        return expenseGroupRepository.findByName(name)
                .orElseThrow(() -> new GroupNotFoundException("Group with name '" + name + "' not found"));
    }

    @Transactional(readOnly = true)
    public ExpenseGroup getGroupByNameWithMembers(String name) {
        return expenseGroupRepository.findByNameWithMembers(name)
                .orElseThrow(() -> new GroupNotFoundException("Group with name '" + name + "' not found"));
    }

    @Transactional(readOnly = true)
    public ExpenseGroup getGroupByNameWithExpenses(String name) {
        return expenseGroupRepository.findByNameWithExpenses(name)
                .orElseThrow(() -> new GroupNotFoundException("Group with name '" + name + "' not found"));
    }

    @Transactional(readOnly = true)
    public ExpenseGroup getGroupByNameWithMembersAndExpenses(String name) {
        return expenseGroupRepository.findByNameWithMembersAndExpenses(name)
                .orElseThrow(() -> new GroupNotFoundException("Group with name '" + name + "' not found"));
    }

    public void deleteGroup(String name) {
        ExpenseGroup group = getGroupByName(name);
        expenseGroupRepository.delete(group);
    }

    public ExpenseGroup updateGroup(String name, String newDescription) {
        ExpenseGroup group = getGroupByName(name);
        group.setDescription(newDescription);
        return expenseGroupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public boolean groupExists(String name) {
        return expenseGroupRepository.existsByName(name);
    }
}