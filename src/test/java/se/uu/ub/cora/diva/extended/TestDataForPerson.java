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

import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;

public class TestDataForPerson {

	public static void setUpPersonRecordTypeToReturnFromSpy(RecordStorageSpy recordStorage) {
		DataGroupExtendedSpy personRecordType = new DataGroupExtendedSpy("recordType");
		DataGroupExtendedSpy metadataId = new DataGroupExtendedSpy("metadataId");
		metadataId.addChild(new DataAtomicSpy("linkedRecordId", "metadataIdForPersonType"));
		personRecordType.addChild(metadataId);
		recordStorage.returnOnRead.put("recordType_person", personRecordType);
	}

	public static void setUpDefaultPersonToReturnFromStorageSpy(RecordStorageSpy recordStorage,
			String dataDivider) {
		DataGroupExtendedSpy person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy recordInfo = TestDataForPerson.createRecordInfo(dataDivider);
		TestDataForPerson.createAndAddUpdated(recordInfo, "1");
		TestDataForPerson.createAndAddUpdated(recordInfo, "4");
		recordInfo.addChild(new DataAtomicSpy("domain", "uu", "1"));
		recordInfo.addChild(new DataAtomicSpy("domain", "kth", "3"));
		person.addChild(recordInfo);
		TestDataForPerson.createAndAddPersonDomainPart(person, "personId:235:uu", "1");
		TestDataForPerson.createAndAddPersonDomainPart(person, "personId:235:kth", "3");
		recordStorage.returnOnRead.put("person_personId:235", person);
	}

	public static void createAndAddUpdated(DataGroupExtendedSpy recordInfo, String repeatId) {
		DataGroupExtendedSpy updated = new DataGroupExtendedSpy("updated");
		updated.setRepeatId(repeatId);
		recordInfo.addChild(updated);
	}

	public static DataGroupExtendedSpy createDataDivider(String dataDivider) {
		DataGroupExtendedSpy dataDividerGroup = new DataGroupExtendedSpy("dataDivider");
		dataDividerGroup.addChild(new DataAtomicSpy("linkedRecordType", "system"));
		dataDividerGroup.addChild(new DataAtomicSpy("linkedRecordId", dataDivider));
		return dataDividerGroup;
	}

	public static DataGroupExtendedSpy createRecordInfo(String dataDivider) {
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		DataGroupExtendedSpy dataDividerGroup = createDataDivider(dataDivider);
		recordInfo.addChild(dataDividerGroup);
		return recordInfo;
	}

	public static void createAndAddPersonDomainPart(DataGroupExtendedSpy person,
			String linkedRecordId, String repeatId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		domainPart.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		domainPart.setRepeatId(repeatId);
		person.addChild(domainPart);
	}

	public static DataGroupExtendedSpy createDomainPartDataGroupWithUpdated(String domainPartId,
			String repeatId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", domainPartId));
		domainPart.addChild(recordInfo);
		TestDataForPerson.createAndAddUpdated(recordInfo, repeatId);
		domainPart.addChild(new DataAtomicSpy("identifier", "3456", "0"));

		DataGroupExtendedSpy affiliation = new DataGroupExtendedSpy("affiliation");
		DataGroupExtendedSpy organisationLink = new DataGroupExtendedSpy("organisationLink");
		organisationLink.addChild(new DataAtomicSpy("linkedRecordType", "organisation"));
		organisationLink.addChild(new DataAtomicSpy("linkedRecordId", "678"));
		affiliation.addChild(organisationLink);
		affiliation.setRepeatId("0");
		domainPart.addChild(affiliation);

		return domainPart;
	}
}
