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
package se.uu.ub.cora.diva.spies.db;

import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class SqlDatabaseFactorySpy implements SqlDatabaseFactory {

	public MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public DatabaseFacade factorDatabaseFacade() {
		MCR.addCall();

		DatabaseFacade dbFacade = new DatabaseFacadeSpy();
		MCR.addReturned(dbFacade);
		return dbFacade;
	}

	@Override
	public TableFacade factorTableFacade() {
		MCR.addCall();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableQuery factorTableQuery(String tableName) {
		MCR.addCall("tableName", tableName);
		// TODO Auto-generated method stub
		return null;
	}

}
