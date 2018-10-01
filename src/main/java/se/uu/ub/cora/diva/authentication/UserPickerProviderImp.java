/*
 * Copyright 2016, 2018 Uppsala University Library
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

package se.uu.ub.cora.diva.authentication;

import java.util.Map;

import se.uu.ub.cora.storage.UserStorageImp;
import se.uu.ub.cora.userpicker.UserInStorageUserPicker;
import se.uu.ub.cora.userpicker.UserPicker;
import se.uu.ub.cora.userpicker.UserPickerProvider;
import se.uu.ub.cora.userpicker.UserStorage;

public final class UserPickerProviderImp implements UserPickerProvider {

	private static final String DIVA_GUEST_USERID = "coraUser:5368656924943436";
	private UserInStorageUserPicker userPicker;

	public UserPickerProviderImp(Map<String, String> initInfo) {
		UserStorage userStorage = new UserStorageImp(initInfo);
		userPicker = UserInStorageUserPicker.usingUserStorageAndGuestUserId(userStorage,
				DIVA_GUEST_USERID);
	}

	@Override
	public UserPicker getUserPicker() {
		return userPicker;
	}

}
