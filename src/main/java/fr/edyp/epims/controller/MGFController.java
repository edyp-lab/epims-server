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
import fr.edyp.epims.json.MgfFileInfoJson;
import fr.edyp.epims.path.PathManager;
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

        try {
            MgfFileInfoJson[] mgfList = mgfService.mgfList();

            return new ResponseEntity(mgfList, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/mgflist", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/studyformgf")
    public ResponseEntity<Integer> studyForMgf(@RequestBody String mgfname) {

        try {
            Integer studyId = mgfService.studyForMGF(mgfname);

            return new ResponseEntity(studyId, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/studyForMgf", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @PostMapping("/createmgf")
    public ResponseEntity<Integer> createMgf(@RequestBody MgfFileInfoJson mgfFileInfoJson) {

        try {

            Optional<Study> studyOptional = studyRepository.findById(mgfFileInfoJson.getStudyId());
            if (!studyOptional.isPresent()) {
                String message ="error in /createmgf  : study not found:"+mgfFileInfoJson.getStudyId();
                LOGGER.error(message);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Study study = studyOptional.get();
            String partialPath = pathManager.getStudyPartialPath(study);
            if (partialPath == null) {
                String message ="error in /createmgf  : partial path not found for study :"+mgfFileInfoJson.getStudyId();
                LOGGER.error(message);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String path = mgfFileInfoJson.getDirectoryPath();
            int index = path.indexOf(partialPath);
            if (index != -1) {
                path = path.substring(index);
            }

            // Create AttachedFile without fileLinks and fileTags
            AttachedFile attachedFileToCreate = new AttachedFile();
            attachedFileToCreate.setDate(mgfFileInfoJson.getDate());
            attachedFileToCreate.setName(mgfFileInfoJson.getName());
            attachedFileToCreate.setPath(path);
            attachedFileToCreate.setSizeMo(mgfFileInfoJson.getSizeMo());
            attachedFileToCreate.setArchived(null);
            Set<FileLink> fileLinkSet = new HashSet<FileLink>();
            attachedFileToCreate.setFileLinks(fileLinkSet);
            Set<FileTags> fileTagsSet = new HashSet<FileTags>();
            attachedFileToCreate.setFileTagses(fileTagsSet);
            AttachedFile attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);

            String acquisitionName = mgfService.acquisitionNameForMGFName(mgfFileInfoJson.getName());
            Optional<ProtocolApplication> protocolApplicationOptional = protocolApplicationRepository.findByName(acquisitionName);
            int protocolaApplicationId;
            if (protocolApplicationOptional.isPresent()) {
                protocolaApplicationId = protocolApplicationOptional.get().getId();
            } else {
                LOGGER.error("error in /api/createMgf : acquisition not found for "+mgfFileInfoJson.getName() );
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Create AcquisitionFile
            AcquisitionFile acqFileToCreate = new AcquisitionFile();
            acqFileToCreate.setAttachedFile(attachedFileCreated);
            acqFileToCreate.setIdFk(protocolaApplicationId);
            AcquisitionFile acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);

            // Create FileTag

            // Suppose acquisition file are RAW files
            Tag rawTag = tagRepository.findByName("SPECTRA").get();
            FileTagsId fileTagsId = new FileTagsId(attachedFileToCreate.getId(), rawTag.getName());
            FileTags fileTag = new FileTags(fileTagsId, attachedFileToCreate, rawTag, 0);
            FileTags fileTagCreated = fileTagsRepository.save(fileTag);

            fileLinkSet.add(acqFileCreated);
            fileTagsSet.add(fileTagCreated);
            attachedFileCreated = attachedFileRepository.save(attachedFileToCreate);


            fileTagsSet.add(fileTag);


            acqFileCreated.setAttachedFile(attachedFileCreated);
            acqFileCreated = acquisitionFileRepository.save(acqFileToCreate);



            return new ResponseEntity(acqFileCreated.getId(), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/createmgf", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
