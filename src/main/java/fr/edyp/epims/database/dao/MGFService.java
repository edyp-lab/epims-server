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

package fr.edyp.epims.database.dao;



import fr.edyp.epims.json.MgfFileInfoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;


@Repository
@Transactional(readOnly = true)
public class MGFService {

    @Autowired
    SampleRepository sampleRepository;

    @PersistenceContext
    private EntityManager em;

    public MgfFileInfoJson[] mgfList() {

        String sql = " select af.\"path\", af.name\n" +
                "from attached_file af, file_tags ft \n" +
                "where af.id=ft.file and tag = 'SPECTRA' ";


        List<Object[]> resultSQLList = em.createNativeQuery(sql).getResultList();


        MgfFileInfoJson[] resultList = new MgfFileInfoJson[resultSQLList.size()];
        int i = 0;
        for (Object[] values : resultSQLList) {

            String path = (String) values[0];
            String name = (String) values[1];
            MgfFileInfoJson mgfFileInfoJson = new MgfFileInfoJson(name, path, -1, null, null);
            resultList[i] = mgfFileInfoJson;
            i++;
        }


        return resultList;
    }

    public Integer studyForMGF(String mgfName) {

        String acquisitionName = acquisitionNameForMGFName(mgfName);


        String sql ="select s.study \n" +
                "from sample s, treatments_application ta, protocol_application pa \n" +
                "where pa.\"name\" = '"+acquisitionName+"' and pa.id=ta.protocol_application and ta.treatments =s.treatments ";

        List<Object> resultSQLList = em.createNativeQuery(sql).getResultList();
        if (resultSQLList.size()>0) {
            Integer sampleId = (Integer) resultSQLList.get(0);
            return sampleId;
        } else {
            return -1;
        }

    }


    public static String acquisitionNameForMGFName(String mgfName) {

        // Look for name like "6536375___HF1_23942.raw.-1.mgf
        int index3Underscore = mgfName.indexOf("___");
        if (index3Underscore != -1) {
            int indexRaw = mgfName.indexOf(".raw");
            if (indexRaw != -1) {
                return mgfName.substring(index3Underscore+3, indexRaw);
            }
        }

        // Look for names with multiple underscores HF1_280043_452323.mgf
        int indexFirstUnderscore = mgfName.indexOf("_");
        if (indexFirstUnderscore != -1) {
            int indexLastUnderscore = mgfName.lastIndexOf("_");
            if ((indexLastUnderscore!=-1) && (indexLastUnderscore>indexFirstUnderscore+1)) {
                return mgfName.substring(0, indexLastUnderscore);
            }
        }

        // Look for names with " (2).mgf"
        int indexOf2 = mgfName.indexOf(" (2).mgf");
        if (indexOf2 != -1) {
            return mgfName.substring(0, indexOf2);
        }

        // normal case HF2_352523.mgf
        int indexMgf = mgfName.indexOf(".mgf");
        if (indexMgf != -1) {
            return mgfName.substring(0, indexMgf);
        }

        return mgfName; // should not happen


    }


}
