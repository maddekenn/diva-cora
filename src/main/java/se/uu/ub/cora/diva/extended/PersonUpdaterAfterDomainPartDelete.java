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

import java.util.List;

import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonUpdaterAfterDomainPartDelete implements ExtendedFunctionality {

	private static final String RECORD_INFO = "recordInfo";
	private static final String LINKED_RECORD_ID = "linkedRecordId";
	private static final String PERSON = "person";
	private RecordStorage recordStorage;
	private DataGroupTermCollector termCollector;
	private DataRecordLinkCollector linkCollector;

	public PersonUpdaterAfterDomainPartDelete(RecordStorage recordStorage,
			DataGroupTermCollector termCollector, DataRecordLinkCollector linkCollector) {
		this.recordStorage = recordStorage;
		this.termCollector = termCollector;
		this.linkCollector = linkCollector;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		String recordId = extractRecordId(data.dataGroup);
		String personIdPartOfId = recordId.substring(0, recordId.lastIndexOf(":"));

		DataGroup person = recordStorage.read(PERSON, personIdPartOfId);
		alterPersonDomainPartList(recordId, person);

		String metadataId = getMetadataId();
		DataGroup collectedTerms = termCollector.collectTerms(metadataId, person);
		DataGroup collectedLinks = linkCollector.collectLinks(metadataId, person, PERSON,
				personIdPartOfId);
		String dataDivider = extractDataDivider(person);
		recordStorage.update(PERSON, personIdPartOfId, person, collectedTerms, collectedLinks,
				dataDivider);

	}

	private void alterPersonDomainPartList(String recordId, DataGroup person) {
		List<DataGroup> currentPersonDomainParts = person
				.getAllGroupsWithNameInData("personDomainPart");

		person.removeAllChildrenWithNameInData("personDomainPart");

		removeMatchingDomainPart(currentPersonDomainParts, recordId, person);
	}

	private void removeMatchingDomainPart(List<DataGroup> currentPersonDomainParts, String recordId,
			DataGroup person) {
		for (DataGroup personDomainPart : currentPersonDomainParts) {
			possiblyAddDomainPartAgain(personDomainPart, recordId, person);
		}
	}

	private void possiblyAddDomainPartAgain(DataGroup personDomainPart, String recordId,
			DataGroup person) {
		String linkedDomainPart = personDomainPart
				.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
		if (notDomainPartToRemove(recordId, linkedDomainPart)) {
			person.addChild(personDomainPart);
		}
	}

	private boolean notDomainPartToRemove(String recordId, String linkedDomainPart) {
		return !recordId.equals(linkedDomainPart);
	}

	private String getMetadataId() {
		DataGroup readRecordType = recordStorage.read("recordType", PERSON);
		DataGroup metadataIdLink = readRecordType.getFirstGroupWithNameInData("metadataId");
		return metadataIdLink.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
	}

	private String extractDataDivider(DataGroup readPerson) {
		DataGroup recordInfo = readPerson.getFirstGroupWithNameInData(RECORD_INFO);
		DataGroup dataDividerGroup = recordInfo.getFirstGroupWithNameInData("dataDivider");
		return dataDividerGroup.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
	}

	private String extractRecordId(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

	public DataGroupTermCollector getTermCollector() {
		return termCollector;
	}

	public DataRecordLinkCollector getLinkCollector() {
		return linkCollector;
	}

}
