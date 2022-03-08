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
package se.uu.ub.cora.diva.extended.person;

import java.util.List;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.extended.ExtendedFunctionalityUtils;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonDomainPartLocalIdValidator implements ExtendedFunctionality {

	private static final String IDENTIFIER = "identifier";

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup previousDomainPart = data.previouslyStoredTopDataGroup;
		List<String> previousIdentifierValues = extractIdentifiers(previousDomainPart);

		DataGroup updatedDataGroup = data.dataGroup;
		List<String> currentIdentifierValues = extractIdentifiers(updatedDataGroup);

		ensureNoIdentifiersHasBeenDeleted(updatedDataGroup, previousIdentifierValues,
				currentIdentifierValues);
	}

	private List<String> extractIdentifiers(DataGroup previousRecord) {
		List<DataAtomic> previousIdentifiers = previousRecord
				.getAllDataAtomicsWithNameInData(IDENTIFIER);
		return ExtendedFunctionalityUtils.getDataAtomicValuesAsList(previousIdentifiers);
	}

	private void ensureNoIdentifiersHasBeenDeleted(DataGroup updatedDataGroup,
			List<String> previousValues, List<String> currentValues) {
		for (String previousValue : previousValues) {
			if (valueHasBeenRemoved(currentValues, previousValue)) {
				addValue(updatedDataGroup, previousValue);
			}
		}
		ExtendedFunctionalityUtils.setNewRepeatIdsToEnsureUnique(updatedDataGroup, IDENTIFIER);
	}

	private boolean valueHasBeenRemoved(List<String> currentValues, String previousValue) {
		return !currentValues.contains(previousValue);
	}

	private void addValue(DataGroup updatedDataGroup, String previousValue) {
		updatedDataGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(IDENTIFIER, previousValue));
	}

}
