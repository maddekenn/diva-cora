/*
 * Copyright 2020, 2021 Uppsala University Library
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
package se.uu.ub.cora.diva;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_RETURN;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.extended.ClassicOrganisationReloader;
import se.uu.ub.cora.diva.extended.LoggerFactorySpy;
import se.uu.ub.cora.diva.extended.OrganisationDifferentDomainDetector;
import se.uu.ub.cora.diva.extended.OrganisationDisallowedDependencyDetector;
import se.uu.ub.cora.diva.extended.OrganisationDuplicateLinksRemover;
import se.uu.ub.cora.diva.extended.PersonDomainPartValidator;
import se.uu.ub.cora.diva.extended.PersonUpdaterAfterDomainPartCreate;
import se.uu.ub.cora.diva.extended.SpiderDependencyProviderSpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.dependency.SpiderInitializationException;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;

public class DivaExtendedFunctionalityFactoryTest {

	private DivaExtendedFunctionalityFactory divaExtendedFunctionality;
	private Map<String, String> initInfo;
	private SpiderDependencyProviderSpy spiderDependencyProvider;
	private RecordStorageProviderSpy recordStorageProvider;
	private LoggerFactorySpy loggerFactorySpy;
	private SqlDatabaseFactorySpy databaseFactorySpy;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		divaExtendedFunctionality = new DivaExtendedFunctionalityFactory();
		initInfo = new HashMap<>();
		initInfo.put("databaseLookupName", "someDBName");
		initInfo.put("classicListUpdateURL", "someUpdateUrl");
		recordStorageProvider = new RecordStorageProviderSpy();
		spiderDependencyProvider = new SpiderDependencyProviderSpy(initInfo);
		spiderDependencyProvider.setRecordStorageProvider(recordStorageProvider);
		databaseFactorySpy = new SqlDatabaseFactorySpy();

		divaExtendedFunctionality.initializeUsingDependencyProvider(spiderDependencyProvider);
	}

	@Test
	public void testInit() {
		assertEquals(divaExtendedFunctionality.getExtendedFunctionalityContexts().size(), 8);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_BEFORE_STORE, "subOrganisation",
				0);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_AFTER_STORE, "subOrganisation",
				1);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_BEFORE_STORE, "topOrganisation",
				2);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_AFTER_STORE, "topOrganisation",
				3);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_BEFORE_STORE, "rootOrganisation",
				4);
		assertCorrectContextUsingPositionRecordTypeAndIndex(UPDATE_AFTER_STORE, "rootOrganisation",
				5);
		assertCorrectContextUsingPositionRecordTypeAndIndex(
				ExtendedFunctionalityPosition.CREATE_AFTER_METADATA_VALIDATION, "personDomainPart",
				6);
		assertCorrectContextUsingPositionRecordTypeAndIndex(CREATE_BEFORE_RETURN,
				"personDomainPart", 7);

		assertLookupNameAndSqlDatabaseFactory();
	}

	private void assertLookupNameAndSqlDatabaseFactory() {
		SqlDatabaseFactoryImp sqlDatabaseFactory1 = (SqlDatabaseFactoryImp) divaExtendedFunctionality
				.onlyForTestGetSqlDatabaseFactory();

		assertEquals(sqlDatabaseFactory1.onlyForTestGetLookupName(), "someDBName");

		SqlDatabaseFactoryImp sqlDatabaseFactory2 = (SqlDatabaseFactoryImp) divaExtendedFunctionality
				.onlyForTestGetSqlDatabaseFactory();

		assertSame(sqlDatabaseFactory1, sqlDatabaseFactory2);
	}

	private void assertCorrectContextUsingPositionRecordTypeAndIndex(
			ExtendedFunctionalityPosition position, String recordType, int index) {
		ExtendedFunctionalityContext updateBefore = divaExtendedFunctionality
				.getExtendedFunctionalityContexts().get(index);
		assertEquals(updateBefore.position, position);
		assertEquals(updateBefore.recordType, recordType);
		assertEquals(updateBefore.runAsNumber, 0);
	}

	@Test(expectedExceptions = SpiderInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "some error message from spy")
	public void testNoClassicListUpdateURL() {
		initInfo.remove("classicListUpdateURL");
		divaExtendedFunctionality.initializeUsingDependencyProvider(spiderDependencyProvider);
	}

	@Test
	public void factorSubOrganisationUpdateAfter() {
		divaExtendedFunctionality.onlyForTestSetSqlDatabaseFactory(databaseFactorySpy);

		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(UPDATE_BEFORE_STORE, "subOrganisation");
		assertCorrectFactoredFunctionalities(functionalities);
	}

	private void assertCorrectFactoredFunctionalities(List<ExtendedFunctionality> functionalities) {
		assertEquals(functionalities.size(), 3);
		assertTrue(functionalities.get(0) instanceof OrganisationDuplicateLinksRemover);

		OrganisationDisallowedDependencyDetector dependencyDetector = (OrganisationDisallowedDependencyDetector) functionalities
				.get(1);
		DatabaseFacade factoredDatabaseFacade = dependencyDetector.onlyForTestGetDatabaseFacade();
		assertTrue(factoredDatabaseFacade instanceof DatabaseFacade);

		databaseFactorySpy.MCR.assertReturn("factorDatabaseFacade", 0, factoredDatabaseFacade);

		OrganisationDifferentDomainDetector differentDomainDetector = (OrganisationDifferentDomainDetector) functionalities
				.get(2);
		assertNotNull(differentDomainDetector.getRecordStorage());
		assertSame(differentDomainDetector.getRecordStorage(), recordStorageProvider.recordStorage);
	}

	@Test
	public void factorRootOrganisationUpdateBefore() {
		divaExtendedFunctionality.onlyForTestSetSqlDatabaseFactory(databaseFactorySpy);
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(UPDATE_BEFORE_STORE, "rootOrganisation");
		assertCorrectFactoredFunctionalities(functionalities);
	}

	@Test
	public void factorTopOrganisationUpdateBefore() {
		divaExtendedFunctionality.onlyForTestSetSqlDatabaseFactory(databaseFactorySpy);
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(UPDATE_BEFORE_STORE, "topOrganisation");
		assertCorrectFactoredFunctionalities(functionalities);
	}

	@Test
	public void factorTopOrganisationForPositionNotHandled() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(UPDATE_AFTER_METADATA_VALIDATION, "topOrganisation");
		assertEquals(functionalities.size(), 0);
	}

	@Test
	public void factorClassicOrganisationUpdaterUpdateAfterStore() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(ExtendedFunctionalityPosition.UPDATE_AFTER_STORE, null);
		assertEquals(functionalities.size(), 1);
		assertTrue(functionalities.get(0) instanceof ClassicOrganisationReloader);
		ClassicOrganisationReloader functionality = (ClassicOrganisationReloader) functionalities
				.get(0);
		HttpHandlerFactory httpHandlerFactory = functionality.getHttpHandlerFactory();
		assertTrue(httpHandlerFactory instanceof HttpHandlerFactoryImp);
		assertEquals(functionality.getUrl(), initInfo.get("classicListUpdateURL"));
	}

	@Test
	public void factorPersonDomainPartUpdateAfterValidation() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(CREATE_AFTER_METADATA_VALIDATION, "personDomainPart");
		assertEquals(functionalities.size(), 1);
		PersonDomainPartValidator validatorFunctionality = (PersonDomainPartValidator) functionalities
				.get(0);
		assertSame(validatorFunctionality.getRecordStorage(), recordStorageProvider.recordStorage);

	}

	@Test
	public void factorPersonUpdateAfterPersonDomainPartCreate() {
		divaExtendedFunctionality.onlyForTestSetSqlDatabaseFactory(databaseFactorySpy);
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(CREATE_BEFORE_RETURN, "personDomainPart");
		assertEquals(functionalities.size(), 1);

		PersonUpdaterAfterDomainPartCreate personUpdater = (PersonUpdaterAfterDomainPartCreate) functionalities
				.get(0);

		assertSame(personUpdater.getRecordStorage(), recordStorageProvider.recordStorage);

		assertSame(personUpdater.getTermCollector(), spiderDependencyProvider.termCollector);
		assertSame(personUpdater.getLinkCollector(), spiderDependencyProvider.linkCollector);
	}
}
