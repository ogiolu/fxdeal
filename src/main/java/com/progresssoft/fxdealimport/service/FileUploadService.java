package com.progresssoft.fxdealimport.service;


import com.progresssoft.fxdealimport.model.FxDeal;
import com.progresssoft.fxdealimport.model.FxDealsStat;

import java.io.InputStream;
import java.util.List;

import java.util.concurrent.CompletableFuture;


public interface FileUploadService {

    CompletableFuture<List<FxDeal>> saveFxDeals(InputStream inputStream, String fileName) throws Exception;

    Boolean doesFileNameExist(String fileName);

    CompletableFuture<FxDealsStat> getAllFxDeals(String filename);
}
