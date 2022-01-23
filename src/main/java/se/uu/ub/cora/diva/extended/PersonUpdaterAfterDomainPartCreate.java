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
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonUpdaterAfterDomainPartCreate implements ExtendedFunctionality {

	private static final String RECORD_INFO = "recordInfo";
	private static final String PERSON = "person";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private RecordStorage recordStorage;
	private DataGroupTermCollector termCollector;
	private DataRecordLinkCollector linkCollector;

	public PersonUpdaterAfterDomainPartCreate(RecordStorage recordStorage,
			DataGroupTermCollector termCollector, DataRecordLinkCollector linkCollector) {
		this.recordStorage = recordStorage;
		this.termCollector = termCollector;
		this.linkCollector = linkCollector;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		String recordId = extractRecordId(dataGroup);
		String personIdPartOfId = recordId.substring(0, recordId.lastIndexOf(":"));
		DataGroup readPerson = recordStorage.read(PERSON, personIdPartOfId);
		createAndAddPersonDomainPartToPerson(recordId, readPerson);

		addUpdatedInfoToPerson(dataGroup, readPerson);

		String dataDivider = extractDataDivider(readPerson);

		String metadataId = getMetadataId();
		DataGroup collectedTerms = collectTerms(readPerson, metadataId);
		DataGroup collectedLinks = linkCollector.collectLinks(metadataId, readPerson, PERSON,
				personIdPartOfId);

		recordStorage.update(PERSON, personIdPartOfId, readPerson, collectedTerms, collectedLinks,
				dataDivider);
	}

	private String extractRecordId(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private void createAndAddPersonDomainPartToPerson(String recordId, DataGroup readPerson) {
		DataGroup personDomainPartLink = DataGroupProvider
				.getDataGroupAsLinkUsingNameInDataTypeAndId(PERSON_DOMAIN_PART, PERSON_DOMAIN_PART,
						recordId);
		readPerson.addChild(personDomainPartLink);
		setNewRepeatIdForRepeatedDataGroupsToEnsureUnique(
				readPerson.getAllGroupsWithNameInData(PERSON_DOMAIN_PART));
	}

	private void setNewRepeatIdForRepeatedDataGroupsToEnsureUnique(List<DataGroup> dataGroups) {
		int counter = 0;
		for (DataGroup repeatedDataGroup : dataGroups) {
			repeatedDataGroup.setRepeatId(String.valueOf(counter));
			counter++;
		}
	}

	private void addUpdatedInfoToPerson(DataGroup dataGroup, DataGroup readPerson) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		DataGroup domainPartUpdated = recordInfo.getFirstGroupWithNameInData("updated");

		DataGroup personRecordInfo = readPerson.getFirstGroupWithNameInData(RECORD_INFO);
		// TODO: är det ett problem att faktiskt sätta samma datagrupp? Borde istället
		// informationen kopieras?
		personRecordInfo.addChild(domainPartUpdated);
		setNewRepeatIdForRepeatedDataGroupsToEnsureUnique(
				personRecordInfo.getAllGroupsWithNameInData("updated"));
	}

	private String extractDataDivider(DataGroup readPerson) {
		DataGroup recordInfo = readPerson.getFirstGroupWithNameInData(RECORD_INFO);
		DataGroup dataDividerGroup = recordInfo.getFirstGroupWithNameInData("dataDivider");
		return dataDividerGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private DataGroup collectTerms(DataGroup readPerson, String metadataId) {
		return termCollector.collectTerms(metadataId, readPerson);
	}

	private String getMetadataId() {
		DataGroup readRecordType = recordStorage.read("recordType", PERSON);
		DataGroup metadataIdLink = readRecordType.getFirstGroupWithNameInData("metadataId");
		return metadataIdLink.getFirstAtomicValueWithNameInData("linkedRecordId");
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
