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
		recordInfo.addChild(new DataAtomicSpy("domain", "someDomain"));
		dataGroup.addChild(recordInfo);
	}

	@Test
	public void testInit() {
		assertSame(functionality.getRecordStorage(), recordStorage);
	}

	@Test
	public void testNoParentNoPredecessor() {
		functionality.useExtendedFunctionality(authToken, dataGroup);
		DataGroupDomainSpy recordInfo = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(0);
		assertEquals(recordInfo.requestedAtomicNameInDatas.get(0), "domain");
		assertEquals(dataGroup.getAllGroupsUsedNameInDatas.get(0), "parentOrganisation");
		assertEquals(dataGroup.getAllGroupsUsedNameInDatas.get(1), "earlierOrganisation");
	}

	@Test
	public void testOneParentNoPredecessorSameDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(1);
		dataGroup.addChildren(parents);

		addOrganisationToReturnFromStorage("organisation_parent0", "someDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		DataGroupDomainSpy returnedParent = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(1);
		DataGroupDomainSpy organisationLink = (DataGroupDomainSpy) returnedParent.totalReturnedDataGroups
				.get(0);
		assertEquals(organisationLink.requestedAtomicNameInDatas.get(0), "linkedRecordId");
		assertEquals(recordStorage.readRecordTypes.get(0), "organisation");
		assertEquals(recordStorage.readRecordIds.get(0), "parent0");
	}

	private void addOrganisationToReturnFromStorage(String key, String domain) {
		DataGroupDomainSpy parentToReturnFromStorage = new DataGroupDomainSpy("organisation");
		DataGroupDomainSpy recordInfo = new DataGroupDomainSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("domain", domain));
		parentToReturnFromStorage.addChild(recordInfo);
		recordStorage.returnOnRead.put(key, parentToReturnFromStorage);
	}

	public List<DataElement> createParentsUsingNumOfParents(int numOfParents) {
		String nameInData = "parentOrganisation";
		return createOrganisationLinksUsingNameInDataAndNumOf(nameInData, numOfParents, "parent");

	}

	public List<DataElement> createPredecessorsUsingNumOfPredecessors(int numOf) {
		String nameInData = "earlierOrganisation";
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
		DataGroup predecessor = createOrganisationLinkUsingNameInDataRepeatIdAndOrgId(
				"earlierOrganisation", repeatId, parentId);
		predecessors.add(predecessor);
		return predecessors;
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "Links to organisations from another domain is not allowed.")
	public void testOneParentNoPredecessorDifferentDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(1);
		dataGroup.addChildren(parents);
		addOrganisationToReturnFromStorage("organisation_parent0", "someOtherDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "Links to organisations from another domain is not allowed.")
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
				.get(1);
		DataGroupDomainSpy organisationLink = (DataGroupDomainSpy) returnedPredecessor.totalReturnedDataGroups
				.get(0);
		assertEquals(organisationLink.requestedAtomicNameInDatas.get(0), "linkedRecordId");
		assertEquals(recordStorage.readRecordTypes.get(0), "organisation");
		assertEquals(recordStorage.readRecordIds.get(0), "predecessor0");
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "Links to organisations from another domain is not allowed.")
	public void testNoParentOnePredecessorDifferentDomain() {
		List<DataElement> predecessors = createPredecessorsUsingNumOfPredecessors(1);
		dataGroup.addChildren(predecessors);
		addOrganisationToReturnFromStorage("organisation_predecessor0", "someOtherDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test
	public void testOneParentOnePredecessorSameDomain() {
		List<DataElement> parents = createParentsUsingNumOfParents(1);
		dataGroup.addChildren(parents);
		List<DataElement> predecessors = createPredecessorsUsingNumOfPredecessors(1);
		dataGroup.addChildren(predecessors);

		addOrganisationToReturnFromStorage("organisation_parent0", "someDomain");
		addOrganisationToReturnFromStorage("organisation_predecessor0", "someDomain");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		assertEquals(dataGroup.totalReturnedDataGroups.size(), 3);
		DataGroupDomainSpy returnedParent = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(1);
		assertCorrectOrganisationLink(returnedParent, "parent0", 0);
		DataGroupDomainSpy returnedPredecessor = (DataGroupDomainSpy) dataGroup.totalReturnedDataGroups
				.get(2);
		assertCorrectOrganisationLink(returnedPredecessor, "predecessor0", 1);
	}

	private void assertCorrectOrganisationLink(DataGroupDomainSpy returnedParent, String recordId,
			int indexInStorage) {
		DataGroupDomainSpy organisationLink = (DataGroupDomainSpy) returnedParent.totalReturnedDataGroups
				.get(0);
		assertEquals(organisationLink.requestedAtomicNameInDatas.get(0), "linkedRecordId");
		assertEquals(recordStorage.readRecordTypes.get(0), "organisation");
		assertEquals(recordStorage.readRecordIds.get(indexInStorage), recordId);
	}

}
