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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.fedora.FedoraException;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class ClassicFedoraUpdaterImp implements ClassicFedoraUpdater {

	private static final int CREATED = 201;
	private static final int OK = 200;
	private HttpHandlerFactory httpHandlerFactory;
	private DivaFedoraConverterFactory divaCoraToFedoraConverterFactory;
	private FedoraConnectionInfo fedoraConnectionInfo;

	public ClassicFedoraUpdaterImp(HttpHandlerFactory httpHandlerFactory,
			DivaFedoraConverterFactory divaCoraToFedoraConverterFactory,
			FedoraConnectionInfo fedoraConnectionInfo) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.divaCoraToFedoraConverterFactory = divaCoraToFedoraConverterFactory;
		this.fedoraConnectionInfo = fedoraConnectionInfo;
	}

	@Override
	public void updateInFedora(String recordType, String recordId, DataGroup dataGroup) {
		HttpHandler httpHandler = setUpHttpHandlerForUpdate(recordId);
		String fedoraXML = convertRecordToFedoraXML(recordType, dataGroup);
		httpHandler.setOutput(fedoraXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfNotOkFromFedora(recordId, responseCode);
	}

	private void throwErrorIfNotOkFromFedora(String id, int responseCode) {
		if (responseCode != OK) {
			throw FedoraException.withMessage("update to fedora failed for record: " + id
					+ ", with response code: " + responseCode);
		}
	}

	private HttpHandler setUpHttpHandlerForUpdate(String recordId) {
		String url = createUpdateURL(recordId);
		HttpHandler httpHandler = setUpHttpHandlerWithAuthorization(url);
		httpHandler.setRequestMethod("PUT");
		return httpHandler;
	}

	private HttpHandler setUpHttpHandlerWithAuthorization(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		setAuthorizationInHttpHandler(httpHandler);
		return httpHandler;
	}

	private void setAuthorizationInHttpHandler(HttpHandler httpHandler) {
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraConnectionInfo.fedoraUsername + ":" + fedoraConnectionInfo.fedoraPassword)
						.getBytes(StandardCharsets.UTF_8));
		httpHandler.setRequestProperty("Authorization", "Basic " + encoded);
	}

	private String convertRecordToFedoraXML(String recordType, DataGroup dataGroup) {
		DivaCoraToFedoraConverter toFedoraConverter = divaCoraToFedoraConverterFactory
				.factorToFedoraConverter(recordType);
		return toFedoraConverter.toXML(dataGroup);
	}

	private String createUpdateURL(String recordId) {
		return fedoraConnectionInfo.fedoraUrl + "objects/" + recordId
				+ "/datastreams/METADATA?format=?xml&controlGroup=M"
				+ "&logMessage=coraWritten&checksumType=SHA-512";
	}

	HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	DivaFedoraConverterFactory getDivaCoraToFedoraConverterFactory() {
		return divaCoraToFedoraConverterFactory;
	}

	public FedoraConnectionInfo getFedoraConnectionInfo() {
		return fedoraConnectionInfo;
	}

	@Override
	public void createInFedora(String recordType, String recordId, DataGroup dataGroup) {
		String url = fedoraConnectionInfo.fedoraUrl + "objects/" + recordId;
		createObjectInFedora(recordId, url);

		String fedoraXML = convertRecordToFedoraXML(recordType, dataGroup);
		createDatastreamInFedora(recordId, url, fedoraXML);
	}

	private void createObjectInFedora(String recordId, String url) {
		HttpHandler httpHandler = setUpHttpHandlerForCreate(url);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfNotCreatedFromFedora(recordId, responseCode);
	}

	private HttpHandler setUpHttpHandlerForCreate(String datastreamUrl) {
		HttpHandler httpHandler = setUpHttpHandlerWithAuthorization(datastreamUrl);
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Content-Type", "text/xml");
		return httpHandler;
	}

	private void throwErrorIfNotCreatedFromFedora(String recordId, int responseCode) {
		throwErrorIfNotCreated(recordId, responseCode, "Create to fedora failed for record: ");
	}

	private void throwErrorIfNotCreated(String recordId, int responseCode, String startOfMessage) {
		if (responseCode != CREATED) {
			throw FedoraException.withMessage(
					startOfMessage + recordId + ", with response code: " + responseCode);
		}
	}

	private void createDatastreamInFedora(String recordId, String url, String fedoraXML) {
		HttpHandler httpHandler = setUpHttpHandlerForCreateDatastream(url);
		httpHandler.setOutput(fedoraXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfDatastreamNotCreatedFromFedora(recordId, responseCode);
	}

	private HttpHandler setUpHttpHandlerForCreateDatastream(String url) {
		String datastreamUrl = url + "/datastreams/METADATA?dsLabel=coraWritten";
		return setUpHttpHandlerForCreate(datastreamUrl);
	}

	private void throwErrorIfDatastreamNotCreatedFromFedora(String recordId, int responseCode) {
		throwErrorIfNotCreated(recordId, responseCode,
				"Adding datastream in fedora failed for record: ");
	}

}
