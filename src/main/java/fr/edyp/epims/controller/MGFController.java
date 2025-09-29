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
import fr.edyp.epims.json.MgfKeysInfoJson;
import fr.edyp.epims.json.MgfFileInfoJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.util.error.EpimServerException;
import fr.edyp.epims.util.error.EpimsErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashSet;

import java.util.Optional;
import java.util.Set;

// Use Global Exception Handler. See GlobalExceptionHandler
@RestController
@RequestMapping("/api")
public class MGFController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MGFController.class);

    @Autowired
    MGFService mgfService;

    @Autowired
    AttachedFileRepository attachedFileRepository;

    @Autowired
    FileTagsRepository fileTagsRepository;

    @Autowired
    AcquisitionFileRepository acquisitionFileRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ProtocolApplicationRepository protocolApplicationRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    PathManager pathManager;

    @PostMapping("/mgflist")
    public ResponseEntity<MgfFileInfoJson[]> mgflist() {

        MgfFileInfoJson[] mgfList = mgfService.mgfList();
        return new ResponseEntity<>(mgfList, HttpStatus.OK);
    }

    @PostMapping("/studyformgf")
    public ResponseEntity<Integer> studyForMgf(@RequestBody String mgfName) {

        Integer studyId = mgfService.studyForMGF(mgfName);
        return new ResponseEntity<>(studyId, HttpStatus.OK);
    }

    @PostMapping("/mgfkeyinfo")
    public ResponseEntity<MgfKeysInfoJson> infoForMgf(@RequestBody MgfKeysInfoJson mgfKeysInfoJson) {

        String acquisitionName = mgfKeysInfoJson.getAcquisitionName();
        if(acquisitionName == null || acquisitionName.isEmpty()) {
            acquisitionName = MGFService.acquisitionNameForMGFName(mgfKeysInfoJson.getName());
        }

        Integer studyId = mgfService.studyIdForAcq(acquisitionName);
        LOGGER.debug("Search information for acquisition {} : found {}", acquisitionName, studyId);

        mgfKeysInfoJson.setAcquisitionName(acquisitionName);
        mgfKeysInfoJson.setStudyId(studyId);

        return new ResponseEntity<>(mgfKeysInfoJson, HttpStatus.OK);
    }


    @Transactional
    @PostMapping("/createmgf")
    public ResponseEntity<Integer> createMgf(@RequestBody MgfFileInfoJson mgfFileInfoJson) {

        Integer sId = mgfFileInfoJson.getStudyId();
        Optional<Study> studyOptional = studyRepository.findById(sId);
        if (studyOptional.isEmpty()) {
          LOGGER.error("error in /createmgf  : study not found:{}", sId);
            throw new EpimServerException(EpimsErrorCode.STUDY_NOT_FOUND, "Create mgf, study not found: " + sId);
        }

        Study study = studyOptional.get();
        String partialPath = pathManager.getStudyPartialPath(study);
        if (partialPath == null) {
            LOGGER.error("error in /createmgf  : partial path not found for study :{}", mgfFileInfoJson.getStudyId());
            throw new EpimServerException(EpimsErrorCode.STUDY_DIRECTORY_ACCESS_ERROR, "Create mgf, study path not found: " + sId);
        }

        String path = mgfFileInfoJson.getDirectoryPath();
        int index = path.indexOf(partialPath);
        if (index != -1) {
            path = path.substring(index);
        }

        String acquisitionName = MGFService.acquisitionNameForMGFName(mgfFileInfoJson.getName());
        Optional<ProtocolApplication> protocolApplicationOptional = protocolApplicationRepository.findByName(acquisitionName);
        if (protocolApplicationOptional.isEmpty()) {
          LOGGER.error("error in /api/createMgf : acquisition not found for {}", mgfFileInfoJson.getName());
            throw new EpimServerException(EpimsErrorCode.ACQUISITION_NOT_FOUND, "Create mgf, acquisition not found: " + acquisitionName);
        }

        int   protocolaApplicationId = protocolApplicationOptional.get().getId();

        // Create AttachedFile without fileLinks and fileTags
        AttachedFile attachedFileToCreate = new AttachedFile();
        attachedFileToCreate.setDate(mgfFileInfoJson.getDate());
        attachedFileToCreate.setName(mgfFileInfoJson.getName());
        attachedFileToCreate.setPath(path);
        attachedFileToCreate.setSizeMo(mgfFileInfoJson.getSizeMo());
        attachedFileToCreate.setArchived(null);
        Set<FileLink> fileLinkSet = new HashSet<>();
        attachedFileToCreate.setFileLinks(fileLinkSet);
        Set<FileTags> fileTagsSet = new HashSet<>();
        attachedFileToCreate.setFileTagses(fileTagsSet);
        AttachedFile attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);

        // Create AcquisitionFile
        AcquisitionFile acqFileToCreate = new AcquisitionFile();
        acqFileToCreate.setAttachedFile(attachedFileCreated);
        acqFileToCreate.setIdFk(protocolaApplicationId);
        AcquisitionFile acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);

        // Create FileTag

        // Suppose acquisition file are RAW files
        Tag rawTag = tagRepository.findByName("SPECTRA").get(); //SHOULD exist
        FileTagsId fileTagsId = new FileTagsId(attachedFileToCreate.getId(), rawTag.getName());
        FileTags fileTag = new FileTags(fileTagsId, attachedFileToCreate, rawTag, 0);
        FileTags fileTagCreated = fileTagsRepository.save(fileTag);

        fileLinkSet.add(acqFileCreated);
        fileTagsSet.add(fileTagCreated);
        attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);

        fileTagsSet.add(fileTag);
        acqFileCreated.setAttachedFile(attachedFileCreated);
        acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);



        return new ResponseEntity<>(acqFileCreated.getId(), HttpStatus.OK);
    }
}
