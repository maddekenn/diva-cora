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
package se.uu.ub.cora.diva.extended;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.record.DataException;

public class OrganisationDifferentDomainDetectorTest {

	private String authToken = "someAuthToken";
	private OrganisationDifferentDomainDetector functionality;
	private DataGroupDomainSpy dataGroup;
	private RecordStorageSpy recordStorage;

	@BeforeMethod
	public void setUp() {
		recordStorage = new RecordStorageSpy();
		functionality = new OrganisationDifferentDomainDetector(recordStorage);
		createDefultDataGroup();
	}

	private void createDefultDataGroup() {
		dataGroup = new DataGroupDomainSpy("organisation");
		DataGroupDomainSpy recordInfo = new DataGroupDomainSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		dataGroup.addChild(recordInfo);
		dataGroup.addChild(new DataAtomicSpy("domain", "someDomain"));
	}

	@Test
	public void testInit() {
		assertSame(functionality.getRecordStorage(), recordStorage);
	}

	@Test
	public void testNoParentNoPredecessor() {
		functionality.useExtendedFunctionality(authToken, dataGroup);
		assertEquals(dataGroup.requestedAtomicNameInDatas.get(0), "domain");
		assertEquals(dataGroup.getAllGroupsUsedNameInDatas.get(0), "parentOrganisation");
		assertEquals(dataGroup.getAllGroupsUsedNameInDatas.get(1), "formerName");
	}

	@Test
	public void testOneParentNoPredecessorSameDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(1);
		dataGroup.addChildren(parents);

		addOrganisationToReturnFromStorage("organisation_parent0", "someDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		DataGroupDomainSpy returnedParent = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(0);
		DataGroupDomainSpy organisationLink = (DataGroupDomainSpy) returnedParent.totalReturnedDataGroups
				.get(0);
		assertEquals(organisationLink.requestedAtomicNameInDatas.get(0), "linkedRecordId");
		assertEquals(recordStorage.readRecordTypes.get(0), "organisation");
		assertEquals(recordStorage.readRecordIds.get(0), "parent0");
	}

	private void addOrganisationToReturnFromStorage(String key, String domain) {
		DataGroupDomainSpy parentToReturnFromStorage = new DataGroupDomainSpy("organisation");
		parentToReturnFromStorage.addChild(new DataAtomicSpy("domain", domain));
		recordStorage.returnOnRead.put(key, parentToReturnFromStorage);
	}

	public List<DataElement> createParentsUsingNumOfParents(int numOfParents) {
		String nameInData = "parentOrganisation";
		return createOrganisationLinksUsingNameInDataAndNumOf(nameInData, numOfParents, "parent");

	}

	public List<DataElement> createPredecessorsUsingNumOfPredecessors(int numOf) {
		String nameInData = "formerName";
		return createOrganisationLinksUsingNameInDataAndNumOf(nameInData, numOf, "predecessor");

	}

	private List<DataElement> createOrganisationLinksUsingNameInDataAndNumOf(String nameInData,
			int numOfParents, String prefix) {
		List<DataElement> parents = new ArrayList<>();
		for (int i = 0; i < numOfParents; i++) {
			String id = String.valueOf(i);
			DataGroup parent = createOrganisationLinkUsingNameInDataRepeatIdAndOrgId(nameInData, id,
					prefix + id);
			parents.add(parent);
		}
		return parents;
	}

	public DataGroup createOrganisationLinkUsingNameInDataRepeatIdAndOrgId(String nameInData,
			String repeatId, String parentId) {
		DataGroupDomainSpy parentGroup = new DataGroupDomainSpy(nameInData);
		parentGroup.setRepeatId(repeatId);
		DataGroupDomainSpy organisationLink = new DataGroupDomainSpy("organisationLink");
		DataAtomicSpy linkedRecordId = new DataAtomicSpy("linkedRecordId", parentId);
		organisationLink.addChild(linkedRecordId);
		DataAtomicSpy linkedRecordType = new DataAtomicSpy("linkedRecordType", "organisation");
		organisationLink.addChild(linkedRecordType);
		parentGroup.addChild(organisationLink);
		return parentGroup;
	}

	public List<DataElement> createListAndAddPredecessorUsingRepeatIdAndId(DataGroup dataGroup,
			String repeatId, String parentId) {
		List<DataElement> predecessors = new ArrayList<>();
		DataGroup predecessor = createOrganisationLinkUsingNameInDataRepeatIdAndOrgId("formerName",
				repeatId, parentId);
		predecessors.add(predecessor);
		return predecessors;
	}

