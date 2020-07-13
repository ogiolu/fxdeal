package com.progresssoft.fxdealimport.vo.payload;

import lombok.Data;

@Data
public class UploadResposeVO {
    private long duration;
    private int validRecordsCount;
    private int inValidRecordsCount;
}
