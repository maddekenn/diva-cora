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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DatabaseFacadeSpy implements DatabaseFacade {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public boolean readReturnsSomeRows = false;
	public boolean throwDataException = false;

	@Override
	public List<Row> readUsingSqlAndValues(String sql, List<Object> values) {
		MCR.addCall("sql", sql, "values", values);

		List<Row> rows = Collections.emptyList();
		if (readReturnsSomeRows) {
			rows = new ArrayList<>();
			rows.add(new RowSpy());
			rows.add(new RowSpy());
			rows.add(new RowSpy());
		}

		MCR.addReturned(rows);

		return rows;
	}

	@Override
	public Row readOneRowOrFailUsingSqlAndValues(String sql, List<Object> values) {
		MCR.addCall("sql", sql, "values", values);
		if (throwDataException) {
			throw SqlDatabaseException.withMessage("Error from spy reading one row.");
		}
		return null;
	}

	@Override
	public int executeSqlWithValues(String sql, List<Object> values) {
		MCR.addCall("sql", sql, "values", values);

		int returnedValue = 0;
		MCR.addReturned(returnedValue);
		return returnedValue;
	}

	@Override
	public void startTransaction() {
		MCR.addCall();

	}

	@Override
	public void endTransaction() {
		MCR.addCall();

	}

	@Override
	public void rollback() {
		MCR.addCall();

	}

	@Override
	public void close() {
		MCR.addCall();

	}

}
