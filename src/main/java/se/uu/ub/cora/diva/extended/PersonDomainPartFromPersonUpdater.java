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
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonDomainPartFromPersonUpdater implements ExtendedFunctionality {

	private static final String RECORD_INFO = "recordInfo";
	private static final String LINKED_RECORD_ID = "linkedRecordId";
	private static final String PUBLIC = "public";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private RecordStorage recordStorage;
	private DataGroupTermCollector termCollector;
	private DataRecordLinkCollector linkCollector;

	public PersonDomainPartFromPersonUpdater(RecordStorage recordStorage,
			DataGroupTermCollector termCollector, DataRecordLinkCollector linkCollector) {
		this.recordStorage = recordStorage;
		this.termCollector = termCollector;
		this.linkCollector = linkCollector;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		String metadataId = getMetadataId();
		DataGroup dataGroup = data.dataGroup;
		List<DataGroup> personDomainParts = dataGroup
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);

		possiblyChangePublicInDomainParts(metadataId, personDomainParts, dataGroup);

	}

	private String getMetadataId() {
		DataGroup readRecordType = recordStorage.read("recordType", PERSON_DOMAIN_PART);
		DataGroup metadataIdLink = readRecordType.getFirstGroupWithNameInData("metadataId");
		return metadataIdLink.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
	}

	private void possiblyChangePublicInDomainParts(String metadataId,
			List<DataGroup> personDomainParts, DataGroup dataGroup) {
		DataGroup personRecordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		DataGroup personLastUpdated = getLastUpdatedFromPerson(personRecordInfo);
		for (DataGroup domainPart : personDomainParts) {
			DataGroup readDomainPart = readDomainPartFromStorage(domainPart);
			possiblyChangeValueInDomainPart(metadataId, dataGroup, readDomainPart,
					personLastUpdated);
		}
	}

	private DataGroup getLastUpdatedFromPerson(DataGroup personRecordInfo) {
		List<DataGroup> updated = personRecordInfo.getAllGroupsWithNameInData("updated");
		return updated.get(updated.size() - 1);
	}

	private void possiblyChangeValueInDomainPart(String metadataId, DataGroup dataGroup,
			DataGroup readDomainPart, DataGroup personLastUpdated) {
		String domainPartPublicValue = extractPublicValue(readDomainPart);
		String personPublicValue = extractPublicValue(dataGroup);
		if (!personPublicValue.equals(domainPartPublicValue)) {
			changeValueInDomainPart(metadataId, personPublicValue, readDomainPart,
					personLastUpdated);
		}
	}

	private void changeValueInDomainPart(String metadataId, String personPublicValue,
			DataGroup readDomainPart, DataGroup personLastUpdated) {
		DataGroup recordInfo = readDomainPart.getFirstGroupWithNameInData(RECORD_INFO);
		changePublicValue(recordInfo, personPublicValue);
		updateUpdateInDomainPart(recordInfo, personLastUpdated);

		String domainPartId = recordInfo.getFirstAtomicValueWithNameInData("id");
		String dataDivider = extractDataDivider(recordInfo);

		DataGroup collectedTerms = termCollector.collectTerms(metadataId, readDomainPart);
		DataGroup collectedLinks = linkCollector.collectLinks(metadataId, readDomainPart,
				PERSON_DOMAIN_PART, domainPartId);
		recordStorage.update(PERSON_DOMAIN_PART, domainPartId, readDomainPart, collectedTerms,
				collectedLinks, dataDivider);
	}

	private void updateUpdateInDomainPart(DataGroup recordInfo, DataGroup personLastUpdated) {
		recordInfo.addChild(personLastUpdated);
		int i = 0;
		for (DataGroup updated : recordInfo.getAllGroupsWithNameInData("updated")) {
			updated.setRepeatId(String.valueOf(i));
			i++;
		}
	}

	private void changePublicValue(DataGroup recordInfo, String personPublicValue) {
		recordInfo.removeFirstChildWithNameInData(PUBLIC);
		recordInfo.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(PUBLIC, personPublicValue));
	}

	private DataGroup readDomainPartFromStorage(DataGroup domainPart) {
		String type = domainPart.getFirstAtomicValueWithNameInData("linkedRecordType");
		String id = domainPart.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
		return recordStorage.read(type, id);
	}

	private String extractDataDivider(DataGroup recordInfo) {
		DataGroup dataDividerGroup = recordInfo.getFirstGroupWithNameInData("dataDivider");
		return dataDividerGroup.getFirstAtomicValueWithNameInData(LINKED_RECORD_ID);
	}

	private String extractPublicValue(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData(PUBLIC);
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
