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

import org.testng.annotations.Test;

import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonDomainPartOrganisationSameDomainValidatorTest {

	private ExtendedFunctionalityData data;

	@Test
	public void testInit() throws Exception {
		ExtendedFunctionality extFunctionality = new PersonDomainPartOrganisationSameDomainValidator();
		data = new ExtendedFunctionalityData();
		extFunctionality.useExtendedFunctionality(data);

		assertTrue(true);
	}

	@Test
	public void testPersonDomainPartWithoutAffiliations() throws Exception {
		ExtendedFunctionality extFunctionality = new PersonDomainPartOrganisationSameDomainValidator();
		data = new ExtendedFunctionalityData();

		DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart");
		DataGroupSpy dataGroupAffiliation = new DataGroupSpy("affiliation");
		DataGroupSpy dataGroupOrganisationLink = new DataGroupSpy("organisationLink");
		DataAtomicSpy dataAtomicSpy = new DataAtomicSpy("organisationLink", "someValue");
		dataGroupOrganisationLink.addChild(dataAtomicSpy);
		dataGroupAffiliation.addChild(dataGroupOrganisationLink);
		dataGroupSpy.addChild(dataGroupAffiliation);

		data.dataGroup = dataGroupSpy;

		extFunctionality.useExtendedFunctionality(data);

		assertTrue(true);
	}
}
