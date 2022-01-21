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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.DatabaseFacadeSpy;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartValidatorTest {

	private DatabaseFacadeSpy databaseFacade;
	private PersonDomainPartValidator validator;
	private String authToken = "someAuthToken";

	@BeforeMethod
	public void setUp() {
		databaseFacade = new DatabaseFacadeSpy();
		validator = new PersonDomainPartValidator(databaseFacade);
	}

	@Test
	public void testInit() {
		assertSame(validator.getDbFacade(), databaseFacade);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "No person exists with record id personId:123. PersonDomainPart was not created. "
			+ "Error from spy reading one row.")
	public void testExtendedFunctionalityNoPersonPresent() {
		databaseFacade.throwDataException = true;
		DataGroupSpy domainPart = createDataGroup("personId:123:domainPartIdPart");
		validator.useExtendedFunctionality(authToken, domainPart);
	}

	private DataGroupSpy createDataGroup(String domainPartId) {
		DataGroupSpy domainPart = new DataGroupSpy("personDomainPart");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", domainPartId));
		domainPart.addChild(recordInfo);
		return domainPart;
	}

	@Test
	public void testExtendedFunctionalityPersonExists() {
		DataGroupSpy domainPart = createDataGroup("personId:123:domainPartIdPart");
		validator.useExtendedFunctionality(authToken, domainPart);

		databaseFacade.MCR.assertParameter("readOneRowOrFailUsingSqlAndValues", 0, "sql",
				"select * from record_person where id = ?");
		Map<String, Object> parameters = databaseFacade.MCR
				.getParametersForMethodAndCallNumber("readOneRowOrFailUsingSqlAndValues", 0);
		List<Object> valuesSentIn = (List<Object>) parameters.get("values");
		assertTrue(valuesSentIn.contains("personId:123"));
	}
}
