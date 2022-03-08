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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.extended.person.PersonDomainPartLocalIdDeletePreventer;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartLocalIdDeletePreventerTest {
	private ExtendedFunctionality preventer;
	private DataGroupExtendedSpy domainPart;

	@BeforeMethod
	public void beforeMethod() {
		preventer = new PersonDomainPartLocalIdDeletePreventer();
		domainPart = TestDataForPerson.createDomainPartDataGroupWithUpdated("personId:235:uu", "2");
	}

	@Test
	public void testExtendedFunctionalityNoIdentifier_doNotThrowError() {
		domainPart.removeFirstChildWithNameInData("identifier");

		preventer.useExtendedFunctionality(createExtendedFunctionalityData());

		assertTrue(true);
	}

	private ExtendedFunctionalityData createExtendedFunctionalityData() {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.dataGroup = domainPart;
		return data;
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "PersonDomainPart contains at least one identifier and can therefor not be deleted.")
	public void testExtendedFunctionalityIdentifier_throwError() {
		preventer.useExtendedFunctionality(createExtendedFunctionalityData());
	}
}
