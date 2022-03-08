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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.beefeater.authentication.User;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartValidator;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartValidatorTest {

	private DataGroupExtendedSpy domainPart;
	private ExtendedFunctionality validator;

	@BeforeMethod
	public void setUp() {
		domainPart = TestDataForPerson.createDomainPartDataGroupWithUpdated("personId:235:uu", "2");
		validator = new PersonDomainPartValidator();

	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "Data is not valid. One of identifier or affiliation must be present.")
	public void testExtendedFunctionalityMissingMandatoryValues() {
		removeMandatoryValues(domainPart);
		validator.useExtendedFunctionality(createDefaultData(domainPart));
	}

	private void removeMandatoryValues(DataGroup domainPart) {
		domainPart.removeFirstChildWithNameInData("identifier");
		domainPart.removeFirstChildWithNameInData("affiliation");
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
	public void testExtendedFunctionalityIdentifierPresent() {
		domainPart.removeFirstChildWithNameInData("affiliation");
		validator.useExtendedFunctionality(createDefaultData(domainPart));
		assertEquals(domainPart.nameInDatasRequestedFromContains.get(0), "identifier");
		assertEquals(domainPart.nameInDatasRequestedFromContains.size(), 1);
	}

	@Test
	public void testExtendedFunctionalityAffiliationPresent() {
		domainPart.removeFirstChildWithNameInData("identifier");
		validator.useExtendedFunctionality(createDefaultData(domainPart));
		assertEquals(domainPart.nameInDatasRequestedFromContains.get(0), "identifier");
		assertEquals(domainPart.nameInDatasRequestedFromContains.get(1), "affiliation");
		assertEquals(domainPart.nameInDatasRequestedFromContains.size(), 2);
	}

}
