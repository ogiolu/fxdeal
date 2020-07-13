package com.progresssoft.fxdealimport.model;

import lombok.Data;

import javax.persistence.Entity;

    @Entity
    @Data
    public class BadFxDeal extends  BaseModel {
        private String uniqueDealId;
        private String currencyFrom;
        private String currencyTo;
        private String dealTime;
        private Double dealAmount;
        private Boolean isValidRecord;
        private String fileName;
        private String comments;

}
