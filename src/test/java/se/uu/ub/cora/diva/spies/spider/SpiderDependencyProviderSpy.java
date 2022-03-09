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
package se.uu.ub.cora.diva.spies.spider;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.linkcollector.DataRecordLinkCollector;
import se.uu.ub.cora.bookkeeper.recordpart.DataRedactor;
import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.bookkeeper.validator.DataValidator;
import se.uu.ub.cora.diva.spies.data.DataGroupTermCollectorSpy;
import se.uu.ub.cora.diva.spies.data.DataRecordLinkCollectorSpy;
import se.uu.ub.cora.diva.spies.storage.RecordStorageSpy;
import se.uu.ub.cora.search.RecordIndexer;
import se.uu.ub.cora.search.RecordSearch;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.authorization.PermissionRuleCalculator;
import se.uu.ub.cora.spider.authorization.SpiderAuthorizator;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.dependency.SpiderInitializationException;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityProvider;
import se.uu.ub.cora.spider.record.DataGroupToRecordEnhancer;
import se.uu.ub.cora.spider.recordtype.RecordTypeHandler;
import se.uu.ub.cora.storage.RecordIdGenerator;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StreamStorage;

public class SpiderDependencyProviderSpy implements SpiderDependencyProvider {

	public boolean getInitInfoValueUsingKeyWasCalled = false;
	public Map<String, String> initInfo = new HashMap<>();
	public DataGroupTermCollector termCollector = new DataGroupTermCollectorSpy();
	public DataRecordLinkCollector linkCollector = new DataRecordLinkCollectorSpy();
	RecordStorage recordStorage = new RecordStorageSpy();

	public SpiderDependencyProviderSpy(Map<String, String> initInfo) {
		this.initInfo = initInfo;
	}

	@Override
	public Authenticator getAuthenticator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordSearch getRecordSearch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordIndexer getRecordIndexer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitInfoValueUsingKey(String key) {
		getInitInfoValueUsingKeyWasCalled = true;
		if (!initInfo.containsKey(key)) {
			throw new SpiderInitializationException("some error message from spy");
		}
		return initInfo.get(key);
	}

	@Override
	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

	@Override
	public StreamStorage getStreamStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordIdGenerator getRecordIdGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderAuthorizator getSpiderAuthorizator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataValidator getDataValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRecordLinkCollector getDataRecordLinkCollector() {
		return linkCollector;
	}

	@Override
	public DataGroupTermCollector getDataGroupTermCollector() {
		return termCollector;
	}

	@Override
	public PermissionRuleCalculator getPermissionRuleCalculator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRedactor getDataRedactor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExtendedFunctionalityProvider getExtendedFunctionalityProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordTypeHandler getRecordTypeHandler(String recordTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroupToRecordEnhancer getDataGroupToRecordEnhancer() {
		// TODO Auto-generated method stub
		return null;
	}

}
