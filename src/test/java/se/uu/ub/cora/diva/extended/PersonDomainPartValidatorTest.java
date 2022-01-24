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

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.RecordStorageSpy;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartValidatorTest {

	private PersonDomainPartValidator validator;
	private String authToken = "someAuthToken";
	private RecordStorageSpy recordStorage;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void setUp() {
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		recordStorage = new RecordStorageSpy();
		validator = new PersonDomainPartValidator(recordStorage);
	}

	@Test
	public void testInit() {
		assertSame(validator.getRecordStorage(), recordStorage);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "No person exists with record id personId:123. PersonDomainPart was not created. "
			+ "Error from record storage spy")
	public void testExtendedFunctionalityNoPersonPresent() {
		recordStorage.throwRecordNotFoundException = true;
		DataGroupExtendedSpy domainPart = createDataGroup("personId:123:someDomain");
		validator.useExtendedFunctionality(authToken, domainPart);
	}

	private DataGroupExtendedSpy createDataGroup(String domainPartId) {
		DataGroupExtendedSpy domainPart = new DataGroupExtendedSpy("personDomainPart");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", domainPartId));
		domainPart.addChild(recordInfo);
		return domainPart;
	}

	@Test
	public void testExtendedFunctionalityPersonExists() {
		createAndSetPersonUsingPublicValue("true");
		DataGroupExtendedSpy domainPart = createDataGroup("personId:123:someDomain");
		validator.useExtendedFunctionality(authToken, domainPart);

		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:123");
	}

	@Test
	public void testPublicFalseIsCopiedFromPersonToDomainPart() {
		String publicValue = "false";
		createAndSetPersonUsingPublicValue(publicValue);
		DataGroupExtendedSpy domainPart = createDataGroup("personId:123:someDomain");
		assertNoPublicInDomainPartBefore(domainPart);

		validator.useExtendedFunctionality(authToken, domainPart);

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
		assertEquals(dataAtomicFactorySpy.usedNameInDatas.get(0), "public");
		assertEquals(dataAtomicFactorySpy.usedValues.get(0), publicValue);

		assertSame(domainPart.getFirstGroupWithNameInData("recordInfo").getFirstChildWithNameInData(
				"public"), dataAtomicFactorySpy.factoredDataAtomics.get(0));
	}

	@Test
	public void testPublicTrueIsCopiedFromPersonToDomainPart() {
		createAndSetPersonUsingPublicValue("true");
		DataGroupExtendedSpy domainPart = createDataGroup("personId:123:someDomain");
		assertNoPublicInDomainPartBefore(domainPart);

		validator.useExtendedFunctionality(authToken, domainPart);

		assertPublicValueCopiedFromPersonToDomainPart("true", domainPart);
	}

}
