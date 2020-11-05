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
package se.uu.ub.cora.diva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extended.ExtendedFunctionality;

public class OrganisationExtendedFunctionality implements ExtendedFunctionality {

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {

		List<DataGroup> parentOrganisations = dataGroup
				.getAllGroupsWithNameInData("parentOrganisation");
		Map<String, DataElement> sortedParents = new HashMap<>();
		List<DataElement> elementsToKeep = new ArrayList<>();

		for (DataGroup parentGroup : parentOrganisations) {
			DataGroup parentLink = parentGroup.getFirstGroupWithNameInData("organisationLink");
			String organisationId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
			if (!sortedParents.containsKey(organisationId)) {
				elementsToKeep.add(parentGroup);
				sortedParents.put(organisationId, parentGroup);
			}
		}

		if (parentOrganisations.size() != elementsToKeep.size()) {
			dataGroup.removeAllChildrenWithNameInData("parentOrganisation");
			dataGroup.addChildren(elementsToKeep);
		}
	}

}
