package com.zerobase.gamecollectors.domain.repository;

import com.zerobase.gamecollectors.domain.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {

}
