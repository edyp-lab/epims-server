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

package fr.edyp.epims.path;

import fr.edyp.epims.database.dao.InstrumentRepository;
import fr.edyp.epims.database.dao.ProtocolApplicationRepository;
import fr.edyp.epims.database.dao.SampleRepository;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.json.AcquisitionFileMessageJson;
import fr.edyp.epims.json.AcquisitionJson;
import fr.edyp.epims.json.ProtocolApplicationJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Configurable
public class PathManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathManager.class);

    public final static String ACQ_CTRL_LC_DIR_NAME="ControleLC";
    public final static String ACQ_CTRL_INSTR_DIR_NAME="ControleInstrument";
    public final static String ACQ_CTRL_BLANK_DIR_NAME="Blanc";

    public final static String DATA_DIR_NAME ="data";
    public final static String SAMPLES_DIR_NAME ="samples";

    @Autowired
    private ProtocolApplicationRepository m_protocolApplicationRepository;

    @Autowired
    private SampleRepository m_sampleRepository;

    @Autowired
    private InstrumentRepository m_instrumentRepository;

    @Autowired
    private PathData m_pathData;

    //private static PathManager m_singleton = null;

    /*private PathManager() {

    }

    public static PathManager getPathManager() {
        if (m_singleton == null) {
            m_singleton = new PathManager();
        }
        return m_singleton;
    }*/

    public String getStudyPath(Study s) {
        if (s == null) {
            return null;
        }

        String partialPath = getStudyPartialPath(s);
        LOGGER.debug(" getStudyPath partial for " + s.getNomenclatureTitle() + " = " + partialPath);

        String containerRepository = getRepositoryFor(partialPath);
        if (containerRepository == null) {
            return null;
        }

        StringBuffer relativePath = new StringBuffer();
        relativePath.append(containerRepository);
        relativePath.append("/");
        relativePath.append(partialPath);
        return relativePath.toString();
    }

    public String getStudyPartialPath(Study s) {
        if (s == null) {
            return null;
        }

        Project p = s.getProject();

        StringBuffer studyPath = new StringBuffer();

        // Project defined
        if (p != null) {
            // Program defined
            if (p.getProgram() != null) {
                studyPath.append(p.getProgram().getNomenclatureTitle());
                // No associated Program
            } else {
                studyPath.append(PathData.UNDEF_DIR_NAME);
            }

            studyPath.append("/");
            studyPath.append(p.getNomenclatureTitle());
        } else {
            // No associated Project so no program
            studyPath.append(PathData.UNDEF_DIR_NAME);
            studyPath.append("/");
            studyPath.append(PathData.UNDEF_DIR_NAME);
        }

        studyPath.append("/");
        studyPath.append(s.getNomenclatureTitle());

        String prjPath = getProjectAbsolutePath(p);
        if(prjPath==null)
            return null;
        StringBuffer fullPath = new StringBuffer(prjPath);
        fullPath.append("/");
        fullPath.append(s.getNomenclatureTitle());
        File f = new File(fullPath.toString());
        if(! f.exists()){
            return null;
        }
        return studyPath.toString();
    }

    private String getRepositoryFor(String suffixPath){
        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        int repositoryIndex = 1;

        //More repository may exist
        boolean moreRepository = true;
        //At least one repository defined
        boolean repositoryDefined = false;

        while (moreRepository) {
            StringBuffer fullPath = new StringBuffer(pimsRoot);
            fullPath.append("/");

            //Test if Next repository is defined
            // - create next repository property key
            StringBuffer rscPimsRoot = new StringBuffer(ResourceManager.PIMS_REPOSITORY_PREFIX);
            rscPimsRoot.append(repositoryIndex);
            repositoryIndex++;
            // - get next repository property
            String nextRepository = m_pathData.getPath(rscPimsRoot.toString());
            if (nextRepository == null) {
                moreRepository = false;
                break;
            }

            fullPath.append(nextRepository);
            repositoryDefined = true;

            fullPath.append("/");
            fullPath.append(suffixPath);

            //Test if specified path is defined in current repository
            File searchedFile = new File(fullPath.toString());

            if (searchedFile.exists()) {
                LOGGER.debug(" Full Path found for "+suffixPath+ " : "+ fullPath.toString());
                return nextRepository;
            }
        }

        if(!repositoryDefined)
            LOGGER.warn("No repository defined in ePims system");
        return null;
    }

    public String getProjectAbsolutePath(Project p) {
        //Get path from program
        String partialPath = getProjectPartialPath(p);

        //Get repository name where project is defined
        String containerRepository = getRepositoryFor(partialPath);
        if (containerRepository == null)
            return null;

        //Create Absolute path
        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        StringBuffer fullPath = new StringBuffer(pimsRoot);
        fullPath.append("/");
        fullPath.append(containerRepository);
        fullPath.append("/");
        fullPath.append(partialPath);
        fullPath.append("/");
        File f = new File(fullPath.toString());
        if(! f.exists()){
            return null;
        }
        return fullPath.toString();

    }

    public String getProjectPartialPath(Project p) {
        LOGGER.debug(" ------- Enter getPartialPathForProject for " + p);
        StringBuffer projectPath = new StringBuffer();
        StringBuffer fullPath = new StringBuffer();

        // Project defined
        if (p != null) {
            String prgAbsPath = getProgramAbsolutePath(p.getProgram());
            if(prgAbsPath == null)
                return null;

            fullPath = new StringBuffer(prgAbsPath);

            // Program defined
            if (p.getProgram() != null) {
                projectPath.append(p.getProgram().getNomenclatureTitle());

                // No associated Program
            } else {
                projectPath.append(m_pathData.getPath(ResourceManager.UNDEFINED_DIR));
            }

            projectPath.append("/");
            projectPath.append(p.getNomenclatureTitle());
            fullPath.append("/");
            fullPath.append(p.getNomenclatureTitle());

        } else {
            // Project is null
            //  No program
            projectPath.append(m_pathData.getPath(ResourceManager.UNDEFINED_DIR));
            projectPath.append("/");
            // No Project
            projectPath.append(m_pathData.getPath(ResourceManager.UNDEFINED_DIR));

            String prgAbsPath = getProgramAbsolutePath(null);
            if(prgAbsPath == null)
                return null;
            fullPath = new StringBuffer(prgAbsPath);
            fullPath.append("/");
            fullPath.append(m_pathData.getPath(ResourceManager.UNDEFINED_DIR));
        }

        LOGGER.debug(" Final project partial PATH " + projectPath);
        File f = new File(fullPath.toString());
        if(! f.exists()){
            return null;
        }
        return projectPath.toString();
    }

    public String getProgramAbsolutePath(Program p){

        String suffixPath = m_pathData.getPath(ResourceManager.UNDEFINED_DIR);
        if (p != null)
            // Program defined
            suffixPath = p.getNomenclatureTitle();

        //Get repository name where program is defined
        String containerRepository = getRepositoryFor(suffixPath);
        if(containerRepository == null)
            return null;

        //Create absolute path
        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT);

        StringBuffer fullPath = new StringBuffer(pimsRoot);
        fullPath.append("/");
        fullPath.append(containerRepository);
        fullPath.append("/");
        fullPath.append(suffixPath);
        return fullPath.toString();
    }


    public String getAcquisitionDestinationPath(AcquisitionFileMessageJson acqContext) throws Exception {


        if (acqContext == null || acqContext.getAcquisitionFileDescriptor().getAcquisition() == null) {
            throw new Exception("L'acquisition spécifiée est null");  // should not happen
        }
        LOGGER.debug(" Search DestinationPath For "+acqContext.getAcquisitionFileDescriptor().getAcquisition().getName());

        // JPM : these protocol acquisition and acquisition have been created in memory by the client, they could not exist in database
        ProtocolApplicationJson protocolApplicationJson = acqContext.getAcquisitionFileDescriptor().getAcquisition();
        AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();

        String wantedAcqPath = null;
        Optional<ProtocolApplication> protocolApplicationOpt = m_protocolApplicationRepository.findByName(protocolApplicationJson.getName());
        if (protocolApplicationOpt.isPresent()) {
            ProtocolApplication protocolApplication = protocolApplicationOpt.get();
            wantedAcqPath = getExistingAcqPath(protocolApplication);
            LOGGER.warn(" ----> 2. Found an existing Path for "+protocolApplicationJson.getName()+ " ? : "+wantedAcqPath);

        }  // else {//No acquisition exists in database with the given name.


        if (wantedAcqPath == null) { // No VALID acquisition found

            AcquisitionJson.Nature tempNature = acquisitionJson.getNatureAsEnum();

            if ( tempNature.equals(AcquisitionJson.Nature.RESEARCH) ) {
                LOGGER.debug(" ----> 4. RESEARCH_VALUE ");
                //Get Study Path
                if (acqContext.getSampleDescriptor() != null && acqContext.getSampleDescriptor().getName() !=null){
                    String rawTagName = PathTag.RAW;

                    LOGGER.debug(" --- 4.a Will get Sample "+acqContext.getSampleDescriptor().getName());

                    Optional<Sample> sampleOpt = m_sampleRepository.findByName(acqContext.getSampleDescriptor().getName());
                    if (! sampleOpt.isPresent()) {
                        String errorMessage = "Problem while retrieving sample by name (Context : the wanted acquisition doesn't exist yet) : "+acqContext.getSampleDescriptor().getName();
                        LOGGER.error(errorMessage);
                        throw new Exception(errorMessage);
                    }

                    Sample sample = sampleOpt.get();
                    wantedAcqPath =  getFilesPath(sample, rawTagName);

                    LOGGER.debug(" ----> 4a. Found a Path for  "+acqContext.getSampleDescriptor().getName()+" ? = "+wantedAcqPath);

                }
            } else { //Not a research acquisition
                LOGGER.debug(" ----> 5. NOT RESEARCH_VALUE ");

                String instrumentName = null;
                Integer instrumentId = acquisitionJson.getInstrumentId();
                if (instrumentId != null) {
                    Optional<Instrument> instrumentOpt = m_instrumentRepository.findById(instrumentId);
                    if (instrumentOpt.isPresent()) {
                        instrumentName = instrumentOpt.get().getName();
                    }
                }

                wantedAcqPath = getSharedAcqPath(acquisitionJson.getNatureAsEnum(), protocolApplicationJson.getDate(), instrumentName);
                LOGGER.debug(" ----> 5a. Found a Path for ctrl "+acqContext.getAcquisitionFileDescriptor().getAcquisition().getAcquisitionJson().getNature()+" = "+wantedAcqPath);
            }

        } // END no acq exists in DB

        if (wantedAcqPath == null) {
            String msg = "Impossible de trouver l'acquisition spécifiée, {0}";
            msg = MessageFormat.format(msg, protocolApplicationJson.getName());
            throw new Exception(msg);
        }

        return wantedAcqPath;
    }

    private String getExistingAcqPath(ProtocolApplication protocolApplication) {
        String rawTagName = PathTag.RAW;

        //if only one acquisition is retrieved, and no other feature is specified (nature or spectrometer), the path is taken from it
        if (protocolApplication != null) {
            return getFilesPath(protocolApplication, rawTagName);
        }

        return null;
    }



    public String getSharedAcqPath(AcquisitionJson.Nature type, Date date, String spectroName) {
        if(date==null && spectroName == null)
            return getSharedAcqPath(type);

        if (type == null)
            return getSharedAcqPath(type);

        if (date==null || spectroName == null)
            return null;
        if ((type != AcquisitionJson.Nature.BLANK) && (type != AcquisitionJson.Nature.CONTROL_INSTRUMENT)
                && (type != AcquisitionJson.Nature.CONTROL_LC))
            return null;

        String partialPath = getSharedAcqPath(type);
        if (partialPath == null)
            return null;

        StringBuffer sb = new StringBuffer(partialPath);
        sb.append("/");
        String instName = spectroName;
        sb.append(instName);

        sb.append("/");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        sb.append(year);

        sb.append("/");
        int month = cal.get(Calendar.MONTH) + 1;// returned month => 0: January ...
        sb.append(month);

        //Test if dir exist and create if necessary
        StringBuffer fullPath = new StringBuffer(m_pathData.getPath(ResourceManager.PIMS_ROOT));
        fullPath.append("/");
        fullPath.append(sb.toString());
        File childFile = new File(fullPath.toString());
        if (!childFile.exists()) {
            childFile.mkdirs();
        }

        return sb.toString();
    }

    public String getSharedAcqAbsolutePath(AcquisitionJson.Nature type, Date date, String spectroName) {
        if (date == null && spectroName == null)
            return getSharedAcqAbsolutePath(type);

        if (type == null)
            return getSharedAcqAbsolutePath(type);

        if (date == null || spectroName == null)
            return null;

        if ((type != AcquisitionJson.Nature.BLANK) && (type != AcquisitionJson.Nature.CONTROL_INSTRUMENT)
                && (type != AcquisitionJson.Nature.CONTROL_LC))
            return null;

        String partialPath = getSharedAcqAbsolutePath(type);
        if (partialPath == null)
            return null;

        StringBuffer sb = new StringBuffer(partialPath);
        sb.append("/");
        String instName = spectroName;
        sb.append(instName);
        sb.append("/");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        sb.append(year);
        sb.append("/");
        int month = cal.get(Calendar.MONTH) + 1;// returned month => 0: January ...
        sb.append(month);

        //Test if dir exist and create if necessary
        File childFile = new File(sb.toString());
        if (!childFile.exists()) {
            childFile.mkdirs();
        }

        return sb.toString();
    }

    private String getSharedAcqPath(AcquisitionJson.Nature type) {

        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        String pimsShare = m_pathData.getPath(ResourceManager.PIMS_SHARE) ;

        if (pimsRoot == null ||pimsShare == null) {
            return null;
        }

        StringBuffer path = new StringBuffer();
        path.append(pimsShare);

        StringBuffer fullPath = new StringBuffer();
        fullPath.append(pimsRoot);
        fullPath.append("/");
        fullPath.append(pimsShare);
        File f = new File(fullPath.toString());

        if (!f.exists())
            return null;

        if (type == null)
            return path.toString();

        path.append("/");
        switch (type) {
            case CONTROL_LC:
                path.append(ACQ_CTRL_LC_DIR_NAME);
                break;
            case CONTROL_INSTRUMENT:
                path.append(ACQ_CTRL_INSTR_DIR_NAME);
                break;
            case BLANK:
                path.append(ACQ_CTRL_BLANK_DIR_NAME);
                break;
        }

        fullPath = new StringBuffer();
        fullPath.append(pimsRoot);
        fullPath.append("/");
        fullPath.append(path);
        f = new File(fullPath.toString());
        if (!f.exists())
            return null;
        return path.toString();
    }

    private String getSharedAcqAbsolutePath(AcquisitionJson.Nature type) {

        String relatifPath = getSharedAcqPath(type);
        if(relatifPath == null)
            return null;

        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        StringBuffer path = new StringBuffer(pimsRoot);
        path.append("/");
        path.append(relatifPath);
        return path.toString();
    }



    public String getFilesPath(ProtocolApplication protocolApplication, String tagName) {

        if (protocolApplication == null || tagName == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();



        String prefixPath = getAcqPath(protocolApplication);
        if (prefixPath == null) {
            return null;
        }
        path.append(prefixPath);
        fullPath.append(getAcqAbsolutePath(protocolApplication));

        //Specify if tag name should be added to path as directory name
        boolean addTagDir =  AcquisitionJson.convertNatureToEnum(protocolApplication.getAcquisition().getNature()).equals(AcquisitionJson.Nature.RESEARCH);
        if (addTagDir) {
            path.append("/");
            path.append(DATA_DIR_NAME);
            path.append("/");
            path.append(tagName);

            fullPath.append("/");
            fullPath.append(DATA_DIR_NAME);
            fullPath.append("/");
            fullPath.append(tagName);
        }

        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }

    public String getFilesPath(Sample sample, String tagName) {

        if (sample == null || tagName == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();

        //Specify if tag name should be added to path as directory name
        boolean addTagDir = true;

        //*** For Acquisition FileLink

        String prefixPath = getStudyPath(sample.getStudy());
        if (prefixPath == null) {
            return null;
        }

        path.append(prefixPath);
        path.append("/");
        path.append(SAMPLES_DIR_NAME);
        fullPath.append(getStudyAbsolutePath(sample.getStudy()));
        fullPath.append("/");
        fullPath.append(SAMPLES_DIR_NAME);

        path.append("/");
        path.append(DATA_DIR_NAME);
        path.append("/");
        path.append(tagName);

        fullPath.append("/");
        fullPath.append(DATA_DIR_NAME);
        fullPath.append("/");
        fullPath.append(tagName);


        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }

    /*
    public String getFilesPath(Object linkedObject, String tagName) {

        if (linkedObject == null || tagName == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();

        //Specify if tag name should be added to path as directory name
        boolean addTagDir = true;

        // --- For Acquisition FileLink
        if (Acquisition.class.isAssignableFrom(linkedObject.getClass())) {
            Acquisition acq = (Acquisition)linkedObject;
            String prefixPath = getAcqPath(acq);
            if (prefixPath == null)
                return null;
            path.append(prefixPath);
            fullPath.append(getAcqAbsolutePath(acq));

            switch (acq.getNatureType()) {
                case Acquisition.RESEARCH_TYPE:
                    addTagDir = true;
                    break;
                case Acquisition.BLANK_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_INST_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_LC_TYPE:
                    addTagDir = false;
                    break;
                default:
                    addTagDir = false;
                    break;
            }

            // For Study FileLink
        } else if (Study.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath((Study) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getStudyAbsolutePath((Study) linkedObject));
            addTagDir = true;

            // For Sample FileLink
        } else if (Sample.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath(((Sample) linkedObject).getStudy());
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            path.append("/");
            path.append(SAMPLES_DIR_NAME);
            fullPath.append(getStudyAbsolutePath(((Sample) linkedObject).getStudy()));
            fullPath.append("/");
            fullPath.append(SAMPLES_DIR_NAME);
            addTagDir = true;

            // For Project FileLink
        } else if (Project.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProjectPath((Project) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProjectAbsolutePath((Project) linkedObject));
            addTagDir = true;

            // For Program FileLink
        } else if (Program.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProgramPath((Program) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProgramAbsolutePath((Program) linkedObject));
            addTagDir = true;


            //Unknown Object type. return null
        } else {
            logger.warn("Unknown Object Type  "+linkedObject);
            return null;
        }

        if (addTagDir) {
            path.append("/");
            path.append(DATA_DIR_NAME);
            path.append("/");
            path.append(tag.getName());

            fullPath.append("/");
            fullPath.append(DATA_DIR_NAME);
            fullPath.append("/");
            fullPath.append(tag.getName());
        }

        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }

    public String getFilesPath(Object linkedObject, String tagName) {

        if (linkedObject == null || tagName == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();

        //Specify if tag name should be added to path as directory name
        boolean addTagDir = true;

        //*** For Acquisition FileLink
        if (Acquisition.class.isAssignableFrom(linkedObject.getClass())) {
            Acquisition acq = (Acquisition)linkedObject;
            String prefixPath = getAcqPath(acq);
            if (prefixPath == null)
                return null;
            path.append(prefixPath);
            fullPath.append(getAcqAbsolutePath(acq));

            switch (acq.getNatureType()) {
                case Acquisition.RESEARCH_TYPE:
                    addTagDir = true;
                    break;
                case Acquisition.BLANK_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_INST_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_LC_TYPE:
                    addTagDir = false;
                    break;
                default:
                    addTagDir = false;
                    break;
            }

            // For Study FileLink
        } else if (Study.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath((Study) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getStudyAbsolutePath((Study) linkedObject));
            addTagDir = true;

            // For Sample FileLink
        } else if (Sample.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath(((Sample) linkedObject).getStudy());
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            path.append("/");
            path.append(SAMPLES_DIR_NAME);
            fullPath.append(getStudyAbsolutePath(((Sample) linkedObject).getStudy()));
            fullPath.append("/");
            fullPath.append(SAMPLES_DIR_NAME);
            addTagDir = true;

            // For Project FileLink
        } else if (Project.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProjectPath((Project) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProjectAbsolutePath((Project) linkedObject));
            addTagDir = true;

            // For Program FileLink
        } else if (Program.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProgramPath((Program) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProgramAbsolutePath((Program) linkedObject));
            addTagDir = true;


            //Unknown Object type. return null
        } else {
            logger.warn("Unknown Object Type  "+linkedObject);
            return null;
        }

        if (addTagDir) {
            path.append("/");
            path.append(DATA_DIR_NAME);
            path.append("/");
            path.append(tag.getName());

            fullPath.append("/");
            fullPath.append(DATA_DIR_NAME);
            fullPath.append("/");
            fullPath.append(tag.getName());
        }

        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }

    public String getFilesPath(Object linkedObject, String tagName) {

        if (linkedObject == null || tagName == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();

        //Specify if tag name should be added to path as directory name
        boolean addTagDir = true;

        //*** For Acquisition FileLink
        if (Acquisition.class.isAssignableFrom(linkedObject.getClass())) {
            Acquisition acq = (Acquisition)linkedObject;
            String prefixPath = getAcqPath(acq);
            if (prefixPath == null)
                return null;
            path.append(prefixPath);
            fullPath.append(getAcqAbsolutePath(acq));

            switch (acq.getNatureType()) {
                case Acquisition.RESEARCH_TYPE:
                    addTagDir = true;
                    break;
                case Acquisition.BLANK_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_INST_TYPE:
                    addTagDir = false;
                    break;
                case Acquisition.CONTROL_LC_TYPE:
                    addTagDir = false;
                    break;
                default:
                    addTagDir = false;
                    break;
            }

            // For Study FileLink
        } else if (Study.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath((Study) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getStudyAbsolutePath((Study) linkedObject));
            addTagDir = true;

            // For Sample FileLink
        } else if (Sample.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getStudyPath(((Sample) linkedObject).getStudy());
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            path.append("/");
            path.append(SAMPLES_DIR_NAME);
            fullPath.append(getStudyAbsolutePath(((Sample) linkedObject).getStudy()));
            fullPath.append("/");
            fullPath.append(SAMPLES_DIR_NAME);
            addTagDir = true;

            // For Project FileLink
        } else if (Project.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProjectPath((Project) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProjectAbsolutePath((Project) linkedObject));
            addTagDir = true;

            // For Program FileLink
        } else if (Program.class.isAssignableFrom(linkedObject.getClass())) {
            String prefixPath = getProgramPath((Program) linkedObject);
            if (prefixPath == null)
                return null;

            path.append(prefixPath);
            fullPath.append(getProgramAbsolutePath((Program) linkedObject));
            addTagDir = true;


            //Unknown Object type. return null
        } else {
            logger.warn("Unknown Object Type  "+linkedObject);
            return null;
        }

        if (addTagDir) {
            path.append("/");
            path.append(DATA_DIR_NAME);
            path.append("/");
            path.append(tag.getName());

            fullPath.append("/");
            fullPath.append(DATA_DIR_NAME);
            fullPath.append("/");
            fullPath.append(tag.getName());
        }

        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }*/

    public String getFilesPath(AcquisitionFile acquisitionFile) {

        if (acquisitionFile == null)
            return null;

        //Path to return
        StringBuffer path = new StringBuffer();

        //Full path to create necessary directories
        StringBuffer fullPath = new StringBuffer();

        //Specify if tag name should be added to path as directory name
        boolean addTagDir = true;

        ProtocolApplication protocolApplication = null;
        Integer idfkAcquitision = acquisitionFile.getIdFk();
        if (idfkAcquitision == null) {

            System.err.println("Potential Bug");
            return null;
        } else {
            Optional<ProtocolApplication> opt = m_protocolApplicationRepository.findById(idfkAcquitision);
            if (opt.isPresent()) {
                protocolApplication = opt.get();
            }
        }

        if (protocolApplication == null) {
            return null;
        }

        String prefixPath = getAcqPath(protocolApplication);
            if (prefixPath == null)
                return null;

        path.append(prefixPath);
        fullPath.append(getAcqAbsolutePath(protocolApplication));

        Acquisition acq = protocolApplication.getAcquisition();
        addTagDir = AcquisitionJson.convertNatureToEnum(acq.getNature()).equals(AcquisitionJson.Nature.RESEARCH);


        if (addTagDir) {
            String tagDir = getTagAsDir(acquisitionFile);
            if (tagDir != null) {
                path.append("/");
                path.append(DATA_DIR_NAME);
                path.append("/");
                path.append(tagDir);

                fullPath.append("/");
                fullPath.append(DATA_DIR_NAME);
                fullPath.append("/");
                fullPath.append(tagDir);
            }
        }

        File f = new File(fullPath.toString());
        if(!f.exists())
            f.mkdirs();

        return path.toString();
    }

    private String getTagAsDir(FileLink linked){
        AttachedFile file = linked.getAttachedFile();
        if (file!= null && file.getFileTagses() != null
                && !file.getFileTagses().isEmpty()) {
            FileTags fileTags = file.getFileTagses().iterator().next();
            Tag tag = fileTags.getTag();
            return tag.getName();
        }
        return null;
    }


    public String getStudyAbsolutePath(Study s) {
        if (s == null)
            return null;

        String partialPath = getStudyPartialPath(s);
        LOGGER.debug(" getStudyAbsolutePath partial for " + s.getNomenclatureTitle()
                + " = " + partialPath);

        String containerRepository = getRepositoryFor(partialPath);
        if(containerRepository == null )
            return null;

        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        StringBuffer fullPath = new StringBuffer(pimsRoot);
        fullPath.append("/");
        fullPath.append(containerRepository);
        fullPath.append("/");
        fullPath.append(partialPath);
        return fullPath.toString();
    }

    public String getAcqPath(ProtocolApplication protocolApplication) {
        if (protocolApplication == null) {
            return null;
        }

        Acquisition acquisition = protocolApplication.getAcquisition();

        String path = null;
        AcquisitionJson.Nature nature = AcquisitionJson.convertNatureToEnum(acquisition.getNature());
        if (nature.equals(AcquisitionJson.Nature.RESEARCH)) {
            //Search associated sample and study
            Set<TreatmentsApplication> treatmentsApps = protocolApplication.getTreatmentsApplications();
            if ((treatmentsApps == null) || treatmentsApps.isEmpty()) {
                return null;   //JPM.TODO : seems to bug
            }

            TreatmentsApplication treatmentsApplication = treatmentsApps.iterator().next();
            if (treatmentsApplication == null) {
                return null;
            }

            Treatments treatments = treatmentsApplication.getTreatments();
            if (treatments == null) {
                return null;
            }
            Set<Sample> samples  = treatments.getSamples();
            if ((samples == null) || samples.isEmpty()) {
                return null;
            }

            Sample sample = samples.iterator().next();
            if (sample == null) {
                return null;
            }

            Study study = sample.getStudy();

            String fullPath = getStudyAbsolutePath(study);
            String studyPath = getStudyPath(study);
            if (studyPath != null) {
                StringBuffer sb = new StringBuffer(studyPath);
                sb.append("/");
                sb.append(SAMPLES_DIR_NAME);
                path = sb.toString();

                StringBuffer fullSplPath = new StringBuffer(fullPath);
                fullSplPath.append("/");
                fullSplPath.append(SAMPLES_DIR_NAME);
                File dir = new File(fullSplPath.toString());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }

        } else {

            if ((! nature.equals(AcquisitionJson.Nature.BLANK)) &&
                    (!nature.equals(AcquisitionJson.Nature.CONTROL_INSTRUMENT)) &&
                    (!nature.equals(AcquisitionJson.Nature.CONTROL_LC))) {
                return null;
            }


            if (acquisition.getSpectrometer()==null)
                return null;

            String partialPath = getSharedAcqPath(nature, protocolApplication.getDate(), acquisition.getSpectrometer().getInstrument().getName());
            path = partialPath;

        }
        return path;
    }


    public String getAcqAbsolutePath(ProtocolApplication protocolApplication) {
        if (protocolApplication == null) {
            return null;
        }

        Acquisition acquisition = protocolApplication.getAcquisition();

        String path = null;
        AcquisitionJson.Nature nature = AcquisitionJson.convertNatureToEnum(acquisition.getNature());
        if (nature.equals(AcquisitionJson.Nature.RESEARCH)) {
            //Search associated sample and study
            Set<TreatmentsApplication> treatmentsApps = protocolApplication.getTreatmentsApplications();
            if ((treatmentsApps == null) || treatmentsApps.isEmpty()) {
                return null;
            }

            Treatments treatments = treatmentsApps.iterator().next().getTreatments();
            Set<Sample> samples  =treatments.getSamples();
            if ((samples == null) || treatmentsApps.isEmpty()) {
                return null;
            }
            Study study = samples.iterator().next().getStudy();
            String studyPath = getStudyAbsolutePath(study);
            if (studyPath != null) {
                StringBuffer sb = new StringBuffer(studyPath);
                sb.append("/");
                sb.append(SAMPLES_DIR_NAME);
                path = sb.toString();
                File dir = new File(path.toString());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }

        } else {
            //Shared Acquisition !
            if ((! nature.equals(AcquisitionJson.Nature.BLANK)) &&
                    (!nature.equals(AcquisitionJson.Nature.CONTROL_INSTRUMENT)) &&
                    (!nature.equals(AcquisitionJson.Nature.CONTROL_LC))) {
                return null;
            }

            String partialPath = getSharedAcqAbsolutePath(nature, protocolApplication.getDate(), acquisition.getSpectrometer().getInstrument().getName());
            if (partialPath == null) {
                return null;
            }

            path = partialPath;
        }
        return path;
    }


    public String getPimsShare() {
        return m_pathData.getPath(ResourceManager.PIMS_SHARE);
    }

    public String getControlAcquisitionRoot() {
        String pimsRoot = m_pathData.getPath(ResourceManager.PIMS_ROOT) ;
        StringBuffer fullPath = new StringBuffer(pimsRoot);
        String pimsShare = m_pathData.getPath(ResourceManager.PIMS_SHARE) ;
        fullPath.append("/");
        fullPath.append(pimsShare);

        return fullPath.toString();
    }

    public String getControlAcquisitionPath(String rootPath, String nature, String instrument, Integer year, Integer month, String fileName) {
        StringBuffer fullPath = new StringBuffer(rootPath);
        fullPath.append('/');
        fullPath.append(nature).append("/");
        fullPath.append(instrument).append("/");
        fullPath.append(year).append("/");
        fullPath.append(month).append("/").append(fileName);
        return fullPath.toString();
    }

}
