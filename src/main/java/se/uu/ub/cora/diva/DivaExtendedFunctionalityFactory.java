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

import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_RETURN;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.DELETE_BEFORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.diva.extended.ClassicOrganisationReloader;
import se.uu.ub.cora.diva.extended.ClassicPersonSynchronizer;
import se.uu.ub.cora.diva.extended.OrganisationDifferentDomainDetector;
import se.uu.ub.cora.diva.extended.OrganisationDisallowedDependencyDetector;
import se.uu.ub.cora.diva.extended.OrganisationDuplicateLinksRemover;
import se.uu.ub.cora.diva.extended.PersonDomainPartFromPersonUpdater;
import se.uu.ub.cora.diva.extended.PersonDomainPartValidator;
import se.uu.ub.cora.diva.extended.PersonUpdaterAfterDomainPartCreate;
import se.uu.ub.cora.diva.extended.PersonUpdaterAfterDomainPartDelete;
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexerFactory;
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexerFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.classic.RelatedLinkCollectorFactory;
import se.uu.ub.cora.diva.mixedstorage.classic.RelatedLinkCollectorFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.classic.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.diva.mixedstorage.classic.RepeatableRelatedLinkCollectorImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.dependency.SpiderInitializationException;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaExtendedFunctionalityFactory implements ExtendedFunctionalityFactory {

	private static final String PERSON = "person";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private static final String TOP_ORGANISATION = "topOrganisation";
	private static final String ROOT_ORGANISATION = "rootOrganisation";
	private static final String SUB_ORGANISATION = "subOrganisation";
	private List<ExtendedFunctionalityContext> contexts = new ArrayList<>();
	private SpiderDependencyProvider dependencyProvider;
	private String url;
	private SqlDatabaseFactory databaseFactory;

	@Override
	public void initializeUsingDependencyProvider(SpiderDependencyProvider dependencyProvider) {
		this.dependencyProvider = dependencyProvider;
		String databaseLookupNameValue = dependencyProvider
				.getInitInfoValueUsingKey("databaseLookupName");

		databaseFactory = SqlDatabaseFactoryImp.usingLookupNameFromContext(databaseLookupNameValue);
		url = dependencyProvider.getInitInfoValueUsingKey("classicListUpdateURL");
		createListOfContexts();
	}

	private void createListOfContexts() {
		createContext(SUB_ORGANISATION);
		createContext(TOP_ORGANISATION);
		createContext(ROOT_ORGANISATION);
		contexts.add(new ExtendedFunctionalityContext(UPDATE_AFTER_STORE, PERSON, 0));
		contexts.add(new ExtendedFunctionalityContext(CREATE_AFTER_METADATA_VALIDATION,
				PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(CREATE_BEFORE_RETURN, PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(DELETE_BEFORE, PERSON_DOMAIN_PART, 0));
	}

	private void createContext(String recordType) {
		contexts.add(new ExtendedFunctionalityContext(UPDATE_BEFORE_STORE, recordType, 0));
		contexts.add(new ExtendedFunctionalityContext(UPDATE_AFTER_STORE, recordType, 0));
	}

	@Override
	public List<ExtendedFunctionalityContext> getExtendedFunctionalityContexts() {
		return contexts;
	}

	@Override
	public List<ExtendedFunctionality> factor(ExtendedFunctionalityPosition position,
			String recordType) {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		if (isOrganisation(recordType)) {
			checkPositionForOrganisation(position, functionalities);
		} else if (PERSON_DOMAIN_PART.equals(recordType)) {
			checkPositionForDomainPart(position, functionalities);
		} else if (PERSON.equals(recordType) && UPDATE_AFTER_STORE == position) {
			addFunctionalityForPersonAfterStore(functionalities);
		}
		return functionalities;
	}

	private void checkPositionForOrganisation(ExtendedFunctionalityPosition position,
			List<ExtendedFunctionality> functionalities) {
		if (UPDATE_BEFORE_STORE == position) {
			addFunctionalityForBeforeStore(functionalities);
		} else if (UPDATE_AFTER_STORE == position) {
			addFunctionalityForOrganisationsAfterStore(functionalities);
		}
	}

	private void checkPositionForDomainPart(ExtendedFunctionalityPosition position,
			List<ExtendedFunctionality> functionalities) {
		if (CREATE_AFTER_METADATA_VALIDATION == position) {
			addFunctionalityForCreateAfterMetadataValidation(functionalities);
		} else if (CREATE_BEFORE_RETURN == position) {
			addFunctionalityForCreateBeforeReturn(functionalities);
		} else if (DELETE_BEFORE == position) {
			addFunctionalityForDeleteBefore(functionalities);
		} else if (UPDATE_AFTER_STORE == position) {
			RecordStorage recordStorage = dependencyProvider.getRecordStorage();
			addClassicSynchronizer(functionalities, recordStorage, PERSON_DOMAIN_PART);
		}
	}

	private boolean isOrganisation(String recordType) {
		return SUB_ORGANISATION.equals(recordType) || ROOT_ORGANISATION.equals(recordType)
				|| TOP_ORGANISATION.equals(recordType);
	}

	private void addFunctionalityForPersonAfterStore(List<ExtendedFunctionality> functionalities) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		functionalities.add(
				new PersonDomainPartFromPersonUpdater(recordStorage, termCollector, linkCollector));
		addClassicSynchronizer(functionalities, recordStorage, PERSON);
	}

	private void addClassicSynchronizer(List<ExtendedFunctionality> functionalities,
			RecordStorage recordStorage, String recordType) {
		ClassicFedoraUpdaterFactoryImp fedoraUpdaterFactory = createClassicFedoraUpdaterFactory(
				recordStorage);
		ClassicIndexerFactory classicIndexerFactory = createClassicIndexerFactory();
		functionalities.add(new ClassicPersonSynchronizer(fedoraUpdaterFactory,
				classicIndexerFactory, recordType));
	}

	private ClassicFedoraUpdaterFactoryImp createClassicFedoraUpdaterFactory(
			RecordStorage recordStorage) {
		HttpHandlerFactoryImp httpHandlerFactory = new HttpHandlerFactoryImp();

		RepeatableRelatedLinkCollector repeatableLinkCollector = createRepeatableLinkCollector(
				recordStorage);
		FedoraConnectionInfo fedoraConnectionInfo = createFedoraConnectionInfo();
		return new ClassicFedoraUpdaterFactoryImp(httpHandlerFactory, repeatableLinkCollector,
				fedoraConnectionInfo);
	}

	private RepeatableRelatedLinkCollector createRepeatableLinkCollector(
			RecordStorage recordStorage) {
		RelatedLinkCollectorFactory linkCollectorFactory = new RelatedLinkCollectorFactoryImp(
				recordStorage);
		return new RepeatableRelatedLinkCollectorImp(linkCollectorFactory);
	}

	private FedoraConnectionInfo createFedoraConnectionInfo() {
		String fedoraURL = dependencyProvider.getInitInfoValueUsingKey("fedoraURL");
		String fedoraUsername = dependencyProvider.getInitInfoValueUsingKey("fedoraUsername");
		String fedoraPassword = dependencyProvider.getInitInfoValueUsingKey("fedoraPassword");
		return new FedoraConnectionInfo(fedoraURL, fedoraUsername, fedoraPassword);
	}

	private ClassicIndexerFactory createClassicIndexerFactory() {
		String classicAuthorityIndexUrl = getAuthorityIndexUrlOrEmptyIfMissing();
		return new ClassicIndexerFactoryImp(classicAuthorityIndexUrl);
	}

	private String getAuthorityIndexUrlOrEmptyIfMissing() {
		try {
			return dependencyProvider.getInitInfoValueUsingKey("authorityIndexUrl");
		} catch (SpiderInitializationException e) {
			// do nothing
		}
		return "";
	}

	private void addFunctionalityForBeforeStore(List<ExtendedFunctionality> functionalities) {
		addDuplicateLinksRemover(functionalities);
		addDisallowedDependencyDetector(functionalities);
		addDifferentDomainDetector(functionalities);
	}

	private void addDuplicateLinksRemover(List<ExtendedFunctionality> functionalities) {
		functionalities.add(new OrganisationDuplicateLinksRemover());
	}

	private void addDifferentDomainDetector(List<ExtendedFunctionality> functionalities) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		functionalities.add(new OrganisationDifferentDomainDetector(recordStorage));
	}

	private void addDisallowedDependencyDetector(List<ExtendedFunctionality> functionalities) {
		DatabaseFacade dbFacade = databaseFactory.factorDatabaseFacade();
		functionalities.add(new OrganisationDisallowedDependencyDetector(dbFacade));
	}

	private void addFunctionalityForOrganisationsAfterStore(
			List<ExtendedFunctionality> functionalities) {
		ClassicOrganisationReloader classicOrganisationReloader = createClassicReloader();
		functionalities.add(classicOrganisationReloader);
	}

	private ClassicOrganisationReloader createClassicReloader() {
		HttpHandlerFactory factory = new HttpHandlerFactoryImp();
		return ClassicOrganisationReloader.usingHttpHandlerFactoryAndUrl(factory, url);
	}

	private void addFunctionalityForCreateAfterMetadataValidation(
			List<ExtendedFunctionality> functionalities) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		functionalities.add(new PersonDomainPartValidator(recordStorage));
	}

	private void addFunctionalityForCreateBeforeReturn(
			List<ExtendedFunctionality> functionalities) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		functionalities.add(new PersonUpdaterAfterDomainPartCreate(recordStorage, termCollector,
				linkCollector));
	}

	private void addFunctionalityForDeleteBefore(List<ExtendedFunctionality> functionalities) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		functionalities.add(new PersonUpdaterAfterDomainPartDelete(recordStorage, termCollector,
				linkCollector));
	}

	public SqlDatabaseFactory onlyForTestGetSqlDatabaseFactory() {
		return databaseFactory;
	}

	public void onlyForTestSetSqlDatabaseFactory(SqlDatabaseFactory databaseFactory) {
		this.databaseFactory = databaseFactory;
	}

}
