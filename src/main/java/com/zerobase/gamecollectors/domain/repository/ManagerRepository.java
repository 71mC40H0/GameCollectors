package com.zerobase.gamecollectors.domain.repository;

import com.zerobase.gamecollectors.domain.entity.Manager;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

    Optional<Manager> findByEmail(String email);

    Optional<Manager> findByIdAndEmail(Long id, String email);
}
