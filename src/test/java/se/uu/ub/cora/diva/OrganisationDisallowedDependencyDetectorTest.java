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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class OrganisationDisallowedDependencyDetectorTest {

	private String authToken = "someAuthToken";
	private OrganisationDisallowedDependencyDetector functionality;
	private DataReaderSpy dataReader;
	private DataGroupSpy dataGroup;

	@BeforeMethod
	public void setUp() {
		dataReader = new DataReaderSpy();
		functionality = new OrganisationDisallowedDependencyDetector(dataReader);
		createDefultDataGroup();
	}

	private void createDefultDataGroup() {
		dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		dataGroup.addChild(recordInfo);
	}

	@Test
	public void testInit() {
		assertSame(functionality.getDataReader(), dataReader);
	}

	@Test
	public void testWhenNoParentOrPredecessorInDataGroupNoCallForDependecyCheck() {
		functionality.useExtendedFunctionality(authToken, dataGroup);
		assertFalse(dataReader.executePreparedStatementWasCalled);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation not updated due to link to self")
	public void testWhenSelfPresentAsParentInDataGroup() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "4567");

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test
	public void testWhenSelfPresentAsParentInDataGroupNoStatementIsExecuted() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "4567");
		try {
			functionality.useExtendedFunctionality(authToken, dataGroup);
		} catch (SqlStorageException e) {
			// do nothing
		}
		assertFalse(dataReader.executePreparedStatementWasCalled);
	}

	@Test
	public void testWhenOneParentInDataGroup() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "51");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		assertTrue(dataReader.executePreparedStatementWasCalled);
		String sql = getExpectedSql("?");

		assertEquals(dataReader.sqlSentToReader, sql);
		List<Object> expectedValues = new ArrayList<>();
		expectedValues.add(51);
		expectedValues.add(4567);
		assertEquals(dataReader.valuesSentToReader, expectedValues);
	}

	private void createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId(
			String nameInData, String repeatId, String parentId) {
		DataGroupSpy parentGroup = new DataGroupSpy(nameInData);
		parentGroup.setRepeatId(repeatId);
		DataGroupSpy organisationLink = new DataGroupSpy("organisationLink");
		DataAtomicSpy linkedRecordId = new DataAtomicSpy("linkedRecordId", parentId);
		organisationLink.addChild(linkedRecordId);
		parentGroup.addChild(organisationLink);
		dataGroup.addChild(parentGroup);
	}

	private String getExpectedSql(String questionsMarks) {
		String sql = "with recursive org_tree as (select distinct organisation_id, relation"
				+ " from organisationrelations where organisation_id in (" + questionsMarks + ") "
				+ "union all" + " select distinct relation.organisation_id, relation.relation from"
				+ " organisationrelations as relation"
				+ " join org_tree as child on child.relation = relation.organisation_id)"
				+ " select * from org_tree where relation = ?";
		return sql;
	}

	@Test
	public void testWhenTwoParentsInDataGroup() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "51");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"1", "3");

		functionality.useExtendedFunctionality(authToken, dataGroup);
		assertTrue(dataReader.executePreparedStatementWasCalled);
		String sql = getExpectedSql("?, ?");

		assertEquals(dataReader.sqlSentToReader, sql);
		List<Object> expectedValues = new ArrayList<>();
		expectedValues.add(51);
		expectedValues.add(3);
		expectedValues.add(4567);
		assertEquals(dataReader.valuesSentToReader, expectedValues);
	}

	@Test
	public void testWhenOneParentAndOnePredecessorInDataGroup() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "51");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("formerName", "0",
				"78");

		functionality.useExtendedFunctionality(authToken, dataGroup);

		assertTrue(dataReader.executePreparedStatementWasCalled);
		String sql = getExpectedSql("?, ?");
		assertEquals(dataReader.sqlSentToReader, sql);

		List<Object> expectedValues = new ArrayList<>();
		expectedValues.add(51);
		expectedValues.add(78);
		expectedValues.add(4567);
		assertEquals(dataReader.valuesSentToReader, expectedValues);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation not updated due to circular dependency with parent or predecessor")
	public void testWhenParentInDataGroupCircularDependencyExist() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "51");
		dataReader.numOfRowsToReturn = 2;
		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation not updated due to same parent and predecessor")
	public void testWhenSamePresentInParentAndPredecessor() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "5");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"1", "7");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("formerName", "0", "5");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("formerName", "1",
				"89");

		functionality.useExtendedFunctionality(authToken, dataGroup);
	}

	@Test
	public void testWhenSamePresentInParentAndPredecessorNoStatementIsExecuted() {
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("parentOrganisation",
				"0", "5");
		createAndAddOrganisationLinkToDefaultUsingRepeatIdAndOrganisationId("formerName", "0", "5");
		try {
			functionality.useExtendedFunctionality(authToken, dataGroup);
		} catch (SqlStorageException e) {
			// do nothing
		}
		assertFalse(dataReader.executePreparedStatementWasCalled);
	}

}