	@Test(expectedExceptions = DataException.class)
	public void testOneParentNoPredecessorDifferentDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(1);
		dataGroup.addChildren(parents);
		addOrganisationToReturnFromStorage("organisation_parent0", "someOtherDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "Links to organisations from antoher domain is not allowed.")
	public void testTwoParentsNoPredecessorOneSameOneDifferentDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(2);
		addOrganisationToReturnFromStorage("organisation_parent0", "someDomain");
		addOrganisationToReturnFromStorage("organisation_parent1", "someOtherDomain");
		dataGroup.addChildren(parents);

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test
	public void testNoParentOnePredecessorSameDomain() {
		List<DataElement> predecessors = createPredecessorsUsingNumOfPredecessors(1);
		dataGroup.addChildren(predecessors);

		addOrganisationToReturnFromStorage("organisation_predecessor0", "someDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		DataGroupDomainSpy returnedPredecessor = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(0);
		DataGroupDomainSpy organisationLink = (DataGroupDomainSpy) returnedPredecessor.totalReturnedDataGroups
				.get(0);
		assertEquals(organisationLink.requestedAtomicNameInDatas.get(0), "linkedRecordId");
		assertEquals(recordStorage.readRecordTypes.get(0), "organisation");
		assertEquals(recordStorage.readRecordIds.get(0), "predecessor0");
	}
	// private List<DataElement> createListWithOneParentUsingRepeatIdAndParentIa(String repeatId,
	// String parentId) {
	// List<DataElement> parents = new ArrayList<>();
	// DataGroup parent = createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// "parentOrganisation", repeatId, parentId);
	// parents.add(parent);
	// return parents;
	// }
	//
	// private DataGroup createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// String nameInData, String repeatId, String parentId) {
	// DataGroupSpy parentGroup = new DataGroupSpy(nameInData);
	// parentGroup.setRepeatId(repeatId);
	// DataGroupSpy organisationLink = new DataGroupSpy("organisationLink");
	// DataAtomicSpy linkedRecordId = new DataAtomicSpy("linkedRecordId", parentId);
	// organisationLink.addChild(linkedRecordId);
	// parentGroup.addChild(organisationLink);
	// dataGroup.addChild(parentGroup);
	// return parentGroup;
	// }
	//
	// private String getExpectedSql(String questionsMarks) {
	// String sql = "with recursive org_tree as (select distinct organisation_id, relation"
	// + " from organisationrelations where organisation_id in (" + questionsMarks + ") "
	// + "union all" + " select distinct relation.organisation_id, relation.relation from"
	// + " organisationrelations as relation"
	// + " join org_tree as child on child.relation = relation.organisation_id)"
	// + " select * from org_tree where relation = ?";
	// return sql;
	// }
	//
	// @Test
	// public void testWhenTwoParentsInDataGroup() {
	// List<DataElement> parents = createListAndAddDefaultParent();
	// DataGroup parent2 = createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// "parentOrganisation", "1", "3");
	// parents.add(parent2);
	// dataGroup.childrenToReturn.put("parentOrganisation", parents);
	//
	// functionality.useExtendedFunctionality(authToken, dataGroup);
	// assertTrue(dataReader.executePreparedStatementWasCalled);
	// String sql = getExpectedSql("?, ?");
	//
	// assertEquals(dataReader.sqlSentToReader, sql);
	// List<Object> expectedValues = new ArrayList<>();
	// expectedValues.add(51);
	// expectedValues.add(3);
	// expectedValues.add(4567);
	// assertEquals(dataReader.valuesSentToReader, expectedValues);
	// }
	//
	// @Test
	// public void testWhenOneParentAndOnePredecessorInDataGroup() {
	// List<DataElement> parents = createListAndAddDefaultParent();
	// dataGroup.childrenToReturn.put("parentOrganisation", parents);
	//
	// List<DataElement> predecessors = createListAndAddPredecessorUsingRepeatIdAndId("0", "78");
	// dataGroup.childrenToReturn.put("formerName", predecessors);
	//
	// functionality.useExtendedFunctionality(authToken, dataGroup);
	//
	// assertTrue(dataReader.executePreparedStatementWasCalled);
	// String sql = getExpectedSql("?, ?");
	// assertEquals(dataReader.sqlSentToReader, sql);
	//
	// List<Object> expectedValues = new ArrayList<>();
	// expectedValues.add(51);
	// expectedValues.add(78);
	// expectedValues.add(4567);
	// assertEquals(dataReader.valuesSentToReader, expectedValues);
	// }
	//
	// private List<DataElement> createListAndAddPredecessorUsingRepeatIdAndId(String repeatId,
	// String parentId) {
	// List<DataElement> predecessors = new ArrayList<>();
	// DataGroup predecessor = createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// "formerName", repeatId, parentId);
	// predecessors.add(predecessor);
	// return predecessors;
	// }
	//
	// @Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
	// + "Organisation not updated due to circular dependency with parent or predecessor")
	// public void testWhenParentInDataGroupCircularDependencyExist() {
	// List<DataElement> parents = createListAndAddDefaultParent();
	// createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
	// "0", "51");
	// dataGroup.childrenToReturn.put("parentOrganisation", parents);
	// dataReader.numOfRowsToReturn = 2;
	// functionality.useExtendedFunctionality(authToken, dataGroup);
	// }
	//
	// @Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
	// + "Organisation not updated due to same parent and predecessor")
	// public void testWhenSamePresentInParentAndPredecessor() {
	// List<DataElement> parents = createListWithOneParentUsingRepeatIdAndParentIa("0", "5");
	// DataGroup parent2 = createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// "parentOrganisation", "1", "7");
	// parents.add(parent2);
	// dataGroup.childrenToReturn.put("parentOrganisation", parents);
	//
	// List<DataElement> predecessors = createListAndAddPredecessorUsingRepeatIdAndId("0", "5");
	// DataGroup predecessor2 = createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
	// "formerName", "1", "89");
	// predecessors.add(predecessor2);
	// dataGroup.childrenToReturn.put("formerName", predecessors);
	//
	// functionality.useExtendedFunctionality(authToken, dataGroup);
	// }
	//
	// @Test
	// public void testWhenSamePresentInParentAndPredecessorNoStatementIsExecuted() {
	// List<DataElement> parents = createListWithOneParentUsingRepeatIdAndParentIa("0", "5");
	// dataGroup.childrenToReturn.put("parentOrganisation", parents);
	//
	// List<DataElement> predecessors = createListAndAddPredecessorUsingRepeatIdAndId("0", "5");
	//
	// dataGroup.childrenToReturn.put("formerName", predecessors);
	// try {
	// functionality.useExtendedFunctionality(authToken, dataGroup);
	// } catch (SqlStorageException e) {
	// // do nothing
	// }
	// assertFalse(dataReader.executePreparedStatementWasCalled);
	// }

}
