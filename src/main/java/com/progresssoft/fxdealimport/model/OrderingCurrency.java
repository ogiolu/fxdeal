package com.progresssoft.fxdealimport.model;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class OrderingCurrency extends  BaseModel {
   private String  currencyISOCode;
   private Long countOfDeals;
}
