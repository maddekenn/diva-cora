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
import se.uu.ub.cora.storage.RecordStorage;

public class PersonDomainPartFromPersonUpdater implements ExtendedFunctionality {

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
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		String metadataId = getMetadataId();
		List<DataGroup> personDomainParts = dataGroup
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);
		String personPublicValue = extractPublicValue(dataGroup);
		for (DataGroup domainPart : personDomainParts) {
			DataGroup readDomainPart = readDomainPartFromStorage(domainPart);

			String domainPartPublicValue = extractPublicValue(readDomainPart);
			if (!personPublicValue.equals(domainPartPublicValue)) {
				DataGroup recordInfo = readDomainPart.getFirstGroupWithNameInData("recordInfo");
				String domainPartId = recordInfo.getFirstAtomicValueWithNameInData("id");
				String dataDivider = extractDataDivider(recordInfo);

				DataGroup collectedTerms = termCollector.collectTerms(metadataId, readDomainPart);
				DataGroup collectedLinks = linkCollector.collectLinks(metadataId, readDomainPart,
						PERSON_DOMAIN_PART, domainPartId);
				recordStorage.update(PERSON_DOMAIN_PART, domainPartId, readDomainPart,
						collectedTerms, collectedLinks, dataDivider);
			}
		}
		// läs upp alla länkade personDomainParts

		// kolla om public är olik public i person
		// om olik, uppdatera värdet i personDomainPart datagruppen och
		// uppdatera i storage

	}

	private String getMetadataId() {
		DataGroup readRecordType = recordStorage.read("recordType", PERSON_DOMAIN_PART);
		DataGroup metadataIdLink = readRecordType.getFirstGroupWithNameInData("metadataId");
		return metadataIdLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private DataGroup readDomainPartFromStorage(DataGroup domainPart) {
		String type = domainPart.getFirstAtomicValueWithNameInData("linkedRecordType");
		String id = domainPart.getFirstAtomicValueWithNameInData("linkedRecordId");
		return recordStorage.read(type, id);
	}

	private String extractDataDivider(DataGroup recordInfo) {
		DataGroup dataDividerGroup = recordInfo.getFirstGroupWithNameInData("dataDivider");
		return dataDividerGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private String extractPublicValue(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("public");
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
