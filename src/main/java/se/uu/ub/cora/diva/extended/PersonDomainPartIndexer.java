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
package se.uu.ub.cora.diva.extended;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.spider.dependency.SpiderInstanceProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.RecordCreator;
import se.uu.ub.cora.spider.record.RecordReader;

public class PersonDomainPartIndexer implements ExtendedFunctionality {

	private static final String RECORD_TYPE = "recordType";

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		if (workOrderIsForPerson(data.dataGroup)) {
			createWorkOrderForDomainPart(data.authToken, data.dataGroup);
		}
	}

	private boolean workOrderIsForPerson(DataGroup dataGroup) {
		String recordType = extractLinkedRecordType(dataGroup);
		return "person".equals(recordType);
	}

	private String extractLinkedRecordType(DataGroup dataGroup) {
		DataGroup recordTypeLink = dataGroup.getFirstGroupWithNameInData(RECORD_TYPE);
		return recordTypeLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void createWorkOrderForDomainPart(String authToken, DataGroup dataGroup) {
		DataGroup readDataGroup = readPersonDataGroup(authToken, dataGroup);
		String indexType = dataGroup.getFirstAtomicValueWithNameInData("type");
		createWorkOrdersForDomainParts(authToken, readDataGroup, indexType);
	}

	private DataGroup readPersonDataGroup(String authToken, DataGroup dataGroup) {
		String recordId = dataGroup.getFirstAtomicValueWithNameInData("recordId");

		RecordReader spiderRecordReader = SpiderInstanceProvider.getRecordReader();
		DataRecord readRecord = spiderRecordReader.readRecord(authToken, "person", recordId);
		return readRecord.getDataGroup();
	}

	private void createWorkOrdersForDomainParts(String authToken, DataGroup readDataGroup,
			String indexType) {
		for (DataGroup domainPart : readDataGroup.getAllGroupsWithNameInData("personDomainPart")) {
			createWorkOrderForDomainPart(authToken, domainPart, indexType);
		}
	}

	private void createWorkOrderForDomainPart(String authToken, DataGroup domainPart,
			String indexType) {
		DataGroup newWorkOrder = createWorkOrderDataGroup(domainPart, indexType);

		RecordCreator spiderRecordCreator = SpiderInstanceProvider.getRecordCreator();
		spiderRecordCreator.createAndStoreRecord(authToken, "workOrder", newWorkOrder);
	}

	private DataGroup createWorkOrderDataGroup(DataGroup personDomainPart, String indexType) {
		String personDomainPartId = personDomainPart
				.getFirstAtomicValueWithNameInData("linkedRecordId");

		DataGroup newWorkOrder = DataGroupProvider.getDataGroupUsingNameInData("workOrder");
		addRecordTypeLink(newWorkOrder);
		addAtomicValues(personDomainPartId, newWorkOrder, indexType);
		return newWorkOrder;
	}

	private void addRecordTypeLink(DataGroup newWorkOrder) {
		DataGroup recordTypeLink = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId(
				RECORD_TYPE, RECORD_TYPE, "personDomainPart");
		newWorkOrder.addChild(recordTypeLink);
	}

	private void addAtomicValues(String personDomainPartId, DataGroup newWorkOrder,
			String indexType) {
		newWorkOrder.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("recordId",
				personDomainPartId));
		newWorkOrder.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("type", indexType));
	}

}
