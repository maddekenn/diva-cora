/*
 * Copyright 2015, 2017, 2018, 2019 Uppsala University Library
 * Copyright 2017 Olov McKie
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
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.naming.InitialContext;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactoryImp;
import se.uu.ub.cora.gatekeeperclient.authentication.AuthenticatorImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.metacreator.extended.MetacreatorExtendedFunctionalityProvider;
import se.uu.ub.cora.solr.SolrClientProviderImp;
import se.uu.ub.cora.solrindex.SolrRecordIndexer;
import se.uu.ub.cora.solrsearch.SolrRecordSearch;
import se.uu.ub.cora.spider.authorization.PermissionRuleCalculator;
import se.uu.ub.cora.spider.record.RecordSearch;
import se.uu.ub.cora.spider.search.RecordIndexer;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.sqldatabase.RecordReaderFactoryImp;

public class DivaDependencyProviderTest {
	private DivaDependencyProvider dependencyProvider;
	private String basePath = "/tmp/divaRecordStorageOnDiskTemp/";
	private Map<String, String> initInfo;
	private LoggerFactorySpy loggerFactorySpy;

	@BeforeMethod
	public void setUp() throws Exception {
		try {
			loggerFactorySpy = new LoggerFactorySpy();
			LoggerProvider.setLoggerFactory(loggerFactorySpy);
			makeSureBasePathExistsAndIsEmpty();
			initInfo = new HashMap<>();
			initInfo.put("mixedStorageClassName", "se.uu.ub.cora.diva.RecordStorageSpy");
			initInfo.put("divaFedoraToCoraStorageClassName", "se.uu.ub.cora.diva.RecordStorageSpy");
			initInfo.put("divaDbToCoraStorageClassName", "se.uu.ub.cora.diva.RecordStorageSpy");
			initInfo.put("fedoraURL", "http://diva-cora-fedora:8088/fedora/");
			initInfo.put("fedoraUsername", "fedoraUser");
			initInfo.put("fedoraPassword", "fedoraPass");
			initInfo.put("storageOnDiskClassName", "se.uu.ub.cora.diva.RecordStorageSpy");
			initInfo.put("gatekeeperURL", "http://localhost:8080/gatekeeper/");
			initInfo.put("storageOnDiskBasePath", basePath);
			initInfo.put("solrURL", "http://localhost:8983/solr/stuff");
			initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");
			dependencyProvider = new DivaDependencyProvider(initInfo);

		} catch (Exception e) {
			// Make the correct tests crash instead of all
		}

	}

	public void makeSureBasePathExistsAndIsEmpty() throws IOException {
		File dir = new File(basePath);
		dir.mkdir();
		deleteFiles();
	}

	private void deleteFiles() throws IOException {
		Stream<Path> list;
		list = Files.list(Paths.get(basePath));
		list.forEach(p -> deleteFile(p));
		list.close();
	}

	private void deleteFile(Path path) {
		try {
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void removeTempFiles() throws IOException {
		if (Files.exists(Paths.get(basePath))) {
			deleteFiles();
			File dir = new File(basePath);
			dir.delete();
		}
	}

	@Test
	public void testInit() {
		assertNotNull(dependencyProvider.getSpiderAuthorizator());
		assertNotNull(dependencyProvider.getRecordStorage());
		assertNotNull(dependencyProvider.getIdGenerator());
		assertNotNull(dependencyProvider.getPermissionRuleCalculator());
		assertNotNull(dependencyProvider.getDataValidator());
		assertNotNull(dependencyProvider.getDataRecordLinkCollector());
		assertNotNull(dependencyProvider.getStreamStorage());
		assertNotNull(dependencyProvider.getExtendedFunctionalityProvider());
		assertTrue(dependencyProvider.getAuthenticator() instanceof AuthenticatorImp);
		assertTrue(dependencyProvider
				.getExtendedFunctionalityProvider() instanceof MetacreatorExtendedFunctionalityProvider);
		assertNotNull(dependencyProvider.getDataGroupTermCollector());
		assertTrue(dependencyProvider.getRecordIndexer() instanceof SolrRecordIndexer);
		SolrRecordSearch solrRecordSearch = (SolrRecordSearch) dependencyProvider.getRecordSearch();
		assertTrue(solrRecordSearch.getSearchStorage() instanceof RecordStorageSpy);
	}

	@Test
	public void testMixedStorage() throws Exception {
		RecordStorageSpy recordStorage = (RecordStorageSpy) dependencyProvider.getRecordStorage();
		assertTrue(recordStorage instanceof RecordStorageSpy);
		assertTrue(recordStorage.basicStorage instanceof RecordStorageSpy);
		assertTrue(recordStorage.divaFedoraToCoraStorage instanceof RecordStorageSpy);
		assertTrue(recordStorage.divaDbToCoraStorage instanceof RecordStorageSpy);
	}

	@Test
	public void testCorrectInitParametersUsedInFedoraToCoraStorage() throws Exception {
		RecordStorageSpy fedoraToCoraStorage = ((RecordStorageSpy) dependencyProvider
				.getRecordStorage()).divaFedoraToCoraStorage;
		assertTrue(fedoraToCoraStorage.httpHandlerFactory instanceof HttpHandlerFactoryImp);
		DivaFedoraConverterFactoryImp converterFactory = (DivaFedoraConverterFactoryImp) fedoraToCoraStorage.converterFactory;
		assertTrue(converterFactory instanceof DivaFedoraConverterFactoryImp);
		assertEquals(converterFactory.getFedoraURL(), initInfo.get("fedoraURL"));
		assertEquals(fedoraToCoraStorage.baseURL, initInfo.get("fedoraURL"));
		assertEquals(fedoraToCoraStorage.fedoraUsername, initInfo.get("fedoraUsername"));
		assertEquals(fedoraToCoraStorage.fedoraPassword, initInfo.get("fedoraPassword"));
	}

	@Test
	public void testDivaDbToRecordStorage() {
		RecordStorageSpy recordStorage = (RecordStorageSpy) dependencyProvider.getRecordStorage();
		DivaDbToCoraConverterFactory dbConverterFactory = recordStorage.divaDbToCoraStorage.dbConverterFactory;
		assertTrue(dbConverterFactory instanceof DivaDbToCoraConverterFactoryImp);

		RecordReaderFactoryImp readerFactory = (RecordReaderFactoryImp) recordStorage.divaDbToCoraStorage.readerFactory;

		DivaDbToCoraFactory divaDbToCoraFactory = recordStorage.divaDbToCoraStorage.divaDbToCoraFactory;
		assertTrue(divaDbToCoraFactory instanceof DivaDbToCoraFactoryImp);

		DivaDbToCoraFactoryImp divaDbToCoraFactoryImp = (DivaDbToCoraFactoryImp) divaDbToCoraFactory;
		assertEquals(divaDbToCoraFactoryImp.getReaderFactory(), readerFactory);
		assertEquals(divaDbToCoraFactoryImp.getConverterFactory(), dbConverterFactory);

		DataReaderImp dataReader = (DataReaderImp) recordStorage.divaDbToCoraStorage.dataReader;
		assertTrue(dataReader instanceof DataReaderImp);

		ContextConnectionProviderImp connectionProvider = (ContextConnectionProviderImp) readerFactory
				.getConnectionProvider();
		assertTrue(connectionProvider instanceof ContextConnectionProviderImp);

		assertEquals(connectionProvider.getName(), initInfo.get("databaseLookupName"));
		assertTrue(connectionProvider.getContext() instanceof InitialContext);
	}

	@Test
	public void testMissingMixedStorageClassNameInInitInfo() {
		initInfo.remove("mixedStorageClassName");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain mixedStorageClassName");
	}

	@Test
	public void testMissingDivaFedoraToCoraStorageClassNameInInitInfo() {
		initInfo.remove("divaFedoraToCoraStorageClassName");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(),
				"InitInfo must contain divaFedoraToCoraStorageClassName");
	}

	@Test
	public void testMissingFedoraURLInInitInfo() {
		initInfo.remove("fedoraURL");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain fedoraURL");
	}

	@Test
	public void testMissingFedoraUsernameInInitInfo() {
		initInfo.remove("fedoraUsername");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain fedoraUsername");
	}

	@Test
	public void testMissingFedoraPasswordInInitInfo() {
		initInfo.remove("fedoraPassword");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain fedoraPassword");
	}

	@Test
	public void testMissingStorageClassNameInInitInfo() {
		initInfo.remove("storageOnDiskClassName");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain storageOnDiskClassName");
	}

	private Exception callSystemOneDependencyProviderAndReturnResultingError() {
		Exception thrownException = null;
		try {
			dependencyProvider = new DivaDependencyProvider(initInfo);
		} catch (Exception e) {
			thrownException = e;
		}
		return thrownException;
	}

	@Test
	public void testNonExisitingStorageOnDiskClassNameInInitInfo() {
		initInfo.put("storageOnDiskClassName", "se.uu.ub.cora.systemone.RecordStorageNON");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "Error starting DivaDependencyProvider: "
				+ "se.uu.ub.cora.systemone.RecordStorageNON");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error starting DivaDependencyProvider: "
			+ "Invocation exception from RecordStorageErrorOnStartupSpy")
	public void testHandlingAndGettingCorrectErrorMessageFromErrorsThrowsOnStartup() {
		initInfo.put("divaFedoraToCoraStorageClassName",
				"se.uu.ub.cora.diva.RecordStorageInvocationErrorOnStartupSpy");
		dependencyProvider = new DivaDependencyProvider(initInfo);
	}

	@Test
	public void testCorrectStorageOnDiskClassInitialized() throws Exception {
		assertEquals(dependencyProvider.getRecordStorage().getClass().getName(),
				initInfo.get("storageOnDiskClassName"));
		assertTrue(dependencyProvider.getRecordStorage() instanceof RecordStorageSpy);
	}

	@Test
	public void testCorrectBasePathSentToStorageOnDisk() throws Exception {
		assertEquals(dependencyProvider.getRecordStorage().getClass().getName(),
				initInfo.get("storageOnDiskClassName"));
		assertEquals(((RecordStorageSpy) dependencyProvider.getRecordStorage()).basicStorage
				.getBasePath(), initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testGetPermissionRuleCalculator() {
		PermissionRuleCalculator permissionRuleCalculator = dependencyProvider
				.getPermissionRuleCalculator();
		PermissionRuleCalculator permissionRuleCalculator2 = dependencyProvider
				.getPermissionRuleCalculator();
		assertNotEquals(permissionRuleCalculator, permissionRuleCalculator2);
	}

	@Test
	public void testMissingBasePathInInitInfo() {
		initInfo.remove("storageOnDiskBasePath");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertEquals(thrownException.getMessage(), "InitInfo must contain storageOnDiskBasePath");
		assertTrue(thrownException instanceof RuntimeException);
	}

	@Test
	public void testMissingGatekeeperUrlInInitInfo() {
		initInfo.remove("gatekeeperURL");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertTrue(thrownException instanceof RuntimeException);
		assertEquals(thrownException.getMessage(), "InitInfo must contain gatekeeperURL");
	}

	@Test
	public void testtestGetAuthenticatorUsesGatekeeperUrl() {
		AuthenticatorImp authenticator = (AuthenticatorImp) dependencyProvider.getAuthenticator();
		assertNotNull(authenticator);
		assertEquals(authenticator.getBaseURL(), initInfo.get("gatekeeperURL"));
	}

	@Test
	public void testGetRecordSearch() {
		assertNotNull(dependencyProvider.getRecordSearch());
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testMissingSolrUrlInInitInfo() {
		Map<String, String> initInfo = new HashMap<>();
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		dependencyProvider = new DivaDependencyProvider(initInfo);
	}

	@Test
	public void testDependencyProviderReturnsOnlyOneInstanceOfRecordndexer() {
		RecordIndexer recordIndexer = dependencyProvider.getRecordIndexer();
		RecordIndexer recordIndexer2 = dependencyProvider.getRecordIndexer();
		assertEquals(recordIndexer, recordIndexer2);
	}

	@Test
	public void testMissingSolrURLInInitInfo() {
		initInfo.remove("solrURL");

		Exception thrownException = callSystemOneDependencyProviderAndReturnResultingError();

		assertEquals(thrownException.getMessage(), "InitInfo must contain solrURL");
		assertTrue(thrownException instanceof RuntimeException);
	}

	@Test
	public void testGetRecordIndexerUsesSolrUrlWhenCreatingSolrClientProvider() {
		SolrRecordIndexer recordIndexer = (SolrRecordIndexer) dependencyProvider.getRecordIndexer();
		SolrClientProviderImp solrClientProviderImp = (SolrClientProviderImp) recordIndexer
				.getSolrClientProvider();
		assertEquals(solrClientProviderImp.getBaseURL(), "http://localhost:8983/solr/stuff");
	}

	@Test
	public void testGetRecordSearchUsesSolrUrlWhenCreatingSolrClientProvider() {
		SolrRecordSearch recordSearcher = (SolrRecordSearch) dependencyProvider.getRecordSearch();
		SolrClientProviderImp solrClientProviderImp = (SolrClientProviderImp) recordSearcher
				.getSolrClientProvider();
		assertEquals(solrClientProviderImp.getBaseURL(), "http://localhost:8983/solr/stuff");
	}

	@Test
	public void testDependencyProviderReturnsDifferentRecordSearch() {
		RecordSearch recordSearch = dependencyProvider.getRecordSearch();
		RecordSearch recordSearch2 = dependencyProvider.getRecordSearch();
		assertNotEquals(recordSearch, recordSearch2);
	}

}