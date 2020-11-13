/*
 * Copyright 2020 Uppsala University Library
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

import static se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition.UPDATE_AFTER_METADATA_VALIDATION;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;
import se.uu.ub.cora.sqldatabase.DataReaderImp;

public class DivaExtendedFunctionalityFactory implements ExtendedFunctionalityFactory {

	private static final String ROOT_ORGANISATION = "rootOrganisation";
	private static final String COMMON_ORGANISATION = "commonOrganisation";
	private List<ExtendedFunctionalityContext> contexts = new ArrayList<>();
	private SpiderDependencyProvider dependencyProvider;

	@Override
	public void initializeUsingDependencyProvider(SpiderDependencyProvider dependencyProvider) {
		this.dependencyProvider = dependencyProvider;
		createListOfContexts();
	}

	private void createListOfContexts() {
		createContext(COMMON_ORGANISATION);
		createContext(ROOT_ORGANISATION);
	}

	private void createContext(String recordType) {
		contexts.add(
				new ExtendedFunctionalityContext(UPDATE_AFTER_METADATA_VALIDATION, recordType, 0));
	}

	@Override
	public List<ExtendedFunctionalityContext> getExtendedFunctionalityContexts() {
		return contexts;
	}

	@Override
	public List<ExtendedFunctionality> factor(ExtendedFunctionalityPosition position,
			String recordType) {
		List<ExtendedFunctionality> functionalities = new ArrayList<>();
		functionalities.add(new OrganisationDuplicateLinksRemover());
		SqlConnectionProvider connectionProvider = tryToCreateConnectionProvider();
		DataReaderImp dataReader = DataReaderImp.usingSqlConnectionProvider(connectionProvider);
		functionalities.add(new OrganisationDisallowedDependencyDetector(dataReader));
		return functionalities;
	}

	private SqlConnectionProvider tryToCreateConnectionProvider() {
		try {
			InitialContext context = new InitialContext();
			String databaseLookupName = dependencyProvider
					.getInitInfoValueUsingKey("databaseLookupName");
			return ContextConnectionProviderImp.usingInitialContextAndName(context,
					databaseLookupName);
		} catch (Exception e) {
			// TODO:what exception??
			throw new RuntimeException();
		}
	}

}
