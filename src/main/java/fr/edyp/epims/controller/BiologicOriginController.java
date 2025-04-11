
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

import fr.edyp.epims.database.dao.SampleSpeciesRepository;
import fr.edyp.epims.database.dao.SampleTypeRepository;
import fr.edyp.epims.database.entities.SampleSpecies;
import fr.edyp.epims.database.entities.SampleType;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.SampleSpeciesJson;
import fr.edyp.epims.json.SampleTypeJson;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class BiologicOriginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BiologicOriginController.class);

    @Autowired
    SampleSpeciesRepository sampleSpeciesRepository;

    @Autowired
    SampleTypeRepository sampleTypeRepository;

    @GetMapping("/samplespecies")
    public ResponseEntity<List<SampleSpeciesJson>> getAllSampleSpecies() {
        try {
            List<SampleSpecies> sampleSpecies = new ArrayList<>();

            sampleSpeciesRepository.findAll().forEach(sampleSpecies::add);


            ArrayList<SampleSpeciesJson> sampleSpeciesJsonArrayList = new ArrayList();
            for (SampleSpecies sampleSpecie : sampleSpecies) {

                sampleSpeciesJsonArrayList.add(new SampleSpeciesJson(sampleSpecie.getId(),sampleSpecie.getName() ));
            }

            Collections.sort(sampleSpeciesJsonArrayList);

            return ControllerUtil.createResponseWithVersion(sampleSpeciesJsonArrayList, SampleSpeciesJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /api/samplespecies", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/addsamplespecies")
    public ResponseEntity<SampleSpeciesJson> addSampleSpecies(@RequestBody SampleSpeciesJson sampleSpeciesJson) {

        try {

            // check that the SampleSpecies does not already exist
            String name = sampleSpeciesJson.getName();
            Optional<SampleSpecies> sampleSpeciesOpt = sampleSpeciesRepository.findByName(name);
            if (sampleSpeciesOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

            SampleSpecies sampleSpecies = new SampleSpecies(name);
            sampleSpecies = sampleSpeciesRepository.save(sampleSpecies);

            DatabaseVersionManager.getSingleton().bumpVersion(SampleSpeciesJson.class, null);

            return new ResponseEntity(Converter.convert(sampleSpecies), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/addsamplespecies", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/sampletypes")
    public ResponseEntity<List<SampleTypeJson>> getAllSampleTypes() {
        try {
            List<SampleType> sampleTypes = new ArrayList<>();

            sampleTypeRepository.findAll().forEach(sampleTypes::add);


            ArrayList<SampleTypeJson> sampleTypeJsonArrayList = new ArrayList();
            for (SampleType sampleType : sampleTypes) {

                sampleTypeJsonArrayList.add(new SampleTypeJson(sampleType.getId(),sampleType.getName() ));
            }

            Collections.sort(sampleTypeJsonArrayList);

            return ControllerUtil.createResponseWithVersion(sampleTypeJsonArrayList, SampleTypeJson.class);

        } catch (Exception e) {
            LOGGER.error("error in /api/sampletypes", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/addsampletype")
    public ResponseEntity<SampleTypeJson> addSampleType(@RequestBody SampleTypeJson sampleTypeJson) {

        try {

            // check that the SampleType does not already exist
            String name = sampleTypeJson.getName();
            Optional<SampleType> sampleTypeOpt = sampleTypeRepository.findByName(name);
            if (sampleTypeOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

            SampleType sampleType = new SampleType(name, null);
            sampleType = sampleTypeRepository.save(sampleType);

            DatabaseVersionManager.getSingleton().bumpVersion(SampleTypeJson.class, null);

            return new ResponseEntity(Converter.convert(sampleType), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/addsampletype", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
