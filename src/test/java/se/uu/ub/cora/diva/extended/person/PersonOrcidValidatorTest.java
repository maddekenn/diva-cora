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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.diva.extended.person.PersonOrcidValidator;
import se.uu.ub.cora.diva.spies.data.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonOrcidValidatorTest {

	private ExtendedFunctionalityData data;
	private DataAtomicFactorySpy dataAtomicFactory;
	private DataGroupExtendedSpy previousPerson;
	private DataGroupExtendedSpy defaultPerson;

	@BeforeMethod
	public void setUp() {
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		createDefaultPreviousPerson();
		defaultPerson = new DataGroupExtendedSpy("person");
		data = new ExtendedFunctionalityData();
		data.recordType = "person";
		data.recordId = "authority-person:001";
		data.previouslyStoredTopDataGroup = previousPerson;
		data.dataGroup = defaultPerson;
	}

	private void createDefaultPreviousPerson() {
		previousPerson = new DataGroupExtendedSpy("person");
		previousPerson.addChild(new DataAtomicSpy("ORCID_ID", "0000", "1"));
	}

	@Test
	public void testOneOrcidPreviousRecordRemovedFromUpdated() {
		ExtendedFunctionality functionality = new PersonOrcidValidator();

		functionality.useExtendedFunctionality(data);

		assertTrue(defaultPerson.containsChildWithNameInData("ORCID_ID"));
		assertSame(defaultPerson.getFirstChildWithNameInData("ORCID_ID"),
				dataAtomicFactory.factoredDataAtomics.get(0));
	}

	@Test
	public void testTwoOrcidPreviousRecordRemovedFromUpdatedOneLeftOneAdded() {
		addMoreOrcidsToPreviousAndDefaultPerson();

		ExtendedFunctionality functionality = new PersonOrcidValidator();
		functionality.useExtendedFunctionality(data);

		List<DataAtomic> orcids = defaultPerson.getAllDataAtomicsWithNameInData("ORCID_ID");
		assertEquals(orcids.size(), 4);
		assertEquals(orcids.get(0).getValue(), "2222");
		assertEquals(orcids.get(1).getValue(), "3333");
		assertEquals(orcids.get(2).getValue(), "0000");
		assertEquals(orcids.get(3).getValue(), "1111");

		assertSame(orcids.get(2), dataAtomicFactory.factoredDataAtomics.get(0));
		assertSame(orcids.get(3), dataAtomicFactory.factoredDataAtomics.get(1));
	}

	private void addMoreOrcidsToPreviousAndDefaultPerson() {
		previousPerson.addChild(new DataAtomicSpy("ORCID_ID", "1111", "2"));
		previousPerson.addChild(new DataAtomicSpy("ORCID_ID", "2222", "3"));

		defaultPerson.addChild(new DataAtomicSpy("ORCID_ID", "2222", "1"));
		defaultPerson.addChild(new DataAtomicSpy("ORCID_ID", "3333", "2"));
	}

	@Test
	public void testnewAndUniqueRepeatIdsAreSet() {
		addMoreOrcidsToPreviousAndDefaultPerson();

		ExtendedFunctionality functionality = new PersonOrcidValidator();
		functionality.useExtendedFunctionality(data);

		List<DataAtomic> orcids = defaultPerson.getAllDataAtomicsWithNameInData("ORCID_ID");
		assertEquals(orcids.get(0).getRepeatId(), "0");
		assertEquals(orcids.get(1).getRepeatId(), "1");
		assertEquals(orcids.get(2).getRepeatId(), "2");
		assertEquals(orcids.get(3).getRepeatId(), "3");

	}

}
