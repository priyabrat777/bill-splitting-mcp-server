package com.billsplitting.service;

import com.billsplitting.entity.ExpenseGroup;
import com.billsplitting.exception.DuplicateEntityException;
import com.billsplitting.exception.GroupNotFoundException;
import com.billsplitting.repository.ExpenseGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseGroupServiceTest {

    @Mock
    private ExpenseGroupRepository expenseGroupRepository;

    @InjectMocks
    private ExpenseGroupService expenseGroupService;

    private ExpenseGroup testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new ExpenseGroup("Test Group", "Test Description");
        testGroup.setId(1L);
    }

    @Test
    void createGroup_Success() {
        // Given
        when(expenseGroupRepository.existsByName("Test Group")).thenReturn(false);
        when(expenseGroupRepository.save(any(ExpenseGroup.class))).thenReturn(testGroup);

        // When
        ExpenseGroup result = expenseGroupService.createGroup("Test Group", "Test Description");

        // Then
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(expenseGroupRepository).existsByName("Test Group");
        verify(expenseGroupRepository).save(any(ExpenseGroup.class));
    }

    @Test
    void createGroup_DuplicateName_ThrowsException() {
        // Given
        when(expenseGroupRepository.existsByName("Test Group")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEntityException.class, 
            () -> expenseGroupService.createGroup("Test Group", "Test Description"));
        
        verify(expenseGroupRepository).existsByName("Test Group");
        verify(expenseGroupRepository, never()).save(any(ExpenseGroup.class));
    }

    @Test
    void listGroups_Success() {
        // Given
        List<ExpenseGroup> groups = Arrays.asList(testGroup);
        when(expenseGroupRepository.findAll()).thenReturn(groups);

        // When
        List<ExpenseGroup> result = expenseGroupService.listGroups();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).getName());
        verify(expenseGroupRepository).findAll();
    }

    @Test
    void getGroupByName_Success() {
        // Given
        when(expenseGroupRepository.findByName("Test Group")).thenReturn(Optional.of(testGroup));

        // When
        ExpenseGroup result = expenseGroupService.getGroupByName("Test Group");

        // Then
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        verify(expenseGroupRepository).findByName("Test Group");
    }

    @Test
    void getGroupByName_NotFound_ThrowsException() {
        // Given
        when(expenseGroupRepository.findByName("Nonexistent Group")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(GroupNotFoundException.class, 
            () -> expenseGroupService.getGroupByName("Nonexistent Group"));
        
        verify(expenseGroupRepository).findByName("Nonexistent Group");
    }

    @Test
    void deleteGroup_Success() {
        // Given
        when(expenseGroupRepository.findByName("Test Group")).thenReturn(Optional.of(testGroup));

        // When
        expenseGroupService.deleteGroup("Test Group");

        // Then
        verify(expenseGroupRepository).findByName("Test Group");
        verify(expenseGroupRepository).delete(testGroup);
    }

    @Test
    void groupExists_True() {
        // Given
        when(expenseGroupRepository.existsByName("Test Group")).thenReturn(true);

        // When
        boolean result = expenseGroupService.groupExists("Test Group");

        // Then
        assertTrue(result);
        verify(expenseGroupRepository).existsByName("Test Group");
    }

    @Test
    void groupExists_False() {
        // Given
        when(expenseGroupRepository.existsByName("Nonexistent Group")).thenReturn(false);

        // When
        boolean result = expenseGroupService.groupExists("Nonexistent Group");

        // Then
        assertFalse(result);
        verify(expenseGroupRepository).existsByName("Nonexistent Group");
    }
}