package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqUserDto;

/**
 * JooqUserRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqUserRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JooqUserRepository(dsl);
    }

    @Test
    void testInsertUser() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .phone("13800138000")
            .build();

        // When
        Long id = repository.insert(user);

        // Then
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    void testFindById() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        Long id = repository.insert(user);

        // When
        JooqUserDto found = repository.findById(id);

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testFindByIdNotFound() {
        // When
        JooqUserDto found = repository.findById(99999L);

        // Then
        assertNull(found);
    }

    @Test
    void testFindByUsername() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        repository.insert(user);

        // When
        JooqUserDto found = repository.findByUsername("testuser");

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testUpdate() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .phone("13800138000")
            .build();
        Long id = repository.insert(user);
        user.setId(id);
        user.setPhone("13900139000");

        // When
        boolean updated = repository.update(user);

        // Then
        assertTrue(updated);
        JooqUserDto updatedUser = repository.findById(id);
        assertEquals("13900139000", updatedUser.getPhone());
    }

    @Test
    void testDelete() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        Long id = repository.insert(user);

        // When
        boolean deleted = repository.delete(id);

        // Then
        assertTrue(deleted);
        assertNull(repository.findById(id));
    }

    @Test
    void testFindAll() {
        // Given
        repository.insert(JooqUserDto.builder().username("user1").email("user1@example.com").build());
        repository.insert(JooqUserDto.builder().username("user2").email("user2@example.com").build());

        // When
        var users = repository.findAll();

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }
}
