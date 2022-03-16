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
package se.uu.ub.cora.diva.fedora;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.diva.spies.data.DataGroupSpy;
import se.uu.ub.cora.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.fedora.FedoraException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class ClassicFedoraUpdaterTest {

	private ClassicFedoraUpdaterImp fedoraUpdater;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private DivaFedoraConverterFactorySpy fedoraConverterFactory;
	private ConverterFactorySpy dataGroupToXmlConverterFactory;
	private String baseUrl = "someBaseUrl/";
	private String fedoraUsername = "someFedoraUserName";
	private String fedoraPassword = "someFedoraPassWord";
	private DataGroupSpy dataGroup = new DataGroupSpy("someNameInData");

	@BeforeMethod
	public void setUp() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		setUpHttpHandlerFactory();
		fedoraConverterFactory = new DivaFedoraConverterFactorySpy();
		dataGroupToXmlConverterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", dataGroupToXmlConverterFactory);
		FedoraConnectionInfo fedoraConnectionInfo = new FedoraConnectionInfo(baseUrl,
				fedoraUsername, fedoraPassword);

		fedoraUpdater = new ClassicFedoraUpdaterImp(httpHandlerFactory, fedoraConverterFactory,
				fedoraConnectionInfo);

	}

	private void setUpHttpHandlerFactory() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseTexts.add("some default responseText");
		httpHandlerFactory.responseCodes.add(200);
	}

	@Test
	public void testInit() {
		assertSame(fedoraUpdater.getHttpHandlerFactory(), httpHandlerFactory);
		assertSame(fedoraUpdater.getDivaCoraToFedoraConverterFactory(), fedoraConverterFactory);
	}

	@Test
	public void testHttpHandler() {
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup);

		String updateUrl = "someBaseUrl/objects/someRecordId/datastreams/METADATA?format=?xml&controlGroup=M&logMessage=coraWritten&checksumType=SHA-512";
		assertCorrectCallToHttpHandler(updateUrl, "PUT");
	}

	private void assertCorrectCallToHttpHandler(String updateUrl, String updateAction) {
		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertNotNull(factoredHttpHandler);

		// assertEquals(httpHandlerFactory.urls.get(0), updateUrl);
		assertEquals(factoredHttpHandler.requestMethod, updateAction);

		assertCorrectCredentials(factoredHttpHandler);

		DivaCoraToFedoraConverterSpy factoredConverter = (DivaCoraToFedoraConverterSpy) fedoraConverterFactory.factoredToFedoraConverters
				.get(0);
		assertEquals(factoredConverter.returnedXML, factoredHttpHandler.outputStrings.get(0));
	}

	private void assertCorrectCredentials(HttpHandlerSpy factoredHttpHandler) {
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		assertEquals(factoredHttpHandler.requestProperties.get("Authorization"),
				"Basic " + encoded);
	}

	@Test
	public void testUpdateInFedora() {
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup);

		DivaCoraToFedoraConverterSpy divaCoraToFedoraConverter = (DivaCoraToFedoraConverterSpy) fedoraConverterFactory.factoredToFedoraConverters
				.get(0);
		assertSame(divaCoraToFedoraConverter.dataRecord, dataGroup);
		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);

		assertEquals(factoredHttpHandler.outputStrings.get(0),
				divaCoraToFedoraConverter.returnedXML);

	}

	@Test(expectedExceptions = FedoraException.class)
	public void testErrorFromFedoraInUpdate() {
		httpHandlerFactory.responseCodes = new ArrayList<>();
		httpHandlerFactory.responseCodes.add(505);
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup);
	}

	@Test
	public void testHttpHandlerForCreate() {
		httpHandlerFactory.responseCodes = List.of(201);
		fedoraUpdater.createInFedora("someRecordType", "someRecordId", dataGroup);

		// String createUrl = "someBaseUrl/objects/someRecordId?format=?xml&logMessage=coraWritten";
		String createUrl = "someBaseUrl/objects/someRecordId?format=info:fedora/fedora-system:FOXML-1.1&logMessage=coraWritten";
		assertCorrectCallToHttpHandler(createUrl, "POST");
	}

	@Test
	public void testCreateInFedora() {
		httpHandlerFactory.responseCodes = List.of(201);

		fedoraUpdater.createInFedora("someRecordType", "someRecordId", dataGroup);
		DivaCoraToFedoraConverterSpy divaCoraToFedoraConverter = (DivaCoraToFedoraConverterSpy) fedoraConverterFactory.factoredToFedoraConverters
				.get(0);
		assertSame(divaCoraToFedoraConverter.dataRecord, dataGroup);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "create to fedora failed for record: someRecordId, with response code: 505")
	public void testErrorFromFedoraOnCreate() {
		httpHandlerFactory.responseCodes = new ArrayList<>();
		httpHandlerFactory.responseCodes.add(505);
		fedoraUpdater.createInFedora("someRecordType", "someRecordId", dataGroup);
	}

	@Test
	public void testRealCreate() {
		HttpHandlerFactory realHttpHandlerFactory = new HttpHandlerFactoryImp();

	}

	@Test(enabled = false)
	public void testRealCallToAuthorityService() {
		String json = "{\"affiliations\":[],\"defaultName\":{\"lastname\":\"Fusksson2\",\"number\":\"\",\"firstname\":\"Fusk2\"},\"identifiers\":[],\"pid\":\"\",\"type\":\"PERSON\",\"publicRecord\":true,\"biographies\":{},\"alternativeNames\":[]}";
		HttpHandlerFactory realHttpHandlerFactory = new HttpHandlerFactoryImp();
		fedoraUpdater = new ClassicFedoraUpdaterImp(realHttpHandlerFactory, fedoraConverterFactory,
				null);
		String responseText = fedoraUpdater.createInFedoraUsingService("someRecordType",
				"someRecordId", dataGroup, json);
		assertEquals(responseText, "");

	}
	// https:/ cora-diva-archive:8443/fedora/objects/authority-person:113?logMessage=coraWritten
}
