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

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;

public class PersonUpdaterAfterDomainPartCreateTest {

	private PersonUpdaterAfterDomainPartCreate personUpdater;
	private RecordStorageSpy recordStorage;
	private DataGroupFactorySpy dataGroupFactory;
	private DataGroupTermCollectorSpy termCollector;
	private DataRecordLinkCollectorSpy linkCollector;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void setUp() {
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		recordStorage = new RecordStorageSpy();
		setUpDataToReturnFromStorageSpy();

		termCollector = new DataGroupTermCollectorSpy();
		linkCollector = new DataRecordLinkCollectorSpy();
		personUpdater = new PersonUpdaterAfterDomainPartCreate(recordStorage, termCollector,
				linkCollector);
	}

	private void setUpDataToReturnFromStorageSpy() {
		setUpPersonToReturnFromStorageSpy();
		DataGroupExtendedSpy personRecordType = new DataGroupExtendedSpy("recordType");
		DataGroupExtendedSpy metadataId = new DataGroupExtendedSpy("metadataId");
		metadataId.addChild(new DataAtomicSpy("linkedRecordId", "metadataIdForPersonType"));
		personRecordType.addChild(metadataId);
		recordStorage.returnOnRead.put("recordType_person", personRecordType);
	}

	private void setUpPersonToReturnFromStorageSpy() {
		DataGroupExtendedSpy person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy recordInfo = createRecordInfo();
		createAndAddUpdated(recordInfo, "1");
		createAndAddUpdated(recordInfo, "4");
		recordInfo.addChild(new DataAtomicSpy("domain", "uu", "1"));
		recordInfo.addChild(new DataAtomicSpy("domain", "kth", "3"));
		person.addChild(recordInfo);
		createAndAddPersonDomainPart(person, "1");
		createAndAddPersonDomainPart(person, "3");
		recordStorage.returnOnRead.put("person_personId:235", person);
	}

	private void createAndAddUpdated(DataGroupExtendedSpy recordInfo, String repeatId) {
		DataGroupExtendedSpy updated = new DataGroupExtendedSpy("updated");
		updated.setRepeatId(repeatId);
		recordInfo.addChild(updated);
	}

	private DataGroupExtendedSpy createRecordInfo() {
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		DataGroupExtendedSpy dataDivider = createDataDivider();
		recordInfo.addChild(dataDivider);
		return recordInfo;
	}

	private DataGroupExtendedSpy createDataDivider() {
		DataGroupExtendedSpy dataDivider = new DataGroupExtendedSpy("dataDivider");
		dataDivider.addChild(new DataAtomicSpy("linkedRecordType", "system"));
		dataDivider.addChild(new DataAtomicSpy("linkedRecordId", "testDiva"));
		return dataDivider;
	}

	private void createAndAddPersonDomainPart(DataGroupExtendedSpy person, String repeatId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		domainPart.setRepeatId(repeatId);
		person.addChild(domainPart);
	}

	@Test
	public void testInit() {
		assertSame(personUpdater.getRecordStorage(), recordStorage);
		assertSame(personUpdater.getTermCollector(), termCollector);
		assertSame(personUpdater.getLinkCollector(), linkCollector);
	}

	@Test
	public void testUseExtendedFunctionality() {
		DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:someDomain");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertCorrectPersonReadFromStorage();
		assertNewDomainPartLinkCorrectlyCreated();

		DataGroup dataGroup = recordStorage.dataGroupsSentToUpdate.get(0);
		assertEquals(recordStorage.updatedRecordIds.get(0), "personId:235");
		assertEquals(recordStorage.updatedRecordTypes.get(0), "person");
		assertSame(dataGroup, recordStorage.returnedDataGroups.get(0));
		assertEquals(recordStorage.dataDivider, "testDiva");

		assertNewDomainPartLinkAddedCorrectly(dataGroup);
	}

	private void assertCorrectPersonReadFromStorage() {
		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:235");
	}

