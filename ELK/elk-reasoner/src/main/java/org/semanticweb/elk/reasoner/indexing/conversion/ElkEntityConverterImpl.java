package org.semanticweb.elk.reasoner.indexing.conversion;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkNamedIndividual;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedEntity;

/**
 * An implementation of {@link ElkEntityConverter} that uses a given
 * {@link ModifiableIndexedEntity.Factory} for creating instances of
 * {@link ModifiableIndexedEntity}
 * 
 * @author "Yevgeny Kazakov"
 *
 */
class ElkEntityConverterImpl extends FailingElkEntityConverter {

	private final ModifiableIndexedEntity.Factory factory_;

	public ElkEntityConverterImpl(ModifiableIndexedEntity.Factory factory) {
		this.factory_ = factory;
	}

	@Override
	public ModifiableIndexedEntity visit(ElkClass expression) {
		return factory_.getIndexedClass(expression);
	}

	@Override
	public ModifiableIndexedEntity visit(ElkNamedIndividual expression) {
		return factory_.getIndexedIndividual(expression);
	}

	@Override
	public ModifiableIndexedEntity visit(ElkObjectProperty expression) {
		return factory_.getIndexedObjectProperty(expression);
	}

}
