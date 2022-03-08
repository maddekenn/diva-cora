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
import se.uu.ub.cora.diva.extended.person.PersonDomainPartLocalIdValidator;
import se.uu.ub.cora.diva.spies.data.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonDomainPartLocalIdValidatorTest {

	private DataGroupExtendedSpy previousPersonDomainPart;
	private ExtendedFunctionalityData data;
	private DataGroupExtendedSpy defaultPersonDomainPart;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void setUp() {
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		createPreviousPersonDomainPart();
		defaultPersonDomainPart = new DataGroupExtendedSpy("personDomainPart");

		data = new ExtendedFunctionalityData();
		data.recordType = "personDomainPart";
		data.recordId = "authorityPerson:123:uu";
		data.previouslyStoredTopDataGroup = previousPersonDomainPart;
		data.dataGroup = defaultPersonDomainPart;

	}

	private void createPreviousPersonDomainPart() {
		previousPersonDomainPart = new DataGroupExtendedSpy("personDomainPart");
		previousPersonDomainPart.addChild(new DataAtomicSpy("identifier", "aaaa", "1"));
	}

	@Test
	public void testOneLocalIdPreviousRecordRemovedFromUpdated() {
		ExtendedFunctionality functionality = new PersonDomainPartLocalIdValidator();
		functionality.useExtendedFunctionality(data);

		assertTrue(defaultPersonDomainPart.containsChildWithNameInData("identifier"));
		assertSame(defaultPersonDomainPart.getFirstChildWithNameInData("identifier"),
				dataAtomicFactory.factoredDataAtomics.get(0));
	}

	@Test
	public void testTwoIdsPreviousRecordRemovedFromUpdatedOneLeftOneAdded() {
		addMoreIdsToPreviousAndDefaultPerson();

		ExtendedFunctionality functionality = new PersonDomainPartLocalIdValidator();
		functionality.useExtendedFunctionality(data);

		List<DataAtomic> identifiers = defaultPersonDomainPart
				.getAllDataAtomicsWithNameInData("identifier");
		assertEquals(identifiers.size(), 4);
		assertEquals(identifiers.get(0).getValue(), "cccc");
		assertEquals(identifiers.get(1).getValue(), "dddd");
		assertEquals(identifiers.get(2).getValue(), "aaaa");
		assertEquals(identifiers.get(3).getValue(), "bbbb");

		assertSame(identifiers.get(2), dataAtomicFactory.factoredDataAtomics.get(0));
		assertSame(identifiers.get(3), dataAtomicFactory.factoredDataAtomics.get(1));
	}

	private void addMoreIdsToPreviousAndDefaultPerson() {
		previousPersonDomainPart.addChild(new DataAtomicSpy("identifier", "bbbb", "2"));
		previousPersonDomainPart.addChild(new DataAtomicSpy("identifier", "cccc", "3"));

		defaultPersonDomainPart.addChild(new DataAtomicSpy("identifier", "cccc", "1"));
		defaultPersonDomainPart.addChild(new DataAtomicSpy("identifier", "dddd", "2"));
	}

	@Test
	public void testnewAndUniqueRepeatIdsAreSet() {
		addMoreIdsToPreviousAndDefaultPerson();

		ExtendedFunctionality functionality = new PersonDomainPartLocalIdValidator();
		functionality.useExtendedFunctionality(data);

		List<DataAtomic> identifiers = defaultPersonDomainPart
				.getAllDataAtomicsWithNameInData("identifier");
		assertEquals(identifiers.get(0).getRepeatId(), "0");
		assertEquals(identifiers.get(1).getRepeatId(), "1");
		assertEquals(identifiers.get(2).getRepeatId(), "2");
		assertEquals(identifiers.get(3).getRepeatId(), "3");

	}

}
