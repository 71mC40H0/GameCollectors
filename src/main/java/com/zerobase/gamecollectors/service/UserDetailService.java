package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.domain.entity.User;
import com.zerobase.gamecollectors.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService {

    private final UserRepository userRepository;

    public Optional<User> findByIdAndEmail(Long id, String email) {
        return userRepository.findByIdAndEmail(id, email);
    }

}