	private void assertNewDomainPartLinkCorrectlyCreated() {
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "personDomainPart");
		assertEquals(dataGroupFactory.usedRecordTypes.get(0), "personDomainPart");
		assertEquals(dataGroupFactory.usedRecordIds.get(0), "personId:235:someDomain");
	}

	private void assertNewDomainPartLinkAddedCorrectly(DataGroup dataGroup) {
		List<DataGroup> personDomainParts = dataGroup
				.getAllGroupsWithNameInData("personDomainPart");
		assertEquals(personDomainParts.size(), 3);
		assertSame(personDomainParts.get(2), dataGroupFactory.factoredDataGroup);

		assertEquals(personDomainParts.get(0).getRepeatId(), "0");
		assertEquals(personDomainParts.get(1).getRepeatId(), "1");
		assertEquals(personDomainParts.get(2).getRepeatId(), "2");
	}

	private DataGroupExtendedSpy createDataGroup(String domainPartId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", domainPartId));
		domainPart.addChild(recordInfo);
		createAndAddUpdated(recordInfo, "2");

		return domainPart;
	}

	@Test
	public void testNewDomainAddedToListOfDomainsInPerson() {
		DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:someDomain");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertEquals(dataAtomicFactorySpy.usedNameInDatas.get(0), "domain");
		assertEquals(dataAtomicFactorySpy.usedValues.get(0), "someDomain");

		DataGroup updatedPerson = recordStorage.dataGroupsSentToUpdate.get(0);
		DataGroup recordInfo = updatedPerson.getFirstGroupWithNameInData("recordInfo");
		List<DataAtomic> domains = recordInfo.getAllDataAtomicsWithNameInData("domain");
		assertSame(domains.get(2), dataAtomicFactorySpy.factoredDataAtomic);
		assertEquals(domains.size(), 3);

		assertNewRepeatIdsWereSet(domains);
	}

	private void assertNewRepeatIdsWereSet(List<DataAtomic> domains) {
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getRepeatId(), String.valueOf(i));
		}
	}

	@Test
	public void testAlreadyExistingDomainNotAddedToPerson() {
		DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:kth");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);
		DataGroup updatedPerson = recordStorage.dataGroupsSentToUpdate.get(0);
		DataGroup recordInfo = updatedPerson.getFirstGroupWithNameInData("recordInfo");
		List<DataAtomic> domains = recordInfo.getAllDataAtomicsWithNameInData("domain");
		assertEquals(domains.size(), 2);
		assertEquals(dataAtomicFactorySpy.factoredDataAtomics.size(), 0);

	}

	@Test
	public void testUpdatedCopiedFromDomainPartToPerson() {
		DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:someDomain");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		DataGroup personSentToUpdate = recordStorage.dataGroupsSentToUpdate.get(0);
		DataGroup recordInfo = personSentToUpdate.getFirstGroupWithNameInData("recordInfo");
		List<DataGroup> updatedList = recordInfo.getAllGroupsWithNameInData("updated");
		assertEquals(updatedList.size(), 3);
		DataGroup domainPartRecordInfo = personDomainPart.getFirstGroupWithNameInData("recordInfo");
		assertSame(updatedList.get(2), domainPartRecordInfo.getFirstGroupWithNameInData("updated"));

		assertNewRepeatIdsAreSetToEnsureUnique(updatedList);
	}

	private void assertNewRepeatIdsAreSetToEnsureUnique(List<DataGroup> updatedList) {
		for (int i = 0; i < updatedList.size(); i++) {
			assertEquals(updatedList.get(i).getRepeatId(), String.valueOf(i));
		}
	}

	@Test
	public void testUseExtendedFunctionalityCheckCollectedTermsAndLinks() {
		DataGroupExtendedSpy personDomainPart = createDataGroup("personId:235:someDomain");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertEquals(recordStorage.readRecordTypes.get(1), "recordType");
		assertEquals(recordStorage.readRecordIds.get(1), "person");

		assertCorrectlyCollectedTerms();
		assertCorrectCollectedLinks();
	}

	private void assertCorrectlyCollectedTerms() {
		assertEquals(termCollector.metadataGroupId, "metadataIdForPersonType");
		assertSame(termCollector.dataGroup, recordStorage.returnedDataGroups.get(0));
		assertSame(recordStorage.collectedTerms, termCollector.returnedCollectedTerms);
	}

	private void assertCorrectCollectedLinks() {
		assertEquals(linkCollector.metadataId, "metadataIdForPersonType");
		assertEquals(linkCollector.dataGroup, recordStorage.returnedDataGroups.get(0));
		assertEquals(linkCollector.fromRecordType, "person");
		assertEquals(linkCollector.fromRecordId, "personId:235");
		assertSame(recordStorage.linkList, linkCollector.collectedLinks);
	}

}