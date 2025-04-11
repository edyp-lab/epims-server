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

public class ResourceManager {

    /**
     * The name of the default resource bundles.
     */
    private static final String DEFAULT_RESOURCES_NAME = "ePCore";

    /**
     * The System property which defines the name of the user resource bundle to
     * use with PitBuL.
     * With new system architecture this should not be useful anymore ... to be confirmed
     */
    public static final String USER_RESOURCE_PROPERTY = "User-ePCore";

    /*
     * The name of the File, in class path, containing version info
     */
    //private static final String VERSION_FILE_NAME = "/eP-Core.VERSION";

    //private final static String VERSION_TAG = "Version";

    /**
     * Key to get directory name used to store orphans studies/projects
     */
    public final static String UNDEFINED_DIR = "UNDEF_DIR_NAME";

    /**
     * Key to get ePims Root directory name
     */
    public final static String PIMS_ROOT = "PIMS_ROOT";

    /**
     * Prefix Key to get repositories. Must add indexes at the end of the key
     */
    public final static String PIMS_REPOSITORY_PREFIX = "PIMS_REPOSITORY_";

    /**
     * Key to get ePims System directory name
     */
    public final static String PIMS_SYSTEM = "PIMS_SYSTEM";

    /**
     * Key to get directory name used to store archives
     */
    public final static String PIMS_ARCHIVE = "PIMS_ARCHIVE";

    /**
     * Key to get file name copied at studies root after archived
     */
    public final static String PIMS_ARCHIVE_FILE = "PIMS_ARCHIVE_FILE";

    /**
     * Key to get directory name used to store commons files
     */
    public final static String PIMS_SHARE = "PIMS_SHARE";

    /**
     * Key to get drop zone directory name used to transfer data to ePims
     */
    public final static String PIMS_DROP_ZONE = "PIMS_DROP_ZONE";

}
