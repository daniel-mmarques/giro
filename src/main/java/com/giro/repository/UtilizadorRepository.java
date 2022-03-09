package com.giro.repository;

import com.giro.entites.UtilizadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UtilizadorRepository extends JpaRepository<UtilizadorEntity, Long> {

    Optional<UtilizadorEntity> findByEmailAndAtivo(String email, boolean ativo);

    Optional<UtilizadorEntity> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("update UtilizadorEntity u set u.ativo = 1 where u.pinAtivacao = :uuid")
    int activateProfile(@Param(value = "uuid") String uuid);

}
