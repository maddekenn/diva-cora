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
package se.uu.ub.cora.diva.spies.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DataGroupSameOrganisationSpy implements DataGroup {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	private String domain = "";
	private String[] organisationIds = new String[0];
	private int numberOfCallsForLinkedRecordId = 0;

	public DataGroupSameOrganisationSpy(String domain, String... organisationIds) {
		this.domain = domain;
		this.organisationIds = organisationIds;
		// DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		// DataAtomicSpy atomicDomain = new DataAtomicSpy("domain", domain);
		//
		// DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart");
		// DataGroupSpy dataGroupAffiliation = new DataGroupSpy("affiliation");
		// DataGroupSpy dataGroupOrganisationLink = new DataGroupSpy("organisationLink");
		// DataAtomicSpy dataAtomicSpy = new DataAtomicSpy("organisationLink", "someValue");
		// dataGroupOrganisationLink.addChild(dataAtomicSpy);
		// dataGroupAffiliation.addChild(dataGroupOrganisationLink);
		// dataGroupSpy.addChild(dataGroupAffiliation);
	}

	@Override
	public void setRepeatId(String repeatId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRepeatId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNameInData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addChild(DataElement dataElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChildren(Collection<DataElement> dataElements) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DataElement> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataElement> getAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataElement> getAllChildrenWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataElement getFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		if (nameInData.equals("domain")) {
			MCR.addReturned(domain);
			return domain;
		}
		if (nameInData.equals("linkedRecordId")) {
			String orgIdToReturn = organisationIds[numberOfCallsForLinkedRecordId];
			numberOfCallsForLinkedRecordId++;
			MCR.addReturned(orgIdToReturn);
			return orgIdToReturn;
		}
		return null;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroup getFirstGroupWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		MCR.addReturned(this);
		return this;
	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		MCR.addCall("nameInData", nameInData);
		List<DataGroup> affiliationsToReturn = new ArrayList<>();
		for (int i = 0; i < organisationIds.length; i++) {
			affiliationsToReturn.add(this);
		}
		MCR.addReturned(affiliationsToReturn);
		return affiliationsToReturn;
	}

	@Override
	public Collection<DataGroup> getAllGroupsWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataAtomic getFirstDataAtomicWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

}
