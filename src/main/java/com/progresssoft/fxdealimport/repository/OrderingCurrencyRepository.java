package com.progresssoft.fxdealimport.repository;

import com.progresssoft.fxdealimport.model.BadFxDeal;
import com.progresssoft.fxdealimport.model.OrderingCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingCurrencyRepository extends JpaRepository<OrderingCurrency, Long> {

    @Query(value ="select p FROM OrderingCurrency p WHERE p.currencyISOCode =:code ")
    OrderingCurrency findOrderingCurrency(@Param("code") String code);


}
