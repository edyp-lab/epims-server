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

import fr.edyp.epims.json.ArchivingInfoJson;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class ArchivingData {

    private LinkedList<ArchivingInfoJson> m_archivingInfoJsonList = null;

    private LinkedList<ArchivingInfoJson> m_finishedInfoJsonList = new LinkedList<>();

    private ArchivingInfoJson m_runningArchiving = null;

    public synchronized boolean setData(LinkedList<ArchivingInfoJson> archivingInfoJsonList) {
        boolean executeArchivage = (m_archivingInfoJsonList == null) || (m_archivingInfoJsonList.isEmpty());

        if (m_archivingInfoJsonList != null) {
            m_archivingInfoJsonList.clear();
        } else {
            m_archivingInfoJsonList = new LinkedList<>();
        }
        if (m_finishedInfoJsonList != null) {
            m_finishedInfoJsonList.clear();
        } else {
            m_finishedInfoJsonList = new LinkedList<>();
        }

        for (ArchivingInfoJson archivingInfo : archivingInfoJsonList) {
            if (archivingInfo.isWaiting()) {
                m_archivingInfoJsonList.add(archivingInfo);
            } else {
                m_finishedInfoJsonList.add(archivingInfo);
            }
        }

        return executeArchivage; // if false : another thread is already archiving
    }

    public synchronized ArchivingInfoJson popData() {
        if ((m_archivingInfoJsonList == null) || (m_archivingInfoJsonList.isEmpty())) {
            return null;
        }

        m_runningArchiving =  m_archivingInfoJsonList.pop();

        return m_runningArchiving;
    }

    public synchronized void actionFinished(ArchivingInfoJson archivingInfoJson) {
        m_finishedInfoJsonList.add(archivingInfoJson);
        m_runningArchiving = null;
    }

    public synchronized ArchivingInfoJson[] getAllArchiving() {
        LinkedList<ArchivingInfoJson> allArchiving = new LinkedList<>();


        if (m_finishedInfoJsonList != null) {
            allArchiving.addAll(m_finishedInfoJsonList);
        }
        if (m_runningArchiving != null) {
            allArchiving.add(m_runningArchiving);
        }
        if (m_archivingInfoJsonList != null) {
            allArchiving.addAll(m_archivingInfoJsonList);
        }

        ArchivingInfoJson[] archivingInfoArray = allArchiving.toArray(new ArchivingInfoJson[allArchiving.size()]);

        return archivingInfoArray;
    }

}
