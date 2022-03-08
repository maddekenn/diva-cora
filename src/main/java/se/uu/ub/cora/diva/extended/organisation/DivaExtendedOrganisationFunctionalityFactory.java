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

import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaExtendedOrganisationFunctionalityFactory implements ExtendedFunctionalityFactory {

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

	private List<ExtendedFunctionality> createListAndAddFunctionality(
			ExtendedFunctionality extendedFunctionality) {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(extendedFunctionality);
		return functionalities;
	}

	public SqlDatabaseFactory onlyForTestGetSqlDatabaseFactory() {
		return databaseFactory;
	}

	public void onlyForTestSetSqlDatabaseFactory(SqlDatabaseFactory databaseFactory) {
		this.databaseFactory = databaseFactory;
	}

}
