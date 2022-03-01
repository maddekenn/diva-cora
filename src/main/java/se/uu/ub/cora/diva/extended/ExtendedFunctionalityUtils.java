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

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class ExtendedFunctionalityUtils {

	private ExtendedFunctionalityUtils() {
		// not called
		throw new UnsupportedOperationException();
	}

	public static void setNewRepeatIdsToEnsureUnique(DataGroup dataGroup, String nameInData) {
		int i = 0;
		for (DataElement repetable : dataGroup.getAllChildrenWithNameInData(nameInData)) {
			repetable.setRepeatId(String.valueOf(i));
			i++;
		}
	}

	public static List<String> getDataAtomicValuesAsList(List<DataAtomic> repeatableDataAtomics) {
		List<String> dataAtomicValues = new ArrayList<>();
		for (DataAtomic dataAtomic : repeatableDataAtomics) {
			dataAtomicValues.add(dataAtomic.getValue());
		}
		return dataAtomicValues;
	}
}
