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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.storage.RecordStorage;

public class PersonUpdaterAfterDomainPartCreate implements ExtendedFunctionality {

	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private RecordStorage recordStorage;

	public PersonUpdaterAfterDomainPartCreate(RecordStorage recordStorage) {
		this.recordStorage = recordStorage;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		String recordId = extractRecordId(dataGroup);

		String personIdPartOfId = recordId.substring(0, recordId.lastIndexOf(":"));
		DataGroup readPerson = recordStorage.read("person", personIdPartOfId);

		createAndAddPersonDomainPartToPerson(recordId, readPerson);
		setNewRepeatIdForAllDomainPartToEnsureUnique(readPerson);
		String dataDivider = extractDataDivider(readPerson);

		recordStorage.update("person", personIdPartOfId, readPerson, null, null, dataDivider);
	}

	private String extractDataDivider(DataGroup readPerson) {
		DataGroup recordInfo = readPerson.getFirstGroupWithNameInData("recordInfo");
		DataGroup dataDividerGroup = recordInfo.getFirstGroupWithNameInData("dataDivider");
		String dataDivider = dataDividerGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
		return dataDivider;
	}

	private String extractRecordId(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private void createAndAddPersonDomainPartToPerson(String recordId, DataGroup readPerson) {
		DataGroup personDomainPartLink = DataGroupProvider
				.getDataGroupAsLinkUsingNameInDataTypeAndId(PERSON_DOMAIN_PART, PERSON_DOMAIN_PART,
						recordId);
		readPerson.addChild(personDomainPartLink);
	}

	private void setNewRepeatIdForAllDomainPartToEnsureUnique(DataGroup readPerson) {
		int counter = 0;
		for (DataGroup personDomainPart : readPerson
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART)) {
			personDomainPart.setRepeatId(String.valueOf(counter));
			counter++;
		}
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

}
