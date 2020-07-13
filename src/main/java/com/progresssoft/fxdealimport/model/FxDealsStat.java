package com.progresssoft.fxdealimport.model;

import lombok.Data;

import javax.annotation.sql.DataSourceDefinition;
import javax.persistence.Entity;

@Entity
@Data
public class FxDealsStat extends  BaseModel {
    private long duration;
    private int validRecordsCount;
    private int inValidRecordsCount;
    private String fileName;
}
