<%--
  ~ Copyright 2015, 2021 Uppsala University Library
  ~
  ~ This file is part of Cora.
  ~
  ~     Cora is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     Cora is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>DiVA REST Api</title>
</head>
<body>
	<h1>DiVA REST Api</h1>

	<h3>Create</h3>
	To create data, use: 
	<br> 
	POST http://epc.ub.uu.se/diva/rest/record/theTypeYouWantToCreate
	<br>
	<br>
	Examples of what the body should look like can be found here: <br>
	<a href="http://epc.ub.uu.se/fitnesse/TheRestTests.CallThroughJavaCode.RecordTypeTests.AbstractRecordType">AbstractRecordType</a>
	<br>
	<h3>Read</h3>
	To read a list of types, use: 
	<br> 
	GET http://epc.ub.uu.se/diva/rest/record/theTypeYouWantToRead
	<br>
	<br>
	To read an instance of a type, use: 
	<br> 
	GET http://epc.ub.uu.se/diva/rest/record/theTypeYouWantToRead/theIdOfTheInstanceYouWantToRead
	<br>
	<h3>Update</h3>
	To update data use: 
	<br> 
	POST http://epc.ub.uu.se/diva/rest/record/theTypeYouWantToUpdate/theIdOfTheDataYouWantToUpdate
	<br>
	<br>
	Examples of what the body should look like can be found here: <br>
	<a href="http://epc.ub.uu.se/fitnesse/TheRestTests.CallThroughJavaCode.RecordTypeTests.AbstractRecordType">AbstractRecordType</a>
	<br>
	<h3>Delete</h3>
	To delete data use: 
	<br> 
	DELETE http://epc.ub.uu.se/diva/rest/record/theTypeYouWantToUpdate/theIdOfTheDataYouWantToDelete
	<br>
	<br>
	
	<h2>Further documentation</h2>
	You can find further documentation in the official DiVA-wiki: <a href="https://wiki.epc.ub.uu.se/x/P4UhBQ">https://wiki.epc.ub.uu.se/x/P4UhBQ</a>.
	
	Examples can be found in our <a href="http://epc.ub.uu.se/fitnesse/TheRestTests">acceptance tests</a>
</body>
</html>
