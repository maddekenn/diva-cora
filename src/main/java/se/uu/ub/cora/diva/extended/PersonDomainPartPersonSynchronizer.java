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
package se.uu.ub.cora.diva.extended;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonDomainPartPersonSynchronizer implements ExtendedFunctionality {

	private static final String RECORD_INFO = "recordInfo";
	private RecordStorage recordStorage;

	public PersonDomainPartPersonSynchronizer(RecordStorage recordStorage) {
		this.recordStorage = recordStorage;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup dataGroup = data.dataGroup;
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		String personId = getPersonId(dataGroup);

		DataGroup readPerson = tryToReadPersonThrowErrorIfNotExists(personId);

		addIdAndRemoveTemporaryPersonLink(dataGroup, recordInfo, personId);
		updateDomainPartWithPublicValueFromPerson(dataGroup, readPerson);
	}

	private String getPersonId(DataGroup dataGroup) {
		DataGroup personLink = dataGroup.getFirstGroupWithNameInData("personLink");
		return personLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void addIdAndRemoveTemporaryPersonLink(DataGroup dataGroup, DataGroup recordInfo,
			String personId) {
		String domainPartId = getIdForDomainPart(recordInfo, personId);

		addIdToDomainPart(recordInfo, domainPartId);
		dataGroup.removeFirstChildWithNameInData("personLink");
	}

	private String getIdForDomainPart(DataGroup recordInfo, String personId) {
		String domain = recordInfo.getFirstAtomicValueWithNameInData("domain");
		return personId + ":" + domain;
	}

	private void addIdToDomainPart(DataGroup recordInfo, String domainPartId) {
		DataAtomic recordId = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id",
				domainPartId);
		recordInfo.addChild(recordId);
	}

	private DataGroup tryToReadPersonThrowErrorIfNotExists(String personIdPartOfId) {
		try {
			return recordStorage.read("person", personIdPartOfId);
		} catch (RecordNotFoundException exception) {
			throw new DataException("No person exists with record id " + personIdPartOfId
					+ ". PersonDomainPart was not created. " + exception.getMessage());
		}
	}

	private void updateDomainPartWithPublicValueFromPerson(DataGroup dataGroup,
			DataGroup readPerson) {
		String publicValue = getPublicValueFromPerson(readPerson);
		DataAtomic publicDataAtomic = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("public", publicValue);
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData(RECORD_INFO);
		recordInfo.addChild(publicDataAtomic);
	}

	private String getPublicValueFromPerson(DataGroup readPerson) {
		DataGroup recordInfo = readPerson.getFirstGroupWithNameInData(RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData("public");
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

}
