/*
 * Copyright 2015, 2016, 2017, 2018, 2019 Uppsala University Library
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import se.uu.ub.cora.beefeater.AuthorizatorImp;
import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollectorImp;
import se.uu.ub.cora.bookkeeper.storage.MetadataStorage;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollectorImp;
import se.uu.ub.cora.bookkeeper.validator.DataValidator;
import se.uu.ub.cora.bookkeeper.validator.DataValidatorImp;
import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactoryImp;
import se.uu.ub.cora.gatekeeperclient.authentication.AuthenticatorImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.metacreator.extended.MetacreatorExtendedFunctionalityProvider;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.solr.SolrClientProviderImp;
import se.uu.ub.cora.solrindex.SolrRecordIndexer;
import se.uu.ub.cora.solrsearch.SolrRecordSearch;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.authorization.BasePermissionRuleCalculator;
import se.uu.ub.cora.spider.authorization.PermissionRuleCalculator;
import se.uu.ub.cora.spider.authorization.SpiderAuthorizator;
import se.uu.ub.cora.spider.authorization.SpiderAuthorizatorImp;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.extended.ExtendedFunctionalityProvider;
import se.uu.ub.cora.spider.record.RecordSearch;
import se.uu.ub.cora.spider.record.storage.RecordIdGenerator;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.spider.record.storage.TimeStampIdGenerator;
import se.uu.ub.cora.spider.role.RulesProvider;
import se.uu.ub.cora.spider.role.RulesProviderImp;
import se.uu.ub.cora.spider.search.RecordIndexer;
import se.uu.ub.cora.spider.stream.storage.StreamStorage;
import se.uu.ub.cora.sqldatabase.DataReader;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordReaderFactoryImp;
import se.uu.ub.cora.storage.StreamStorageOnDisk;

public class DivaDependencyProvider extends SpiderDependencyProvider {

	private RecordStorage recordStorage;
	private MetadataStorage metadataStorage;
	private RecordIdGenerator idGenerator;
	private StreamStorage streamStorage;
	private String gatekeeperUrl;
	private String solrUrl;
	private SolrRecordIndexer solrRecordIndexer;
	private SolrClientProviderImp solrClientProvider;
	private SearchStorage searchStorage;
	private String basePath;
	private String storageOnDiskClassName;
	private String mixedStorageClassName;
	private String fedoraURL;
	private String fedoraUsername;
	private String fedoraPassword;
	private String divaFedoraToCoraStorageClassName;
	private String divaDbToCoraStorageClassName;
	private String databaseLookupName;

	public DivaDependencyProvider(Map<String, String> initInfo) {
		super(initInfo);
	}

	@Override
	protected void readInitInfo() {
		mixedStorageClassName = tryToGetInitParameter("mixedStorageClassName");
		fedoraURL = tryToGetInitParameter("fedoraURL");
		fedoraUsername = tryToGetInitParameter("fedoraUsername");
		fedoraPassword = tryToGetInitParameter("fedoraPassword");
		divaFedoraToCoraStorageClassName = tryToGetInitParameter(
				"divaFedoraToCoraStorageClassName");
		divaDbToCoraStorageClassName = tryToGetInitParameter("divaDbToCoraStorageClassName");
		gatekeeperUrl = tryToGetInitParameter("gatekeeperURL");
		basePath = tryToGetInitParameter("storageOnDiskBasePath");
		storageOnDiskClassName = tryToGetStorageOnDiskClassName();
		solrUrl = tryToGetInitParameter("solrURL");
		databaseLookupName = tryToGetInitParameter("databaseLookupName");
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			throw new RuntimeException("InitInfo must contain " + key);
		}
	}

	private String tryToGetStorageOnDiskClassName() {
		throwErrorIfMissingKeyIsMissingFromInitInfo("storageOnDiskClassName");
		return initInfo.get("storageOnDiskClassName");
	}

	private void throwErrorIfMissingKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			throw new RuntimeException("InitInfo must contain " + key);
		}
	}

	@Override
	protected void tryToInitialize() throws NoSuchMethodException, ClassNotFoundException,
			IllegalAccessException, InvocationTargetException, NamingException {
		RecordStorage basicStorage = tryToCreateRecordStorage();
		RecordStorage divaFedoraToCoraStorage = tryToCreateDivaFedoraToCoraStorage();
		RecordStorage divaDbToCoraStorage = tryToCreateDivaDbToCoraStorage();
		recordStorage = tryToCreateMixedRecordStorage(basicStorage, divaFedoraToCoraStorage,
				divaDbToCoraStorage);

		metadataStorage = (MetadataStorage) basicStorage;
		idGenerator = new TimeStampIdGenerator();
		streamStorage = StreamStorageOnDisk.usingBasePath(basePath + "streams/");
		solrClientProvider = SolrClientProviderImp.usingBaseUrl(solrUrl);
		solrRecordIndexer = SolrRecordIndexer
				.createSolrRecordIndexerUsingSolrClientProvider(solrClientProvider);
		searchStorage = (SearchStorage) basicStorage;
	}

	private RecordStorage tryToCreateRecordStorage() throws NoSuchMethodException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		Class<?>[] cArg = new Class[1];
		cArg[0] = String.class;
		Method constructor = Class.forName(storageOnDiskClassName)
				.getMethod("createRecordStorageOnDiskWithBasePath", cArg);
		return (RecordStorage) constructor.invoke(null, basePath);
	}

	private RecordStorage tryToCreateDivaFedoraToCoraStorage() throws NoSuchMethodException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		Class<?>[] cArg = new Class[5];
		cArg[0] = HttpHandlerFactory.class;
		cArg[1] = DivaFedoraConverterFactory.class;
		cArg[2] = String.class;
		cArg[3] = String.class;
		cArg[4] = String.class;
		Method constructor = Class.forName(divaFedoraToCoraStorageClassName).getMethod(
				"usingHttpHandlerFactoryAndConverterFactoryAndBaseURLAndUsernameAndPassword", cArg);
		return (RecordStorage) constructor.invoke(null, new HttpHandlerFactoryImp(),
				DivaFedoraConverterFactoryImp.usingFedoraURL(fedoraURL), fedoraURL, fedoraUsername,
				fedoraPassword);
	}

	private RecordStorage tryToCreateDivaDbToCoraStorage()
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NamingException {
		Class<?>[] cArg = new Class[4];
		cArg[0] = RecordReaderFactory.class;
		cArg[1] = DivaDbToCoraConverterFactory.class;
		cArg[2] = DivaDbToCoraFactory.class;
		cArg[3] = DataReader.class;
		SqlConnectionProvider connectionProvider = createConnectionProvider();

		Method constructor = Class.forName(divaDbToCoraStorageClassName).getMethod(
				"usingRecordReaderFactoryConverterFactoryAndDbToCoraFactoryAndDataReader", cArg);
		RecordReaderFactoryImp recordReaderFactoryImp = new RecordReaderFactoryImp(
				connectionProvider);
		DataReaderImp dataReader = DataReaderImp.usingSqlConnectionProvider(connectionProvider);
		DivaDbToCoraConverterFactoryImp divaDbToCoraConverterFactoryImp = new DivaDbToCoraConverterFactoryImp();

		return (RecordStorage) constructor.invoke(null, recordReaderFactoryImp,
				divaDbToCoraConverterFactoryImp,
				new DivaDbToCoraFactoryImp(recordReaderFactoryImp, divaDbToCoraConverterFactoryImp),
				dataReader);
	}

	private SqlConnectionProvider createConnectionProvider() throws NamingException {
		InitialContext context = new InitialContext();
		return ContextConnectionProviderImp.usingInitialContextAndName(context, databaseLookupName);
	}

	private RecordStorage tryToCreateMixedRecordStorage(RecordStorage basicStorage,
			RecordStorage divaFedoraToCoraStorage, RecordStorage divaDbToCoraStorage)
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException {
		Class<?>[] cArg = new Class[3];
		cArg[0] = RecordStorage.class;
		cArg[1] = RecordStorage.class;
		cArg[2] = RecordStorage.class;
		Method constructor = Class.forName(mixedStorageClassName)
				.getMethod("usingBasicAndFedoraAndDbStorage", cArg);
		return (RecordStorage) constructor.invoke(null, basicStorage, divaFedoraToCoraStorage,
				divaDbToCoraStorage);
	}

	@Override
	public SpiderAuthorizator getSpiderAuthorizator() {
		RulesProvider rulesProvider = new RulesProviderImp(recordStorage);
		return SpiderAuthorizatorImp.usingSpiderDependencyProviderAndAuthorizatorAndRulesProvider(
				this, new AuthorizatorImp(), rulesProvider);
	}

	@Override
	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

	@Override
	public RecordIdGenerator getIdGenerator() {
		return idGenerator;
	}

	@Override
	public PermissionRuleCalculator getPermissionRuleCalculator() {
		return new BasePermissionRuleCalculator();
	}

	@Override
	public DataValidator getDataValidator() {
		return new DataValidatorImp(metadataStorage);
	}

	@Override
	public DataRecordLinkCollector getDataRecordLinkCollector() {
		return new DataRecordLinkCollectorImp(metadataStorage);
	}

	@Override
	public StreamStorage getStreamStorage() {
		return streamStorage;
	}

	@Override
	public ExtendedFunctionalityProvider getExtendedFunctionalityProvider() {
		return new MetacreatorExtendedFunctionalityProvider(this);
	}

	@Override
	public Authenticator getAuthenticator() {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		return AuthenticatorImp.usingBaseUrlAndHttpHandlerFactory(gatekeeperUrl,
				httpHandlerFactory);
	}

	@Override
	public RecordSearch getRecordSearch() {
		return SolrRecordSearch.createSolrRecordSearchUsingSolrClientProviderAndSearchStorage(
				solrClientProvider, searchStorage);
	}

	@Override
	public DataGroupTermCollector getDataGroupTermCollector() {
		return new DataGroupTermCollectorImp(metadataStorage);
	}

	@Override
	public RecordIndexer getRecordIndexer() {
		return solrRecordIndexer;
	}

}