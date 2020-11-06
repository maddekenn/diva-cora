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
package se.uu.ub.cora.diva;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import se.uu.ub.cora.spider.extended.ExtendedFunctionality;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityContext;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory;
import se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityPosition;

public class DivaExtendedFunctionalityFactory implements ExtendedFunctionalityFactory {

	private static final String ROOT_ORGANISATION = "rootOrganisation";
	private static final String COMMON_ORGANISATION = "commonOrganisation";
	private List<ExtendedFunctionalityContext> contexts = new ArrayList<>();
	private EnumMap<ExtendedFunctionalityPosition, List<String>> sortedContext = new EnumMap<>(
			ExtendedFunctionalityPosition.class);

	public DivaExtendedFunctionalityFactory() {
		initializeContexts();
	}

	private void initializeContexts() {
		createAndAddUpdateBeforeContextForRecordTypeAndNumber(contexts, COMMON_ORGANISATION, 1);
		createAndAddUpdateBeforeContextForRecordTypeAndNumber(contexts, ROOT_ORGANISATION, 1);
		createHolderForPositionAndRecordType();
	}

	private void createAndAddUpdateBeforeContextForRecordTypeAndNumber(
			List<ExtendedFunctionalityContext> contexts, String recordType, int runAsNumber) {
		ExtendedFunctionalityContext context = new ExtendedFunctionalityContext(
				ExtendedFunctionalityPosition.UPDATE_BEFORE_METADATA_VALIDATION, recordType,
				runAsNumber);
		contexts.add(context);
	}

	private void createHolderForPositionAndRecordType() {
		List<String> recordTypes = new ArrayList<>();
		recordTypes.add(COMMON_ORGANISATION);
		recordTypes.add(ROOT_ORGANISATION);
		sortedContext.put(ExtendedFunctionalityPosition.UPDATE_BEFORE_METADATA_VALIDATION,
				recordTypes);
	}

	@Override
	public ExtendedFunctionality factor(ExtendedFunctionalityPosition position, String recordType) {
		if (recordTypeIsOrganisation(recordType)
				&& positionExistsForRecordType(position, recordType)) {
			return new OrganisationExtendedFunctionality();
		}
		throw NotImplementedException
				.withMessage("Extended functionality not implemented for recordType: " + recordType
						+ " and position: " + position.toString());

	}

	private boolean recordTypeIsOrganisation(String recordType) {
		return COMMON_ORGANISATION.equals(recordType) || ROOT_ORGANISATION.equals(recordType);
	}

	private boolean positionExistsForRecordType(ExtendedFunctionalityPosition position,
			String recordType) {
		return sortedContext.containsKey(position)
				&& sortedContext.get(position).contains(recordType);
	}

	@Override
	public List<ExtendedFunctionalityContext> getExtendedFunctionalityContexts() {
		return contexts;
	}

}
