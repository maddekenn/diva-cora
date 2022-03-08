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
package se.uu.ub.cora.diva.extended;

import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_AFTER_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_RETURN;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.DELETE_AFTER;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.DELETE_BEFORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_METADATA_VALIDATION;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.diva.extended.organisation.ClassicOrganisationReloader;
import se.uu.ub.cora.diva.extended.organisation.OrganisationDifferentDomainDetector;
import se.uu.ub.cora.diva.extended.organisation.OrganisationDisallowedDependencyDetector;
import se.uu.ub.cora.diva.extended.organisation.OrganisationDuplicateLinksRemover;
import se.uu.ub.cora.diva.extended.person.ClassicPersonSynchronizer;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartFromPersonUpdater;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartLocalIdDeletePreventer;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartLocalIdValidator;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartPersonSynchronizer;
import se.uu.ub.cora.diva.extended.person.PersonDomainPartValidator;
import se.uu.ub.cora.diva.extended.person.PersonOrcidValidator;
import se.uu.ub.cora.diva.extended.person.PersonUpdaterAfterDomainPartCreate;
import se.uu.ub.cora.diva.extended.person.PersonUpdaterAfterDomainPartDelete;
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
		createContextForBeforeAndAfterUpdateUsingRecordType(SUB_ORGANISATION);
		createContextForBeforeAndAfterUpdateUsingRecordType(TOP_ORGANISATION);
		createContextForBeforeAndAfterUpdateUsingRecordType(ROOT_ORGANISATION);

		createContextForBeforeAndAfterUpdateUsingRecordType(PERSON);

		contexts.add(new ExtendedFunctionalityContext(CREATE_BEFORE_METADATA_VALIDATION,
				PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(CREATE_AFTER_METADATA_VALIDATION,
				PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(CREATE_BEFORE_RETURN, PERSON_DOMAIN_PART, 0));

		contexts.add(new ExtendedFunctionalityContext(UPDATE_BEFORE_METADATA_VALIDATION,
				PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(DELETE_BEFORE, PERSON_DOMAIN_PART, 0));
		contexts.add(new ExtendedFunctionalityContext(DELETE_AFTER, PERSON_DOMAIN_PART, 0));
	}

	private void createContextForBeforeAndAfterUpdateUsingRecordType(String recordType) {
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
		if (isOrganisation(recordType)) {
			return createFunctionalitesForOrganisationAtPosition(position);
		}
		if (PERSON_DOMAIN_PART.equals(recordType)) {
			return createFunctionalitiesForDomainPartAtPosition(position);
		}
		if (PERSON.equals(recordType)) {
			return createFunctionalitiesForPersonAtPosition(position);
		}
		return Collections.emptyList();
	}

	private boolean isOrganisation(String recordType) {
		return SUB_ORGANISATION.equals(recordType) || ROOT_ORGANISATION.equals(recordType)
				|| TOP_ORGANISATION.equals(recordType);
	}

	private List<ExtendedFunctionality> createFunctionalitesForOrganisationAtPosition(
			ExtendedFunctionalityPosition position) {

		if (UPDATE_BEFORE_STORE == position) {
			return addFunctionalitiesForBeforeStoreForOrganisation();
		}
		if (UPDATE_AFTER_STORE == position) {
			return addFunctionalityForAfterStoreForOrganisation();
		}
		return Collections.emptyList();
	}

	private List<ExtendedFunctionality> addFunctionalitiesForBeforeStoreForOrganisation() {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(new OrganisationDuplicateLinksRemover());
		functionalities.add(addDisallowedDependencyDetector());
		functionalities.add(addDifferentDomainDetector());
		return functionalities;
	}

	private ExtendedFunctionality addDisallowedDependencyDetector() {
		DatabaseFacade dbFacade = databaseFactory.factorDatabaseFacade();
		return new OrganisationDisallowedDependencyDetector(dbFacade);
	}

	private ExtendedFunctionality addDifferentDomainDetector() {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		return new OrganisationDifferentDomainDetector(recordStorage);
	}

	private List<ExtendedFunctionality> addFunctionalityForAfterStoreForOrganisation() {
		return createListAndAddFunctionality(createClassicReloader());
	}

	private ClassicOrganisationReloader createClassicReloader() {
		HttpHandlerFactory factory = new HttpHandlerFactoryImp();
		return ClassicOrganisationReloader.usingHttpHandlerFactoryAndUrl(factory, url);
	}

	private List<ExtendedFunctionality> createFunctionalitiesForDomainPartAtPosition(
			ExtendedFunctionalityPosition position) {
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();

		if (CREATE_BEFORE_METADATA_VALIDATION == position) {
			return createListAndAddFunctionality(new PersonDomainPartValidator());
		}
		if (CREATE_AFTER_METADATA_VALIDATION == position) {
			return createListAndAddFunctionality(
					new PersonDomainPartPersonSynchronizer(recordStorage));
		}
		if (CREATE_BEFORE_RETURN == position) {
			return addFunctionalityForCreateBeforeReturn(recordStorage);
		}
		if (UPDATE_BEFORE_METADATA_VALIDATION == position) {
			return addFunctionalitiesForUpdateBeforeMetadataValidation();
		}
		if (UPDATE_AFTER_STORE == position) {
			return createListAndAddFunctionality(
					createClassicPersonSynchronizer(recordStorage, PERSON_DOMAIN_PART));
		}
		if (DELETE_BEFORE == position) {
			return createListAndAddFunctionality(new PersonDomainPartLocalIdDeletePreventer());
		}
		if (DELETE_AFTER == position) {
			return addFunctionalityForDeleteAfter();
		}
		return Collections.emptyList();
	}

	private List<ExtendedFunctionality> createListAndAddFunctionality(
			ExtendedFunctionality extendedFunctionality) {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(extendedFunctionality);
		return functionalities;
	}

	private List<ExtendedFunctionality> addFunctionalitiesForUpdateBeforeMetadataValidation() {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(new PersonDomainPartValidator());
		functionalities.add(new PersonDomainPartLocalIdValidator());
		return functionalities;
	}

	private List<ExtendedFunctionality> addFunctionalityForCreateBeforeReturn(
			RecordStorage recordStorage) {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(addPersonUpdateAfterDomainPartCreate(recordStorage));
		functionalities.add(createClassicPersonSynchronizer(recordStorage, PERSON_DOMAIN_PART));
		return functionalities;
	}

	private ExtendedFunctionality addPersonUpdateAfterDomainPartCreate(
			RecordStorage recordStorage) {
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		return new PersonUpdaterAfterDomainPartCreate(recordStorage, termCollector, linkCollector);
	}

	private ExtendedFunctionality createClassicPersonSynchronizer(RecordStorage recordStorage,
			String recordType) {
		ClassicFedoraUpdaterFactoryImp fedoraUpdaterFactory = createClassicFedoraUpdaterFactory(
				recordStorage);
		ClassicIndexerFactory classicIndexerFactory = createClassicIndexerFactory();
		return new ClassicPersonSynchronizer(fedoraUpdaterFactory, classicIndexerFactory,
				recordType, recordStorage);
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

	private List<ExtendedFunctionality> addFunctionalityForDeleteAfter() {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		functionalities.add(addPersonUpdaterAfterDomainPartDelete(recordStorage));
		functionalities.add(createClassicPersonSynchronizer(recordStorage, PERSON_DOMAIN_PART));
		return functionalities;
	}

	private ExtendedFunctionality addPersonUpdaterAfterDomainPartDelete(
			RecordStorage recordStorage) {
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		return new PersonUpdaterAfterDomainPartDelete(recordStorage, termCollector, linkCollector);
	}

	private String getAuthorityIndexUrlOrEmptyIfMissing() {
		try {
			return dependencyProvider.getInitInfoValueUsingKey("authorityIndexUrl");
		} catch (SpiderInitializationException e) {
			return "";
		}
	}

	private List<ExtendedFunctionality> createFunctionalitiesForPersonAtPosition(
			ExtendedFunctionalityPosition position) {
		if (UPDATE_AFTER_STORE == position) {
			return addFunctionalityForPersonAfterStore();
		}
		if (UPDATE_BEFORE_STORE == position) {
			return createListAndAddFunctionality(new PersonOrcidValidator());
		}
		return Collections.emptyList();
	}

	private List<ExtendedFunctionality> addFunctionalityForPersonAfterStore() {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		RecordStorage recordStorage = dependencyProvider.getRecordStorage();
		DataGroupTermCollector termCollector = dependencyProvider.getDataGroupTermCollector();
		DataRecordLinkCollector linkCollector = dependencyProvider.getDataRecordLinkCollector();
		functionalities.add(
				new PersonDomainPartFromPersonUpdater(recordStorage, termCollector, linkCollector));
		functionalities.add(createClassicPersonSynchronizer(recordStorage, PERSON));
		return functionalities;
	}

	public SqlDatabaseFactory onlyForTestGetSqlDatabaseFactory() {
		return databaseFactory;
	}

	public void onlyForTestSetSqlDatabaseFactory(SqlDatabaseFactory databaseFactory) {
		this.databaseFactory = databaseFactory;
	}

}
