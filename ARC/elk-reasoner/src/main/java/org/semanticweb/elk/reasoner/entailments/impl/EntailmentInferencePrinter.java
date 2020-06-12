package org.semanticweb.elk.reasoner.entailments.impl;

/*-
 * #%L
 * ELK Reasoner Core
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2017 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Iterator;
import java.util.List;

import org.semanticweb.elk.reasoner.entailments.DefaultEntailmentInferenceVisitor;
import org.semanticweb.elk.reasoner.entailments.model.Entailment;
import org.semanticweb.elk.reasoner.entailments.model.EntailmentInference;
import org.semanticweb.elk.reasoner.entailments.model.HasReason;

public class EntailmentInferencePrinter
		extends DefaultEntailmentInferenceVisitor<String> {

	public static final EntailmentInferencePrinter INSTANCE = new EntailmentInferencePrinter();

	public static String toString(
			final EntailmentInference entailmentInference) {
		return entailmentInference.accept(INSTANCE);
	}

	private EntailmentInferencePrinter() {
		// private default constructor
	}

	@Override
	protected String defaultVisit(
			final EntailmentInference entailmentInference) {
		final StringBuilder result = new StringBuilder(
				entailmentInference.getConclusion().toString());
		result.append(" -| ");
		final List<? extends Entailment> premises = entailmentInference
				.getPremises();
		final Iterator<? extends Entailment> iter = premises.iterator();
		if (iter.hasNext()) {
			result.append(iter.next().toString());
		}
		while (iter.hasNext()) {
			result.append("; ").append(iter.next().toString());
		}
		if (entailmentInference instanceof HasReason) {
			if (!premises.isEmpty()) {
				result.append("; ");
			}
			result.append(((HasReason<?>) entailmentInference).getReason()
					.toString());
		}
		return result.toString();
	}

}