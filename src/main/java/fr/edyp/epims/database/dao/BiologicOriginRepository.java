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

import fr.edyp.epims.database.entities.BiologicOrigin;
import fr.edyp.epims.database.entities.SampleSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BiologicOriginRepository extends JpaRepository<BiologicOrigin, Integer> {

    @Query(value = "SELECT bo FROM BiologicOrigin bo WHERE ((:sampleKindId is null) OR sampleKind.id = :sampleKindId) AND " +
            " ((:sampleSpeciesId is null) OR sampleSpecies.id = :sampleSpeciesId) AND " +
            " ((:sampleSubcellularLocalisationId is null) OR sampleSubcellularLocalisation.id = :sampleSubcellularLocalisationId) AND " +
            " ((:sampleTypeId is null) OR sampleType.id = :sampleTypeId) AND " +
            " (commentOrigin is null)")
    public List<BiologicOrigin> findByValues(@Param("sampleKindId") Integer sampleKindId,
                                             @Param("sampleSpeciesId") Integer sampleSpeciesId,
                                             @Param("sampleSubcellularLocalisationId") Integer sampleSubcellularLocalisationId,
                                             @Param("sampleTypeId") Integer sampleTypeId);
}
