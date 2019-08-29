/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2013 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.indexing.classes;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.printers.OwlFunctionalStylePrinter;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkAxiomConverter;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkIndexingUnsupportedException;
import org.semanticweb.elk.reasoner.indexing.model.IndexingListener;
import org.semanticweb.elk.reasoner.indexing.model.Occurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basically an adapter from {@link ElkAxiomConverter} to
 * {@link ElkAxiomProcessor} specifically for classes which index axioms.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class ChangeIndexingProcessor implements ElkAxiomProcessor {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ChangeIndexingProcessor.class);

	/**
	 * Some predefined types of processing
	 */
	public static final String ADDITION = "addition", REMOVAL = "removal";

	private final ElkAxiomConverter indexer_;

	private final String type_; // deletion or addition

	private final IndexingListener indexingListener_;

	public ChangeIndexingProcessor(ElkAxiomConverter indexer, String type,
			final IndexingListener indexingListener) {
		this.indexer_ = indexer;
		this.type_ = type;
		this.indexingListener_ = indexingListener;
	}

	@Override
	public void visit(ElkAxiom elkAxiom) {
		try {
			if (LOGGER_.isTraceEnabled())
				LOGGER_.trace("$$ indexing "
						+ OwlFunctionalStylePrinter.toString(elkAxiom) + " for "
						+ type_);
			elkAxiom.accept(indexer_);
		} catch (ElkIndexingUnsupportedException e) {
			indexingListener_.onIndexing(
					Occurrence.OCCURRENCE_OF_UNSUPPORTED_EXPRESSION);
		}
	}
}
