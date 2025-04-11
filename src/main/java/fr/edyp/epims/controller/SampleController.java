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
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SampleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    BiologicOriginRepository biologicOriginRepository;

    @Autowired
    SampleKindRepository sampleKindRepository;

    @Autowired
    SampleSpeciesRepository sampleSpeciesRepository;

    @Autowired
    SampleSubcellularLocalisationRepository sampleSubcellularLocalisationRepository;

    @Autowired
    SampleTypeRepository sampleTypeRepository;

    @Autowired
    TreatmentsRepository treatmentsRepository;

    @Autowired
    ProtocolApplicationRepository protocolApplicationRepository;

    @Autowired
    TreatmentsApplicationRepository treatmentsApplicationRepository;

    @Autowired
    AliquotageRepository aliquotageRepository;

    @Autowired
    SeparationRepository separationRepository;

    @Autowired
    SampleFamilyRepository sampleFamilyRepository;

    @Autowired
    SampleLocatorRepository sampleLocatorRepository;

    @Autowired
    SeparationResultRepository separationResultRepository;

    @Autowired
    SamplesSetRepository samplesSetRepository;

    @Autowired
    PathManager pathManager;

    @Transactional
    @PostMapping("/createfragment")
    public ResponseEntity<List<SampleJson>> createFragments(@RequestBody FragmentsGroupToCreateJson fragmentsGroupToCreateJson) {

            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String todayString = format.format(today);

            List<SampleJson> sampleJsons = new ArrayList<>();


            HashMap<String, ArrayList<FragmentToCreateJson>> fragmentsGroupsMap = fragmentsGroupToCreateJson.getParentToFragmentsMap();
            for (String parentSampleKey : fragmentsGroupsMap.keySet()) {

                ArrayList<FragmentToCreateJson> fragments = fragmentsGroupsMap.get(parentSampleKey);

                Sample parentSample = sampleRepository.findByName(parentSampleKey).get();
                Actor actor = parentSample.getActor();
                BiologicOrigin biologicOrigin = parentSample.getBiologicOrigin();
                Study study = parentSample.getStudy();

                Treatments treatments = parentSample.getTreatments();
                if (treatments == null) {
                    treatments = new Treatments(null, null, null, null);
                    treatments = treatmentsRepository.save(treatments);
                    parentSample.setTreatments(treatments);
                    parentSample = sampleRepository.save(parentSample);

                }

                Integer rank = 0;
                Set<TreatmentsApplication> treatmentsApplicationSet = treatments.getTreatmentsApplications();
                if (treatmentsApplicationSet != null) {
                    rank = treatmentsApplicationSet.size();
                }

                String protocolName = "Frag-"+ parentSample.getName()+"-"+todayString;

                SeparationResult separationResult =  new SeparationResult("ResultOf_"+protocolName, null, /*separations*/ null,
                        null, null, /*studies*/ null, null, null);
                separationResult = separationResultRepository.save(separationResult);


                ProtocolApplication protocolApplication = new ProtocolApplication(actor, null, protocolName, today,
                        null, null, null, null,
                        /*Set<SampleFamily> sampleFamilies*/ null, null, null,
                        /*Set<TreatmentsApplication> treatmentsApplications*/ null, null);

                protocolApplication = protocolApplicationRepository.save(protocolApplication);

                TreatmentsApplicationId treatmentsApplicationId = new TreatmentsApplicationId(treatments.getId(), protocolApplication.getId());
                TreatmentsApplication treatmentsApplication = new TreatmentsApplication(treatmentsApplicationId, protocolApplication, treatments, rank);
                treatmentsApplicationRepository.save(treatmentsApplication);


                for (FragmentToCreateJson fragment : fragments) {

                    Sample sample = new Sample(fragment.getName(), actor, biologicOrigin, study, null,
                            fragment.getDescription(), fragment.getVolume(), "Available", fragment.getQuantity(),
                            parentSample.getOriginalName(), parentSample.getRadioactivity(), parentSample.getToxicity(), today,
                            null, null, null, null, null, null, null);


                    sample = sampleRepository.save(sample);


                    SampleFamilyId sampleFamilyId = new SampleFamilyId(parentSample.getName(), sample.getName());
                    SampleFamily sampleFamily = new SampleFamily(sampleFamilyId, protocolApplication, sample, parentSample);
                    sampleFamily = sampleFamilyRepository.save(sampleFamily);

                    SampleLocator sampleLocator = new SampleLocator(sample, separationResult);
                    sampleLocator = sampleLocatorRepository.save(sampleLocator);


                    sampleJsons.add(Converter.convert(sample, null));
                }




                SamplesSet samplesSet = new SamplesSet(separationResult, fragments.size());
                samplesSet = samplesSetRepository.save(samplesSet);
                separationResult.setSamplesSet(samplesSet);
                separationResult = separationResultRepository.save(separationResult);

                Aliquotage aliquotage = new Aliquotage(null, fragments.size());
                Separation separation = new Separation(protocolApplication, separationResult, aliquotage, null, null, null, null);
                aliquotage.setSeparation(separation);
                protocolApplication.setSeparation(separation);
                aliquotage = aliquotageRepository.save(aliquotage);
                separation = separationRepository.save(separation);

            }

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);


            return new ResponseEntity(sampleJsons, HttpStatus.OK);

    }

    @Transactional
    @PostMapping("/addsample")
    public ResponseEntity<List<SampleJson>> addSamples(@RequestBody List<SampleJson> samples) {

            // -- Retrieve Study

            // JPM.WARN : we consider that all new samples are of the same Study
            // We could add a check, but it is the case for the moment.

            Optional<Study> studyOpt = studyRepository.findById(samples.get(0).getStudy());
            if (!studyOpt.isPresent()) {
                throw new DataIntegrityViolationException("Study not found");
            }
            Study study = studyOpt.get();


            // Retrieve actor

            // JPM.WARN : we consider that all new samples are created by the same actor.
            // We could add a check, but it is the case for the moment.

            Optional<Actor> actorOpt = actorRepository.findById(samples.get(0).getActorKey());
            if (!actorOpt.isPresent()) {
                throw new DataIntegrityViolationException("Actor not found");
            }
            Actor actor = actorOpt.get();


            // -- Biologic Origin

            // JPM.WARN : we consider that all new samples are of the same Biologic Orign
            // We could add a check, but it is the case for the moment.

            // Retrieve or Create biological origin
            BiologicOriginJson biologicOriginJson = samples.get(0).getBiologicOriginJson();
            Integer biologicOriginJsonId = biologicOriginJson.getId();

            BiologicOrigin biologicOrigin = null;
            if (biologicOriginJsonId == null) {

                // we must create it or retrieve it

                SampleKind sampleKind = null;
                if (biologicOriginJson.getSampleKind() != null) {
                    Optional<SampleKind> sampleKindOpt = sampleKindRepository.findById(biologicOriginJson.getSampleKind());
                    if (!sampleKindOpt.isPresent()) {
                        throw new DataIntegrityViolationException("Sample Kind not found");
                    }
                    sampleKind = sampleKindOpt.get();
                }

                SampleSpecies sampleSpecies = null;
                Optional<SampleSpecies> sampleSpeciesOpt = sampleSpeciesRepository.findById(biologicOriginJson.getSampleSpecies());
                if (!sampleSpeciesOpt.isPresent()) {
                    throw new DataIntegrityViolationException("SampleSpecies not found");
                }
                sampleSpecies = sampleSpeciesOpt.get();

                SampleSubcellularLocalisation sampleSubcellularLocalisation = null;
                if (biologicOriginJson.getSampleSubcellularLocalisation() != null) {
                    Optional<SampleSubcellularLocalisation> sampleSubcellularLocalisationOpt = sampleSubcellularLocalisationRepository.findById(biologicOriginJson.getSampleSubcellularLocalisation());
                    if (!sampleSubcellularLocalisationOpt.isPresent()) {
                        throw new DataIntegrityViolationException("SampleSubcellularLocalisation not found");
                    }
                    sampleSubcellularLocalisation = sampleSubcellularLocalisationOpt.get();
                }

                SampleType sampleType = null;
                if (biologicOriginJson.getSampleType()  != null) {
                    Optional<SampleType> sampleTypeOpt = sampleTypeRepository.findById(biologicOriginJson.getSampleType());
                    if (!sampleTypeOpt.isPresent()) {
                        throw new DataIntegrityViolationException("SampleType not found");
                    }
                    sampleType = sampleTypeOpt.get();
                }


                List<BiologicOrigin> biologicOriginList =  biologicOriginRepository.findByValues((sampleKind == null ) ? null : sampleKind.getId(),
                        (sampleSpecies == null ) ? null : sampleSpecies.getId(),
                        (sampleSubcellularLocalisation == null ) ? null : sampleSubcellularLocalisation.getId(),
                        (sampleType == null ) ? null : sampleType.getId());
                if (! biologicOriginList.isEmpty()) {
                    biologicOrigin = biologicOriginList.get(0);
                } else {
                    biologicOrigin = new BiologicOrigin(sampleKind, sampleSpecies, sampleSubcellularLocalisation, sampleType, biologicOriginJson.getCommentOrigin());
                    biologicOriginRepository.save(biologicOrigin);
                }



            } else {
                Optional<BiologicOrigin> biologicOriginOpt = biologicOriginRepository.findById(biologicOriginJsonId);
                if (!biologicOriginOpt.isPresent()) {
                    throw new DataIntegrityViolationException("BiologicOrigin not found");
                }
                biologicOrigin = biologicOriginOpt.get();
            }


            // -- Create Samples

            List<SampleJson> sampleJsons = new ArrayList<>();

            for (SampleJson sampleJson : samples) {

                Sample sample = new Sample(sampleJson.getName(), actor, biologicOrigin, study, null,
                        sampleJson.getDescription(), sampleJson.getVolume(), sampleJson.getStatus(), sampleJson.getQuantity(),
                        sampleJson.getOriginalName(), sampleJson.getRadioactivity(), sampleJson.getToxicity(), sampleJson.getCreationDate(),
                        new HashSet<>(), new HashSet<>(),new HashSet<>(),new HashSet<>(),new HashSet<>(),new HashSet<>(),new HashSet<>());

                Sample sampleSaved = sampleRepository.save(sample);

                sampleJsons.add(Converter.convert(sampleSaved, null));

            }

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);

            return new ResponseEntity(sampleJsons, HttpStatus.OK);

    }


    @GetMapping("/sampleforname/{samplename}")
    public ResponseEntity<SampleJson> getSampleForName(@PathVariable("samplename") String samplename) {

        Optional<Sample> sampleOpt = sampleRepository.findByName(samplename);
        if (! sampleOpt.isPresent()) {
            return new ResponseEntity<SampleJson>((SampleJson) null, HttpStatus.OK);
        }

        Sample sample = sampleOpt.get();

        return new ResponseEntity<SampleJson>(Converter.convert(sample, null), HttpStatus.OK);
    }

    @GetMapping("/studypathforsamplename/{samplename}")
    public ResponseEntity<StudyPathJson> getStudyPathForSampleName(@PathVariable("samplename") String samplename) {

        StudyPathJson studyPathJson = null;

        Optional<Sample> sampleOpt = sampleRepository.findByName(samplename);
        if (sampleOpt.isPresent()) {
            Study s = sampleOpt.get().getStudy();
            studyPathJson = Converter.convert(s, pathManager.getStudyPath(s));
            return new ResponseEntity<StudyPathJson>(studyPathJson, HttpStatus.OK);
        } else {
            return new ResponseEntity<StudyPathJson>((StudyPathJson) null, HttpStatus.OK);
        }


    }

    @GetMapping("/samples/{id}")
    public ResponseEntity<List<SampleJson>> getSampleByStudyId(@PathVariable("id") int id) {
        try {
            List<Sample> samples = new ArrayList<>();

            ArrayList<SampleJson> list = new ArrayList();

            Optional<Study> studyData = studyRepository.findById(id);
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
                                AcquisitionJson acquisitionJson = new AcquisitionJson(
                                        acquisition.getId(),
                                        acquisition.getSpectrometer().getInstrument().getId(),
                                        acquisition.getNature(),
                                        acquisition.getDurationMin(),
                                        acquisition.getSpectrometer().getInstrument().getName());

                                String sampleActorKey = (s.getActor() != null) ? s.getActor().getLogin() : null;
                                String actorKey = (protocolApplication.getActor() != null) ? protocolApplication.getActor().getLogin() : null;
                                ProtocolApplicationJson protocolApplicationJson = new ProtocolApplicationJson(
                                        protocolApplication.getId(),
                                        s.getName(),
                                        sampleActorKey,
                                        actorKey,
                                        protocolApplication.getName(),
                                        protocolApplication.getDate(),
                                        protocolApplication.getComment(),
                                        study.getId(),
                                        acquisitionJson,
                                        null,
                                        null,
                                        rank
                                );

                                protocolApplicationJsonsList.add(protocolApplicationJson);
                            } else {
                                RunRobot runRobot = protocolApplication.getRunRobot();
                                if (runRobot != null) {

                                    RunRobotJson runRobotJson = new RunRobotJson(runRobot.getId());

                                    String sampleActorKey = (s.getActor() != null) ? s.getActor().getLogin() : null;
                                    String actorKey = (protocolApplication.getActor() != null) ? protocolApplication.getActor().getLogin() : null;
                                    ProtocolApplicationJson protocolApplicationJson = new ProtocolApplicationJson(
                                            protocolApplication.getId(),
                                            s.getName(),
                                            sampleActorKey,
                                            actorKey,
                                            protocolApplication.getName(),
                                            protocolApplication.getDate(),
                                            protocolApplication.getComment(),
                                            study.getId(),
                                            null,
                                            runRobotJson,
                                            null,
                                            rank
                                    );


                                    protocolApplicationJsonsList.add(protocolApplicationJson);
                                } else {
                                    Separation separation = protocolApplication.getSeparation();
                                    if (separation != null) {
                                        Aliquotage aliquotage = separation.getAliquotage();
                                        if (aliquotage != null) {

                                            AliquotageJson aliquotageJson = new AliquotageJson(aliquotage.getId(), aliquotage.getSubSampleCount());

                                            String sampleActorKey = (s.getActor() != null) ? s.getActor().getLogin() : null;
                                            String actorKey = (protocolApplication.getActor() != null) ? protocolApplication.getActor().getLogin() : null;
                                            ProtocolApplicationJson protocolApplicationJson = new ProtocolApplicationJson(
                                                    protocolApplication.getId(),
                                                    s.getName(),
                                                    sampleActorKey,
                                                    actorKey,
                                                    protocolApplication.getName(),
                                                    protocolApplication.getDate(),
                                                    protocolApplication.getComment(),
                                                    study.getId(),
                                                    null,
                                                    null,
                                                    aliquotageJson,
                                                    rank
                                            );
                                            protocolApplicationJsonsList.add(protocolApplicationJson);
                                        }
                                    }
                                }


                            }

                        }
                    }

                    Collections.sort(protocolApplicationJsonsList, new Comparator<ProtocolApplicationJson>() {
                        @Override
                        public int compare(ProtocolApplicationJson o1, ProtocolApplicationJson o2) {
                            Date d2 = o2.getDate();
                            Date d1 = o1.getDate();
                            if ((d1 != null) && (d2 != null)) {
                                if (d1.after(d2)) {
                                    return 1;
                                } else {
                                    return -1;
                                }

                            }
                            return o1.getId()-o2.getId();
                        }
                    });

                    SampleJson sampleJson = Converter.convert(s, protocolApplicationJsonsList);

                    list.add(sampleJson);



                }
            }

            Collections.sort(list);

            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /samples/{id}", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/modifysample")
    public ResponseEntity modifySample(@RequestBody SampleJson sampleJson) {

        Optional<Sample> sampleOpt = sampleRepository.findById(sampleJson.getName());
        if (!sampleOpt.isPresent()) {
            throw new DataIntegrityViolationException("Sample not found");
        }

        Sample sample = sampleOpt.get();
        sample.setDescription(sampleJson.getDescription());

        sample = sampleRepository.save(sample);


        return new ResponseEntity(HttpStatus.OK);
    }
}
