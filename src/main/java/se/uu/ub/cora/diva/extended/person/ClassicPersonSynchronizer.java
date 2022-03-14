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
import se.uu.ub.cora.diva.classic.ClassicIndexer;
import se.uu.ub.cora.diva.classic.ClassicIndexerFactory;
import se.uu.ub.cora.diva.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.storage.RecordStorage;

public class ClassicPersonSynchronizer {

	protected static final String PERSON = "person";
	protected ClassicFedoraUpdaterFactory classicFedoraUpdaterFactory;
	protected ClassicIndexerFactory classicIndexerFactory;
	protected String recordType;
	protected RecordStorage recordStorage;

	protected void indexInClassic(String recordId) {
		ClassicIndexer classicIndexer = classicIndexerFactory.factor(PERSON);
		classicIndexer.index(recordId);
	}

	protected String getIdFromRecordInfo(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	public ClassicFedoraUpdaterFactory getClassicFedoraUpdaterFactory() {
		return classicFedoraUpdaterFactory;
	}

	public ClassicIndexerFactory getClassicIndexer() {
		return classicIndexerFactory;
	}

	public String getRecordType() {
		return recordType;
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}
}
