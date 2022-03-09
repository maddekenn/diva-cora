/*
 * Copyright 2020, 2021, 2022 Uppsala University Library
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
package se.uu.ub.cora.diva.extended.organisation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_RETURN;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.DELETE_AFTER;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.spies.LoggerFactorySpy;
import se.uu.ub.cora.diva.spies.db.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.spies.spider.SpiderDependencyProviderSpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.dependency.SpiderInitializationException;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaExtendedOrganisationFunctionalityFactoryTest {

	private DivaExtendedOrganisationFunctionalityFactory divaExtendedFunctionality;
	private Map<String, String> initInfo;
	private SpiderDependencyProviderSpy spiderDependencyProvider;
	private LoggerFactorySpy loggerFactorySpy;
	private SqlDatabaseFactorySpy databaseFactorySpy;
	private RecordStorage recordStorage;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		divaExtendedFunctionality = new DivaExtendedOrganisationFunctionalityFactory();
		setUpInitInfo();
		spiderDependencyProvider = new SpiderDependencyProviderSpy(initInfo);
		recordStorage = spiderDependencyProvider.getRecordStorage();
		databaseFactorySpy = new SqlDatabaseFactorySpy();

		divaExtendedFunctionality.initializeUsingDependencyProvider(spiderDependencyProvider);
	}

	private void setUpInitInfo() {
		initInfo = new HashMap<>();
		initInfo.put("databaseLookupName", "someDBName");
		initInfo.put("classicListUpdateURL", "someUpdateUrl");
		initInfo.put("fedoraURL", "someFedoraUrl");
		initInfo.put("fedoraUsername", "someUsername");
		initInfo.put("fedoraPassword", "somePassword");
		initInfo.put("authorityIndexUrl", "classicAuthorityIndexUrl");
	}

	@Test
	public void testInit() {
		assertEquals(divaExtendedFunctionality.getExtendedFunctionalityContexts().size(), 6);

		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_BEFORE_STORE,
				"subOrganisation", 0);
		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_AFTER_STORE,
				"subOrganisation", 0);
		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_BEFORE_STORE,
				"topOrganisation", 0);
		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_AFTER_STORE,
				"topOrganisation", 0);
		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_BEFORE_STORE,
				"rootOrganisation", 0);
		assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(UPDATE_AFTER_STORE,
				"rootOrganisation", 0);

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

	private void assertCorrectContextUsingPositionRecordTypeAndRunAsNumber(
			ExtendedFunctionalityPosition position, String recordType, int runAsNumber) {
		List<ExtendedFunctionalityContext> extendedFunctionalityContexts = divaExtendedFunctionality
				.getExtendedFunctionalityContexts();
		int foundTimes = 0;
		for (ExtendedFunctionalityContext context : extendedFunctionalityContexts) {
			if (extendedFunctionalityExists(position, recordType, runAsNumber, context)) {
				foundTimes++;
			}
		}
		assertEquals(foundTimes, 1);
	}

	private boolean extendedFunctionalityExists(ExtendedFunctionalityPosition position,
			String recordType, int runAsNumber, ExtendedFunctionalityContext context) {
		return context.position.equals(position) && context.recordType.equals(recordType)
				&& context.runAsNumber == runAsNumber;
	}

	@Test(expectedExceptions = SpiderInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "some error message from spy")
	public void testNoClassicListUpdateURL() {
		initInfo.remove("classicListUpdateURL");
		divaExtendedFunctionality.initializeUsingDependencyProvider(spiderDependencyProvider);
	}

	@Test
	public void factorSubOrganisationUpdateBeforeStore() {
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
		assertSame(differentDomainDetector.getRecordStorage(), recordStorage);
	}

	@Test
	public void factorRootOrganisationUpdateBeforeStore() {
		divaExtendedFunctionality.onlyForTestSetSqlDatabaseFactory(databaseFactorySpy);
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(UPDATE_BEFORE_STORE, "rootOrganisation");
		assertCorrectFactoredFunctionalities(functionalities);
	}

	@Test
	public void factorTopOrganisationUpdateBeforeStore() {
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
	public void factorClassicOrganisationUpdaterUpdateAfterStoreForSubOrganisation() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(ExtendedFunctionalityPosition.UPDATE_AFTER_STORE, "subOrganisation");
		assertEquals(functionalities.size(), 1);
		assertTrue(functionalities.get(0) instanceof ClassicOrganisationReloader);
		ClassicOrganisationReloader functionality = (ClassicOrganisationReloader) functionalities
				.get(0);
		HttpHandlerFactory httpHandlerFactory = functionality.getHttpHandlerFactory();
		assertTrue(httpHandlerFactory instanceof HttpHandlerFactoryImp);
		assertEquals(functionality.getUrl(), initInfo.get("classicListUpdateURL"));
	}

	@Test
	public void factorClassicOrganisationUpdaterUpdateAfterStoreForRootOrganisation() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(ExtendedFunctionalityPosition.UPDATE_AFTER_STORE, "rootOrganisation");
		assertEquals(functionalities.size(), 1);
		assertTrue(functionalities.get(0) instanceof ClassicOrganisationReloader);
	}

	@Test
	public void factorClassicOrganisationUpdaterUpdateAfterStoreForTopOrganisation() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(ExtendedFunctionalityPosition.UPDATE_AFTER_STORE, "topOrganisation");
		assertEquals(functionalities.size(), 1);
		assertTrue(functionalities.get(0) instanceof ClassicOrganisationReloader);
	}

	@Test
	public void factorCreateAfterValidationOtherType() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(CREATE_AFTER_METADATA_VALIDATION, "otherType");
		assertEquals(functionalities.size(), 0);
	}

	@Test
	public void factorCreateBeforeReturnOtherType() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality
				.factor(CREATE_BEFORE_RETURN, "otherType");
		assertEquals(functionalities.size(), 0);
	}

	@Test
	public void factorDeleteAfterOtherType() {
		List<ExtendedFunctionality> functionalities = divaExtendedFunctionality.factor(DELETE_AFTER,
				"otherType");
		assertEquals(functionalities.size(), 0);
	}

}
