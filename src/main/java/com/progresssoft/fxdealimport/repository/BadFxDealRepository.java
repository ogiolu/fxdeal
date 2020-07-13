package com.progresssoft.fxdealimport.repository;

import com.progresssoft.fxdealimport.model.BadFxDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BadFxDealRepository extends JpaRepository<BadFxDeal, Long> {
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN 'true' ELSE 'false' END FROM BadFxDeal u WHERE u.fileName = :filename ")
    Boolean doesBadFxDealFileNameExists(@Param("filename") String filename);
}
