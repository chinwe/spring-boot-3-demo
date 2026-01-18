package com.example.demo.service.jooq;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqUserRepository;

/**
 * JooqUserService 单元测试
 * 验证用户服务的功能正确性
 *
 * @author chinwe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JOOQ 用户服务单元测试")
class JooqUserServiceTest {

    @Mock
    private JooqUserRepository userRepository;

    @InjectMocks
    private JooqUserService userService;

    private JooqUserDto testUser;

    @BeforeEach
    void setUp() {
        testUser = JooqUserDto.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phone("1234567890")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("成功创建用户")
    void testCreateUser_Success() {
        // Given
        JooqUserDto newUser = JooqUserDto.builder()
            .username("newuser")
            .email("newuser@example.com")
            .phone("9876543210")
            .build();

        when(userRepository.insert(any(JooqUserDto.class))).thenReturn(100L);

        // When
        Long resultId = userService.createUser(newUser);

        // Then
        assertEquals(100L, resultId);
        verify(userRepository, times(1)).insert(newUser);
    }

    @Test
    @DisplayName("空用户处理")
    void testCreateUser_WithNullUser() {
        // When & Then
        // UserService 会先调用 user.getUsername() 导致 NullPointerException
        assertThrows(NullPointerException.class, () -> {
            userService.createUser(null);
        });
    }

    @Test
    @DisplayName("根据ID找到用户")
    void testGetUserById_Found() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(testUser);

        // When
        JooqUserDto result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("用户不存在抛出EntityNotFoundException")
    void testGetUserById_NotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("User not found with id"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("根据用户名找到用户")
    void testGetUserByUsername_Found() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When
        JooqUserDto result = userService.getUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("用户名不存在抛出异常")
    void testGetUserByUsername_NotFound_ThrowsException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserByUsername(username);
        });

        assertTrue(exception.getMessage().contains("User not found with username"));
        assertTrue(exception.getMessage().contains("nonexistent"));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("返回用户列表")
    void testGetAllUsers_ReturnsList() {
        // Given
        JooqUserDto user1 = JooqUserDto.builder()
            .id(1L)
            .username("user1")
            .email("user1@example.com")
            .build();

        JooqUserDto user2 = JooqUserDto.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .build();

        List<JooqUserDto> expectedUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<JooqUserDto> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("返回空列表")
    void testGetAllUsers_EmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<JooqUserDto> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("成功更新用户")
    void testUpdateUser_Success() {
        // Given
        JooqUserDto updatedUser = JooqUserDto.builder()
            .id(1L)
            .username("updateduser")
            .email("updated@example.com")
            .phone("1111111111")
            .build();

        when(userRepository.update(updatedUser)).thenReturn(true);

        // When
        boolean result = userService.updateUser(updatedUser);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).update(updatedUser);
    }

    @Test
    @DisplayName("成功删除用户")
    void testDeleteUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.delete(userId)).thenReturn(true);

        // When
        boolean result = userService.deleteUser(userId);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).delete(userId);
    }
}
