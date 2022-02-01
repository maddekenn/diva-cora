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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.DataGroupExtendedSpy;

public class ClassicPersonSynchronizerTest {

	private ClassicFedoraUpdaterFactorySpy classicFedoraUpdaterFactory;
	private ClassicIndexerFactorySpy classicIndexerFactory;
	private ClassicPersonSynchronizer functionality;

	@BeforeMethod
	public void setUp() {
		classicFedoraUpdaterFactory = new ClassicFedoraUpdaterFactorySpy();
		classicIndexerFactory = new ClassicIndexerFactorySpy();
		String recordType = "person";
		functionality = new ClassicPersonSynchronizer(classicFedoraUpdaterFactory, classicIndexerFactory,
				recordType);

	}

	@Test
	public void testInit() {
		assertSame(functionality.getClassicFedoraUpdaterFactory(), classicFedoraUpdaterFactory);
		assertSame(functionality.getClassicIndexer(), classicIndexerFactory);
	}

	@Test
	public void testUseExtendedFunctionalityForPerson() {
		DataGroupExtendedSpy person = createPerson();

		functionality.useExtendedFunctionality("someAuthToken", person);

		assertCorrectCallToUpdater(person);
		assertCorrectCallToIndexer();
	}

	private void assertCorrectCallToUpdater(DataGroupExtendedSpy person) {
		assertEquals(classicFedoraUpdaterFactory.recordType, "person");
		ClassicFedoraUpdaterSpy classicUpdater = classicFedoraUpdaterFactory.factoredUpdater;
		assertEquals(classicUpdater.recordType, "person");
		assertEquals(classicUpdater.recordId, "personId:235");
		assertSame(classicUpdater.dataGroup, person);
	}

	private void assertCorrectCallToIndexer() {
		assertEquals(classicIndexerFactory.type, "person");
		ClassicIndexerSpy factoredIndexer = classicIndexerFactory.factoredIndexer;
		assertEquals(factoredIndexer.recordId, "personId:235");
	}

	private DataGroupExtendedSpy createPerson() {
		DataGroupExtendedSpy person = new DataGroupExtendedSpy("person");
		DataGroupExtendedSpy recordInfo = TestDataForPerson.createRecordInfo("testDiva");
		recordInfo.addChild(new DataAtomicSpy("id", "personId:235"));
		person.addChild(recordInfo);
		return person;
	}

	@Test
	public void testUseExtendedFunctionalityForPersonDomainPart() {
		DataGroupExtendedSpy personDomainPart = createPersonDomainPart();

		String recordType = "personDomainPart";
		functionality = new ClassicPersonSynchronizer(classicFedoraUpdaterFactory, classicIndexerFactory,
				recordType);
		functionality.useExtendedFunctionality("someAuthToken", personDomainPart);

		assertCorrectCallToUpdater(personDomainPart);
		assertCorrectCallToIndexer();
	}

	private DataGroupExtendedSpy createPersonDomainPart() {
		DataGroupExtendedSpy personDomainPart = new DataGroupExtendedSpy("personDomainpart");
		DataGroupExtendedSpy recordInfo = TestDataForPerson.createRecordInfo("testDiva");
		recordInfo.addChild(new DataAtomicSpy("id", "personId:235:uu"));
		personDomainPart.addChild(recordInfo);
		return personDomainPart;
	}

}
