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
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.util.error.EpimServerException;
import fr.edyp.epims.util.error.EpimsErrorCode;
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

/**
 * Controller responsible for handling project-related API endpoints.
 * This class provides methods to create, update, and retrieve project information
 * in the system. It communicates with various repositories to manage persistence.
 *
 * The endpoints handle functionalities such as adding new projects, closing
 * projects, associating members, adding contacts, and listing all projects.
 *
 * The class leverages Spring's REST controller capabilities and transactional
 * management to maintain data consistency.
 */
// Use Global Exception Handler. See GlobalExceptionHandler

@RestController
@RequestMapping("/api")
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ProjContactsRepository projContactsRepository;

    @Autowired
    private Environment env;

    @Transactional
    @PostMapping("/addproject")
    public ResponseEntity<ProjectJson> addproject(@RequestBody ProjectJson projectJson) {

            int programId = projectJson.getProgramId();
            Program program = null;
            if (programId != -1) {
                Optional<Program> programOpt = programRepository.findById(programId);
                if (programOpt.isEmpty()) {
                    throw new EpimServerException(EpimsErrorCode.PROGRAM_NOT_FOUND, "Program ID: " + programId);
                }

                program = programOpt.get();
            }

            Optional<Actor> actorOpt = actorRepository.findById(projectJson.getActorKey());
            if (actorOpt.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Actor Key: " + projectJson.getActorKey());
            }
            Actor actor = actorOpt.get();

            Set<Actor> actors = new HashSet<>();
            for (String actorKey : projectJson.getActorsKey()) {
                actorOpt =actorRepository.findById(actorKey);
                if (actorOpt.isEmpty()) {
                    throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Actor Key: " + actorKey);
                }
                actors.add(actorOpt.get());
            }

            Optional<Project> existingProjectByNomenclature = projectRepository.findByNomenclatureTitle(projectJson.getNomenclatureTitle());
            if (existingProjectByNomenclature.isPresent()) {
                throw new EpimServerException(EpimsErrorCode.DUPLICATE_NOMENCLATURE,
                        "A project with nomenclature title '" + projectJson.getNomenclatureTitle() + "' already exists");
            }

            Optional<Project> existingProjectByTitle = projectRepository.findByTitle(projectJson.getTitle());
            if (existingProjectByTitle.isPresent()) {
                throw new EpimServerException(EpimsErrorCode.DUPLICATE_TITLE,
                        "A project with title '" + projectJson.getTitle() + "' already exists");
            }

            Set<ProjContacts> projContactes = new HashSet<>();
            Set<Study> studies = new HashSet<>();

            Project project = new Project(actor, program, projectJson.getTitle(), projectJson.getNomenclatureTitle(),
                    projectJson.getLongTitle(), projectJson.getDescription(), projectJson.getContractualFrame(),
                    projectJson.getIdentificationType(), projectJson.getCreationDate(), null, projectJson.getConfidential(),
                    projContactes, studies, actors
                    );

            Project projectSaved = projectRepository.save(project);

            for (Integer contactKey : projectJson.getContactsKey()) {
                Optional<Contact> contactOpt = contactRepository.findById(contactKey);
                if (contactOpt.isEmpty()) {
                    throw new EpimServerException(EpimsErrorCode.CONTACT_NOT_FOUND, "Contact Key: " + contactKey);
                }
                Contact contact = contactOpt.get();
                ProjContacts projContact = new ProjContacts(contact, projectSaved);
                projContact = projContactsRepository.save(projContact);
                projContactes.add(projContact);
            }

            projectSaved.setProjContactses (projContactes);

            // Create the Project Directory in the correct Program Directory (or in _UNCLASS_ directory when no program is specified)
            Preferences preferences = ServerEpimsPreferences.root();
            String epimsRepository = preferences.get(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));
            File parentDir = new File(epimsRepository);
            boolean projectDirCreated = false;
            if (parentDir.exists() && parentDir.isDirectory()) {
                File[] files = parentDir.listFiles();

                Program p = projectSaved.getProgram();
                String programDirName;
                if (p == null) {
                    programDirName = "_UNCLASS_";
                } else {
                    programDirName = p.getNomenclatureTitle();
                }

                // look for correct Program Directory and Create Project Directory
                if(files != null ) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            String name = f.getName();
                            if (name.length() == 1) {
                                char c = name.charAt(0);
                                if ((c >= 'a') && (c <= 'z')) {
                                    File[] azFiles = f.listFiles();
                                    if (azFiles != null) {
                                        for (File azf : azFiles) {
                                            if (azf.getName().equals(programDirName)) {
                                                File projectDir = new File(azf.getAbsolutePath() + "/" + projectJson.getNomenclatureTitle());
                                                projectDir.mkdir();
                                                projectDirCreated = true;
                                                break;
                                            }
                                        }
                                        if (projectDirCreated) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!projectDirCreated) {
                LOGGER.error("error in /api/addproject : !projectDirCreated");
                throw new EpimServerException(EpimsErrorCode.PROJECT_DIRECTORY_CREATION_FAILED, "Project Key: " + project.getNomenclatureTitle());
            }

            DatabaseVersionManager.getSingleton().bumpVersion(ProjectJson.class, null);
            DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

            return new ResponseEntity<>(Converter.convert(projectSaved), HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/projectclose/{projectId}")
    public ResponseEntity<ProjectJson> projectClose(@RequestBody Date closingDate, @PathVariable("projectId") int projectId) {
        Optional<Project> p = projectRepository.findById(projectId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROJECT_NOT_FOUND, "Project Key: " + projectId);
        }

        Project project = p.get();
        project.setClosingDate(closingDate);

        Project projectSaved = projectRepository.save(project);

        DatabaseVersionManager.getSingleton().bumpVersion(ProjectJson.class, null);

        return new ResponseEntity<>(Converter.convert(projectSaved), HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/projectaddmember/{projectId}")
    public ResponseEntity<ProjectJson> addMember(@RequestBody List<String> actorIds, @PathVariable("projectId") int projectId) {

        Optional<Project> p = projectRepository.findById(projectId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROJECT_NOT_FOUND, "Project Key: " + projectId);
        }


        for (String actorId : actorIds) {

            Optional<Actor> a = actorRepository.findById(actorId);
            if (a.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Actor Key: " + actorId);
            }

            p.get().addMember(a.get());

        }
        Project projectSaved = projectRepository.save(p.get());

        DatabaseVersionManager.getSingleton().bumpVersion(ProjectJson.class, null);

        return new ResponseEntity<>(Converter.convert(projectSaved), HttpStatus.OK);
    }


    @Transactional
    @PostMapping("/projectaddcontact/{projectId}")
    public ResponseEntity<ProjectJson> addContact(@RequestBody List<Integer> contactIds, @PathVariable("projectId") int projectId) {

        Optional<Project> p = projectRepository.findById(projectId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROJECT_NOT_FOUND, "Project Key: " + projectId);
        }

        Set<ProjContacts> projContactsSet = null;

        ProjContacts projContact = null;
        for (Integer contactId : contactIds) {

            Optional<Contact> c = contactRepository.findById(contactId);
            if (c.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.CONTACT_NOT_FOUND, "Contact Key: " + contactId);
            }

            projContact = new ProjContacts(c.get(), p.get());
            projContact = projContactsRepository.save(projContact);

            if (projContactsSet == null) {
                Project project = projContact.getProject();
                projContactsSet = project.getProjContactses();
                if (projContactsSet == null) {
                    projContactsSet = new HashSet<>();
                }
            }

            projContactsSet.add(projContact);
        }

        Project project = projContact.getProject();
        project.setProjContactses (projContactsSet);

        DatabaseVersionManager.getSingleton().bumpVersion(ProjectJson.class, null);

        return new ResponseEntity<>(Converter.convert(project), HttpStatus.OK);
    }

//    @Transactional(readOnly = true)
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectJson>> getAllProjects() {

        List<Project> projects = new ArrayList<>(projectRepository.findAll());

        if (projects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        ArrayList<ProjectJson> projectList = new ArrayList<>();

        for (Project project : projects) {
            ProjectJson projectJson = Converter.convert(project);
            projectList.add(projectJson);
        }

        Collections.sort(projectList);

        return ControllerUtil.createResponseWithVersion(projectList, ProjectJson.class);
    }
}
