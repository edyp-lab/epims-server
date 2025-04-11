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
import fr.edyp.epims.jms.JmsProducer;
import fr.edyp.epims.path.PathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ProtocolApplicationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolApplicationController.class);

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    AcquisitionRepository acquisitionRepository;

    @Autowired
    ProtocolApplicationRepository protocolApplicationRepository;

    @Autowired
    ProtocolApplicationService protocolApplicationService;

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    InstrumentRepository instrumentRepository;

    @Autowired
    SpectrometerRepository spectrometerRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    TreatmentsRepository treatmentsRepository;

    @Autowired
    TreatmentsApplicationRepository treatmentsApplicationRepository;

    @Autowired
    AcquisitionFileRepository acquisitionFileRepository;

    @Autowired
    AttachedFileRepository attachedFileRepository;

    @Autowired
    FileTagsRepository fileTagsRepository;

    @Autowired
    FixService fixService;

    @Autowired
    PathManager pathManager;

    @Autowired
    JmsProducer jmsProducer;




    // JPM.TODO code used to fix file_link table
    @GetMapping("/fixfilelink")
    public ResponseEntity<String> fixfilelink() {

        // read file link to fix
        List<FixService.FileLinkJson> files = fixService.fileLinkToFix();

        // read Protocol Application where to find
        List<ProtocolApplication> protocolApplicationList = protocolApplicationRepository.wartPA();

        HashMap<String, ProtocolApplication> paMAp = new HashMap<>();
        for (ProtocolApplication pa : protocolApplicationList) {
            String name = pa.getName();
            paMAp.put(name, pa);
        }

        for (FixService.FileLinkJson fileLinkJson : files) {
            String nameFileSrc = fileLinkJson.name;
            String nameFile = fileLinkJson.name;
            int indexDot = nameFile.indexOf('.');
            if (indexDot != -1) {
                nameFile = nameFile.substring(0, indexDot);
            }

            ProtocolApplication pa = paMAp.get(nameFile);

            if (pa == null) {
                System.out.println("Problem "+nameFileSrc);
            } else {
                Optional<AcquisitionFile>  fileLinkOpt = acquisitionFileRepository.findById(fileLinkJson.id);

                AcquisitionFile acquisitionFile = fileLinkOpt.get();
                System.out.println("Done "+pa.getId()+"   "+nameFileSrc+"   "+pa.getName());
                acquisitionFile.setIdFk(pa.getId());
                acquisitionFile.setProtocolApplication(null);
                acquisitionFileRepository.save(acquisitionFile);

            }
        }




        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @GetMapping("/protocolApplications/{studyId}")
    public ResponseEntity<List<ProtocolApplicationJson>> getProtocolApplicationByStudyId(@PathVariable("studyId") int studyId) {
        try {

            ArrayList<ProtocolApplicationJson> list = new ArrayList();

            Optional<Study> studyData = studyRepository.findById(studyId);
            if (studyData.isPresent()) {
                Study study = studyData.get();

                for (Sample s : study.getSamples()) {

                    Treatments treatments = s.getTreatments();
                    if (treatments != null) {
                        Set<TreatmentsApplication> treatmentsApplications = treatments.getTreatmentsApplications();

                        Protocol protocol = treatments.getProtocol();
                        String commentTreatment = treatments.getComment();
                        Set<Sample> samplesForTreatments = treatments.getSamples();
                        //System.out.println(samplesForTreatments.size());

                        for (TreatmentsApplication treatmentsApplication : treatmentsApplications) {
                            ProtocolApplication protocolApplication = treatmentsApplication.getProtocolApplication();

                            Integer rank = treatmentsApplication.getRank();
                            //System.out.println("rank: "+rank);

                            Acquisition acquisition = protocolApplication.getAcquisition();

                            AcquisitionJson acquisitionJson = null;

                            if (acquisition != null) {
                                // Acquisition can be null : JPM check what it means
                                acquisitionJson = new AcquisitionJson(
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

                                list.add(protocolApplicationJson);
                            }
                            // not an acquisition : nothing to do
                            /*else {



                                Pool pool = protocolApplication.getPool();
                                String name = protocolApplication.getName(); // aliq name
                                Preparation prep = protocolApplication.getPreparation();
                                RunRobot runRobot = protocolApplication.getRunRobot();
                                if (runRobot != null) {
                                    ProtocolApplication paRobot = runRobot.getProtocolApplication();
                                    String name2 = paRobot.getName();
                                    //System.out.println("name robot ? : "+name2);
                                }
                                Protocol protocol2 = protocolApplication.getProtocol();
                                if (protocol2 != null) {
                                    //System.out.println(protocol.toString());
                                }
                                Separation separation = protocolApplication.getSeparation();
                                if (separation != null) {
                                    //System.out.println(separation.toString());

                                    Aliquotage aliquotage = separation.getAliquotage();
                                    int subSamples = aliquotage.getSubSampleCount();
                                    SeparationResult separationResult = separation.getSeparationResult();
                                    Lane lane = separationResult.getLane(); // toujours null
                                    String comment = separationResult.getComment(); // toujours null
                                    String separationName = separationResult.getName(); // pas utile
                                    Set<SampleLocator> locators = separationResult.getSampleLocators();
                                    for (SampleLocator locator : locators) {
                                        //System.out.println(locator.getSample().getName());
                                    }
                                    SamplesSet samplesSet = separationResult.getSamplesSet();


                                }
                                Set<SampleFamily> families = protocolApplication.getSampleFamilies();
                                Set<Transfer> transfers = protocolApplication.getTransfers();

                                for (SampleFamily family : families) {
                                    Sample father = family.getSampleByFather();
                                    Sample son = family.getSampleByFather();
                                }

                                for (Transfer transfer : transfers) {
                                    Support start = transfer.getSupportByStartSupport();
                                    Support end = transfer.getSupportByEndSupport();
                                    Tube tube = start.getTube();
                                    Well well = start.getWell();
                                    Tube tube2 = end.getTube();
                                    Well well2 = end.getWell();

                                }

                            }*/

                        }
                    }


                }

                Collections.sort(list);
            }


            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/protocolApplications", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/acquisitionsSearch/{searchText}/{acquisitionType}/{instrumentId}/{studyMemberActorKey}/{startDate}/{endDate}")
    public ResponseEntity<List<ProtocolApplicationJson>> getProtocolApplicationByStudyId(@PathVariable("searchText") String searchText,
                                                                                         @PathVariable("acquisitionType") String acquisitionType,
                                                                                         @PathVariable("instrumentId") int instrumentId,
                                                                                         @PathVariable("studyMemberActorKey") String studyMemberActorKey,
                                                                                         @PathVariable("startDate") String startDate,
                                                                                         @PathVariable("endDate") String endDate) {
        try {

            List<ProtocolApplicationJson> list2 = protocolApplicationService.searchAcquisitions(searchText, acquisitionType, instrumentId, studyMemberActorKey, startDate, endDate);



            return new ResponseEntity<>(list2, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error on acquisitionsSearch", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/protocolapplicationforname/{acqname}/{instrumentName}")
    public ResponseEntity<List<ProtocolApplicationJson>> getProtocolApplicationByNameAndInstrument(@PathVariable("acqname") String acqname,
                                                                                         @PathVariable("instrumentName") String instrumentName) {
        try {

            List<ProtocolApplicationJson> list = protocolApplicationService.searchAcquisitions(acqname, instrumentName);

            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error on protocolapplicationforname", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/acquisitiontypes")
    public ResponseEntity<List<String>> getAcquisitionType() {
        try {

            List<String> natures = acquisitionRepository.findDistinctNatures();

            Collections.sort(natures);


            return new ResponseEntity<>(natures, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error on acquisitiontypes", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createacquisition")
    public ResponseEntity createacquisition(@RequestBody AcquisitionFileMessageJson acquisitionFileMessageJson) {

        try {
            // Prepare Protocol Application and its Acquisition
            ProtocolApplication protocoalApplicationToCreate = fillNewAcquisition(acquisitionFileMessageJson);

            // get Sample and modify its status
            Sample sample = null;
            if (AcquisitionJson.convertNatureToEnum(protocoalApplicationToCreate.getAcquisition().getNature()).equals(AcquisitionJson.Nature.RESEARCH)) {

                Optional<Sample> sampleOpt = sampleRepository.findByName(acquisitionFileMessageJson.getSampleDescriptor().getName());
                if (sampleOpt.isPresent()) {
                    sample = sampleOpt.get();
                }

                //Set sample status && delete planned analysis
                sample.setStatus(Sample.AVAILABLE_STATUS_VALUE);
                //logger.debug(" Save Sample ");
                sample = sampleRepository.save(sample);
            }

            // Create Protocol Application in Database
            ProtocolApplication protocolApplicationCreated = createAcquisition(protocoalApplicationToCreate, sample);

            // Create AcquisitionFile in Database
            AcquisitionFile acqFileCreated = fillNewAcquisitionFile(acquisitionFileMessageJson, protocolApplicationCreated);

            // Dispatch the creation of the AcquisitionFile thanks to JMS
            LOGGER.info("Start Dispatch Acquisition File created to JMS");
            acquisitionFileAvailable(acqFileCreated);
            LOGGER.info("Finished Dispatch Acquisition File created to JMS");

        } catch (Exception serviceExc) {
            LOGGER.error("Problem while saving the acquisition " +acquisitionFileMessageJson.getAcquisitionFileDescriptor().getAcquisition().getName(), serviceExc);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);


    }


    @Transactional
    @PostMapping("/modifyprotocolapplication")
    public ResponseEntity modifyProtocolApplication(@RequestBody ProtocolApplicationJson protocolApplicationJson) {

        Optional<ProtocolApplication> protocolApplicationOpt = protocolApplicationRepository.findById(protocolApplicationJson.getId());
        if (!protocolApplicationOpt.isPresent()) {
            throw new DataIntegrityViolationException("ProtocolApplication (Acquisition) not found");
        }

        ProtocolApplication protocolApplication = protocolApplicationOpt.get();
        protocolApplication.setComment(protocolApplicationJson.getComment());

        protocolApplication = protocolApplicationRepository.save(protocolApplication);


        return new ResponseEntity(HttpStatus.OK);
    }

    private ProtocolApplication fillNewAcquisition(AcquisitionFileMessageJson acqMsg) throws Exception {

        ProtocolApplicationJson protocolApplicationJson = acqMsg.getAcquisitionFileDescriptor().getAcquisition();
        AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();

        Actor actor = null; // actor null is allowed
        Optional<Actor> actorOpt = actorRepository.findByLogin(protocolApplicationJson.getActorKey());
        if (actorOpt.isPresent()) {
            actor = actorOpt.get();
        }

        Optional<Instrument> instrumentOpt = instrumentRepository.findByName(acquisitionJson.getInstrumentName());
        if (! instrumentOpt.isPresent()) {
            return null;
        }

        Optional<Spectrometer> spectrometerOpt = spectrometerRepository.findById(instrumentOpt.get().getId());
        if (! spectrometerOpt.isPresent()) {
            return null;
        }

        ProtocolApplication protocolApplicationToCreate = new ProtocolApplication();
        Acquisition acquisitionToCreate = new Acquisition();

        protocolApplicationToCreate.setActor(actor);
        protocolApplicationToCreate.setComment(protocolApplicationJson.getComment());
        protocolApplicationToCreate.setDate(protocolApplicationJson.getDate());
        protocolApplicationToCreate.setName(protocolApplicationJson.getName());

        acquisitionToCreate.setDurationMin(acquisitionJson.getDurationMin());
        acquisitionToCreate.setNature(acquisitionJson.getNature());
        acquisitionToCreate.setSpectrometer(spectrometerOpt.get());

        acquisitionToCreate.setProtocolApplication(protocolApplicationToCreate);
        protocolApplicationToCreate.setAcquisition(acquisitionToCreate);


        return protocolApplicationToCreate;
    }

    private AcquisitionFile fillNewAcquisitionFile(AcquisitionFileMessageJson acqMsg, ProtocolApplication protocolApplication) throws Exception{
        AcquisitionFileDescriptorJson acqFileDesc = acqMsg.getAcquisitionFileDescriptor();


        // Create AttachedFile without fileLinks and fileTags
        AttachedFile attachedFileToCreate = new AttachedFile();
        attachedFileToCreate.setDate(acqFileDesc.getDate());
        attachedFileToCreate.setName(acqFileDesc.getFileName());
        attachedFileToCreate.setPath(acqFileDesc.getPath());
        attachedFileToCreate.setSizeMo(acqFileDesc.getFileSize());
        attachedFileToCreate.setArchived(false);
        Set<FileLink> fileLinkSet = new HashSet<FileLink>();
        attachedFileToCreate.setFileLinks(fileLinkSet);
        Set<FileTags> fileTagsSet = new HashSet<FileTags>();
        attachedFileToCreate.setFileTagses(fileTagsSet);
        AttachedFile attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);

        // Create AcquisitionFile
        AcquisitionFile acqFileToCreate = new AcquisitionFile();
        acqFileToCreate.setAttachedFile(attachedFileCreated);
        acqFileToCreate.setIdFk(protocolApplication.getId());
        AcquisitionFile acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);

        // Create FileTag

        // Suppose acquisition file are RAW files
        Tag rawTag = tagRepository.findByName("RAW").get();
        FileTagsId fileTagsId = new FileTagsId(attachedFileToCreate.getId(), rawTag.getName());
        FileTags fileTag = new FileTags(fileTagsId, attachedFileToCreate, rawTag, 0);
        FileTags fileTagCreated = fileTagsRepository.save(fileTag);

        fileLinkSet.add(acqFileCreated);
        fileTagsSet.add(fileTagCreated);
        attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);


        fileTagsSet.add(fileTag);


        acqFileCreated.setAttachedFile(attachedFileCreated);
        acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);

        return acqFileCreated;
    }

    private ProtocolApplication createAcquisition(ProtocolApplication protocolApplicationToCreate, Sample sample) throws Exception {
        if (protocolApplicationToCreate == null) {
            throw new Exception("Can not create null Acquisition ");
        }

        // create acquisition (and super-object protocolApplication)
        Acquisition acquisition = protocolApplicationToCreate.getAcquisition();
        protocolApplicationToCreate.setAcquisition(null);
        ProtocolApplication protocolApplication = protocolApplicationRepository.save(protocolApplicationToCreate);

        acquisition.setProtocolApplication(protocolApplication);
        acquisition = acquisitionRepository.save(acquisition);
        protocolApplication.setAcquisition(acquisition);
        protocolApplication = protocolApplicationRepository.save(protocolApplication);


        if (sample != null) {

            //Get next treatmentsApplication rank for current sample
            int treatmAppRank = getNextTreatmentsApplicationRank(sample);

            Treatments treatments = sample.getTreatments();

            //treatments_application creation
            TreatmentsApplicationId treatmAppId = new TreatmentsApplicationId(treatments.getId(), protocolApplication.getId());

            TreatmentsApplication treatmApp = new TreatmentsApplication(treatmAppId, protocolApplication, sample.getTreatments(), treatmAppRank);

            //	create Link between protocolApplication and sample
            treatmApp = treatmentsApplicationRepository.save(treatmApp);

            //Change Status to available as the acquisition is saved
            sample.setStatus(Sample.AVAILABLE_STATUS_VALUE);
            sample = sampleRepository.save(sample);
            //logger.debug(" **** currentSample bio : "+sample.getBiologicOrigin());

            treatments.getSamples().add(sample);
            treatments = treatmentsRepository.save(treatments);


            protocolApplication.getTreatmentsApplications().add(treatmApp);
            protocolApplication = protocolApplicationRepository.save(protocolApplication);
        }


        return protocolApplication;
    }

    public int getNextTreatmentsApplicationRank(Sample sample) throws Exception {
        int nextRank = 0;

        // if no treatments exists for this sample
        if (sample.getTreatments() == null) {
            Treatments treatments = new Treatments();
            treatments = treatmentsRepository.save(treatments);
            sample.setTreatments(treatments);
            sampleRepository.save(sample);

        } else // => a "treatments" already exists for the sample, get next
        // treatments_application rank
        {
            // search for the last treatments_application which have the given
            // treatments id


            Integer maxRank = treatmentsApplicationRepository.maxRank(sample.getTreatments().getId());

            if (maxRank != null) {
                nextRank = maxRank + 1;
            }
        }
        return nextRank;
    }


    private void acquisitionFileAvailable(AcquisitionFile acquisitionFile) throws Exception {
        if (acquisitionFile == null) {
            LOGGER.info("Acquisition File is null");
            throw new Exception("Acquisition File is null");
        }

        String name = "";
        AttachedFile attachedFile = acquisitionFile.getAttachedFile();
        if (attachedFile != null) {
            name = attachedFile.getName();
        }

        if (LOGGER.isDebugEnabled()) {


            LOGGER.debug("Send Msg acquisition file available for acq " + name);
        }
        LOGGER.info("Send Msg acquisition file available for acq " + name);


        AcquisitionFileMessageJson acquisitionFileMessageJson = createMessageInformation(acquisitionFile);
        LOGGER.info("Message Ready to Send " + name);
        jmsProducer.sendAcquisitionFileAvailable(acquisitionFileMessageJson);


    }

    private AcquisitionFileMessageJson createMessageInformation(AcquisitionFile acqFile) {

        ProtocolApplication acq = null;
        Integer idfkAcquitision = acqFile.getIdFk();
        if (idfkAcquitision == null) {
            // it can happen due to a bug // JPM.WART
            //acq = acqFile.getProtocolApplication();
            System.err.println("Potential Bug");

        } else {
            Optional<ProtocolApplication> opt = protocolApplicationRepository.findById(idfkAcquitision);
            if (opt.isPresent()) {
                acq = opt.get();
            }
        }


        AcquisitionFileMessageJson acqMsg = new AcquisitionFileMessageJson();


        //Create Instrument Descriptor
        InstrumentJson instDesc = new InstrumentJson();
        instDesc.setName(acq.getAcquisition().getSpectrometer().getInstrument().getName());
        instDesc.setModel(acq.getAcquisition().getSpectrometer().getInstrument().getModel());


        //Create Acquisition Descriptor
        ProtocolApplicationJson acqDesc = new ProtocolApplicationJson();
        acqDesc.setAcquisitionJson(new AcquisitionJson());
        acqDesc.setName(acq.getName());
        acqDesc.getAcquisitionJson().setInstrumentId(acq.getAcquisition().getSpectrometer().getInstrument().getId());   //JPM.TODO ? setInstrument(instDesc);
        acqDesc.getAcquisitionJson().setInstrumentName(acq.getAcquisition().getSpectrometer().getInstrument().getName());
        acqDesc.setComment(acq.getComment());
        acqDesc.setDate(acq.getDate());
        acqDesc.getAcquisitionJson().setDurationMin(acq.getAcquisition().getDurationMin());



        if (acq.getActor() != null) {
            acqDesc.setActor(acq.getActor().getLogin());
        }


        acqDesc.getAcquisitionJson().setNature(acq.getAcquisition().getNature());


        // Create AcquisitionFile Descriptor
        AcquisitionFileDescriptorJson acqFileDesc = new AcquisitionFileDescriptorJson();
        acqFileDesc.setFileName(acqFile.getAttachedFile().getName());
        acqFileDesc.setFileSize(acqFile.getAttachedFile().getSizeMo());
        acqFileDesc.setPath(pathManager.getFilesPath(acqFile));

        Set<FileTags> fileTagsSet = acqFile.getAttachedFile().getFileTagses();
        if (fileTagsSet != null && !fileTagsSet.isEmpty()) {
            FileTags firstTag  = fileTagsSet.iterator().next();
            acqFileDesc.setTag(firstTag.getTag().getName());
        }
        acqFileDesc.setAcquisition(acqDesc);


        acqMsg.setAcquisitionFileDescriptor(acqFileDesc);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create AcqFileMessage : Acq related info Done / Add Sample ");
        }


        // test if a Sample is associate to acquisition (only for RESEACH Type
        if (AcquisitionJson.convertNatureToEnum(acq.getAcquisition().getNature()) == AcquisitionJson.Nature.RESEARCH) {

            try {

                List<Sample> samples = sampleRepository.findByProtocolApplication(acq.getId());
                if (samples != null && !samples.isEmpty()) {

                    Sample sample = samples.get(0);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("   - Add Sample info " + sample.getName());
                    }
                    SampleJson splDesc = new SampleJson();
                    splDesc.setName(sample.getName());


                    BiologicOriginJson biologicOriginJson = new BiologicOriginJson();
                    splDesc.setBiologicOrigin(biologicOriginJson);
                    if (sample.getBiologicOrigin() != null	&& sample.getBiologicOrigin().getSampleSpecies() != null)
                        biologicOriginJson.setSampleSpecies(sample.getBiologicOrigin().getSampleSpecies().getId());

                    acqMsg.setSampleDescriptor(splDesc);


                    Study study = sample.getStudy();
                    StudyPathJson stdDesc = new StudyPathJson();
                    stdDesc.setNomenclatureTitle(study.getNomenclatureTitle());
                    stdDesc.setTitle(study.getTitle());
                    stdDesc.setPath(pathManager.getStudyPath(study));
                    if (study.getProject() != null) {
                        Project prj = study.getProject();
                        stdDesc.setProjectNomenclatureTitle(prj.getNomenclatureTitle());
                        if (prj.getProgram() != null) {
                            stdDesc.setProgramNomenclatureTitle(prj.getProgram().getNomenclatureTitle());
                        }
                    }


                    splDesc.setStudyPath(stdDesc);

                }
            } catch (Exception epse) {
                LOGGER.warn("Research Acquisition Sample Error "+acq.getName());
            }

        }


        return acqMsg;
    }



}

