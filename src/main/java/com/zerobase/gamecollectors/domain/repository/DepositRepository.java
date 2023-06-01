package com.zerobase.gamecollectors.domain.repository;

import com.zerobase.gamecollectors.domain.entity.Deposit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    Optional<Deposit> findByUserId(Long userId);

}
