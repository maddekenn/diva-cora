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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.record.DataException;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;

public class PersonDomainPartValidator implements ExtendedFunctionality {

	private DatabaseFacade databaseFacade;

	public PersonDomainPartValidator(DatabaseFacade databaseFacade) {
		this.databaseFacade = databaseFacade;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		String personIdPartOfId = getPersonIdPartOfPersonDomainPart(dataGroup);
		List<Object> values = addIdAsValue(personIdPartOfId);
		tryToReadPerson(personIdPartOfId, values);
	}

	private void tryToReadPerson(String personIdPartOfId, List<Object> values) {
		try {
			databaseFacade.readOneRowOrFailUsingSqlAndValues(
					"select * from record_person where id = ?", values);

		} catch (SqlDatabaseException exception) {
			throw new DataException("No person exists with record id " + personIdPartOfId
					+ ". PersonDomainPart was not created. " + exception.getMessage());
		}
	}

	private String getPersonIdPartOfPersonDomainPart(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		String recordId = recordInfo.getFirstAtomicValueWithNameInData("id");
		return recordId.substring(0, recordId.lastIndexOf(":"));
	}

	private List<Object> addIdAsValue(String personIdPartOfId) {
		List<Object> values = new ArrayList<>();
		values.add(personIdPartOfId);
		return values;
	}

	public DatabaseFacade getDbFacade() {
		return databaseFacade;
	}

}
