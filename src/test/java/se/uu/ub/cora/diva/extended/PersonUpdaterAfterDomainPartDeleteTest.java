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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;

public class PersonUpdaterAfterDomainPartDeleteTest {
	private DataGroupTermCollectorSpy termCollector;
	private DataRecordLinkCollectorSpy linkCollector;
	private RecordStorageSpy recordStorage;
	private PersonUpdaterAfterDomainPartDelete personUpdater;

	@BeforeMethod
	public void setUp() {
		recordStorage = new RecordStorageSpy();
		TestDataForPerson.setUpDefaultPersonToReturnFromStorageSpy(recordStorage);
		TestDataForPerson.setUpPersonRecordTypeToReturnFromSpy(recordStorage);
		termCollector = new DataGroupTermCollectorSpy();
		linkCollector = new DataRecordLinkCollectorSpy();
		personUpdater = new PersonUpdaterAfterDomainPartDelete(recordStorage, termCollector,
				linkCollector);
	}

	@Test
	public void testInit() {
		assertSame(personUpdater.getRecordStorage(), recordStorage);
		assertSame(personUpdater.getTermCollector(), termCollector);
		assertSame(personUpdater.getLinkCollector(), linkCollector);
	}

	@Test
	public void testUseExtendedFunctionality() {
		DataGroup domainPart = TestDataForPerson.createDataGroup("personId:235:uu", "2");
		personUpdater.useExtendedFunctionality("someAuthToken", domainPart);

		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:235");

		DataGroup personSentToUpdate = recordStorage.dataGroupsSentToUpdate.get(0);
		assertSame(recordStorage.returnedDataGroups.get(0), personSentToUpdate);
		assertEquals(personSentToUpdate.getAllGroupsWithNameInData("personDomainPart").size(), 1);

		assertEquals(recordStorage.updatedRecordTypes.get(0), "person");
		assertEquals(recordStorage.updatedRecordIds.get(0), "personId:235");
		assertEquals(recordStorage.dataDividers.get(0), "testDiva");
	}

	@Test
	public void testUseExtendedFunctionalityCheckCollectedTermsAndLinks() {
		DataGroupExtendedSpy personDomainPart = TestDataForPerson
				.createDataGroup("personId:235:kth", "2");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertEquals(recordStorage.readRecordTypes.get(1), "recordType");
		assertEquals(recordStorage.readRecordIds.get(1), "person");

		assertCorrectlyCollectedTerms();
		assertCorrectCollectedLinks();
	}

	private void assertCorrectlyCollectedTerms() {
		assertEquals(termCollector.metadataGroupIds.get(0), "metadataIdForPersonType");
		assertSame(termCollector.dataGroups.get(0), recordStorage.returnedDataGroups.get(0));
		assertSame(recordStorage.collectedTermsList.get(0),
				termCollector.returnedCollectedTerms.get(0));
	}

	private void assertCorrectCollectedLinks() {
		assertEquals(linkCollector.metadataIds.get(0), "metadataIdForPersonType");
		assertEquals(linkCollector.dataGroups.get(0), recordStorage.returnedDataGroups.get(0));
		assertEquals(linkCollector.fromRecordTypes.get(0), "person");
		assertEquals(linkCollector.fromRecordIds.get(0), "personId:235");
		assertSame(recordStorage.linkLists.get(0), linkCollector.returnedCollectedLinks.get(0));
	}

	// TODO: för att detta ska funka måste vi ha tillgång till en user att sätta som updatedBy
	// vi har inget block att kopiera eftersom domainParten är borttagen
	// @Test
	// public void testNewUpdatedCreatedInPerson() {
	// DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:uu");
	//
	// personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);
	//
	// DataGroup personSentToUpdate = recordStorage.dataGroupsSentToUpdate.get(0);
	// DataGroup recordInfo = personSentToUpdate.getFirstGroupWithNameInData("recordInfo");
	// List<DataGroup> updatedList = recordInfo.getAllGroupsWithNameInData("updated");
	// assertEquals(updatedList.size(), 3);
	// DataGroup domainPartRecordInfo = personDomainPart.getFirstGroupWithNameInData("recordInfo");
	//
	// assertNewRepeatIdsAreSetToEnsureUnique(updatedList);
	// }
	//
	// private void assertNewRepeatIdsAreSetToEnsureUnique(List<DataGroup> updatedList) {
	// for (int i = 0; i < updatedList.size(); i++) {
	// assertEquals(updatedList.get(i).getRepeatId(), String.valueOf(i));
	// }
	// }
}
