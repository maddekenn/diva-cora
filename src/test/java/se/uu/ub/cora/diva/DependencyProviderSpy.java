package se.uu.ub.cora.diva;

import java.util.Map;

import se.uu.ub.cora.search.RecordIndexer;
import se.uu.ub.cora.search.RecordSearch;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProvider;
import se.uu.ub.cora.spider.extended.ExtendedFunctionalityProvider;

public class DependencyProviderSpy extends SpiderDependencyProvider {

	public DependencyProviderSpy(Map<String, String> initInfo) {
		super(initInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void tryToInitialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readInitInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public ExtendedFunctionalityProvider getExtendedFunctionalityProvider() {
		// TODO Auto-generated method stub
		return null;
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

}
