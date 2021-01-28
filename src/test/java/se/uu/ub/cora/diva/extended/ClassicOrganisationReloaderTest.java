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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionality;

public class ClassicOrganisationReloaderTest {

	private ExtendedFunctionality classicOrganisationReloader;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private String url;

	@BeforeMethod
	public void setUp() {
		url = "https://somethingWithDiva/";
		httpHandlerFactory = new HttpHandlerFactorySpy();
		classicOrganisationReloader = new ClassicOrganisationReloader(httpHandlerFactory, url);

	}

	@Test
	public void testServletHasBeenCalled() {
		classicOrganisationReloader.useExtendedFunctionality("authToken", null);

		assertEquals(httpHandlerFactory.url, url);

		HttpHandlerSpy factoredHttpHandlerSpy = httpHandlerFactory.factoredHttpHandlerSpy;

	}

}
