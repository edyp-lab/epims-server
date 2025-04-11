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
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;

import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

@RestController
@RequestMapping("/api")
public class StudyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyController.class);

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    StudyContactsRepository studyContactsRepository;

    @Autowired
    ArchiveService archiveService;

    @Autowired
    private Environment env;

    @Autowired
    PathManager pathManager;

    @Transactional
    @PostMapping("/addstudy")
    public ResponseEntity<StudyJson> addStudy(@RequestBody StudyJson studyJson) {

        try {

            int projectId = studyJson.getProjectId();
            Project project = null;
            if (projectId != -1) {
                Optional<Project> projectOpt = projectRepository.findById(projectId);
                if (!projectOpt.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                project = projectOpt.get();
            }


            Optional<Actor> actorOpt = actorRepository.findById(studyJson.getActorKey());
            if (!actorOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Actor actor = actorOpt.get();

            Set<Actor> actors = new HashSet<>();
            for (String actorKey : studyJson.getActorsKey()) {
                actorOpt =actorRepository.findById(actorKey);
                if (!actorOpt.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                actors.add(actorOpt.get());
            }

            Set<StudyContacts> studyContactsSet = new HashSet<>();

            Study study = new Study(
                    actor, project, studyJson.getTitle(), studyJson.getNomenclatureTitle(),
                    studyJson.getLongTitle(), studyJson.getDescription(), studyJson.getIdentificationType(),
                    studyJson.getContractualFrame(), Boolean.FALSE, studyJson.getCreationDate(), null,
                    null,  studyJson.getStatus(), studyJson.getConfidential(), actors, studyContactsSet, null, null, studyJson.getComment()
                    );

            study = studyRepository.save(study);

            for (Integer contactKey : studyJson.getContactsKey()) {
                Optional<Contact> contactOpt = contactRepository.findById(contactKey);
                if (!contactOpt.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Contact contact = contactOpt.get();
                StudyContacts studyContact = new StudyContacts(contact, study);
                studyContact = studyContactsRepository.save(studyContact);
                studyContactsSet.add(studyContact);
            }

            study.setStudyContactses(studyContactsSet);

            // Create the Study Directory in the correct Program/Project Directory (or in _UNCLASS_ directories when no program or project is specified)
            Preferences preferences = ServerEpimsPreferences.root();
            String epimsRepository = preferences.get(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));
            File parentDir = new File(epimsRepository);
            boolean studyDirCreated = false;
            if (parentDir.exists() && parentDir.isDirectory()) {
                File[] files = parentDir.listFiles();


                Program p = (project!= null) ? project.getProgram() : null;
                String programDirName = null;
                if (p == null) {
                    programDirName = "_UNCLASS_";
                } else {
                    programDirName = p.getNomenclatureTitle();
                }

                String projectDirName = null;
                if (project == null) {
                    projectDirName = "_UNCLASS_";
                } else {
                    projectDirName = project.getNomenclatureTitle();
                }

                // look for correct Program and Project Directory and Create Study Directory and its sub-directories
                for (File f : files) {
                    if (f.isDirectory()) {
                        String name = f.getName();
                        if (name.length() == 1) {
                            char c = name.charAt(0);
                            if ((c >= 'a') && (c <= 'z')) {
                                File[] azFiles = f.listFiles();
                                for (File azf : azFiles) {
                                    if (azf.getName().equals(programDirName)) {
                                        File projectDir = new File(azf.getAbsolutePath()+"/"+projectDirName);
                                        if (projectDir.exists()) {
                                            File studyDir = new File(projectDir.getAbsolutePath()+"/"+study.getNomenclatureTitle());
                                            studyDir.mkdir();
                                            studyDirCreated = true;
                                            File dataDir = new File(studyDir.getAbsolutePath()+"/data");
                                            dataDir.mkdir();
                                            File samplesDir = new File(studyDir.getAbsolutePath()+"/samples");
                                            samplesDir.mkdir();
                                            File samplesDataDir = new File(samplesDir.getAbsolutePath()+"/data");
                                            samplesDataDir.mkdir();
                                            File rawDir = new File(samplesDataDir.getAbsolutePath()+"/RAW");
                                            rawDir.mkdir();
                                            break;
                                        }

                                    }
                                }
                                if (studyDirCreated) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!studyDirCreated) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);
            DatabaseVersionManager.getSingleton().bumpVersion(ProjectJson.class, null);

            return new ResponseEntity(Converter.convert(study), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/addstudy", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/studychangestatus/{studyId}")
    public ResponseEntity<StudyJson> studyChangeStatus(@RequestBody String status, @PathVariable("studyId") int studyId) {

        try {

            Optional<Study> s = studyRepository.findById(studyId);
            if (!s.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Study study = s.get();
            study.setStatus(status);

            Study studySaved = studyRepository.save(s.get());

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);

            return new ResponseEntity(Converter.convert(studySaved), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /studychangestatus/{studyId}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/studychangecomment/{studyId}")
    public ResponseEntity<StudyJson> studyChangeComment(@RequestBody String comment, @PathVariable("studyId") int studyId) {

        try {

            Optional<Study> s = studyRepository.findById(studyId);
            if (!s.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Study study = s.get();
            study.setComment(comment);

            Study studySaved = studyRepository.save(s.get());

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);

            return new ResponseEntity(Converter.convert(studySaved), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /studychangestatus/{studyId}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/studyaddmember/{studyId}")
    public ResponseEntity<StudyJson> addMember(@RequestBody List<String> actorIds, @PathVariable("studyId") int studyId) {

        try {

            Optional<Study> s = studyRepository.findById(studyId);
            if (!s.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }


            for (String actorId : actorIds) {

                Optional<Actor> a = actorRepository.findById(actorId);
                if (!a.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                s.get().addActor(a.get());

            }
            Study studySaved = studyRepository.save(s.get());

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);

            return new ResponseEntity(Converter.convert(studySaved), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /studyaddmember/{studyId}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/studyaddcontact/{studyId}")
    public ResponseEntity<StudyJson> addContact(@RequestBody List<Integer> contactIds, @PathVariable("studyId") int studyId) {

        try {

            Optional<Study> s = studyRepository.findById(studyId);
            if (!s.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Set<StudyContacts> studyContactsSet = null;

            StudyContacts studyContact = null;
            for (Integer contactId : contactIds) {

                Optional<Contact> c = contactRepository.findById(contactId);
                if (!c.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                studyContact = new StudyContacts(c.get(), s.get());
                studyContact = studyContactsRepository.save(studyContact);

                if (studyContactsSet == null) {
                    Study study = studyContact.getStudy();
                    studyContactsSet = study.getStudyContactses();
                    if (studyContactsSet == null) {
                        studyContactsSet = new HashSet<>();
                    }
                }

                studyContactsSet.add(studyContact);
            }

            Study study = studyContact.getStudy();
            study.setStudyContactses(studyContactsSet);

            DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);

            return new ResponseEntity(Converter.convert(study), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /studyaddcontact/{studyId}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







    @GetMapping("/studies")
    public ResponseEntity<List<StudyJson>> getAllStudies() {
        try {
            List<Study> studies = new ArrayList<>();

            studyRepository.findAll().forEach(studies::add);

            if (studies.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            ArrayList<StudyJson> studyJsonList = new ArrayList<>();
            for (Study study : studies) {

                StudyJson studyJson = Converter.convert(study);

                studyJsonList.add(studyJson);
            }
            Collections.sort(studyJsonList);



            return ControllerUtil.createResponseWithVersion(studyJsonList, StudyJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /studies", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

