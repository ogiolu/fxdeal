package com.progresssoft.fxdealimport.controller;

import com.progresssoft.fxdealimport.model.FxDeal;
import com.progresssoft.fxdealimport.service.FileUploadService;
import com.progresssoft.fxdealimport.vo.payload.DataResponse;
import com.progresssoft.fxdealimport.vo.payload.IDataResponse;
import com.progresssoft.fxdealimport.vo.payload.UploadResposeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RestController
@RequestMapping("/app/fxdeal/v1/fx" )
public class FxController {

    @Autowired
    private  FileUploadService fileUploadService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FxController.class);

    @RequestMapping (method = RequestMethod.POST, consumes={MediaType.MULTIPART_FORM_DATA_VALUE},
            produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    DataResponse uploadFile(
            @RequestParam (value = "file") MultipartFile file,@RequestParam  (value = "filename") String filename) {
        IDataResponse dataResponse = new IDataResponse();
        try {
            if (fileUploadService.doesFileNameExist(filename)){
                dataResponse.setValid(false);
                dataResponse.setData(Collections.singletonList("File has been Processed "));
            }else{
                dataResponse.setValid(true);
                dataResponse.setData(Collections.singletonList(fileUploadService.saveFxDeals(file.getInputStream(), filename)));
            }

        } catch( Exception e) {
            e.printStackTrace();
            dataResponse.setValid(false);
            dataResponse.setData(Collections.singletonList(e.getMessage()));
        }

        return dataResponse;

    }

    @GetMapping("/getstat/{filename}")
    public @ResponseBody
    CompletableFuture<ResponseEntity> getAllFxDeals(@PathVariable ("filename") String filename ) {
        return fileUploadService.getAllFxDeals(filename).<ResponseEntity>thenApply(ResponseEntity::ok)
                .exceptionally(handleGetDealFailure);
    }

    private static Function<Throwable, ResponseEntity<? extends List<FxDeal>>> handleGetDealFailure = throwable -> {
        LOGGER.error("Failed to read records: {}", throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    };
}


