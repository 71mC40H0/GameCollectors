package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.domain.entity.Manager;
import com.zerobase.gamecollectors.domain.repository.ManagerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerDetailService {

    private final ManagerRepository managerRepository;

    public Optional<Manager> findByIdAndEmail(Long id, String email) {
        return managerRepository.findByIdAndEmail(id, email);
    }

}
