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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;

public class PersonUpdaterAfterDomainPartCreateTest {

	private PersonUpdaterAfterDomainPartCreate personUpdater;
	private RecordStorageSpy recordStorage;
	private DataGroupFactorySpy dataGroupFactory;

	@BeforeMethod
	public void setUp() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		recordStorage = new RecordStorageSpy();
		setUpPersonToReturnFromStorageSpy();
		personUpdater = new PersonUpdaterAfterDomainPartCreate(recordStorage);
	}

	private void setUpPersonToReturnFromStorageSpy() {
		DataGroupExtendedSpy person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		DataGroupExtendedSpy dataDivider = new DataGroupExtendedSpy("dataDivider");
		dataDivider.addChild(new DataAtomicSpy("linkedRecordType", "system"));
		dataDivider.addChild(new DataAtomicSpy("linkedRecordId", "testDiva"));
		recordInfo.addChild(dataDivider);
		person.addChild(recordInfo);
		createAndAddPersonDomainPart(person, "1");
		createAndAddPersonDomainPart(person, "3");
		recordStorage.returnOnRead.put("person_personId:235", person);
	}

	private void createAndAddPersonDomainPart(DataGroupExtendedSpy person, String repeatId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		domainPart.setRepeatId(repeatId);
		person.addChild(domainPart);
	}

	@Test
	public void testInit() {
		assertSame(personUpdater.getRecordStorage(), recordStorage);
	}

	@Test
	public void testUseExtendedFunctionality() {
		DataGroupSpy personDomainPart = createDataGroup("personId:235:domainPartId");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertCorrectPersonReadFromStorage();
		assertNewLinkCorrectlyCreated();

		DataGroup dataGroup = recordStorage.dataGroupsSentToUpdate.get(0);
		assertEquals(recordStorage.updatedRecordIds.get(0), "personId:235");
		assertEquals(recordStorage.updatedRecordTypes.get(0), "person");

		List<DataGroup> personDomainParts = dataGroup
				.getAllGroupsWithNameInData("personDomainPart");
		assertEquals(personDomainParts.size(), 3);
		assertSame(personDomainParts.get(2), dataGroupFactory.factoredDataGroup);

		assertEquals(personDomainParts.get(0).getRepeatId(), "0");
		assertEquals(personDomainParts.get(1).getRepeatId(), "1");
		assertEquals(personDomainParts.get(2).getRepeatId(), "2");
	}

	private void assertNewLinkCorrectlyCreated() {
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "personDomainPart");
		assertEquals(dataGroupFactory.usedRecordTypes.get(0), "personDomainPart");
		assertEquals(dataGroupFactory.usedRecordIds.get(0), "personId:235:domainPartId");
	}

	private void assertCorrectPersonReadFromStorage() {
		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:235");
	}

	private DataGroupSpy createDataGroup(String domainPartId) {
		DataGroupSpy domainPart = new DataGroupSpy("personDomainPart");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", domainPartId));
		domainPart.addChild(recordInfo);
		return domainPart;
	}

	@Test
	public void testUseExtendedFunctionalityCheckParametersSentToUpdate() {
		DataGroupSpy personDomainPart = createDataGroup("personId:235:domainPartId");

		personUpdater.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertEquals(recordStorage.dataDivider, "testDiva");
	}

}