package se.uu.ub.cora.diva;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
//import se.uu.ub.cora.sqldatabase.SqlStorageException;
//import se.uu.ub.cora.sqldatabase.DataReader;

public class OrganisationDisallowedDependencyDetector implements ExtendedFunctionality {

	// private static final String ORGANISATION_ID = "organisation_id";
	// private Map<String, Object> organisationConditions;
	// private DataReader dataReader;

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		// TODO Auto-generated method stub

	}

	// private void possiblyThrowErrorIfDisallowedDependencyDetected(DataGroup dataGroup) {
	// List<Integer> parentIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
	// "parentOrganisation");
	// List<Integer> predecessorIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
	// "formerName");
	//
	// throwErrorIfSameParentAndPredecessor(parentIds, predecessorIds);
	// possiblyThrowErrorIfCircularDependencyDetected(parentIds, predecessorIds);
	// }
	//
	// private List<Integer> getIdsFromOrganisationLinkUsingNameInData(DataGroup dataGroup,
	// String nameInData) {
	// List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData(nameInData);
	// return getIdListOfParentsAndPredecessors(parents);
	// }
	//
	// private void throwErrorIfSameParentAndPredecessor(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// boolean sameIdInBothList = parentIds.stream().anyMatch(predecessorIds::contains);
	// if (sameIdInBothList) {
	// // throw SqlStorageException
	// // .withMessage("Organisation not updated due to same parent and predecessor");
	// }
	// }
	//
	// private void possiblyThrowErrorIfCircularDependencyDetected(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// List<Integer> parentsAndPredecessors = combineParentsAndPredecessors(parentIds,
	// predecessorIds);
	//
	// if (!parentsAndPredecessors.isEmpty()) {
	// throwErrorIfLinkToSelf(parentsAndPredecessors);
	// throwErrorIfCircularDependencyDetected(parentsAndPredecessors);
	// }
	// }
	//
	// private List<Integer> combineParentsAndPredecessors(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// List<Integer> combinedList = new ArrayList<>();
	// combinedList.addAll(parentIds);
	// combinedList.addAll(predecessorIds);
	// return combinedList;
	// }
	//
	// private void throwErrorIfLinkToSelf(List<Integer> parentsAndPredecessorIds) {
	// int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
	// if (parentsAndPredecessorIds.contains(organisationsId)) {
	// // throw SqlStorageException.withMessage("Organisation not updated due to link to
	// // self");
	// }
	// }
	//
	// private List<Integer> getIdListOfParentsAndPredecessors(
	// List<DataGroup> parentsAndPredecessors) {
	// List<Integer> organisationIds = new ArrayList<>();
	// for (DataGroup parent : parentsAndPredecessors) {
	// int organisationId = extractOrganisationIdFromLink(parent);
	// organisationIds.add(organisationId);
	// }
	// return organisationIds;
	// }
	//
	// private void throwErrorIfCircularDepen
	// possiblyThrowErrorIfDisallowedDependencyDetected(DataGroup dataGroup) {
	// List<Integer> parentIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
	// "parentOrganisation");
	// List<Integer> predecessorIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
	// "formerName");
	//
	// throwErrorIfSameParentAndPredecessor(parentIds, predecessorIds);
	// possiblyThrowErrorIfCircularDependencyDetected(parentIds, predecessorIds);
	// }
	//
	// private List<Integer> getIdsFromOrganisationLinkUsingNameInData(DataGroup dataGroup,
	// String nameInData) {
	// List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData(nameInData);
	// return getIdListOfParentsAndPredecessors(parents);
	// }
	//
	// private void throwErrorIfSameParentAndPredecessor(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// boolean sameIdInBothList = parentIds.stream().anyMatch(predecessorIds::contains);
	// if (sameIdInBothList) {
	//// throw SqlStorageException
	//// .withMessage("Organisation not updated due to same parent and predecessor");
	// }
	// }
	//
	// private void possiblyThrowErrorIfCircularDependencyDetected(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// List<Integer> parentsAndPredecessors = combineParentsAndPredecessors(parentIds,
	// predecessorIds);
	//
	// if (!parentsAndPredecessors.isEmpty()) {
	// throwErrorIfLinkToSelf(parentsAndPredecessors);
	// throwErrorIfCircularDependencyDetected(parentsAndPredecessors);
	// }
	// }
	//
	// private List<Integer> combineParentsAndPredecessors(List<Integer> parentIds,
	// List<Integer> predecessorIds) {
	// List<Integer> combinedList = new ArrayList<>();
	// combinedList.addAll(parentIds);
	// combinedList.addAll(predecessorIds);
	// return combinedList;
	// }
	//
	// private void throwErrorIfLinkToSelf(List<Integer> parentsAndPredecessorIds) {
	// int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
	// if (parentsAndPredecessorIds.contains(organisationsId)) {
	//// throw SqlStorageException.withMessage("Organisation not updated due to link to
	//// self");
	// }
	// }
	//
	// private List<Integer> getIdListOfParentsAndPredecessors(
	// List<DataGroup> parentsAndPredecessors) {
	// List<Integer> organisationIds = new ArrayList<>();
	// for (DataGroup parent : parentsAndPredecessors) {
	// int organisationId = extractOrganisationIdFromLink(parent);
	// organisationIds.add(organisationId);
	// }
	// return organisationIds;
	// }
	//
	// private void throwErrorIfCircularDependencyDetected(List<Integer> parentsAndPredecessorIds) {
	// StringJoiner questionMarkPart = getCorrectNumberOfConditionPlaceHolders(
	// parentsAndPredecessorIds);
	//
	// String sql = getSqlForFindingRecursion(questionMarkPart);
	// List<Object> values = createValuesForCircularDependencyQuery(parentsAndPredecessorIds);
	// executeAndThrowErrorIfCircularDependencyExist(sql, values);
	// }
	//
	// private StringJoiner getCorrectNumberOfConditionPlaceHolders(
	// List<Integer> parentsAndPredecessors) {
	// StringJoiner questionMarkPart = new StringJoiner(", ");
	// for (int i = 0; i < parentsAndPredecessors.size(); i++) {
	// questionMarkPart.add("?");
	// }
	// return questionMarkPart;
	// }
	//
	// private String getSqlForFindingRecursion(StringJoiner questionMarkPart) {
	// return "with recursive org_tree as (select distinct organisation_id, relation"
	// + " from organisationrelations where organisation_id in (" + questionMarkPart + ") "
	// + "union all" + " select distinct relation.organisation_id, relation.relation from"
	// + " organisationrelations as relation"
	// + " join org_tree as child on child.relation = relation.organisation_id)"
	// + " select * from org_tree where relation = ?";
	// }
	//
	// private void executeAndThrowErrorIfCircularDependencyExist(String sql, List<Object> values) {
	// List<Map<String, Object>> result = dataReader
	// .executePreparedStatementQueryUsingSqlAndValues(sql, values);
	// if (!result.isEmpty()) {
	//// throw SqlStorageException.withMessage(
	//// "Organisation not updated due to circular dependency with parent or predecessor");
	// }
	// }
	//
	// private List<Object> createValuesForCircularDependencyQuery(
	// List<Integer> parentsAndPredecessorIds) {
	// List<Object> values = new ArrayList<>();
	// addValuesForParentsAndPredecessors(parentsAndPredecessorIds, values);
	// addValueForOrganisationId(values);
	// return values;
	// }
	//
	// private void addValueForOrganisationId(List<Object> values) {
	// int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
	// values.add(organisationsId);
	// }
	//
	// private void addValuesForParentsAndPredecessors(List<Integer> parentsAndPredecessorIds,
	// List<Object> values) {
	// for (Integer parentId : parentsAndPredecessorIds) {
	// values.add(parentId);
	// }
	// }
	//
	// private int extractOrganisationIdFromLink(DataGroup parent) {
	// DataGroup parentLink = parent.getFirstGroupWithNameInData("organisationLink");
	// String linkedRecordId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	// return Integer.parseInt(linkedRecordId);
	// }dencyDetected(List<Integer> parentsAndPredecessorIds) {
	// StringJoiner questionMarkPart = getCorrectNumberOfConditionPlaceHolders(
	// parentsAndPredecessorIds);
	//
	// String sql = getSqlForFindingRecursion(questionMarkPart);
	// List<Object> values = createValuesForCircularDependencyQuery(parentsAndPredecessorIds);
	// executeAndThrowErrorIfCircularDependencyExist(sql, values);
	// }
	//
	// private StringJoiner getCorrectNumberOfConditionPlaceHolders(
	// List<Integer> parentsAndPredecessors) {
	// StringJoiner questionMarkPart = new StringJoiner(", ");
	// for (int i = 0; i < parentsAndPredecessors.size(); i++) {
	// questionMarkPart.add("?");
	// }
	// return questionMarkPart;
	// }
	//
	// private String getSqlForFindingRecursion(StringJoiner questionMarkPart) {
	// return "with recursive org_tree as (select distinct organisation_id, relation"
	// + " from organisationrelations where organisation_id in (" + questionMarkPart + ") "
	// + "union all" + " select distinct relation.organisation_id, relation.relation from"
	// + " organisationrelations as relation"
	// + " join org_tree as child on child.relation = relation.organisation_id)"
	// + " select * from org_tree where relation = ?";
	// }
	//
	// private void executeAndThrowErrorIfCircularDependencyExist(String sql, List<Object> values) {
	// List<Map<String, Object>> result = dataReader
	// .executePreparedStatementQueryUsingSqlAndValues(sql, values);
	// if (!result.isEmpty()) {
	// // throw SqlStorageException.withMessage(
	// // "Organisation not updated due to circular dependency with parent or predecessor");
	// }
	// }
	//
	// private List<Object> createValuesForCircularDependencyQuery(
	// List<Integer> parentsAndPredecessorIds) {
	// List<Object> values = new ArrayList<>();
	// addValuesForParentsAndPredecessors(parentsAndPredecessorIds, values);
	// addValueForOrganisationId(values);
	// return values;
	// }
	//
	// private void addValueForOrganisationId(List<Object> values) {
	// int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
	// values.add(organisationsId);
	// }
	//
	// private void addValuesForParentsAndPredecessors(List<Integer> parentsAndPredecessorIds,
	// List<Object> values) {
	// for (Integer parentId : parentsAndPredecessorIds) {
	// values.add(parentId);
	// }
	// }
	//
	// private int extractOrganisationIdFromLink(DataGroup parent) {
	// DataGroup parentLink = parent.getFirstGroupWithNameInData("organisationLink");
	// String linkedRecordId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	// return Integer.parseInt(linkedRecordId);
	// }

}
