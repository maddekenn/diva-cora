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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.beefeater.authentication.User;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.extended.person.PersonUpdaterAfterDomainPartDelete;
import se.uu.ub.cora.diva.spies.data.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupTermCollectorSpy;
import se.uu.ub.cora.diva.spies.data.DataRecordLinkCollectorSpy;
import se.uu.ub.cora.diva.spies.storage.RecordStorageSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonUpdaterAfterDomainPartDeleteTest {
	private static final String TIMESTAMP_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z";

	private DataGroupTermCollectorSpy termCollector;
	private DataRecordLinkCollectorSpy linkCollector;
	private RecordStorageSpy recordStorage;
	private PersonUpdaterAfterDomainPartDelete personUpdater;
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void setUp() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);

		recordStorage = new RecordStorageSpy();
		TestDataForPerson.setUpDefaultPersonToReturnFromStorageSpy(recordStorage, "testDiva");
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
		DataGroup domainPart = TestDataForPerson
				.createDomainPartDataGroupWithUpdated("personId:235:uu", "2");
		personUpdater.useExtendedFunctionality(createDefaultData(domainPart));

		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:235");

		DataGroup personSentToUpdate = recordStorage.dataGroupsSentToUpdate.get(0);
		assertSame(recordStorage.returnedDataGroups.get(0), personSentToUpdate);
		assertEquals(personSentToUpdate.getAllGroupsWithNameInData("personDomainPart").size(), 1);

		assertEquals(recordStorage.updatedRecordTypes.get(0), "person");
		assertEquals(recordStorage.updatedRecordIds.get(0), "personId:235");
		assertEquals(recordStorage.dataDividers.get(0), "testDiva");

		DataGroup recordInfo = personSentToUpdate.getFirstGroupWithNameInData("recordInfo");
		List<DataAtomic> domains = recordInfo.getAllDataAtomicsWithNameInData("domain");
		assertEquals(domains.size(), 1);
	}

	private ExtendedFunctionalityData createDefaultData(DataGroup dataGroup) {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = "someAuthToken";
		data.dataGroup = dataGroup;
		User user = new User("someUserId");
		data.user = user;
		return data;
	}

	@Test
	public void testUseExtendedFunctionalityCheckCollectedTermsAndLinks() {
		DataGroupExtendedSpy personDomainPart = TestDataForPerson
				.createDomainPartDataGroupWithUpdated("personId:235:kth", "2");

		personUpdater.useExtendedFunctionality(createDefaultData(personDomainPart));

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

	@Test
	public void testNewUpdatedCreatedInPerson() {
		DataGroupExtendedSpy personDomainPart = TestDataForPerson
				.createDomainPartDataGroupWithUpdated("personId:235:uu", "2");

		personUpdater.useExtendedFunctionality(createDefaultData(personDomainPart));

		DataGroup personSentToUpdate = recordStorage.dataGroupsSentToUpdate.get(0);
		DataGroup recordInfo = personSentToUpdate.getFirstGroupWithNameInData("recordInfo");
		List<DataGroup> updatedList = recordInfo.getAllGroupsWithNameInData("updated");

		List<DataGroupSpy> factoredDataGroups = dataGroupFactory.factoredDataGroups;
		assertCorrectFactoredDataGroups(factoredDataGroups);

		DataGroupSpy addedUpdated = factoredDataGroups.get(0);
		DataGroup updatedBy = addedUpdated.getFirstGroupWithNameInData("updatedBy");
		assertSame(updatedBy, factoredDataGroups.get(1));

		List<DataAtomicSpy> factoredDataAtomics = dataAtomicFactory.factoredDataAtomics;
		assertCorrectFactoredDataAtomics(factoredDataAtomics);

		assertSame(updatedBy.getFirstDataAtomicWithNameInData("linkedRecordType"),
				factoredDataAtomics.get(0));
		assertSame(updatedBy.getFirstDataAtomicWithNameInData("linkedRecordId"),
				factoredDataAtomics.get(1));

		assertEquals(updatedList.size(), 3);
		assertSame(updatedList.get(2), addedUpdated);

		assertNewRepeatIdsWereSetToEnsureUnique(updatedList);
	}

	private void assertCorrectFactoredDataGroups(List<DataGroupSpy> factoredDataGroups) {
		assertEquals(factoredDataGroups.size(), 2);
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "updated");
		assertEquals(dataGroupFactory.usedNameInDatas.get(1), "updatedBy");
	}

	private void assertCorrectFactoredDataAtomics(List<DataAtomicSpy> factoredDataAtomics) {
		assertEquals(factoredDataAtomics.size(), 3);

		assertEquals(dataAtomicFactory.usedNameInDatas.get(0), "linkedRecordType");
		assertEquals(dataAtomicFactory.usedNameInDatas.get(1), "linkedRecordId");
		assertEquals(dataAtomicFactory.usedNameInDatas.get(2), "tsUpdated");

		assertEquals(dataAtomicFactory.usedValues.get(0), "user");
		assertEquals(dataAtomicFactory.usedValues.get(1), "someUserId");
		assertTrue(dataAtomicFactory.usedValues.get(2).matches(TIMESTAMP_FORMAT));
	}

	private void assertNewRepeatIdsWereSetToEnsureUnique(List<DataGroup> updatedList) {
		for (int i = 0; i < updatedList.size(); i++) {
			assertEquals(updatedList.get(i).getRepeatId(), String.valueOf(i));
		}
	}
}
