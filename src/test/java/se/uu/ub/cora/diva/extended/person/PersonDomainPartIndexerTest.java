/*
 * Copyright 2021 Uppsala University Library
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

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartIndexer;
import se.uu.ub.cora.diva.spies.data.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupFactorySpy;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;
import se.uu.ub.cora.diva.spies.spider.SpiderInstanceFactorySpy;
import se.uu.ub.cora.diva.spies.spider.SpiderRecordCreatorSpy;
import se.uu.ub.cora.diva.spies.spider.SpiderRecordReaderSpy;
import se.uu.ub.cora.spider.dependency.SpiderInstanceProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class PersonDomainPartIndexerTest {

	private String authToken = "someAuthToken";
	private PersonDomainPartIndexer functionality;
	private SpiderInstanceFactorySpy instanceFactory;
	private DataAtomicFactorySpy dataAtomicFactory;
	private DataGroupFactorySpy dataGroupFactory;

	@BeforeMethod
	public void setUp() {
		instanceFactory = new SpiderInstanceFactorySpy();
		SpiderInstanceProvider.setSpiderInstanceFactory(instanceFactory);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		functionality = new PersonDomainPartIndexer();
	}

	@Test
	public void testExtendedFunctionalityNotPerson() {
		DataGroup workOrder = new DataGroupSpy("workOrder");

		DataGroup recordTypeLink = new DataGroupSpy("recordType");
		recordTypeLink.addChild(new DataAtomicSpy("linkedRecordType", "recordType"));
		recordTypeLink.addChild(new DataAtomicSpy("linkedRecordId", "NOTperson"));
		workOrder.addChild(recordTypeLink);

		functionality.useExtendedFunctionality(createDefaultData(workOrder));

		assertEquals(instanceFactory.spiderRecordReaders.size(), 0);

	}

	private ExtendedFunctionalityData createDefaultData(DataGroup dataGroup) {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = "someAuthToken";
		data.dataGroup = dataGroup;
		return data;
	}

	@Test
	public void testExtendedFunctionalityOneDomainPart() {
		DataGroup workOrder = createWorkOrder("index");

		functionality.useExtendedFunctionality(createDefaultData(workOrder));

		assertCorrectCalledRecordReader();
		assertEquals(instanceFactory.spiderRecordCreators.size(), 2);

		assertCorrectlyCreated(0, 0);
		assertCorrectlyCreated(1, 2);

		assertCorrectlyFactoredDataGroups();
		assertCorrectlyFactoredDataAtomics("index");
	}

	private void assertCorrectCalledRecordReader() {
		assertEquals(instanceFactory.spiderRecordReaders.size(), 1);
		SpiderRecordReaderSpy factoredReader = (SpiderRecordReaderSpy) instanceFactory.spiderRecordReaders
				.get(0);
		assertEquals(factoredReader.authToken, authToken);
		assertEquals(factoredReader.type, "person");
		assertEquals(factoredReader.id, "personOne");
	}

	private void assertCorrectlyCreated(int creatorIndex, int dataGroupIndex) {
		SpiderRecordCreatorSpy spiderRecordCreator = (SpiderRecordCreatorSpy) instanceFactory.spiderRecordCreators
				.get(creatorIndex);
		assertEquals(spiderRecordCreator.authToken, authToken);
		assertEquals(spiderRecordCreator.type, "workOrder");
		assertEquals(spiderRecordCreator.record,
				dataGroupFactory.factoredDataGroups.get(dataGroupIndex));
	}

	private void assertCorrectlyFactoredDataGroups() {
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "workOrder");
		assertEquals(dataGroupFactory.usedNameInDatas.get(1), "recordType");
		assertEquals(dataGroupFactory.usedNameInDatas.get(2), "workOrder");
		assertEquals(dataGroupFactory.usedNameInDatas.get(3), "recordType");

		assertEquals(dataGroupFactory.usedRecordTypes.get(0), "recordType");
		assertEquals(dataGroupFactory.usedRecordTypes.get(1), "recordType");
		assertEquals(dataGroupFactory.usedRecordIds.get(0), "personDomainPart");
		assertEquals(dataGroupFactory.usedRecordIds.get(1), "personDomainPart");
	}

	private void assertCorrectlyFactoredDataAtomics(String indexType) {
		String idCreatedInReaderSpy = "somePerson:uu";
		assertEquals(dataAtomicFactory.usedNameInDatas.get(0), "recordId");
		assertEquals(dataAtomicFactory.usedValues.get(0), idCreatedInReaderSpy);
		assertEquals(dataAtomicFactory.usedNameInDatas.get(1), "type");
		assertEquals(dataAtomicFactory.usedValues.get(1), indexType);

		String secondIdCreatedInReaderSpy = "somePerson:test";
		assertEquals(dataAtomicFactory.usedNameInDatas.get(2), "recordId");
		assertEquals(dataAtomicFactory.usedValues.get(2), secondIdCreatedInReaderSpy);
		assertEquals(dataAtomicFactory.usedNameInDatas.get(3), "type");
		assertEquals(dataAtomicFactory.usedValues.get(3), indexType);
	}

	public DataGroup createWorkOrder(String indexType) {
		DataGroup workOrder = new DataGroupSpy("workOrder");

		DataGroup recordTypeLink = new DataGroupSpy("recordType");
		recordTypeLink.addChild(new DataAtomicSpy("linkedRecordType", "recordType"));
		recordTypeLink.addChild(new DataAtomicSpy("linkedRecordId", "person"));
		workOrder.addChild(recordTypeLink);

		workOrder.addChild(new DataAtomicSpy("recordId", "personOne"));
		workOrder.addChild(new DataAtomicSpy("type", indexType));
		return workOrder;
	}

	@Test
	public void testExtendedFunctionalityOneDomainPartRemoveFromIndex() {
		DataGroup workOrder = createWorkOrder("removeFromIndex");

		functionality.useExtendedFunctionality(createDefaultData(workOrder));

		assertCorrectCalledRecordReader();
		assertEquals(instanceFactory.spiderRecordCreators.size(), 2);

		assertCorrectlyCreated(0, 0);
		assertCorrectlyCreated(1, 2);

		assertCorrectlyFactoredDataGroups();
		assertCorrectlyFactoredDataAtomics("removeFromIndex");
	}
}
