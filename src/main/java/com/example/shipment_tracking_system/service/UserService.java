package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.dto.request.UserCreateRequest;
import com.example.shipment_tracking_system.dto.response.UserResponse;
import com.example.shipment_tracking_system.exception.ResourceNotFoundException;
import com.example.shipment_tracking_system.mapper.UserMapper;
import com.example.shipment_tracking_system.model.Role;
import com.example.shipment_tracking_system.model.User;
import com.example.shipment_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserResponse create(UserCreateRequest request) {
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.CUSTOMER);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}