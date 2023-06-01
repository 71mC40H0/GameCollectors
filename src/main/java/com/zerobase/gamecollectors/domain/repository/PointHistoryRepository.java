package com.zerobase.gamecollectors.domain.repository;

import com.zerobase.gamecollectors.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

}
