package se.uu.ub.cora.diva.extended.person;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;

public class PersonTest {

	@Test
	public void testToJsonMinimum() {
		DataGroupSpy personDataGroup = new DataGroupSpy("person");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "authority-person:123"));
		recordInfo.addChild(new DataAtomicSpy("public", "yes"));
		personDataGroup.addChild(recordInfo);
		DataGroupSpy authorizedName = new DataGroupSpy("authorisedName");
		authorizedName.addChild(new DataAtomicSpy("familyName", "Testsson"));
		authorizedName.addChild(new DataAtomicSpy("givenName", "Testsson"));

		Person person = new Person();
		person.populateFromDataGroup(personDataGroup);
		assertEquals(person.getId(), "authority-person:123");

	}

}
