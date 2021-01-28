/*
 * Copyright 2021 Uppsala University Library
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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;

public class ClassicOrganisationReloader implements ExtendedFunctionality {

	private HttpHandlerFactory httpHandlerFactory;

	public ClassicOrganisationReloader(HttpHandlerFactory httpHandlerFactory, String url) {
		// this.httpHandlerFactory = httpHandlerFactory;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		// TODO Auto-generated method stub
		// DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		// String domain = recordInfo.getFirstAtomicValueWithNameInData("domain");
		// String url = "https://divanånting/listUpdateServlet?list=organisation&domain=uu";
		// HttpHandler factor = httpHandlerFactory.factor(url);
		// factor.setRequestMethod("GET");
		// factor.getResponseCode();

		// Anropa servlet oavsett dataGroup
		// URL
		// HTTP Request

	}

}
