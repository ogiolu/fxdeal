package com.progresssoft.fxdealimport.repository;

import com.progresssoft.fxdealimport.model.FxDealsStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxDealsStatRepository  extends JpaRepository<FxDealsStat, Long> {
    FxDealsStat findFxDealsStatByFileName(String fileName);
}
