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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartPersonSynchronizerTest {

	private PersonDomainPartPersonSynchronizer synchronizer;
	private String authToken = "someAuthToken";
	private RecordStorageSpy recordStorage;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void setUp() {
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		recordStorage = new RecordStorageSpy();
		synchronizer = new PersonDomainPartPersonSynchronizer(recordStorage);
	}

	@Test
	public void testInit() {
		assertSame(synchronizer.getRecordStorage(), recordStorage);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "No person exists with record id personId:123. PersonDomainPart was not created. "
			+ "Error from record storage spy")
	public void testExtendedFunctionalityNoPersonPresent() {
		recordStorage.throwRecordNotFoundException = true;
		DataGroupExtendedSpy domainPart = createDataGroup("someDomain", "personId:123");
		synchronizer.useExtendedFunctionality(createDefaultData(domainPart));
	}

	private DataGroupExtendedSpy createDataGroup(String domain, String personId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("domain", domain));
		domainPart.addChild(recordInfo);
		DataGroupExtendedSpy personLink = new DataGroupExtendedSpy("personLink");
		personLink.addChild(new DataAtomicSpy("linkedRecordId", personId));
		domainPart.addChild(personLink);
		return domainPart;
	}

	private ExtendedFunctionalityData createDefaultData(DataGroup dataGroup) {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = authToken;
		data.dataGroup = dataGroup;
		return data;
	}

	@Test
	public void testExtendedFunctionalityPersonDomainPartCompleted() {
		createAndSetPersonUsingPublicValue("true");
		DataGroupExtendedSpy domainPart = createDataGroup("someDomain", "personId:123");
		synchronizer.useExtendedFunctionality(createDefaultData(domainPart));

		DataAtomicSpy factoredRecordId = dataAtomicFactorySpy.factoredDataAtomics.get(0);
		DataGroup recordInfo = domainPart.getFirstGroupWithNameInData("recordInfo");
		DataAtomic addedRecordId = recordInfo.getFirstDataAtomicWithNameInData("id");
		assertSame(addedRecordId, factoredRecordId);
		assertEquals(addedRecordId.getValue(), "personId:123:someDomain");

		assertFalse(domainPart.containsChildWithNameInData("personLink"));
	}

	@Test
	public void testExtendedFunctionalityPersonExists() {
		createAndSetPersonUsingPublicValue("true");
		DataGroupExtendedSpy domainPart = createDataGroup("someDomain", "personId:123");
		synchronizer.useExtendedFunctionality(createDefaultData(domainPart));

		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:123");
	}

	@Test
	public void testPublicFalseIsCopiedFromPersonToDomainPart() {
		String publicValue = "false";
		createAndSetPersonUsingPublicValue(publicValue);
		DataGroupExtendedSpy domainPart = createDataGroup("someDomain", "personId:123");
		assertNoPublicInDomainPartBefore(domainPart);

		synchronizer.useExtendedFunctionality(createDefaultData(domainPart));

		assertPublicValueCopiedFromPersonToDomainPart(publicValue, domainPart);
	}

	private void assertNoPublicInDomainPartBefore(DataGroupExtendedSpy domainPart) {
		DataGroup recordInfo = domainPart.getFirstGroupWithNameInData("recordInfo");
		assertFalse(recordInfo.containsChildWithNameInData("public"));
	}

	private void createAndSetPersonUsingPublicValue(String publicValue) {
		DataGroupExtendedSpy person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy personRecordInfo = new DataGroupExtendedSpy("recordInfo");
		personRecordInfo.addChild(new DataAtomicSpy("public", publicValue));
		person.addChild(personRecordInfo);
		recordStorage.returnOnRead.put("person_personId:123", person);
	}

	private void assertPublicValueCopiedFromPersonToDomainPart(String publicValue,
			DataGroupExtendedSpy domainPart) {
		assertEquals(dataAtomicFactorySpy.usedNameInDatas.get(1), "public");
		assertEquals(dataAtomicFactorySpy.usedValues.get(1), publicValue);

		assertSame(domainPart.getFirstGroupWithNameInData("recordInfo").getFirstChildWithNameInData(
				"public"), dataAtomicFactorySpy.factoredDataAtomics.get(1));
	}

	@Test
	public void testPublicTrueIsCopiedFromPersonToDomainPart() {
		createAndSetPersonUsingPublicValue("true");
		DataGroupExtendedSpy domainPart = createDataGroup("someDomain", "personId:123");
		assertNoPublicInDomainPartBefore(domainPart);

		synchronizer.useExtendedFunctionality(createDefaultData(domainPart));

		assertPublicValueCopiedFromPersonToDomainPart("true", domainPart);
	}

}
