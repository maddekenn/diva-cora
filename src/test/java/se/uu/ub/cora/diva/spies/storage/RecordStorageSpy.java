/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.spies.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class RecordStorageSpy implements RecordStorage {

	public List<String> readRecordTypes = new ArrayList<>();
	public List<String> readRecordIds = new ArrayList<>();
	public List<String> updatedRecordTypes = new ArrayList<>();
	public List<String> updatedRecordIds = new ArrayList<>();
	public List<DataGroup> dataGroupsSentToUpdate = new ArrayList<>();
	public List<DataGroup> returnedDataGroups = new ArrayList<>();
	public Map<String, DataGroupExtendedSpy> returnOnRead = new HashMap<>();
	public List<DataGroup> collectedTermsList = new ArrayList<>();
	public List<String> dataDividers = new ArrayList<>();
	public List<DataGroup> linkLists = new ArrayList<>();
	public boolean throwRecordNotFoundException = false;

	@Override
	public DataGroup read(String type, String id) {
		readRecordTypes.add(type);
		readRecordIds.add(id);
		if (throwRecordNotFoundException) {
			throw new RecordNotFoundException("Error from record storage spy");
		}
		if (returnOnRead.containsKey(type + "_" + id)) {
			DataGroupExtendedSpy presetReturnValue = returnOnRead.get(type + "_" + id);
			returnedDataGroups.add(presetReturnValue);
			return presetReturnValue;
		}
		DataGroupExtendedSpy dataGroupToReturn = new DataGroupExtendedSpy(type + "_" + id);
		returnedDataGroups.add(dataGroupToReturn);
		return dataGroupToReturn;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		collectedTermsList.add(collectedTerms);
		linkLists.add(linkList);
		dataDividers.add(dataDivider);
		updatedRecordTypes.add(type);
		updatedRecordIds.add(id);
		dataGroupsSentToUpdate.add(record);
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		// TODO Auto-generated method stub
		return 0;
	}

}
