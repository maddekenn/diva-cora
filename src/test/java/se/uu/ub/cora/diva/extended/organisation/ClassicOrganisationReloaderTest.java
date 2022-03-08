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
package se.uu.ub.cora.diva.extended.organisation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.extended.organisation.ClassicOrganisationReloader;
import se.uu.ub.cora.diva.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.diva.spies.HttpHandlerSpy;
import se.uu.ub.cora.diva.spies.LoggerFactorySpy;
import se.uu.ub.cora.diva.spies.LoggerSpy;
import se.uu.ub.cora.diva.spies.data.DataAtomicSpy;
import se.uu.ub.cora.diva.spies.data.DataGroupExtendedSpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityData;

public class ClassicOrganisationReloaderTest {

	private ExtendedFunctionality classicOrganisationReloader;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private String url;
	private DataGroupExtendedSpy dataGroup;
	private LoggerFactorySpy loggerFactorySpy;
	private String otherDomain = "otherDomain";

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		url = "https://somethingWithDiva/servlet";
		httpHandlerFactory = new HttpHandlerFactorySpy();
		createDefaultDataGroup("someDomain");
		classicOrganisationReloader = ClassicOrganisationReloader
				.usingHttpHandlerFactoryAndUrl(httpHandlerFactory, url);

	}

	private void createDefaultDataGroup(String domain) {
		dataGroup = new DataGroupExtendedSpy("organisation");
		DataGroupExtendedSpy recordInfo = new DataGroupExtendedSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		recordInfo.addChild(new DataAtomicSpy("domain", domain));
		dataGroup.addChild(recordInfo);
	}

	@Test
	public void testNoCallWhenEmptyUrl() {
		classicOrganisationReloader = ClassicOrganisationReloader
				.usingHttpHandlerFactoryAndUrl(httpHandlerFactory, "");
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);

		assertNull(httpHandlerFactory.factoredHttpHandlerSpy);

		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 0);
		assertEquals(logger.infoMessages.size(), 1);
		assertEquals(logger.infoMessages.get(0),
				"Empty URL, no call made to list update in classic.");
	}

	private ExtendedFunctionalityData createDefaultData() {
		ExtendedFunctionalityData data = new ExtendedFunctionalityData();
		data.authToken = "authToken";
		data.dataGroup = dataGroup;
		return data;
	}

	@Test
	public void testServletHasBeenCalled() {
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);

		assertHandlerFactoryReceivesCorrectURL("someDomain");
		HttpHandlerSpy httpHandlerSpy = httpHandlerFactory.factoredHttpHandlerSpy;
		assertEquals(httpHandlerSpy.requestMethod, "GET");
	}

	@Test
	public void testServletWithOtherDomain() {
		createDefaultDataGroup(otherDomain);
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		assertHandlerFactoryReceivesCorrectURL(otherDomain);
	}

	private void assertHandlerFactoryReceivesCorrectURL(String domain) {
		String urlWithParameters = this.url + "?list=ORGANISATION&domain=" + domain;
		assertEquals(httpHandlerFactory.url, urlWithParameters);
	}

	@Test
	public void testResponseCode200() {
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 0);
		assertEquals(logger.infoMessages.size(), 1);
		assertEquals(logger.infoMessages.get(0),
				"List update succesful for parameters ORGANISATION and someDomain.");
	}

	@Test
	public void testResponseCode200OtherDomain() {
		createDefaultDataGroup(otherDomain);
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 0);
		assertEquals(logger.infoMessages.size(), 1);
		assertEquals(logger.infoMessages.get(0),
				"List update succesful for parameters ORGANISATION and otherDomain.");
	}

	@Test
	public void testResponseCode400() {
		httpHandlerFactory.responseCode = 400;
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 1);
		assertEquals(logger.errorMessages.get(0),
				getErrorMessageWithDomainAndError("someDomain", "Invalid argument"));
	}

	private String getErrorMessageWithDomainAndError(String domain, String errorMessage) {
		return "Error when updating list for organisation for parameters ORGANISATION and " + domain
				+ ". " + errorMessage + ".";
	}

	@Test
	public void testResponseCode500() {
		createDefaultDataGroup(otherDomain);
		httpHandlerFactory.responseCode = 500;
		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 1);
		assertEquals(logger.errorMessages.get(0),
				getErrorMessageWithDomainAndError(otherDomain, "Internal server error"));
	}

	@Test
	public void testUnexpectedResponseCode() {
		httpHandlerFactory.responseCode = 418;

		ExtendedFunctionalityData data = createDefaultData();
		classicOrganisationReloader.useExtendedFunctionality(data);
		LoggerSpy logger = loggerFactorySpy.logger;
		assertEquals(logger.errorMessages.size(), 1);
		assertEquals(logger.errorMessages.get(0),
				getErrorMessageWithDomainAndError("someDomain", "Unexpected error"));
	}

}
