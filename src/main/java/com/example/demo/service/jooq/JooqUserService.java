package com.example.demo.service.jooq;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * JOOQ 用户服务
 *
 * @author chinwe
 */
@Service
@RequiredArgsConstructor
public class JooqUserService {

    private final JooqUserRepository userRepository;

    public Long createUser(JooqUserDto user) {
        return userRepository.insert(user);
    }

    public JooqUserDto getUserById(Long id) {
        return userRepository.findById(id);
    }

    public JooqUserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username);
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
