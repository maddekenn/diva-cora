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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.spider.dependency.SpiderInstanceFactory;
import se.uu.ub.cora.spider.record.SpiderDownloader;
import se.uu.ub.cora.spider.record.SpiderRecordCreator;
import se.uu.ub.cora.spider.record.SpiderRecordDeleter;
import se.uu.ub.cora.spider.record.SpiderRecordIncomingLinksReader;
import se.uu.ub.cora.spider.record.SpiderRecordListReader;
import se.uu.ub.cora.spider.record.SpiderRecordReader;
import se.uu.ub.cora.spider.record.SpiderRecordSearcher;
import se.uu.ub.cora.spider.record.SpiderRecordUpdater;
import se.uu.ub.cora.spider.record.SpiderRecordValidator;
import se.uu.ub.cora.spider.record.SpiderUploader;

public class SpiderInstanceFactorySpy implements SpiderInstanceFactory {

	public List<SpiderRecordCreator> spiderRecordCreators = new ArrayList<>();
	public List<SpiderRecordReader> spiderRecordReaders = new ArrayList<>();

	@Override
	public String getDependencyProviderClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordReader factorSpiderRecordReader() {
		SpiderRecordReaderSpy spiderRecordReaderSpy = new SpiderRecordReaderSpy();
		spiderRecordReaders.add(spiderRecordReaderSpy);
		return spiderRecordReaderSpy;
	}

	@Override
	public SpiderRecordIncomingLinksReader factorSpiderRecordIncomingLinksReader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordListReader factorSpiderRecordListReader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordCreator factorSpiderRecordCreator() {
		SpiderRecordCreatorSpy spiderRecordCreatorSpy = new SpiderRecordCreatorSpy();
		spiderRecordCreators.add(spiderRecordCreatorSpy);
		return spiderRecordCreatorSpy;
	}

	@Override
	public SpiderRecordUpdater factorSpiderRecordUpdater() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordDeleter factorSpiderRecordDeleter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderUploader factorSpiderUploader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderDownloader factorSpiderDownloader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordSearcher factorSpiderRecordSearcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderRecordValidator factorSpiderRecordValidator() {
		// TODO Auto-generated method stub
		return null;
	}

}
