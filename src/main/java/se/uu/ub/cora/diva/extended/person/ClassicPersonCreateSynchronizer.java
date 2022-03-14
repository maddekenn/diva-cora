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
import se.uu.ub.cora.diva.classic.ClassicIndexerFactory;
import se.uu.ub.cora.diva.fedora.ClassicFedoraUpdater;
import se.uu.ub.cora.diva.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;
import se.uu.ub.cora.storage.RecordStorage;

public class ClassicPersonCreateSynchronizer extends ClassicPersonSynchronizer
		implements ExtendedFunctionality {

	public ClassicPersonCreateSynchronizer(ClassicFedoraUpdaterFactory classicFedoraUpdaterFactory,
			ClassicIndexerFactory classicIndexer, String recordType, RecordStorage recordStorage) {
		this.classicFedoraUpdaterFactory = classicFedoraUpdaterFactory;
		this.classicIndexerFactory = classicIndexer;
		this.recordType = recordType;
		this.recordStorage = recordStorage;
	}

	@Override
	public void useExtendedFunctionality(ExtendedFunctionalityData data) {
		String recordId = getIdFromRecordInfo(data.dataGroup);
		DataGroup dataGroup = data.dataGroup;
		createInClassic(dataGroup, recordId);
		indexInClassic(recordId);
	}

	private void createInClassic(DataGroup dataGroup, String recordId) {
		ClassicFedoraUpdater fedoraUpdater = classicFedoraUpdaterFactory.factor(PERSON);
		fedoraUpdater.createInFedora(PERSON, recordId, dataGroup);
	}

}
