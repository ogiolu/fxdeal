package com.progresssoft.fxdealimport.model;

import lombok.Data;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@Data
public class FxDeal extends  BaseModel {
    private String uniqueDealId;
    private String currencyFrom;
    private String currencyTo;
    private Date dealTime;
    private Double dealAmount;
    private Boolean isValidRecord;
    private String fileName;
    private String comments;

}
