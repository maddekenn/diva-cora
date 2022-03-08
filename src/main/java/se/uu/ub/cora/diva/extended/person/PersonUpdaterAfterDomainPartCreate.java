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

import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.extended.ExtendedFunctionalityUtils;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonUpdaterAfterDomainPartCreate implements ExtendedFunctionality {

	private static final String DOMAIN = "domain";
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
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup dataGroup = data.dataGroup;
		String recordId = extractRecordId(dataGroup);
		String personIdPartOfId = recordId.substring(0, recordId.lastIndexOf(':'));

		DataGroup readPerson = recordStorage.read(PERSON, personIdPartOfId);
		createAndAddPersonDomainPartToPerson(recordId, readPerson);

		addInfoFromDomainPartToPerson(dataGroup, readPerson, recordId);

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
		ExtendedFunctionalityUtils.setNewRepeatIdsToEnsureUnique(readPerson, PERSON_DOMAIN_PART);
	}

	private void addInfoFromDomainPartToPerson(DataGroup dataGroup, DataGroup readPerson,
			String recordId) {
		DataGroup domainPartUpdated = extractUpdated(dataGroup);

		DataGroup personRecordInfo = readPerson.getFirstGroupWithNameInData(RECORD_INFO);
		personRecordInfo.addChild(domainPartUpdated);
		ExtendedFunctionalityUtils.setNewRepeatIdsToEnsureUnique(personRecordInfo, "updated");

		possiblyAddDomainToPerson(recordId, personRecordInfo);
	}

	private DataGroup extractUpdated(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstGroupWithNameInData("updated");
	}

	private void possiblyAddDomainToPerson(String recordId, DataGroup personRecordInfo) {
		String domainPartOfId = recordId.substring(recordId.lastIndexOf(':') + 1);
		boolean domainAlreadyExist = domainAlreadyExists(personRecordInfo, domainPartOfId);
		if (!domainAlreadyExist) {
			createAndAddDomainInPerson(personRecordInfo, domainPartOfId);
			int counter = 0;
			for (DataAtomic repeatedDataGroup : personRecordInfo
					.getAllDataAtomicsWithNameInData(DOMAIN)) {
				repeatedDataGroup.setRepeatId(String.valueOf(counter));
				counter++;
			}
		}
	}

	private boolean domainAlreadyExists(DataGroup personRecordInfo, String domainPartOfId) {
		List<DataAtomic> existingDomains = personRecordInfo.getAllDataAtomicsWithNameInData(DOMAIN);
		for (DataAtomic existingDomain : existingDomains) {
			if (existingDomain.getValue().equals(domainPartOfId)) {
				return true;
			}
		}
		return false;
	}

	private void createAndAddDomainInPerson(DataGroup personRecordInfo, String domainPartOfId) {
		DataAtomic domain = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(DOMAIN,
				domainPartOfId);
		personRecordInfo.addChild(domain);
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
