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
package se.uu.ub.cora.diva.spies;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import se.uu.ub.cora.logger.Logger;

public class LoggerSpy implements Logger {

	public List<String> errorMessages = new ArrayList<>();
	public List<String> infoMessages = new ArrayList<>();
	public List<String> warningMessages = new ArrayList<>();

	@Override
	public void logFatalUsingMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logFatalUsingMessageAndException(String message, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logErrorUsingMessage(String message) {
		errorMessages.add(message);

	}

	@Override
	public void logErrorUsingMessageAndException(String message, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logWarnUsingMessage(String message) {
		warningMessages.add(message);

	}

	@Override
	public void logWarnUsingMessageAndException(String message, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logInfoUsingMessage(String message) {
		infoMessages.add(message);

	}

	@Override
	public void logDebugUsingMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logDebugUsingMessageSupplier(Supplier<String> messageSupplier) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logTraceUsingMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logTraceUsingMessageSupplier(Supplier<String> messageSupplier) {
		// TODO Auto-generated method stub

	}

}
