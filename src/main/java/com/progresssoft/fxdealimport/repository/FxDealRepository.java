package com.progresssoft.fxdealimport.repository;

import com.progresssoft.fxdealimport.model.FxDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FxDealRepository extends JpaRepository<FxDeal, Long> {

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN 'true' ELSE 'false' END FROM FxDeal u WHERE u.fileName = :filename ")
    Boolean doesFxDealFileNameExists(@Param("filename") String filename);

}
