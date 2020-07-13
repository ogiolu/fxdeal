package com.progresssoft.fxdealimport.service.impl;

import com.progresssoft.fxdealimport.model.BadFxDeal;
import com.progresssoft.fxdealimport.model.FxDeal;
import com.progresssoft.fxdealimport.model.FxDealsStat;
import com.progresssoft.fxdealimport.model.OrderingCurrency;
import com.progresssoft.fxdealimport.repository.BadFxDealRepository;
import com.progresssoft.fxdealimport.repository.FxDealRepository;
import com.progresssoft.fxdealimport.repository.FxDealsStatRepository;
import com.progresssoft.fxdealimport.repository.OrderingCurrencyRepository;
import com.progresssoft.fxdealimport.service.FileUploadService;
import com.progresssoft.fxdealimport.vo.payload.UploadResposeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    private final FxDealRepository fxDealRepository;
    private final BadFxDealRepository badFxDealRepository;
    private final OrderingCurrencyRepository orderingCurrencyRepository;
    private final FxDealsStatRepository fxDealsStatRepository;


    public FileUploadServiceImpl(FxDealRepository fxDealRepository, BadFxDealRepository badFxDealRepository, OrderingCurrencyRepository orderingCurrencyRepository, FxDealsStatRepository fxDealsStatRepository) {
        this.fxDealRepository = fxDealRepository;
        this.badFxDealRepository = badFxDealRepository;
        this.orderingCurrencyRepository = orderingCurrencyRepository;
        this.fxDealsStatRepository = fxDealsStatRepository;
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<List<FxDeal>> saveFxDeals(final InputStream inputStream, String fileName) throws Exception {
        if (doesFileNameExist(fileName)){
            throw new Exception("The File has already been processed ");
        }

        UploadResposeVO uploadResposeVO = new UploadResposeVO();
        final long start = System.currentTimeMillis();
        List<FxDeal> fxDeals = parseCSVFile(inputStream,fileName);
        LOGGER.info("collect  >>>size >>>>>>>>" +fxDeals.size());
        List<FxDeal> goodFxDeals= fxDeals.stream()
                .filter(k->k.getIsValidRecord()).collect(Collectors.toList());
        uploadResposeVO.setInValidRecordsCount(goodFxDeals.size());
        collectAndSaveCurrency(goodFxDeals);
        int badDealsNo =collectAndSaveBadDeals(fxDeals);
        saveGoodDeals(goodFxDeals);
        LOGGER.info("fxDeals>>>>>>>", fxDeals.size());
        uploadResposeVO.setValidRecordsCount(badDealsNo);

        FxDealsStat fxDealsStat = new FxDealsStat();
        fxDealsStat.setDuration((System.currentTimeMillis() - start));
        fxDealsStat.setFileName(fileName);
        fxDealsStat.setInValidRecordsCount(badDealsNo);
        fxDealsStat.setValidRecordsCount(goodFxDeals.size());
        LOGGER.info("Elapsed time: >>> {}", (System.currentTimeMillis() - start));
        fxDealsStatRepository.save(fxDealsStat);
        return  CompletableFuture.completedFuture(fxDeals);
    }

    private void collectAndSaveCurrency(List<FxDeal> goodFxDeals) throws Exception {
        List<OrderingCurrency> orderingCurrencyList = getOrderingCurrencies(goodFxDeals);
        orderingCurrencyRepository.save(orderingCurrencyList);
    }

    private int collectAndSaveBadDeals( List<FxDeal> fxDeals) throws Exception{

        List<BadFxDeal> badFxDeals =new ArrayList<BadFxDeal>();
        fxDeals.stream()
                .filter(k->!k.getIsValidRecord()).forEach(k->{
            BadFxDeal badFxdeal = new BadFxDeal();
            badFxdeal.setCurrencyFrom(k.getCurrencyFrom());
            badFxdeal.setCurrencyTo(k.getCurrencyTo());
            badFxdeal.setDealAmount(k.getDealAmount());
            badFxdeal.setFileName(k.getFileName());
            badFxdeal.setUniqueDealId(k.getUniqueDealId());
            badFxDeals.add(badFxdeal);
        });
        LOGGER.info("badFxDeals >>>size >>>>>>>>"+badFxDeals.size());
        saveBadDeals(badFxDeals);
        return badFxDeals.size();
    }

    private void saveBadDeals(List<BadFxDeal> badFxDeals) {
        if (badFxDeals!= null && badFxDeals.size() > 0){
            badFxDealRepository.save(badFxDeals);
        }
    }

    private void saveGoodDeals(List<FxDeal> goodFxDeals) throws Exception{
        LOGGER.info("goodFxDeals >>>size >>>>>>>>"+goodFxDeals.size());
        if (goodFxDeals!= null && goodFxDeals.size() > 0){
            fxDealRepository.save(goodFxDeals);
        }
    }

    private List<OrderingCurrency> getOrderingCurrencies(List<FxDeal> goodFxDeals) throws Exception {
        Map<String, Long> groupResult =
                goodFxDeals.stream().collect(
                        Collectors.groupingBy(FxDeal::getCurrencyFrom
                                , Collectors.counting()));
        List<OrderingCurrency> orderingCurrencyList = new ArrayList<OrderingCurrency>();
        if(groupResult!= null && groupResult.size() >0 ){
            groupResult.forEach((v,k)-> {

                OrderingCurrency orderingCurrency = null;
                orderingCurrency= orderingCurrencyRepository.findOrderingCurrency(v);
                if(orderingCurrency== null){
                    orderingCurrency = new OrderingCurrency();
                    orderingCurrency.setCountOfDeals(k);
                    orderingCurrency.setCurrencyISOCode(v);
                }else{
                    orderingCurrency.setCountOfDeals(orderingCurrency.getCountOfDeals()+k);
                }
                orderingCurrencyList.add(orderingCurrency);
            });

        }

        return orderingCurrencyList;
    }


    private List<FxDeal> parseCSVFile(final InputStream inputStream,String fileName) throws Exception {
        LOGGER.info("Request to get a list of FxDeal");
         List<FxDeal> fxDealList=new ArrayList<>();
        try {
            try (

                     BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String line;
                while ((line=br.readLine()) != null) {
                    FxDeal fxDeal = buildLineItem(fileName, line);
                    fxDealList.add(fxDeal);

                }

                LOGGER.info("fxDealList >> size>>>>"+fxDealList.size());
                return fxDealList;

            }

        } catch(final IOException e) {
            LOGGER.error("Failed to parse CSV file {}", e);
            throw new Exception("Failed to parse CSV file {}", e);
        }
    }

    private FxDeal buildLineItem(String fileName, String line) {
        String[] data=line.split(",");
        FxDeal fxDeal=new FxDeal();
        fxDeal.setUniqueDealId(data[0]);
        fxDeal.setCurrencyFrom(data[1]);
        fxDeal.setCurrencyTo(data[2]);
        Date dealdate= convertStringDateToDate(data[3]);
        if(dealdate!=null){
            fxDeal.setDealTime(dealdate);
            fxDeal.setIsValidRecord(true);
        }
        else{
            fxDeal.setIsValidRecord(false);
            fxDeal.setComments("Invalid Deal Date Format ");
        }
        if(fxDeal.getIsValidRecord() && validateNumber(data[4])){
            fxDeal.setDealAmount(Double.parseDouble(data[4]));
            fxDeal.setIsValidRecord(true);
        }else{
            fxDeal.setIsValidRecord(false);
            fxDeal.setComments(fxDeal.getComments() + " Invalid Number");
        }
        if (fxDeal.getIsValidRecord() && validateEmptyString(fxDeal)){
            fxDeal.setIsValidRecord(true);
        }else{
            fxDeal.setIsValidRecord(false);
            fxDeal.setComments(fxDeal.getComments() +" Empty field Exist" );
        }
        fxDeal.setFileName(fileName);
        return fxDeal;
    }


    private Boolean validateNumber(String stringValue){
        final  String DOUBLE_PATTERN = "[0-9]+(\\.){0,1}[0-9]*";
       return Pattern.matches(DOUBLE_PATTERN,stringValue);
    }

    private Boolean validateEmptyString( FxDeal fxDeal){
        if(StringUtils.isEmpty(fxDeal.getCurrencyFrom()) || StringUtils.isEmpty(fxDeal.getCurrencyTo())|| StringUtils.isEmpty(fxDeal.getUniqueDealId())){
            return false;
        }
        else{
            return true;
        }
    }

    private  Date convertToDate(SimpleDateFormat simpleDateFormat, String dateString) {
        try {
            Date date = simpleDateFormat.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
  /////"YYYY-MM-DD HH:mm:ss"
    public Date convertStringDateToDate(String dateString){
        String pattern="YYYY-MM-DD HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return convertToDate(simpleDateFormat, dateString);
    }

    @Override
    public Boolean doesFileNameExist(String fileName){
        boolean fileNameExists=false;
        if (fxDealRepository.doesFxDealFileNameExists(fileName) || badFxDealRepository.doesBadFxDealFileNameExists(fileName) ){
            fileNameExists=true;
        }
        return fileNameExists;
    }

    @Async
    @Override
    public CompletableFuture<FxDealsStat> getAllFxDeals(String filename) {
        LOGGER.info("Request to get a list of FxDeal");
        UploadResposeVO uploadResposeVO = new UploadResposeVO();
        FxDealsStat fxDealsStat =fxDealsStatRepository.findFxDealsStatByFileName(filename);
        return CompletableFuture.completedFuture(fxDealsStat);
    }
}
