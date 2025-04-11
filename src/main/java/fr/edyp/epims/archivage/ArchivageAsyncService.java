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

package fr.edyp.epims.archivage;

import fr.edyp.epims.database.dao.ArchiveService;
import fr.edyp.epims.database.dao.AttachedFileRepository;
import fr.edyp.epims.database.dao.StudyRepository;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.json.ArchivingInfoJson;
import fr.edyp.epims.json.ControlAcquisitionArchivableJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.path.PathManager;
import fr.edyp.epims.path.ResourceManager;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

@Service
public class ArchivageAsyncService {

    @Autowired
    PathManager pathManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivageAsyncService.class);


    @Autowired
    private ArchivingData m_archivingData;

    @Autowired
    ArchiveService archiveService;

    @Autowired
    AttachedFileRepository attachedFileRepository;

    @Autowired
    StudyRepository studyRepository;



    @Transactional
    @Async
    public void executeTask(LinkedList<ArchivingInfoJson> archivingInfoJsonList) {

        // Asynchronous task : done in a separate thread

        boolean exectuteArchivage = m_archivingData.setData(archivingInfoJsonList);

        if (exectuteArchivage) {
            ArchivingInfoJson archivingAction = m_archivingData.popData();
            while (archivingAction != null) {

                archivingAction.setRunning();

                String message;
                StudyJson studyJson = archivingAction.getStudyJson();;
                if (studyJson != null) {
                    message = studyArchiving(studyJson);

                } else {
                    ControlAcquisitionArchivableJson archivableData = archivingAction.getControlAcquisitionArchivableJson();
                    message = controlArchiving(archivableData);

                }
                if (message != null) {
                    archivingAction.setMessage(message);
                    archivingAction.setFailed();
                } else {
                    archivingAction.setDone();
                }
                m_archivingData.actionFinished(archivingAction);

                archivingAction = m_archivingData.popData();
            }
        }

    }


    private String controlArchiving(ControlAcquisitionArchivableJson archivableData) {

        String message = null;

        try {

            // look for archive root path
            Preferences preferences = ServerEpimsPreferences.root();
            String archiveRoot = preferences.get(PreferencesKeys.ARCHIVE_ROOT, null);
            if (archiveRoot == null) {
                message = "In Preferences File ARCHIVE_ROOT is not specified";
                LOGGER.error("error in /controlarchive  : "+message);
                return message;
            }
            File archiveDirectory = new File(archiveRoot);
            if (!archiveDirectory.exists()) {
                message = "Archive directory does not exist:"+archiveDirectory.getAbsolutePath();
                LOGGER.error("error in /controlarchive  : "+message);
                return message;
            }

            // copy files
            String sourcePathRoot = pathManager.getControlAcquisitionRoot();

            ArrayList<File> sourceFilesCopied = new ArrayList<>();
            ArrayList<File> filesCopied = new ArrayList<>();
            List<ArchiveService.AcquisitionToArchive> acquisitionToArchiveList = archiveService.controlToBeArchived(archivableData);
            for (ArchiveService.AcquisitionToArchive acquisitionToArchive : acquisitionToArchiveList) {
                File sourceFile = new File(pathManager.getControlAcquisitionPath(sourcePathRoot, acquisitionToArchive.nature, archivableData.instrument, archivableData.year, archivableData.month, acquisitionToArchive.fileName));
                Path sourceFilePath =  sourceFile.toPath();

                String destinationRoot = archiveDirectory.getAbsolutePath()+'/'+pathManager.getPimsShare();
                File destinationFile = new File(pathManager.getControlAcquisitionPath(destinationRoot, acquisitionToArchive.nature, archivableData.instrument, archivableData.year, archivableData.month, acquisitionToArchive.fileName));
                Path destinationFilePath = destinationFile.toPath();

                try {
                    Files.createDirectories(destinationFilePath.getParent());
                    Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException io) {

                    // we remove previous copied files
                    for (File f : filesCopied) {
                        try {
                            f.delete();
                        } catch (Exception e) {

                        }
                    }

                    message = "Impossible to copy file from " +sourceFilePath+" to "+destinationFilePath;
                    LOGGER.error("error in /controlarchive  : "+message);
                    return message;
                }

                filesCopied.add(destinationFile);
                sourceFilesCopied.add(sourceFile);
            }

            // save in database the fact that files have been copied
            try {
                for (ArchiveService.AcquisitionToArchive acquisitionToArchive : acquisitionToArchiveList) {
                    int id = acquisitionToArchive.attachedFileId;
                    Optional<AttachedFile> attachedFileOpt = attachedFileRepository.findById(id);
                    if (attachedFileOpt.isPresent()) {
                        AttachedFile attachedFile = attachedFileOpt.get();
                        attachedFile.setArchived(true);
                        attachedFileRepository.save(attachedFile);
                    }
                }
            } catch (Exception e) {

                // we remove previous copied files
                for (File f : filesCopied) {
                    try {
                        f.delete();
                    } catch (Exception e2) {

                    }
                }

                message = "Database error while archiving "+archivableData.getInstrument()+" "+archivableData.getYear()+" "+archivableData.getMonth();
                LOGGER.error("error in /controlarchive  : "+message);
                return message;
            }


            // remove source files
            for (File f : sourceFilesCopied) {
                try {
                    f.delete();
                } catch (Exception e) {

                }
            }

            // return ok status : no comments
        } catch (Exception e) {
            message = "Unexpected error while archiving "+archivableData.getInstrument()+" "+archivableData.getYear()+" "+archivableData.getMonth();
            LOGGER.error(message, e);
            return message;
        }
        return null;
    }


    private String studyArchiving(StudyJson studyJson) {

        try {

            Preferences preferences = ServerEpimsPreferences.root();
            String archiveRoot = preferences.get(PreferencesKeys.ARCHIVE_ROOT, null);
            if (archiveRoot == null) {
                String message = "error in /studyarchive  : in Preferences File ARCHIVE_ROOT is not specified";
                LOGGER.error(message);
                return message;
            }
            File archiveDirectory = new File(archiveRoot);
            if (!archiveDirectory.exists()) {
                String message ="error in /studyarchive  : archive directory does not exist:"+archiveDirectory.getAbsolutePath();
                LOGGER.error(message);
                return message;
            }


            Optional<Study> studyOptional = studyRepository.findById(studyJson.getId());
            if (!studyOptional.isPresent()) {
                String message ="error in /studyarchive  : study not found:"+studyJson.getNomenclatureTitle();
                LOGGER.error(message);
                return message;
            }
            Study study = studyOptional.get();
            boolean archivable = study.getStatus().equals("archivable");
            if (!archivable) {
                String message ="study :"+studyJson.getNomenclatureTitle()+" is not archivable";
                LOGGER.error(message);
                return message;
            }


            String studyPath = pathManager.getStudyPath(study);
            String studyPathAbsolute = pathManager.getStudyAbsolutePath(study);

            if ((studyPath == null) || (studyPathAbsolute == null)) {
                String message = "Study Path not found for Study " + study.getNomenclatureTitle();
                LOGGER.error(message);
                return message;
            }

            File studyInArchive = new File(archiveDirectory, studyPath);
            boolean success = studyInArchive.mkdirs();
            if (!success) {
                String message = "error in /studyarchive  : can not create subdirectory for " + studyPath;
                LOGGER.error(message);
                return message;
            }

            File studyDir = new File(studyPathAbsolute);

            // Copy each directory from study to archive
            File[] files = studyDir.listFiles();
            try {
                for (int i = 0; i < files.length; i++) {
                    FileUtils.secureCopy(files[i], new File(studyInArchive, files[i].getName()));
                }
            } catch (IOException ioe) {
                String message = " Archive Study " + study.getNomenclatureTitle() + " Failed : " + ioe.getMessage();
                LOGGER.error(message);
                try {
                    org.apache.commons.io.FileUtils.cleanDirectory(studyInArchive);
                } catch (IOException e) {
                }
                return message;
            }

            // Clean study directory

            try {
                // Change write access on Unix System!
                String os = System.getProperty("os.name").toUpperCase();
                if (os.indexOf("UNIX") != -1 || os.indexOf("LINUX") != -1) {
                    Runtime.getRuntime().exec("chmod -R a+rwx " + studyDir.getAbsolutePath());
                }
            } catch (IOException e) {
                String message = " Ouverture des droits d'ecriture sur " + studyDir.getAbsolutePath()
                        + " impossible ! (" + e.getMessage() + ")";
                LOGGER.error(message);
                return message;
            }

            // /////////////
            // VD : En cas d'erreur : stop ou continue ???
            // /////////////

            // remove files from repository
            org.apache.commons.io.FileUtils.cleanDirectory(studyDir);

            // *****************
            // Copy study_archived file to study directory
            copyStudyArchivedFile(studyDir);

            // *****************
            // Indicate archived done in DB
            archiveStudyInDB(study);


        } catch (Exception e) {
            String message = "error in studyArchiving "+e.getMessage();
            LOGGER.error("error in studyArchiving", e);
            return message;
        }



        return null; // everything went ok


    }

    private void copyStudyArchivedFile(File studyDir) throws Exception {


            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String todayString = format.format(today);

            PrintWriter out = new PrintWriter(studyDir.getAbsolutePath()+"/studyArchived.txt");

            out.write("L'etude recherchee a ete archivee le "+todayString);
            out.write("\n\nPour acceder aux donnees, il vous faut faire une demande de restauration aupres de l'administrateur.\n\n");

            out.close();

    }

    private void archiveStudyInDB(Study study)  throws Exception {

        List<AttachedFile> attachedFiles = attachedFileRepository.findAttachedFilesForStudy(study.getId());
        for (AttachedFile attachedFile : attachedFiles) {
            attachedFile.setArchived(true);
            attachedFileRepository.save(attachedFile);
        }

        study.setStatus("archivée");
        studyRepository.save(study);


    }
}
