package com.zerobase.gamecollectors.domain.repository;

import com.zerobase.gamecollectors.domain.entity.Game;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByDeletedAtIsNull();

    Optional<Game> findByIdAndDeletedAtIsNotNull(Long id);
}
