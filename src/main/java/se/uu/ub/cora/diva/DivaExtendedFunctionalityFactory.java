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

import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.CREATE_BEFORE_RETURN;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_STORE;
import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_BEFORE_STORE;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.diva.extended.ClassicOrganisationReloader;
import se.uu.ub.cora.diva.extended.OrganisationDifferentDomainDetector;
import se.uu.ub.cora.diva.extended.OrganisationDisallowedDependencyDetector;
import se.uu.ub.cora.diva.extended.OrganisationDuplicateLinksRemover;
import se.uu.ub.cora.diva.extended.PersonDomainPartIndexer;
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

public class DivaExtendedFunctionalityFactory implements ExtendedFunctionalityFactory {

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
		contexts.add(new ExtendedFunctionalityContext(CREATE_BEFORE_RETURN, "workOrder", 0));
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
		if (UPDATE_BEFORE_STORE == position) {
			addFunctionalityForBeforeStore(functionalities);
		} else if (UPDATE_AFTER_STORE == position) {
			addFunctionalityForAfterStore(functionalities);
		} else if (CREATE_BEFORE_RETURN == position) {
			addFunctionalityCreateBeforeReturn(functionalities);
		}

		return functionalities;

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

	private void addFunctionalityForAfterStore(List<ExtendedFunctionality> functionalities) {
		ClassicOrganisationReloader classicOrganisationReloader = createClassicReloader();
		functionalities.add(classicOrganisationReloader);
	}

	private ClassicOrganisationReloader createClassicReloader() {
		HttpHandlerFactory factory = new HttpHandlerFactoryImp();
		return ClassicOrganisationReloader.usingHttpHandlerFactoryAndUrl(factory, url);
	}

	private void addFunctionalityCreateBeforeReturn(List<ExtendedFunctionality> functionalities) {
		functionalities.add(new PersonDomainPartIndexer());

	}

	public SqlDatabaseFactory onlyForTestGetSqlDatabaseFactory() {
		return databaseFactory;
	}

	public void onlyForTestSetSqlDatabaseFactory(SqlDatabaseFactory databaseFactory) {
		this.databaseFactory = databaseFactory;
	}

}
