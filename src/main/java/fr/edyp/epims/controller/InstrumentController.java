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

import fr.edyp.epims.database.dao.InstrumentRepository;
import fr.edyp.epims.database.dao.RobotRepository;
import fr.edyp.epims.database.dao.SpectrometerRepository;
import fr.edyp.epims.database.entities.Instrument;
import fr.edyp.epims.database.entities.Robot;
import fr.edyp.epims.database.entities.Spectrometer;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.InstrumentJson;
import fr.edyp.epims.version.DatabaseVersionManager;
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
public class InstrumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentController.class);

    @Autowired
    InstrumentRepository instrumentRepository;

    @Autowired
    SpectrometerRepository spectrometerRepository;

    @Autowired
    RobotRepository robotRepository;

    @Transactional
    @PostMapping("/modifyinstrument")
    public ResponseEntity<InstrumentJson> modifyInstrument(@RequestBody InstrumentJson instrumentJson) {

        try {

            // check that the instrument exists
            Optional<Instrument> instrumentOpt = instrumentRepository.findById(instrumentJson.getId());
            if (! instrumentOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Instrument instrument = instrumentOpt.get();
            instrument.setManufacturer(instrumentJson.getManufacturer());
            instrument.setModel(instrumentJson.getModel());
            instrument.setStatus(instrumentJson.getStatus());
            instrument = instrumentRepository.save(instrument);


            instrumentJson = new InstrumentJson(
                    instrument.getId(),
                    instrument.getName(),
                    instrument.getManufacturer(),
                    instrument.getModel(),
                    instrument.getStatus(),
                    instrumentJson.getIsSpectrometer()
            );

            DatabaseVersionManager.getSingleton().bumpVersion(InstrumentJson.class, null);

            return new ResponseEntity(instrumentJson, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/modifyinstrument", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createinstrument")
    public ResponseEntity<InstrumentJson> createInstrument(@RequestBody InstrumentJson instrumentJson) {

        try {

            // check that the instrument does not already exist
            String name = instrumentJson.getName();
            Optional<Instrument> instrumentOpt = instrumentRepository.findByName(name);
            if (instrumentOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

            Instrument instrument = new Instrument(-1, instrumentJson.getName(), instrumentJson.getManufacturer(), instrumentJson.getModel(),
                    instrumentJson.getStatus(), null, null);
            instrument = instrumentRepository.save(instrument);

            if (instrumentJson.getIsSpectrometer()) {
                Spectrometer spectrometer = new Spectrometer();
                spectrometer.setInstrument(instrument);
                spectrometerRepository.save(spectrometer);
            } else {
                Robot robot = new Robot();
                robot.setInstrument(instrument);
                robotRepository.save(robot);
            }


            instrumentJson = new InstrumentJson(
                    instrument.getId(),
                    instrument.getName(),
                    instrument.getManufacturer(),
                    instrument.getModel(),
                    instrument.getStatus(),
                    instrumentJson.getIsSpectrometer()
            );

            DatabaseVersionManager.getSingleton().bumpVersion(InstrumentJson.class, null);

            return new ResponseEntity(instrumentJson, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/createinstrument", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/instrumentforname/{instrumentname}")
    public ResponseEntity<InstrumentJson> getSampleForName(@PathVariable("instrumentname") String instrumentname) {

        InstrumentJson instrumentJson = null;
        Optional<Instrument> instrumentOpt = instrumentRepository.findByName(instrumentname);
        if (instrumentOpt.isPresent()) {
            instrumentJson = Converter.convert(instrumentOpt.get());
        }

        return new ResponseEntity<InstrumentJson>(instrumentJson, HttpStatus.OK);
    }

    @GetMapping("/spectrometers")
    public ResponseEntity<List<InstrumentJson>> getAllInstruments() {
        try {
            List<Instrument> instruments = new ArrayList<>();
            instrumentRepository.findAll().forEach(instruments::add);

            HashSet<Integer> spectrometersIdSet = new HashSet<>();
            List<Spectrometer> spectrometers = new ArrayList<>();
            spectrometerRepository.findAll().forEach(spectrometers::add);
            for (Spectrometer s : spectrometers) {
                spectrometersIdSet.add(s.getId());
            }


            ArrayList<InstrumentJson> instrumentJsonList = new ArrayList<>();
            for (Instrument instrument : instruments) {

                InstrumentJson instrumentJson = new InstrumentJson(
                        instrument.getId(),
                        instrument.getName(),
                        instrument.getManufacturer(),
                        instrument.getModel(),
                        instrument.getStatus(),
                        spectrometersIdSet.contains(instrument.getId())
                );

                instrumentJsonList.add(instrumentJson);

            }

            Collections.sort(instrumentJsonList);

            return ControllerUtil.createResponseWithVersion(instrumentJsonList, InstrumentJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /api/spectrometers", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

