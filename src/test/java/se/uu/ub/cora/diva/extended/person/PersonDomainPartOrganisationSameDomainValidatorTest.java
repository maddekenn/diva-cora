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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.spies.data.DataGroupSameOrganisationSpy;
import se.uu.ub.cora.diva.spies.storage.RecordStorageSameOrganisationSpy;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class PersonDomainPartOrganisationSameDomainValidatorTest {

	private ExtendedFunctionalityData data;
	private ExtendedFunctionality extFunctionality;
	private RecordStorageSameOrganisationSpy recordStorage;

	@BeforeMethod
	public void beforeMethod() {
		recordStorage = new RecordStorageSameOrganisationSpy();
		extFunctionality = PersonDomainPartOrganisationSameDomainValidator
				.usingRecordStorage(recordStorage);
		data = new ExtendedFunctionalityData();
	}

	@Test
	public void testWithoutAffiliations() throws Exception {
		DataGroupSameOrganisationSpy sameOrgGroupSpy = new DataGroupSameOrganisationSpy("uu");
		data.dataGroup = sameOrgGroupSpy;

		extFunctionality.useExtendedFunctionality(data);

		MethodCallRecorder MCR = sameOrgGroupSpy.MCR;
		MCR.assertParameters("getAllGroupsWithNameInData", 0, "affiliation");
		MCR.assertMethodNotCalled("getFirstGroupWithNameInData");
	}

	@Test
	public void testWithThreeAffiliationSameDomain() throws Exception {
		DataGroupSameOrganisationSpy sameOrgGroupSpy = new DataGroupSameOrganisationSpy("uu",
				"1000", "2000", "3000");
		data.dataGroup = sameOrgGroupSpy;
		recordStorage.setDomainsToReturnOnRead("uu", "uu", "uu");
		extFunctionality.useExtendedFunctionality(data);

		sameOrgGroupSpy.MCR.assertParameters("getAllGroupsWithNameInData", 0, "affiliation");

		sameOrgGroupSpy.MCR.assertParameters("getFirstGroupWithNameInData", 0, "recordInfo");
		sameOrgGroupSpy.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "domain");

		assertOrganisationIsReadFromStorageBasedOnLinkedOrganisation2(sameOrgGroupSpy.MCR, 1);
		assertOrganisationIsReadFromStorageBasedOnLinkedOrganisation2(sameOrgGroupSpy.MCR, 2);
		assertOrganisationIsReadFromStorageBasedOnLinkedOrganisation2(sameOrgGroupSpy.MCR, 3);
		recordStorage.MCR.assertNumberOfCallsToMethod("read", 3);

	}

	private void assertOrganisationIsReadFromStorageBasedOnLinkedOrganisation2(
			MethodCallRecorder sameOrgGroupSpyMCR, int callNumber) {
		int callNumberForRecordStorage = callNumber - 1;
		var organisationId = assertAndReturnOrganisationIsReadFromStorageBasedOnLinkedOrganisation(
				sameOrgGroupSpyMCR, callNumber);

		assertOrganisationReadFromStorageAndDomainExtracted(callNumberForRecordStorage,
				organisationId);
	}

	private void assertOrganisationReadFromStorageAndDomainExtracted(int callNumberForRecordStorage,
			Object organisationId) {
		recordStorage.MCR.assertParameters("read", callNumberForRecordStorage, "organisation",
				organisationId);

		DataGroupSameOrganisationSpy organisationSpy = (DataGroupSameOrganisationSpy) recordStorage.MCR
				.getReturnValue("read", callNumberForRecordStorage);
		organisationSpy.MCR.assertParameters("getFirstGroupWithNameInData", 0, "recordInfo");
		organisationSpy.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "domain");
	}

	private Object assertAndReturnOrganisationIsReadFromStorageBasedOnLinkedOrganisation(
			MethodCallRecorder sameOrgGroupSpyMCR, int callNumber) {
		sameOrgGroupSpyMCR.assertParameters("getFirstGroupWithNameInData", callNumber,
				"organisationLink");
		sameOrgGroupSpyMCR.assertParameters("getFirstAtomicValueWithNameInData", callNumber,
				"linkedRecordId");
		var organisationId = sameOrgGroupSpyMCR.getReturnValue("getFirstAtomicValueWithNameInData",
				callNumber);
		return organisationId;
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "PersonDomainPart contains at least one linked organisation from a different domain. "
			+ "Linked organisation 1750 has domain kth, but PersonDomainPart has domain uu")
	public void testWithOneAffiliationOtherDomain() throws Exception {
		DataGroupSameOrganisationSpy sameOrgGroupSpy = new DataGroupSameOrganisationSpy("uu",
				"1750");
		data.dataGroup = sameOrgGroupSpy;
		recordStorage.setDomainsToReturnOnRead("kth");

		extFunctionality.useExtendedFunctionality(data);
		recordStorage.MCR.assertNumberOfCallsToMethod("read", 1);
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "PersonDomainPart contains at least one linked organisation from a different domain. "
			+ "Linked organisation 3000 has domain kth, but PersonDomainPart has domain uu")
	public void testWithThreeAffiliationOneFromOtherDomain() throws Exception {
		DataGroupSameOrganisationSpy sameOrgGroupSpy = new DataGroupSameOrganisationSpy("uu",
				"1000", "2000", "3000");
		data.dataGroup = sameOrgGroupSpy;
		recordStorage.setDomainsToReturnOnRead("uu", "uu", "kth");

		extFunctionality.useExtendedFunctionality(data);
		recordStorage.MCR.assertNumberOfCallsToMethod("read", 3);
	}
}
