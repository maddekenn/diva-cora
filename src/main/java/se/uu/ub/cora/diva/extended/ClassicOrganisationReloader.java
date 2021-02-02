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
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;

public class ClassicOrganisationReloader implements ExtendedFunctionality {

	private HttpHandlerFactory httpHandlerFactory;
	private String url;
	private Logger log = LoggerProvider.getLoggerForClass(ClassicOrganisationReloader.class);

	public static ClassicOrganisationReloader usingHttpHandlerFactoryAndUrl(
			HttpHandlerFactory httpHandlerFactory, String url) {
		return new ClassicOrganisationReloader(httpHandlerFactory, url);
	}

	private ClassicOrganisationReloader(HttpHandlerFactory httpHandlerFactory, String url) {
		this.url = url;
		this.httpHandlerFactory = httpHandlerFactory;
	}

	@Override
	public void useExtendedFunctionality(String authToken, DataGroup dataGroup) {
		if (urlIsEmpty()) {
			log.logInfoUsingMessage("Empty URL, no call made to list update in classic.");
		} else {
			callListUpdateInClassic(dataGroup);
		}
	}

	private boolean urlIsEmpty() {
		return "".equals(url);
	}

	private void callListUpdateInClassic(DataGroup dataGroup) {
		String domain = extractDomain(dataGroup);
		HttpHandler httpHandler = factorHttpHandler(domain);
		int responseCode = httpHandler.getResponseCode();
		logResponse(domain, responseCode);
	}

	private String extractDomain(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("domain");
	}

	private HttpHandler factorHttpHandler(String domain) {
		String urlWithParameters = this.url + "?list=ORGANISATION&domain=" + domain;

		HttpHandler httpHandler = httpHandlerFactory.factor(urlWithParameters);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private void logResponse(String domain, int responseCode) {
		if (responseCode == 200) {
			log.logInfoUsingMessage(
					"List update succesful for parameters ORGANISATION and " + domain + ".");
		} else {
			logErrorResponse(domain, responseCode);
		}
	}

	private void logErrorResponse(String domain, int responseCode) {
		String errorMessageBase = "Error when updating list for organisation for parameters "
				+ "ORGANISATION and " + domain + ". ";
		if (responseCode == 400) {
			log.logErrorUsingMessage(errorMessageBase + "Invalid argument.");
		} else if (responseCode == 500) {
			log.logErrorUsingMessage(errorMessageBase + "Internal server error.");
		} else {
			log.logErrorUsingMessage(errorMessageBase + "Unexpected error.");
		}
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for test
		return httpHandlerFactory;
	}

	public String getUrl() {
		// needed for test
		return url;
	}

}
