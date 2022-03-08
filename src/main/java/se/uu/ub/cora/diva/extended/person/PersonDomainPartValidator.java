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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.spider.record.DataException;

public class PersonDomainPartValidator implements ExtendedFunctionality {

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		DataGroup personDomainPart = data.dataGroup;
		throwErrorIfNoneOfNecessaryValueArePresent(personDomainPart);
	}

	private void throwErrorIfNoneOfNecessaryValueArePresent(DataGroup personDomainPart) {
		if (identifierNotPresent(personDomainPart) && affiliationNotPresent(personDomainPart)) {
			throw new DataException(
					"Data is not valid. One of identifier or affiliation must be present.");
		}
	}

	private boolean identifierNotPresent(DataGroup personDomainPart) {
		return !personDomainPart.containsChildWithNameInData("identifier");
	}

	private boolean affiliationNotPresent(DataGroup personDomainPart) {
		return !personDomainPart.containsChildWithNameInData("affiliation");
	}

}
