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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.spies.classic.ClassicFedoraUpdaterFactorySpy;
import se.uu.ub.cora.diva.spies.classic.ClassicFedoraUpdaterSpy;
import se.uu.ub.cora.diva.spies.classic.ClassicIndexerFactorySpy;
import se.uu.ub.cora.diva.spies.classic.ClassicIndexerSpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.diva.spies.storage.RecordStorageSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class ClassicPersonUpdateSynchronizerTest {

	private ClassicFedoraUpdaterFactorySpy classicFedoraUpdaterFactory;
	private ClassicIndexerFactorySpy classicIndexerFactory;
	private ClassicPersonUpdateSynchronizer functionality;
	private RecordStorageSpy recordStorage;

	@BeforeMethod
	public void setUp() {
		recordStorage = new RecordStorageSpy();
		classicFedoraUpdaterFactory = new ClassicFedoraUpdaterFactorySpy();
		classicIndexerFactory = new ClassicIndexerFactorySpy();
		String recordType = "person";
		functionality = new ClassicPersonUpdateSynchronizer(classicFedoraUpdaterFactory,
				classicIndexerFactory, recordType, recordStorage);

	}

	@Test
	public void testInit() {
		assertSame(functionality.getClassicFedoraUpdaterFactory(), classicFedoraUpdaterFactory);
		assertSame(functionality.getClassicIndexer(), classicIndexerFactory);
		assertSame(functionality.getRecordStorage(), recordStorage);
	}

	@Test
	public void testUseExtendedFunctionalityForPerson() {
		DataGroupExtendedSpy person = createPerson();

		functionality.useExtendedFunctionality(createDefaultData(person));

		assertCorrectCallToUpdater(person);
		assertCorrectCallToIndexer();
		assertEquals(recordStorage.readRecordTypes.size(), 0);
	}

	private ExtendedFunctionalityData createDefaultData(DataGroup person) {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = "authToken";
		data.dataGroup = person;
		return data;
	}

	private void assertCorrectCallToUpdater(DataGroup person) {
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
		ExtendedFunctionalityData defaultData = createDefaultData(personDomainPart);
		defaultData.recordType = "personDomainPart";
		functionality = new ClassicPersonUpdateSynchronizer(classicFedoraUpdaterFactory,
				classicIndexerFactory, "personDomainPart", recordStorage);
		functionality.useExtendedFunctionality(defaultData);

		assertCorrectCallToIndexer();
		assertEquals(recordStorage.readRecordTypes.size(), 1);
		assertEquals(recordStorage.readRecordTypes.get(0), "person");
		assertEquals(recordStorage.readRecordIds.get(0), "personId:235");
		assertCorrectCallToUpdater(recordStorage.returnedDataGroups.get(0));
	}

	private DataGroupExtendedSpy createPersonDomainPart() {
		DataGroupExtendedSpy personDomainPart = new DataGroupExtendedSpy("personDomainpart");
		DataGroupExtendedSpy recordInfo = TestDataForPerson.createRecordInfo("testDiva");
		recordInfo.addChild(new DataAtomicSpy("id", "personId:235:uu"));
		personDomainPart.addChild(recordInfo);
		return personDomainPart;
	}

}
