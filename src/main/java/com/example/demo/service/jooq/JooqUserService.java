package com.example.demo.service.jooq;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 用户服务
 *
 * @author chinwe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JooqUserService {

    private final JooqUserRepository userRepository;

    public Long createUser(JooqUserDto user) {
        log.debug("Creating user: {}", user.getUsername());
        Long id = userRepository.insert(user);
        log.info("User created successfully with id: {}", id);
        return id;
    }

    public JooqUserDto getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        JooqUserDto user = userRepository.findById(id);
        if (user == null) {
            log.warn("User not found with id: {}", id);
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public JooqUserDto getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        JooqUserDto user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("User not found with username: {}", username);
            throw new EntityNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public List<JooqUserDto> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean updateUser(JooqUserDto user) {
        return userRepository.update(user);
    }

    public boolean deleteUser(Long id) {
        return userRepository.delete(id);
    }
}
