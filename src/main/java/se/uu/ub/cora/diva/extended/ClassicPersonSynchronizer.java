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
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexer;
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexerFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdater;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;

public class ClassicPersonSynchronizer implements ExtendedFunctionality {

	private static final String PERSON = "person";
	private ClassicFedoraUpdaterFactory classicFedoraUpdaterFactory;
	private ClassicIndexerFactory classicIndexerFactory;
	private String recordType;

	public ClassicPersonSynchronizer(ClassicFedoraUpdaterFactory classicFedoraUpdaterFactory,
			ClassicIndexerFactory classicIndexer, String recordType) {
		this.classicFedoraUpdaterFactory = classicFedoraUpdaterFactory;
		this.classicIndexerFactory = classicIndexer;
		this.recordType = recordType;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		String recordId = extractRecordId(dataGroup);
		updateInClassic(dataGroup, recordId);
		indexInClassic(recordId);
	}

	private String extractRecordId(DataGroup dataGroup) {
		String recordId = getIdFromRecordInfo(dataGroup);
		if ("personDomainPart".equals(recordType)) {
			return recordId.substring(0, recordId.lastIndexOf(":"));
		}
		return recordId;
	}

	private String getIdFromRecordInfo(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private void updateInClassic(DataGroup dataGroup, String recordId) {
		ClassicFedoraUpdater fedoraUpdater = classicFedoraUpdaterFactory.factor(PERSON);
		fedoraUpdater.updateInFedora(PERSON, recordId, dataGroup);
	}

	private void indexInClassic(String recordId) {
		ClassicIndexer classicIndexer = classicIndexerFactory.factor(PERSON);
		classicIndexer.index(recordId);
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

}
