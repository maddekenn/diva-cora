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
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;
import se.uu.ub.cora.storage.RecordStorage;

public class OrganisationDifferentDomainDetector implements ExtendedFunctionality {

	private RecordStorage recordStorage;

	public OrganisationDifferentDomainDetector(RecordStorage recordStorage) {
		this.recordStorage = recordStorage;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup dataGroup = data.dataGroup;
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		String domain = recordInfo.getFirstAtomicValueWithNameInData("domain");
		List<DataGroup> combinedList = getListOfParentsAndPredecessors(dataGroup);
		checkLinksAndThrowErrorIfDifferentDomain(domain, combinedList);

	}

	private List<DataGroup> getListOfParentsAndPredecessors(DataGroup dataGroup) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData("parentOrganisation");
		List<DataGroup> predecessors = dataGroup.getAllGroupsWithNameInData("earlierOrganisation");
		return combineToOneList(parents, predecessors);
	}

	private List<DataGroup> combineToOneList(List<DataGroup> parents,
			List<DataGroup> predecessors) {
		List<DataGroup> combinedList = new ArrayList<>();
		combinedList.addAll(parents);
		combinedList.addAll(predecessors);
		return combinedList;
	}

	private void checkLinksAndThrowErrorIfDifferentDomain(String domain,
			List<DataGroup> linkedOrganisations) {
		for (DataGroup parent : linkedOrganisations) {
			checkLinkAndThrowErrorIfDifferentDomain(domain, parent);
		}
	}

	private void checkLinkAndThrowErrorIfDifferentDomain(String domain, DataGroup parent) {
		DataGroup organisationLink = parent.getFirstGroupWithNameInData("organisationLink");
		String recordId = organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		String recordType = organisationLink.getFirstAtomicValueWithNameInData("linkedRecordType");
		readLinkedOrgFromStorageAndPossiblyThrowException(domain, recordType, recordId);
	}

	private void readLinkedOrgFromStorageAndPossiblyThrowException(String domain, String recordType,
			String recordId) {
		DataGroup readParent = recordStorage.read(recordType, recordId);
		DataGroup recordInfo = readParent.getFirstGroupWithNameInData("recordInfo");
		String linkedOrganisationDomain = recordInfo.getFirstAtomicValueWithNameInData("domain");
		throwErrorIfDifferentDomain(domain, linkedOrganisationDomain);
	}

	private void throwErrorIfDifferentDomain(String domain, String parentDomain) {
		if (!parentDomain.equals(domain)) {
			throw new DataException("Links to organisations from another domain is not allowed.");
		}
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

}
