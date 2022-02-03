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

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonDomainPartFromPersonUpdaterTest {

	private DataGroupExtendedSpy person;
	private PersonDomainPartFromPersonUpdater functionality;
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

		createPersonDataGroup();
		recordStorage = new RecordStorageSpy();
		createAndSetDomainPartsToReturnFromStorage();

		DataGroupExtendedSpy personDomainPartRecordType = new DataGroupExtendedSpy("recordType");
		DataGroupExtendedSpy metadataId = new DataGroupExtendedSpy("metadataId");
		metadataId.addChild(new DataAtomicSpy("linkedRecordId", "metadataIdForPersonDomainPart"));
		personDomainPartRecordType.addChild(metadataId);

		recordStorage.returnOnRead.put("recordType_personDomainPart", personDomainPartRecordType);
		termCollector = new DataGroupTermCollectorSpy();
		linkCollector = new DataRecordLinkCollectorSpy();

		functionality = new PersonDomainPartFromPersonUpdater(recordStorage, termCollector,
				linkCollector);
	}

	private void createAndSetDomainPartsToReturnFromStorage() {
		createAndSetDomainPartInStorage("no", "test", "testDiva");
		createAndSetDomainPartInStorage("no", "uu", "testDiva");
		createAndSetDomainPartInStorage("yes", "kth", "testDiva");
		createAndSetDomainPartInStorage("yes", "liu", "testSystem");
	}

	private void createAndSetDomainPartInStorage(String publicValue, String domainId,
			String dataDivider) {
		String id = "personDomainPart_authority-person:106:" + domainId;
		DataGroupExtendedSpy personDomainPart = createDomainPartDataGroup(id, publicValue,
				dataDivider);
		recordStorage.returnOnRead.put(id, personDomainPart);
	}

	private DataGroupExtendedSpy createDomainPartDataGroup(String id, String publicValue,
			String dataDivider) {
		DataGroupExtendedSpy personDomainPart = new DataGroupExtendedSpy("personDomainPart");
		DataGroupExtendedSpy recordInfo = createRecordInfoWothPublicValue(publicValue);
		recordInfo.addChild(new DataAtomicSpy("id", id));
		recordInfo.addChild(createDataDivider(dataDivider));
		createAndAddUpdated(recordInfo, "1");
		createAndAddUpdated(recordInfo, "3");
		personDomainPart.addChild(recordInfo);
		return personDomainPart;
	}

	private DataGroupExtendedSpy createDataDivider(String dataDivider) {
		DataGroupExtendedSpy dataDividerGroup = new DataGroupExtendedSpy("dataDivider");
		dataDividerGroup.addChild(new DataAtomicSpy("linkedRecordType", "system"));
		dataDividerGroup.addChild(new DataAtomicSpy("linkedRecordId", dataDivider));
		return dataDividerGroup;
	}

	private void createAndAddUpdated(DataGroupExtendedSpy recordInfo, String repeatId) {
		DataGroupExtendedSpy updated = new DataGroupExtendedSpy("updated");
		updated.setRepeatId(repeatId);
		recordInfo.addChild(updated);
	}

	@Test
	public void testInit() {
		assertSame(functionality.getRecordStorage(), recordStorage);
		assertSame(functionality.getTermCollector(), termCollector);
		assertSame(functionality.getLinkCollector(), linkCollector);
	}

	private void createPersonDataGroup() {
		person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy recordInfo = createRecordInfoWothPublicValue("no");
		createAndAddUpdated(recordInfo, "11");
		createAndAddUpdated(recordInfo, "12");
		person.addChild(recordInfo);
	}

	private DataGroupExtendedSpy createRecordInfoWothPublicValue(String publicValue) {
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("public", publicValue));
		return recordInfo;
	}

	@Test
	public void testPersonDomainPartRecordTypeIsRead() {
		functionality.useExtendedFunctionality(createDefaultData(person));
		assertEquals(recordStorage.readRecordTypes.size(), 1);
		assertEquals(recordStorage.readRecordTypes.get(0), "recordType");
		assertEquals(recordStorage.readRecordIds.get(0), "personDomainPart");
	}

	private ExtendedFunctionalityData createDefaultData(DataGroup dataGroup) {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = "someAuthToken";
		data.dataGroup = dataGroup;
		return data;
	}

	@Test
	public void testPersonDomainPartUpdateNoDomainParts() {
		functionality.useExtendedFunctionality(createDefaultData(person));
		assertEquals(recordStorage.readRecordTypes.size(), 1);
	}

	@Test
	public void testPersonDomainPartUpdateDomainPartsSameValuePublic() {
		createAndAddPersonDomainPartToPerson("1", "authority-person:106:test");
		createAndAddPersonDomainPartToPerson("3", "authority-person:106:uu");

		functionality.useExtendedFunctionality(createDefaultData(person));

		assertEquals(recordStorage.readRecordTypes.size(), 3);
		assertCorrectReadPersonDomainParts();

		assertEquals(recordStorage.dataGroupsSentToUpdate.size(), 0);
	}

	private void assertCorrectReadPersonDomainParts() {
		assertEquals(recordStorage.readRecordTypes.get(1), "personDomainPart");
		assertEquals(recordStorage.readRecordTypes.get(2), "personDomainPart");
		assertEquals(recordStorage.readRecordIds.get(1), "authority-person:106:test");
		assertEquals(recordStorage.readRecordIds.get(2), "authority-person:106:uu");
	}

	private void createAndAddPersonDomainPartToPerson(String repeatId, String linkedRecordId) {
		DataGroupExtendedSpy personDomainPart = new DataGroupExtendedSpy("personDomainPart");
		personDomainPart.setRepeatId(repeatId);
		personDomainPart.addChild(new DataAtomicSpy("linkedRecordType", "personDomainPart"));
		personDomainPart.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		person.addChild(personDomainPart);
	}

	@Test
	public void testPersonDomainPartUpdateDomainPartsTwoDifferentOneSameValuePublic() {
		addThreeDomainParts();

		functionality.useExtendedFunctionality(createDefaultData(person));

		assertEquals(recordStorage.readRecordTypes.size(), 4);
		assertEquals(recordStorage.dataGroupsSentToUpdate.size(), 2);

		assertCorrectParametersSentToUpdate(0, "liu", "testSystem");
		assertSame(recordStorage.dataGroupsSentToUpdate.get(0),
				recordStorage.returnedDataGroups.get(1));

		assertCorrectParametersSentToUpdate(1, "kth", "testDiva");
		assertSame(recordStorage.dataGroupsSentToUpdate.get(1),
				recordStorage.returnedDataGroups.get(3));
	}

	@Test
	public void testUpdateDomainPartsTwoDifferentOneSameValuePublicCheckChangedValues() {
		addThreeDomainParts();

		functionality.useExtendedFunctionality(createDefaultData(person));

		assertPublicValueWasChanged(0);
		assertUpdatedWasAddedCorrectly(recordStorage.dataGroupsSentToUpdate.get(0));

		assertPublicValueWasChanged(1);
		assertUpdatedWasAddedCorrectly(recordStorage.dataGroupsSentToUpdate.get(1));
	}

	private void assertUpdatedWasAddedCorrectly(DataGroup updatedDomainPart) {
		DataGroup recordInfo = updatedDomainPart.getFirstGroupWithNameInData("recordInfo");
		List<DataGroup> updatedGroups = recordInfo.getAllGroupsWithNameInData("updated");
		for (var i = 0; i < updatedGroups.size(); i++) {
			assertEquals(updatedGroups.get(i).getRepeatId(), String.valueOf(i));
		}

		DataGroup recordInfoPerson = person.getFirstGroupWithNameInData("recordInfo");
		List<DataGroup> personUpdated = recordInfoPerson.getAllGroupsWithNameInData("updated");
		assertSame(updatedGroups.get(updatedGroups.size() - 1),
				personUpdated.get(personUpdated.size() - 1));
	}

	private void assertPublicValueWasChanged(int index) {
		DataGroup updatedDomainPart = recordStorage.dataGroupsSentToUpdate.get(index);
		DataGroup recordInfo = updatedDomainPart.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("public"), "no");
	}

	private void addThreeDomainParts() {
		createAndAddPersonDomainPartToPerson("1", "authority-person:106:liu");
		createAndAddPersonDomainPartToPerson("3", "authority-person:106:uu");
		createAndAddPersonDomainPartToPerson("4", "authority-person:106:kth");
	}

	@Test
	public void testUpdatePersonDomainPartCollectedTermsUsedCorrectly() {
		addThreeDomainParts();

		functionality.useExtendedFunctionality(createDefaultData(person));
		assertCorrectlyCollectedTerms(0, 1);
		assertCorrectlyCollectedTerms(1, 3);
		assertCorrectCollectedLinks(0, 1, "liu");
		assertCorrectCollectedLinks(1, 3, "kth");
	}

	private void assertCorrectlyCollectedTerms(int index, int readDataGroupIndex) {
		assertEquals(termCollector.metadataGroupIds.get(index), "metadataIdForPersonDomainPart");
		assertSame(termCollector.dataGroups.get(index),
				recordStorage.returnedDataGroups.get(readDataGroupIndex));

		assertSame(recordStorage.collectedTermsList.get(index),
				termCollector.returnedCollectedTerms.get(index));
	}

	private void assertCorrectCollectedLinks(int index, int readDataGroupIndex, String domain) {
		assertEquals(linkCollector.metadataIds.get(index), "metadataIdForPersonDomainPart");
		assertEquals(linkCollector.dataGroups.get(index),
				recordStorage.returnedDataGroups.get(readDataGroupIndex));

		assertEquals(linkCollector.fromRecordTypes.get(index), "personDomainPart");
		assertEquals(linkCollector.fromRecordIds.get(index),
				"personDomainPart_authority-person:106:" + domain);

		assertSame(recordStorage.linkLists.get(index),
				linkCollector.returnedCollectedLinks.get(index));
	}

	private void assertCorrectParametersSentToUpdate(int index, String domain, String dataDivider) {
		assertEquals(recordStorage.updatedRecordTypes.get(index), "personDomainPart");
		assertEquals(recordStorage.updatedRecordIds.get(index),
				"personDomainPart_authority-person:106:" + domain);
		assertEquals(recordStorage.dataDividers.get(index), dataDivider);
	}

}
