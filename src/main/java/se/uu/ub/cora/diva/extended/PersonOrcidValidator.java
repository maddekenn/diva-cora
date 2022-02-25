/*
 * Copyright 2022 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.extended;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonOrcidValidator implements ExtendedFunctionality {

	private static final String ORCID_ID = "ORCID_ID";

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup previousRecord = data.previouslyStoredTopDataGroup;
		List<String> previousOrcidValues = extractPreviousOrcids(previousRecord);

		DataGroup updatedDataGroup = data.dataGroup;
		List<String> currentOrcidValues = extractCurrentOrcids(updatedDataGroup);

		for (String previousOrcid : previousOrcidValues) {
			if (!currentOrcidValues.contains(previousOrcid)) {
				updatedDataGroup.addChild(DataAtomicProvider
						.getDataAtomicUsingNameInDataAndValue(ORCID_ID, previousOrcid));
			}
		}
	}

	private List<String> extractCurrentOrcids(DataGroup updatedDataGroup) {
		List<DataAtomic> currentOrcids = updatedDataGroup.getAllDataAtomicsWithNameInData(ORCID_ID);
		return getValuesAsList(currentOrcids);
	}

	private List<String> extractPreviousOrcids(DataGroup previousRecord) {
		List<DataAtomic> previousOrcids = previousRecord.getAllDataAtomicsWithNameInData(ORCID_ID);
		return getValuesAsList(previousOrcids);
	}

	private List<String> getValuesAsList(List<DataAtomic> orcids) {
		List<String> orcidValues = new ArrayList<>();
		for (DataAtomic dataAtomic : orcids) {
			orcidValues.add(dataAtomic.getValue());
		}
		return orcidValues;
	}

}
