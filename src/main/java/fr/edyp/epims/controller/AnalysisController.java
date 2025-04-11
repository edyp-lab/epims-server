
/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.edyp.epims.controller;


import fr.edyp.epims.database.dao.*;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    AnalysisRequestRepository analysisRequestRepository;

    @Autowired
    AnalysisPriceListRepository analysisPriceListRepository;

    @Autowired
    AnalysisPriceItemRepository analysisPriceItemRepository;

    @Autowired
    AnalysisPriceList2ItemRepository analysisPriceList2ItemRepository;

    @Autowired
    StudyRepository studyRepository;

    @Transactional
    @PostMapping("/saveanalysis")
    public ResponseEntity<AnalysisMapJson> saveAnalysis(@RequestBody AnalysisMapJson json) {

        try {

            // JPM.WART : we need to drop the role constraint : this line can be suppressed when the new epims
            // has worked one time on the production server and an actor has been created.
            //actorRoleRepository.dropRoleConstraint();

            AnalysisRequest request = null;
            int proAnalyseId = json.getProAnalyseId();
            if (proAnalyseId != -1) {
                Optional<AnalysisRequest> analysisRequestOpt = analysisRequestRepository.findByProAnalysisId(proAnalyseId);
                if (analysisRequestOpt.isPresent()) {
                    request = analysisRequestOpt.get();
                }
            }
            if (request == null) {
                request = new AnalysisRequest();
            }

            request.setProAnalysisId(json.getProAnalyseId());
            request.setPriceListId(json.getPriceListId());
            request.setJsonData(json.getData());
            request.setStudyRef(json.getStudyRef());
            request.setSaveDate(new Date());

            request = analysisRequestRepository.save(request);

            AnalysisMapJson result = new AnalysisMapJson();
            result.setId(request.getId());
            result.setProAnalyseId(request.getProAnalysisId());
            result.setPriceListId(request.getPriceListId());
            result.setExportDate(request.getExportDate());
            result.setSaveDate(request.getSaveDate());
            result.setStudyRef(request.getStudyRef());
            result.setData(request.getJsonData());

            return new ResponseEntity(result, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/saveanalysis", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/loadanalysis/{proanalysisid}")
    public ResponseEntity<AnalysisMapJson> loadAnalysis(@PathVariable("proanalysisid") int proAnalysisId) {

        try {

            Optional<AnalysisRequest> analysisRequestOpt = analysisRequestRepository.findByProAnalysisId(proAnalysisId);
            AnalysisRequest request = null;
            if (analysisRequestOpt.isPresent()) {
                request = analysisRequestOpt.get();
            } else {
                return new ResponseEntity(null, HttpStatus.OK);
            }


            AnalysisMapJson result = new AnalysisMapJson();
            result.setId(request.getId());
            result.setProAnalyseId(request.getProAnalysisId());
            result.setPriceListId(request.getPriceListId());
            result.setSaveDate(request.getSaveDate());
            result.setExportDate(request.getExportDate());
            result.setData(request.getJsonData());
            result.setStudyRef(request.getStudyRef());

            return new ResponseEntity(result, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/loadanalysis", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/loadallanalysis")
    public ResponseEntity<AnalysisMapJson[]> loadallanalysis() {

        try {

            List<AnalysisRequest> requests = analysisRequestRepository.findAll();
            AnalysisMapJson[] result = new AnalysisMapJson[requests.size()];
            int i = 0;
            for (AnalysisRequest r : requests) {
                AnalysisMapJson analysisMapJson = new AnalysisMapJson();
                analysisMapJson.setId(r.getId());
                analysisMapJson.setProAnalyseId(r.getProAnalysisId());
                analysisMapJson.setPriceListId(r.getPriceListId());
                analysisMapJson.setSaveDate(r.getSaveDate());
                analysisMapJson.setExportDate(r.getExportDate());
                analysisMapJson.setData(r.getJsonData());
                analysisMapJson.setStudyRef(r.getStudyRef());
                result[i++] = analysisMapJson;
            }

            return new ResponseEntity(result, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/loadallanalysis", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/analysisexported/{proanalysisid}")
    public ResponseEntity<Date> analysisExported(@PathVariable("proanalysisid") int proAnalysisId, @RequestBody Date exportDate) {

        try {

            AnalysisRequest request = null;
            Optional<AnalysisRequest> analysisRequestOpt = analysisRequestRepository.findByProAnalysisId(proAnalysisId);
            if (analysisRequestOpt.isPresent()) {
                request = analysisRequestOpt.get();
            }

            if (request == null) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            request.setExportDate(exportDate);

            request = analysisRequestRepository.save(request);


            return new ResponseEntity(HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/analysisexported", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/savepricelist")
    public ResponseEntity<List<AnalysisPriceListJson>> savePriceList(@RequestBody AnalysisPriceListJson json) {


        try {

            AnalysisPriceList analysisPriceList = null;

            // if the new price is not different of the last price, we replace the previous price list
            List<AnalysisPriceList> priceListResult = analysisPriceListRepository.findAllDateDesc();
            if ((priceListResult != null) && (!priceListResult.isEmpty())) {
                AnalysisPriceList curPriceList = priceListResult.get(0);
                if (Math.abs(curPriceList.getDate().getTime() - json.getDate().getTime()) < 24 * 3600 * 1000) {
                    analysisPriceList = curPriceList;
                    analysisPriceList.setDate(json.getDate());

                    Set<AnalysisPriceList2Item> set = analysisPriceList.getAnalysisPriceList2Items();
                    for (AnalysisPriceList2Item list2Item : set) {
                        AnalysisPriceItem priceItem = list2Item.getAnalysisPriceItem();
                        analysisPriceList2ItemRepository.delete(list2Item);
                        analysisPriceItemRepository.delete(priceItem);

                    }
                    analysisPriceList.setAnalysisPriceList2Items(new HashSet<>());
                    analysisPriceList = analysisPriceListRepository.save(analysisPriceList);

                    priceListResult.remove(curPriceList);
                }
            }

            if (analysisPriceList == null) {
                analysisPriceList = new AnalysisPriceList();
                analysisPriceList.setDate(json.getDate());
                analysisPriceList = analysisPriceListRepository.save(analysisPriceList);
            }

            // Fill the price list
            Set<AnalysisPriceList2Item> list2ItemSet = new HashSet<>();

            HashMap<String, AnalysePriceItemJson> priceMap = json.getPriceMap();
            for (AnalysePriceItemJson priceItemJson : priceMap.values()) {
                AnalysisPriceItem priceItem = new AnalysisPriceItem();
                priceItem.setPrice(priceItemJson.getPrice());
                priceItem.setLabel(priceItemJson.getLabel());
                priceItem = analysisPriceItemRepository.save(priceItem);

                AnalysisPriceList2Item analysisPriceList2Item = new AnalysisPriceList2Item();
                analysisPriceList2Item.setAnalysisPriceList(analysisPriceList);
                analysisPriceList2Item.setAnalysisPriceItem(priceItem);
                analysisPriceList2Item = analysisPriceList2ItemRepository.save(analysisPriceList2Item);
                list2ItemSet.add(analysisPriceList2Item);
            }

            analysisPriceList.setAnalysisPriceList2Items(list2ItemSet);
            analysisPriceList = analysisPriceListRepository.save(analysisPriceList);

            priceListResult.add(analysisPriceList);

            ArrayList<AnalysisPriceListJson> resultList = toJson(priceListResult);

            return new ResponseEntity(resultList, HttpStatus.OK);


        } catch (Exception e) {
            LOGGER.error("error in /api/savepricelist", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/allpricelist")
    public ResponseEntity<List<AnalysisPriceListJson>> allPriceList() {

        try {

            List<AnalysisPriceList> priceListResult = analysisPriceListRepository.findAll();

            ArrayList<AnalysisPriceListJson> resultList = toJson(priceListResult);

            return new ResponseEntity(resultList, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/allpricelist", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/analysisinfo/{studyref}")
    public ResponseEntity<List<AnalyseProgressJson>> analysisInfo(@PathVariable("studyref") String studyRef) {

        try {

            int nbAcquisitions = 0;
            Date lastRobotDate = null;
            Date lastAcquisitionDate = null;


            Optional<Study> studyData = studyRepository.findByNomenclatureTitle(studyRef);
            if (studyData.isPresent()) {
                Study study = studyData.get();

                for (Sample s : study.getSamples()) {

                    ArrayList<ProtocolApplicationJson> protocolApplicationJsonsList = new ArrayList<>();

                    Treatments treatments = s.getTreatments();
                    if (treatments != null) {
                        Set<TreatmentsApplication> treatmentsApplications = treatments.getTreatmentsApplications();


                        for (TreatmentsApplication treatmentsApplication : treatmentsApplications) {

                            Integer rank = treatmentsApplication.getRank();

                            ProtocolApplication protocolApplication = treatmentsApplication.getProtocolApplication();

                            Acquisition acquisition = protocolApplication.getAcquisition();
                            if (acquisition != null) {
                                // found an acquisition
                                nbAcquisitions++;
                                Date d = protocolApplication.getDate();
                                if ((lastAcquisitionDate == null) || (d.after(lastAcquisitionDate))) {
                                    lastAcquisitionDate = d;
                                }
                            } else {
                                RunRobot runRobot = protocolApplication.getRunRobot();
                                if (runRobot != null) {
                                    Date d = protocolApplication.getDate();
                                    if ((lastRobotDate == null) || (d.after(lastRobotDate))) {
                                        lastRobotDate = d;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnalyseProgressJson analyseProgressJson = new AnalyseProgressJson(lastRobotDate, lastAcquisitionDate, nbAcquisitions);

            return new ResponseEntity(analyseProgressJson, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/analysisinfo", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private ArrayList<AnalysisPriceListJson> toJson(List<AnalysisPriceList> priceListResult) {

        ArrayList<AnalysisPriceListJson> resultList = new ArrayList<>();

        for (
                AnalysisPriceList priceList : priceListResult) {

            AnalysisPriceListJson result = new AnalysisPriceListJson();
            result.setId(priceList.getId());
            result.setDate(priceList.getDate());

            HashMap<String, AnalysePriceItemJson> itemMapJson = new HashMap<>();
            Set<AnalysisPriceList2Item> setList2Item = priceList.getAnalysisPriceList2Items();
            for (AnalysisPriceList2Item list2item : setList2Item) {
                AnalysisPriceItem analysisPriceItem = list2item.getAnalysisPriceItem();
                AnalysePriceItemJson analysePriceItemJson = new AnalysePriceItemJson();
                analysePriceItemJson.setId(analysisPriceItem.getId());
                analysePriceItemJson.setPrice(analysisPriceItem.getPrice());
                analysePriceItemJson.setLabel(analysisPriceItem.getLabel());
                itemMapJson.put(analysisPriceItem.getLabel(), analysePriceItemJson);
            }
            result.setPriceMap(itemMapJson);

            resultList.add(result);

        }

        Collections.sort(resultList);

        return resultList;
    }

}
