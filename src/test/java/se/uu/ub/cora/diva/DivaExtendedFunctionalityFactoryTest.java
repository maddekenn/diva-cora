/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_METADATA_VALIDATION;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory;

public class DivaExtendedFunctionalityFactoryTest {

	private ExtendedFunctionalityFactory factory;

	@BeforeMethod
	public void setUp() {
		factory = new DivaExtendedFunctionalityFactory();
		factory.initializeUsingDependencyProvider(null);
	}

	@Test
	public void testInit() {
		assertEquals(factory.getExtendedFunctionalityContexts().size(), 2);
		assertCorrectContextUsingIndexAndRecordType(0, "commonOrganisation");
		assertCorrectContextUsingIndexAndRecordType(1, "rootOrganisation");
	}

	private void assertCorrectContextUsingIndexAndRecordType(int index, String recordType) {
		ExtendedFunctionalityContext updateBefore = factory.getExtendedFunctionalityContexts()
				.get(index);
		assertEquals(updateBefore.position, UPDATE_BEFORE_METADATA_VALIDATION);
		assertEquals(updateBefore.recordType, recordType);
		assertEquals(updateBefore.runAsNumber, 0);
	}

	@Test
	public void factorCommonOrganisationUpdateBefore() {
		List<ExtendedFunctionality> functionalities = factory
				.factor(UPDATE_BEFORE_METADATA_VALIDATION, "commonOrganisation");
		assertTrue(functionalities.get(0) instanceof OrganisationDuplicateLinksRemover);

	}

	@Test
	public void factorRootOrganisationUpdateBefore() {
		List<ExtendedFunctionality> functionalities = factory
				.factor(UPDATE_BEFORE_METADATA_VALIDATION, "rootOrganisation");
		assertTrue(functionalities.get(0) instanceof OrganisationDuplicateLinksRemover);

	}

}
