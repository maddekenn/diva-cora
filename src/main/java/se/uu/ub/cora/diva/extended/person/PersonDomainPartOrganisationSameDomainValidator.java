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

import java.text.MessageFormat;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;
import se.uu.ub.cora.storage.RecordStorage;

/**
 * PersonDomainPartOrganisationSameDomainValidator ensures that all linked organisations from
 * affiliations belong to the same domain as the personDomainPart.
 * <p>
 * If any affiliation belongs to a different domain will a {@link DataException} be throw.
 */
public class PersonDomainPartOrganisationSameDomainValidator implements ExtendedFunctionality {

	private RecordStorage recordStorage;

	public static PersonDomainPartOrganisationSameDomainValidator usingRecordStorage(
			RecordStorage recordStorage) {
		return new PersonDomainPartOrganisationSameDomainValidator(recordStorage);
	}

	private PersonDomainPartOrganisationSameDomainValidator(RecordStorage recordStorage) {
		this.recordStorage = recordStorage;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup personDomainPart = data.dataGroup;
		List<DataGroup> affiliations = personDomainPart.getAllGroupsWithNameInData("affiliation");
		if (personDomainPartHasAffiliations(affiliations)) {
			checkDomainsFromAffiliationsAreTheSameAsInPersonDomainPart(personDomainPart,
					affiliations);
		}
	}

	private boolean personDomainPartHasAffiliations(List<DataGroup> affiliations) {
		return !affiliations.isEmpty();
	}

	private void checkDomainsFromAffiliationsAreTheSameAsInPersonDomainPart(
			DataGroup personDomainPart, List<DataGroup> affiliations) {
		String personDomainPartsDomain = getDomainFromRecord(personDomainPart);
		for (DataGroup affiliation : affiliations) {
			checkDomainFromAffiliationIsTheSameAsPersonDomainPart(personDomainPartsDomain,
					affiliation);
		}
	}

	private void checkDomainFromAffiliationIsTheSameAsPersonDomainPart(
			String personDomainPartsDomain, DataGroup affiliation) {
		String organisationId = getOrganisationIdFromAffiliation(affiliation);
		String organisationDomain = getDomainFromOrganisation(organisationId);

		throwExceptionIfDomainsAreDifferent(personDomainPartsDomain, organisationId,
				organisationDomain);
	}

	private boolean domainsAreDifferent(String personDomainPartsDomain, String organisationDomain) {
		return !personDomainPartsDomain.equals(organisationDomain);
	}

	private String getDomainFromOrganisation(String organisationId) {
		DataGroup organisation = recordStorage.read("organisation", organisationId);
		return getDomainFromRecord(organisation);
	}

	private String getOrganisationIdFromAffiliation(DataGroup affiliation) {
		DataGroup organisationLink = affiliation.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private String getDomainFromRecord(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("domain");
	}

	private void throwExceptionIfDomainsAreDifferent(String personDomainPartsDomain,
			String organisationId, String organisationDomain) {
		if (domainsAreDifferent(personDomainPartsDomain, organisationDomain)) {
			String message = buildExceptionMessage(personDomainPartsDomain, organisationId,
					organisationDomain);
			throw new DataException(message);
		}
	}

	private String buildExceptionMessage(String personDomainPartsDomain, String organisationId,
			String organisationDomain) {
		String message = "PersonDomainPart contains at least one linked organisation from a "
				+ "different domain. Linked organisation {0} has domain {1}, but "
				+ "PersonDomainPart has domain {2}.";
		return MessageFormat.format(message, organisationId, organisationDomain,
				personDomainPartsDomain);
	}

	RecordStorage getRecordStorageOnlyForTest() {
		return recordStorage;
	}
}
