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



import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)

public class FixService {

    @PersistenceContext
    private EntityManager em;

    public List<FileLinkJson> fileLinkToFix() {

        String sql = "select fl.id, fl.attached_file, af.name FROM public.file_link as fl, public.attached_file as af WHERE fl.attached_file=af.id AND fl.protocol_application IS NOT NULL AND fl.id_fk IS NULL ORDER BY fl.id ";


        List<Object[]> resultSQLList = em.createNativeQuery(sql).getResultList();


        List<FileLinkJson> resultList = new ArrayList<>(resultSQLList.size());
        for (Object[] values : resultSQLList) {

            FileLinkJson fileLinkJson = new FileLinkJson();

            fileLinkJson.id = ((Integer)values[0]).intValue();
            fileLinkJson.attached_file = ((Integer)values[1]).intValue();
            fileLinkJson.name = (String) values[2];

            resultList.add(fileLinkJson);
        }


        return resultList;
    }


    public static class FileLinkJson {
        public int id;
        public int attached_file;
        public String name;
    }

}
