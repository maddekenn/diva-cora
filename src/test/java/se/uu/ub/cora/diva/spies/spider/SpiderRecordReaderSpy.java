/*
 * Copyright 2021 Uppsala University Library
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
package se.uu.ub.cora.diva.spies.spider;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;
import se.uu.ub.cora.diva.spies.data.DataRecordSpy;
import se.uu.ub.cora.spider.record.RecordReader;

public class SpiderRecordReaderSpy implements RecordReader {
	public String authToken;
	public String type;
	public String id;
	public DataRecord dataRecord;

	@Override
	public DataRecord readRecord(String authToken, String type, String id) {
		this.authToken = authToken;
		this.type = type;
		this.id = id;

		DataGroupSpy person = new DataGroupSpy("person");
		DataGroup domainPart = createDomainPart("somePerson:uu");
		person.addChild(domainPart);
		DataGroup domainPart2 = createDomainPart("somePerson:test");
		person.addChild(domainPart2);

		List<DataElement> domainParts = new ArrayList<>();
		domainParts.add(domainPart);
		domainParts.add(domainPart2);
		person.childrenToReturn.put("personDomainPart", domainParts);

		dataRecord = new DataRecordSpy(person);
		return dataRecord;
	}

	public DataGroup createDomainPart(String linkedRecordId) {
		DataGroupSpy domainPart = new DataGroupSpy("personDomainPart");
		domainPart.addChild(new DataAtomicSpy("linkedRecordType", "personDomainPart"));
		domainPart.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));

		return domainPart;
	}

}
