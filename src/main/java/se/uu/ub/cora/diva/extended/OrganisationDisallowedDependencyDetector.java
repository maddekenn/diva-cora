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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.record.DataException;
//import se.uu.ub.cora.sqldatabase.SqlStorageException;
//import se.uu.ub.cora.sqldatabase.DataReader;
import se.uu.ub.cora.sqldatabase.DataReader;

public class OrganisationDisallowedDependencyDetector implements ExtendedFunctionality {

	private DataReader dataReader;
	private DataGroup dataGroup;

	public OrganisationDisallowedDependencyDetector(DataReader dataReader) {
		this.dataReader = dataReader;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		possiblyThrowErrorIfDisallowedDependencyDetected(dataGroup);

	}

	public DataReader getDataReader() {
		return dataReader;
	}

	private void possiblyThrowErrorIfDisallowedDependencyDetected(DataGroup dataGroup) {
		List<Integer> parentIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
				"parentOrganisation");
		List<Integer> predecessorIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
				"formerName");
		throwErrorIfSameParentAndPredecessor(parentIds, predecessorIds);
		possiblyThrowErrorIfCircularDependencyDetected(parentIds, predecessorIds);
	}

	private List<Integer> getIdsFromOrganisationLinkUsingNameInData(DataGroup dataGroup,
			String nameInData) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData(nameInData);
		return getIdListOfParentsAndPredecessors(parents);
	}

	private void throwErrorIfSameParentAndPredecessor(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		boolean sameIdInBothList = parentIds.stream().anyMatch(predecessorIds::contains);
		if (sameIdInBothList) {
			throw new DataException("Organisation not updated due to same parent and predecessor");
		}
	}

	private void possiblyThrowErrorIfCircularDependencyDetected(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		List<Integer> parentsAndPredecessors = combineParentsAndPredecessors(parentIds,
				predecessorIds);
		if (!parentsAndPredecessors.isEmpty()) {
			throwErrorIfLinkToSelf(parentsAndPredecessors);
			throwErrorIfCircularDependencyDetected(parentsAndPredecessors);
		}
	}

	private List<Integer> combineParentsAndPredecessors(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		List<Integer> combinedList = new ArrayList<>();
		combinedList.addAll(parentIds);
		combinedList.addAll(predecessorIds);
		return combinedList;
	}

	private void throwErrorIfLinkToSelf(List<Integer> parentsAndPredecessorIds) {
		int organisationsId = getIdFromDataGroup();
		if (parentsAndPredecessorIds.contains(organisationsId)) {
			throw new DataException("Organisation not updated due to link to self");
		}
	}

	private int getIdFromDataGroup() {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		String id = recordInfo.getFirstAtomicValueWithNameInData("id");
		return Integer.parseInt(id);
	}

	private List<Integer> getIdListOfParentsAndPredecessors(
			List<DataGroup> parentsAndPredecessors) {
		List<Integer> organisationIds = new ArrayList<>(parentsAndPredecessors.size());
		for (DataGroup parent : parentsAndPredecessors) {
			int organisationId = extractOrganisationIdFromLink(parent);
			organisationIds.add(organisationId);
		}
		return organisationIds;
	}

	private void throwErrorIfCircularDependencyDetected(List<Integer> parentsAndPredecessorIds) {
		StringJoiner questionMarkPart = getCorrectNumberOfConditionPlaceHolders(
				parentsAndPredecessorIds);

		String sql = getSqlForFindingRecursion(questionMarkPart);
		List<Object> values = createValuesForCircularDependencyQuery(parentsAndPredecessorIds);
		executeAndThrowErrorIfCircularDependencyExist(sql, values);
	}

	private StringJoiner getCorrectNumberOfConditionPlaceHolders(
			List<Integer> parentsAndPredecessors) {
		StringJoiner questionMarkPart = new StringJoiner(", ");
		for (int i = 0; i < parentsAndPredecessors.size(); i++) {
			questionMarkPart.add("?");
		}
		return questionMarkPart;
	}

	private String getSqlForFindingRecursion(StringJoiner questionMarkPart) {
		return "with recursive org_tree as (select distinct organisation_id, relation"
				+ " from organisationrelations where organisation_id in (" + questionMarkPart + ") "
				+ "union all" + " select distinct relation.organisation_id, relation.relation from"
				+ " organisationrelations as relation"
				+ " join org_tree as child on child.relation = relation.organisation_id)"
				+ " select * from org_tree where relation = ?";
	}

	private List<Object> createValuesForCircularDependencyQuery(
			List<Integer> parentsAndPredecessorIds) {
		List<Object> values = new ArrayList<>();
		addValuesForParentsAndPredecessors(parentsAndPredecessorIds, values);
		addValueForOrganisationId(values);
		return values;
	}

	private void executeAndThrowErrorIfCircularDependencyExist(String sql, List<Object> values) {
		List<Map<String, Object>> result = dataReader
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		if (!result.isEmpty()) {
			throw new DataException(
					"Organisation not updated due to circular dependency with parent or predecessor");
		}
	}

	private void addValuesForParentsAndPredecessors(List<Integer> parentsAndPredecessorIds,
			List<Object> values) {
		for (Integer parentId : parentsAndPredecessorIds) {
			values.add(parentId);
		}
	}

	private void addValueForOrganisationId(List<Object> values) {
		int organisationsId = getIdFromDataGroup();
		values.add(organisationsId);
	}

	private int extractOrganisationIdFromLink(DataGroup parent) {
		DataGroup parentLink = parent.getFirstGroupWithNameInData("organisationLink");
		String linkedRecordId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		return Integer.parseInt(linkedRecordId);
	}

}
