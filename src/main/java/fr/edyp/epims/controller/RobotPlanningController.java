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
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.*;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RobotPlanningController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RobotPlanningController.class);

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    RobotPlanningRepository robotPlanningRepository;

    @Autowired
    VirtualPlateRepository virtualPlateRepository;

    @Autowired
    VirtualWellRepository virtualWellRepository;

    @Autowired
    SupportRepository supportRepository;

    @Autowired
    PlateRepository plateRepository;

    @Autowired
    ProtocolApplicationRepository protocolApplicationRepository;

    @Autowired
    RunRobotRepository runRobotRepository;

    @Autowired
    TubeRepository tubeRepository;

    @Autowired
    WellRepository wellRepository;

    @Autowired
    TreatmentsApplicationRepository treatmentsApplicationRepository;

    @Autowired
    TreatmentsRepository treatmentsRepository;

    @PostMapping("/checkrobotplanning")
    public ResponseEntity checkCanAddRobotPlannings(@RequestBody List<RobotPlanningJson> robotPlanningJsonList) {
        for (RobotPlanningJson robotPlanningJson : robotPlanningJsonList) {
            List<RobotPlanning> l = robotPlanningRepository.findBySampleName(robotPlanningJson.getSample().getName());

            if ((l != null) && (! l.isEmpty())) {
                return new ResponseEntity(robotPlanningJson.getSample().getName(), HttpStatus.OK);
            }
        }

        return new ResponseEntity(null, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/addrobotplanning")
    public ResponseEntity addRobotPlannings(@RequestBody List<RobotPlanningJson> robotPlanningJsonList) {

        for (RobotPlanningJson robotPlanningJson : robotPlanningJsonList) {


            Optional<Actor> actorOpt = actorRepository.findById(robotPlanningJson.getActorKey());
            if (!actorOpt.isPresent()) {
                throw new DataIntegrityViolationException("Actor not found");
            }
            Actor actor = actorOpt.get();

            Sample sample = sampleRepository.findByName(robotPlanningJson.getSample().getName()).get();


            //TODO : refuse if a RobotPlanning with same sample already exists

            RobotPlanning robotPlanning = new RobotPlanning(actor, sample, robotPlanningJson.getTrypsineVol(), robotPlanningJson.getProteinQty(),
                    robotPlanningJson.getSeparationResultClass(), robotPlanningJson.getDate(), robotPlanningJson.getLoadCount(), robotPlanningJson.getDescription(), robotPlanningJson.getSampleConsumed(),robotPlanningJson.getName(), null
                    );

            robotPlanningRepository.save(robotPlanning);


        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DatabaseVersionManager.getSingleton().bumpVersion(RobotDataJson.class, auth.getPrincipal().toString());


        return new ResponseEntity(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/modifyrobotplanning")
    public ResponseEntity modifyRobotPlanning(@RequestBody RobotPlanningJson robotPlanningJson) {

        Optional<RobotPlanning> robotPlanningOpt = robotPlanningRepository.findById(robotPlanningJson.getId());
        if (!robotPlanningOpt.isPresent()) {
            throw new DataIntegrityViolationException("RobotPlanning not found");
        }

        RobotPlanning robotPlanning = robotPlanningOpt.get();
        robotPlanning.setLoadCount(robotPlanningJson.getLoadCount());
        robotPlanning.setProteinQty(robotPlanningJson.getProteinQty());
        robotPlanning.setTrypsineVol(robotPlanningJson.getTrypsineVol());
        robotPlanning.setDescription(robotPlanningJson.getDescription());

        robotPlanning = robotPlanningRepository.save(robotPlanning);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DatabaseVersionManager.getSingleton().bumpVersion(RobotDataJson.class, auth.getPrincipal().toString());


        return new ResponseEntity(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/addplate")
    public ResponseEntity addPlate(@RequestBody VirtualPlateJson virtualPlateJson) {

        Optional<VirtualPlate> virtualPlatOpt = virtualPlateRepository.findByName(virtualPlateJson.getName());
        if (virtualPlatOpt.isPresent()) {
            throw new DataIntegrityViolationException("Virtual Plate"+virtualPlateJson.getName()+"already exists");
        }

        Optional<Actor> actorOpt = actorRepository.findById(virtualPlateJson.getActor());
        if (!actorOpt.isPresent()) {
            throw new DataIntegrityViolationException("Actor not found");
        }
        Actor actor = actorOpt.get();

        VirtualPlate virtualPlate = new VirtualPlate(virtualPlateJson.getName(), actor, virtualPlateJson.getPlannedDate(),
                Boolean.FALSE, virtualPlateJson.getXSize(), virtualPlateJson.getYSize(), new HashSet<>());

        virtualPlateRepository.save(virtualPlate);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DatabaseVersionManager.getSingleton().bumpVersion(RobotDataJson.class, auth.getPrincipal().toString());


        return new ResponseEntity(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/saveplate")
    public ResponseEntity<RobotDataJson> savePlate(@RequestBody VirtualPlateJson virtualPlateJson) {

        // Check existence of virtualPlate
        Optional<VirtualPlate> virtualPlatOpt = virtualPlateRepository.findByName(virtualPlateJson.getName());
        if (! virtualPlatOpt.isPresent()) {
            throw new DataIntegrityViolationException("Virtual Plate"+virtualPlateJson.getName()+"does not exist");
        }
        VirtualPlate virtualPlate = virtualPlatOpt.get();

        // Retrieve all robotPlannings
        HashMap<Integer, RobotPlanning> robotPlanningMap = new HashMap<>();
        List<RobotPlanning> robotPlanningList = robotPlanningRepository.findAll();
        for (RobotPlanning rb : robotPlanningList) {
            robotPlanningMap.put(rb.getId(), rb);
        }

        // Delete all Wells of the virtual Plate
        virtualWellRepository.deleteWells(virtualPlate.getName());

        // Create all Wells needed according to json data
        for (VirtualWellJson virtualWellJson : virtualPlateJson.getVirtualWells()) {
            RobotPlanningJson robotPlanningJson = virtualWellJson.getRobotPlanning();
            RobotPlanning robotPlanning = robotPlanningMap.get(robotPlanningJson.getId());
            if (robotPlanning == null) {
                // should not happen
                throw new DataIntegrityViolationException("Save Plate "+virtualPlateJson.getName()+" : impossible to retrieve a RobotPlanning");
            }

            VirtualWell well = new VirtualWell(robotPlanning, virtualPlate, virtualWellJson.getXCoord(), virtualWellJson.getYCoord());
            virtualWellRepository.save(well);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DatabaseVersionManager.getSingleton().bumpVersion(RobotDataJson.class, auth.getPrincipal().toString());


        return new ResponseEntity(HttpStatus.OK);
    }



    @GetMapping("/robotdata")
    public ResponseEntity<RobotDataJson> getRobotData() {
        try {

            HashMap<String, VirtualPlateJson> platesMap = new HashMap<>();
            ArrayList<RobotPlanningJson> freeRobotPlanningList = new ArrayList<>();

            RobotDataJson robotDataJson = new RobotDataJson();
            robotDataJson.setPlatesMap(platesMap);
            robotDataJson.setFreeRobotPlanningList(freeRobotPlanningList);

            // Create VirtualPlateJsons
            List<VirtualPlate> plateList = virtualPlateRepository.findAll();
            for (VirtualPlate virtualPlate : plateList) {
                String plateKey = virtualPlate.getName();
                Actor actor = virtualPlate.getActor();
                String actorKey = (actor != null) ? actor.getLogin() : null;
                VirtualPlateJson virtualPlateJson = new VirtualPlateJson(virtualPlate.getName(), actorKey, virtualPlate.getPlannedDate(), virtualPlate.getLocked(), virtualPlate.getXSize(),
                            virtualPlate.getYSize(), new HashSet<>());
                platesMap.put(plateKey, virtualPlateJson);
            }


            // Create RobotPlanningJsons and VirtualPlateJson
            List<RobotPlanning> robotPlanningList = robotPlanningRepository.findAll();

            for (RobotPlanning robotPlanning : robotPlanningList) {

                Actor actor = robotPlanning.getActor();
                String actorKey = (actor != null) ? actor.getLogin() : null;

                RobotPlanningJson robotPlanningJson = new RobotPlanningJson(robotPlanning.getId(), actorKey,
                        Converter.convert(robotPlanning.getSample(), null) , robotPlanning.getTrypsineVol(), robotPlanning.getProteinQty(),
                        robotPlanning.getSeparationResultClass(), robotPlanning.getDate(), robotPlanning.getLoadCount(), robotPlanning.getDescription(),
                        robotPlanning.getSampleConsumed(), robotPlanning.getName(), null);

                Set<VirtualWell> virtualWellSet = robotPlanning.getVirtualWells();
                if( virtualWellSet.isEmpty()) {
                    freeRobotPlanningList.add(robotPlanningJson);
                } else {
                    Set<Integer> virtualWellsJsonId = new HashSet<>();
                    robotPlanningJson.setVirtualWellsId(virtualWellsJsonId);

                    for (VirtualWell virtualWell : virtualWellSet) {

                        VirtualPlate virtualPlate = virtualWell.getVirtualPlate();
                        String plateKey = virtualPlate.getName();
                        VirtualPlateJson virtualPlateJson = platesMap.get(plateKey);

                        VirtualWellJson virtualWellJson = new VirtualWellJson(virtualWell.getId(), robotPlanningJson, plateKey, virtualWell.getXCoord(), virtualWell.getYCoord());
                        virtualPlateJson.getVirtualWells().add(virtualWellJson);
                        virtualWellsJsonId.add(virtualWellJson.getId());

                    }
                }
            }

            return ControllerUtil.createResponseWithVersion(robotDataJson, RobotDataJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /api/robotdata", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/closeplate")
    public ResponseEntity closeplate(@RequestBody ClosePlateJson closePlateJson) {

        // Retrieve data in database from parameter
        Optional<VirtualPlate> plateOpt = virtualPlateRepository.findByName(closePlateJson.getPlateName());
        if (! plateOpt.isPresent()) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        VirtualPlate virtualPlate = plateOpt.get();

        Optional<Actor> actorOpt = actorRepository.findByLogin(closePlateJson.getActorLogin());
        if (! actorOpt.isPresent()) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Actor actor = actorOpt.get();

        Plate plate = new Plate(virtualPlate.getName(), virtualPlate.getXSize(), virtualPlate.getYSize());
        plate = plateRepository.save(plate);

        ProtocolApplication protocolApplication = new ProtocolApplication(actor, null, null, virtualPlate.getPlannedDate(), null, null,
                null, null, null, null /* Set<Transfer> transfers ??? */, null, null, null /*runRobot*/);
        protocolApplication = protocolApplicationRepository.save(protocolApplication);


        RunRobot runRobot = new RunRobot(plate, protocolApplication, null);
        protocolApplication.setRunRobot(runRobot);

        runRobot = runRobotRepository.save(runRobot);
        protocolApplication = protocolApplicationRepository.save(protocolApplication);

        HashSet<Sample> sampleSet = new HashSet();

        Set<VirtualWell> wellsSet = virtualPlate.getVirtualWells();
        for (VirtualWell virtualWell : wellsSet) {

            RobotPlanning robotPlanning = virtualWell.getRobotPlanning();

            Sample sample = robotPlanning.getSample();
            sampleSet.add(sample);
            Support support = new Support(sample, new Integer(0), null, null, null, null);
            support = supportRepository.save(support);
            Tube tube = new Tube(support);
            tube = tubeRepository.save(tube);
            support.setTube(tube);


            Well well = new Well(plate, support, virtualWell.getXCoord(), virtualWell.getYCoord());
            well = wellRepository.save(well);
            support.setWell(well);
            support = supportRepository.save(support);

            robotPlanningRepository.delete(virtualWell.getRobotPlanning());
            virtualWellRepository.delete(virtualWell);
        }
        virtualPlateRepository.delete(virtualPlate);

        for (Sample sample : sampleSet) {
            Treatments treatments = sample.getTreatments();
            Integer rank = 0;
            if (treatments == null) {
                treatments = new Treatments();
                treatmentsRepository.save(treatments);
                sample.setTreatments(treatments);
                sample = sampleRepository.save(sample);
            } else {
                Set<TreatmentsApplication> treatmentsApplicationSet = treatments.getTreatmentsApplications();
                if (treatmentsApplicationSet != null) {
                    rank = treatmentsApplicationSet.size();
                }
            }

            TreatmentsApplicationId treatmentsApplicationId = new TreatmentsApplicationId(treatments.getId(), protocolApplication.getId());
            TreatmentsApplication treatmentsApplication = new TreatmentsApplication(treatmentsApplicationId, protocolApplication, treatments, rank);
            treatmentsApplication = treatmentsApplicationRepository.save(treatmentsApplication);

        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DatabaseVersionManager.getSingleton().bumpVersion(RobotDataJson.class, auth.getPrincipal().toString());

        // StudyJson : because Study is modified via its samples
        DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, auth.getPrincipal().toString());

        return new ResponseEntity(HttpStatus.OK);
    }


}
