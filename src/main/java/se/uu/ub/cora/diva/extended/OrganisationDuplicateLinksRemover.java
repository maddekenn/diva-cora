/*
 * Copyright 2020 Uppsala University Library
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class OrganisationDuplicateLinksRemover implements ExtendedFunctionality {

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		handleChildrenInDataGroupUsingNameInData(data.dataGroup, "parentOrganisation");
		handleChildrenInDataGroupUsingNameInData(data.dataGroup, "earlierOrganisation");
	}

	private void handleChildrenInDataGroupUsingNameInData(DataGroup dataGroup, String nameInData) {
		if (dataGroup.containsChildWithNameInData(nameInData)) {
			List<DataGroup> organisations = dataGroup.getAllGroupsWithNameInData(nameInData);
			List<DataElement> elementsToKeep = calculateParentsToKeep(organisations);

			if (organisationListHasBeenReduced(organisations, elementsToKeep)) {
				dataGroup.removeAllChildrenWithNameInData(nameInData);
				dataGroup.addChildren(elementsToKeep);
			}
		}
	}

	private boolean organisationListHasBeenReduced(List<DataGroup> organisations,
			List<DataElement> elementsToKeep) {
		return organisations.size() != elementsToKeep.size();
	}

	private List<DataElement> calculateParentsToKeep(List<DataGroup> parentOrganisations) {
		Map<String, DataElement> sortedParents = new HashMap<>();
		List<DataElement> elementsToKeep = new ArrayList<>();
		for (DataGroup parentGroup : parentOrganisations) {
			calculateWhetherToKeepOrganisation(sortedParents, elementsToKeep, parentGroup);
		}
		return elementsToKeep;
	}

	private void calculateWhetherToKeepOrganisation(Map<String, DataElement> sortedParents,
			List<DataElement> elementsToKeep, DataGroup parentGroup) {
		DataGroup parentLink = parentGroup.getFirstGroupWithNameInData("organisationLink");
		String organisationId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		if (organisationDoesNotAlreadyExist(sortedParents, organisationId)) {
			elementsToKeep.add(parentGroup);
			sortedParents.put(organisationId, parentGroup);
		}
	}

	private boolean organisationDoesNotAlreadyExist(Map<String, DataElement> sortedParents,
			String organisationId) {
		return !sortedParents.containsKey(organisationId);
	}

}
