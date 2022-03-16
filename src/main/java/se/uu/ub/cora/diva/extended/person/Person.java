package se.uu.ub.cora.diva.extended.person;

import se.uu.ub.cora.data.DataGroup;

public class Person {

	private String id;

	public void populateFromDataGroup(DataGroup personDataGroup) {
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		this.id = recordInfo.getFirstAtomicValueWithNameInData("id");

	}

	public String getId() {
		return id;
	}

}
