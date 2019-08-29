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
package org.semanticweb.elk.reasoner.indexing.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.liveontologies.puli.Producer;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkDeclarationAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDifferentIndividualsAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkDisjointUnionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentObjectPropertiesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkIndividual;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyChain;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyDomainAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyRangeAxiom;
import org.semanticweb.elk.owl.interfaces.ElkReflexiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSameIndividualAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkTransitiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.predefined.ElkPolarity;
import org.semanticweb.elk.owl.predefined.PredefinedElkClassFactory;
import org.semanticweb.elk.reasoner.indexing.classes.ModifiableIndexedObjectBaseFactory;
import org.semanticweb.elk.reasoner.indexing.classes.ResolvingModifiableIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.classes.UpdatingModifiableIndexedObjectFactory;
import org.semanticweb.elk.reasoner.indexing.model.CachedIndexedObject;
import org.semanticweb.elk.reasoner.indexing.model.IndexedAxiom;
import org.semanticweb.elk.reasoner.indexing.model.IndexedAxiomInference;
import org.semanticweb.elk.reasoner.indexing.model.IndexedClass;
import org.semanticweb.elk.reasoner.indexing.model.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.model.IndexedObject;
import org.semanticweb.elk.reasoner.indexing.model.IndexingListener;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedAxiom;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedAxiomInference;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedClass;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedClassExpressionList;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedIndividual;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObject;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObjectCache;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObjectIntersectionOf;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedPropertyChain;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableOntologyIndex;
import org.semanticweb.elk.reasoner.indexing.model.Occurrence;
import org.semanticweb.elk.reasoner.indexing.model.OccurrenceIncrement;

/**
 * An implementation of {@link ElkAxiomConverter} that converts {@link ElkAxiom}
 * s to {@link ModifiableIndexedAxiom}s using a
 * {@link ModifiableIndexedAxiomInference.Factory} in addition to converters and
 * factories for other types of {@link IndexedObject}s.
 * 
 * @author Frantisek Simancik
 * @author "Yevgeny Kazakov"
 * @author Peter Skocovsky
 */
public class ElkAxiomConverterImpl extends FailingElkAxiomConverter {

	/**
	 * The factory for creating entities
	 */
	private final PredefinedElkClassFactory elkFactory_;

	/**
	 * The factory to convert axioms
	 */
	private final ModifiableIndexedAxiomInference.Factory axiomInferenceFactory_;

	/**
	 * The factories to create objects of respectively positive and negative
	 * polarities of occurrences.
	 */
	private final ModifiableIndexedObject.Factory positiveFactory_,
			negativeFactory_;

	private final ElkPolarityExpressionConverter positiveConverter_,
			negativeConverter_, dualConverter_;

	private final ElkEntityConverter entityConverter_;

	private final IndexingListener indexingListener_;

	/**
	 * Creates an {@link ElkAxiomConverter} that uses four
	 * {@link ModifiableIndexedObject.Factory}s to create all
	 * {@link ModifiableIndexedObject} sub-expression of the resulting
	 * {@link ModifiableIndexedAxiom}s depending on (logical) polarity of their
	 * occurrence.
	 * 
	 * @param elkFactory
	 *            used to create auxiliary {@link ElkObject}s
	 * 
	 * @param neutralFactory
	 *            this {@link ModifiableIndexedAxiomInference.Factory} is used
	 *            to create {@link IndexedAxiom}s as well as
	 *            {@link ModifiableIndexedObject}s that do not occur (logically)
	 *            positively or negatively in the ontology, such as entities in
	 *            declaration axioms
	 * @param positiveFactory
	 *            this {@link ModifiableIndexedObject.Factory} is used to create
	 *            positive occurrences of {@link ModifiableIndexedObject}s, such
	 *            as {@link IndexedClassExpression}s on the right-hand-side of
	 *            {@code SubClassOf} axioms.
	 * 
	 * @param negativeFactory
	 *            this {@link ModifiableIndexedObject.Factory} is used to create
	 *            negative occurrences of {@link ModifiableIndexedObject}s, such
	 *            as {@link IndexedClassExpression}s on the right-hand-side of
	 *            {@code SubClassOf} axioms.
	 * @param dualFactory
	 *            this {@link ModifiableIndexedObject.Factory} is used to create
	 *            simultaneously positive and negative occurrences of
	 *            {@link ModifiableIndexedObject}s, such as
	 *            {@link IndexedClassExpression}s in {@code EquivalentClasses}
	 *            axioms.
	 * @param indexingListener
	 *            the {@link IndexingListener} notified about indexed
	 *            occurrences
	 * @param producer
	 *            a {@link Producer} used to record the conversion inferences
	 */
	public ElkAxiomConverterImpl(PredefinedElkClassFactory elkFactory,
			ModifiableIndexedObject.Factory neutralFactory,
			ModifiableIndexedObject.Factory positiveFactory,
			ModifiableIndexedObject.Factory negativeFactory,
			ModifiableIndexedObject.Factory dualFactory,
			final IndexingListener indexingListener,
			Producer<? super IndexedAxiomInference> producer) {
		this.elkFactory_ = elkFactory;
		this.axiomInferenceFactory_ = new ModifiableIndexedAxiomInferenceConclusionVisitingFactory(
				producer, neutralFactory);
		this.positiveFactory_ = positiveFactory;
		this.negativeFactory_ = negativeFactory;
		this.positiveConverter_ = new ElkPolarityExpressionConverterImpl(
				ElkPolarity.POSITIVE, elkFactory, positiveFactory,
				negativeFactory, indexingListener);
		this.negativeConverter_ = positiveConverter_
				.getComplementaryConverter();
		this.dualConverter_ = new ElkPolarityExpressionConverterImpl(elkFactory,
				dualFactory, indexingListener);
		this.entityConverter_ = new ElkEntityConverterImpl(neutralFactory);
		this.indexingListener_ = indexingListener;
	}

	/**
	 * Creates an {@link ElkAxiomConverter} that uses the given
	 * {@link ModifiableIndexedObject.Factory}s to create all
	 * {@link ModifiableIndexedObject} sub-expression of the resulting
	 * {@link ModifiableIndexedAxiom}s.
	 * 
	 * @param elkFactory
	 *            used to create auxiliary {@link ElkObject}s
	 * 
	 * @param factory
	 *            the {@link ModifiableIndexedObject.Factory} that is used to
	 *            create all {@link ModifiableIndexedObject}s necessary for
	 *            conversion of {@link ElkAxiom}s.
	 * @param indexingListener
	 *            the {@link IndexingListener} notified about indexed
	 *            occurrences
	 * @param producer
	 *            a {@link Producer} used to record the conversion inferences
	 */
	public ElkAxiomConverterImpl(PredefinedElkClassFactory elkFactory,
			ModifiableIndexedObject.Factory factory,
			final IndexingListener indexingListener,
			Producer<? super IndexedAxiomInference> producer) {
		this(elkFactory, factory, factory, factory, factory, indexingListener,
				producer);
	}

	/**
	 * Creates an {@link ElkAxiomConverter} for converting {@link ElkAxiom}s
	 * that have already been indexed in the given
	 * {@link ModifiableIndexedObjectCache}. All {@link ModifiableIndexedObject}
	 * s subexpression of the converted {@link ModifiabledIndexedAxiom}s are
	 * taken from this {@link ModifiableIndexedObjectCache} as well. The
	 * provided {@link ModifiableIndexedObjectCache} will not change. The
	 * converter may not work properly if used with {@link ElkAxiom}s that have
	 * not been indexed in the given {@link ModifiableIndexedObjectCache} (e.g.,
	 * {@link NullPointerException}s may occur).
	 * 
	 * @param elkFactory
	 *            used to create auxiliary {@link ElkObject}s
	 * 
	 * @param index
	 *            the {@link ModifiableIndexedObjectCache} from which all
	 *            {@link ModifiableIndexedObject}s are used.
	 */
	public ElkAxiomConverterImpl(PredefinedElkClassFactory elkFactory,
			ModifiableOntologyIndex index) {
		this(elkFactory, new ResolvingModifiableIndexedObjectFactory(index),
				index, Producer.Dummy.get());
	}

	<F extends CachedIndexedObject.Factory & ModifiableIndexedObject.Factory> ElkAxiomConverterImpl(
			PredefinedElkClassFactory elkFactory, F baseFactory,
			ModifiableOntologyIndex index, int increment) {
		this(elkFactory,
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getNeutralIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getPositiveIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getNegativeIncrement(increment)),
				new UpdatingModifiableIndexedObjectFactory(baseFactory, index,
						OccurrenceIncrement.getDualIncrement(increment)),
				index, Producer.Dummy.get());
	}

	/**
	 * Creates an {@link ElkAxiomConverter} that converts {@link ElkAxiom}s and
	 * adds them to the provided {@link ModifiableOntologyIndex} with the given
	 * multiplicity. The converter will also insert all intermediate
	 * {@link ModifiableIndexedObject} subexpressions of the created
	 * {@link ModifiableIndexedAxiom}s with the same multiplicity for polarity
	 * of their occurrences. Some {@link ModifiableIndexedObject}s will be
	 * reused and only their multiplicity will be updated.
	 * 
	 * @param elkFactory
	 *            used to create auxiliary {@link ElkObject}s
	 * 
	 * @param index
	 *            the {@link ModifiableOntologyIndex} representing the ontology
	 *            to which the resulting {@link ModifiableIndexedObject}s will
	 *            be added and processed (compiled to rules).
	 * @param increment
	 *            the multiplicity with which the created
	 *            {@link ModifiableIndexedAxiom}s should be inserted to the
	 *            {@link ModifiableOntologyIndex} (e.g., 2 means 2 copies of
	 *            each create axiom should be added); it can be negative, which
	 *            means that the {@link ModifiableIndexedAxiom}s should be
	 *            removed the corresponding number of times.
	 * 
	 */
	public ElkAxiomConverterImpl(PredefinedElkClassFactory elkFactory,
			ModifiableOntologyIndex index, int increment) {
		this(elkFactory, new ModifiableIndexedObjectBaseFactory(), index,
				increment);
	}

	@Override
	public Void visit(ElkObjectPropertyDomainAxiom axiom) {
		ModifiableIndexedClass indexedOwlThing = negativeFactory_
				.getIndexedClass(elkFactory_.getOwlThing());
		axiomInferenceFactory_.getElkObjectPropertyDomainAxiomConversion(axiom,
				negativeFactory_.getIndexedObjectSomeValuesFrom(
						axiom.getProperty().accept(negativeConverter_),
						indexedOwlThing),
				axiom.getDomain().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkObjectPropertyRangeAxiom axiom) {
		indexingListener_
				.onIndexing(Occurrence.OCCURRENCE_OF_OBJECT_PROPERTY_RANGE);
		axiomInferenceFactory_.getElkObjectPropertyRangeAxiomConversion(axiom,
				axiom.getProperty().accept(negativeConverter_),
				axiom.getRange().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkReflexiveObjectPropertyAxiom axiom) {
		axiomInferenceFactory_.getElkReflexiveObjectPropertyAxiomConversion(
				axiom,
				negativeFactory_.getIndexedClass(elkFactory_.getOwlThing()),
				positiveFactory_.getIndexedObjectHasSelf(
						axiom.getProperty().accept(positiveConverter_)));
		return null;
	}

	@Override
	public Void visit(ElkSubObjectPropertyOfAxiom axiom) {
		axiomInferenceFactory_.getElkSubObjectPropertyOfAxiomConversion(axiom,
				axiom.getSubObjectPropertyExpression().accept(this),
				axiom.getSuperObjectPropertyExpression()
						.accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkEquivalentObjectPropertiesAxiom axiom) {
		// reduces equivalence to sub-property axioms
		ModifiableIndexedObjectProperty first = null;
		List<? extends ElkObjectPropertyExpression> members = axiom
				.getObjectPropertyExpressions();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkObjectPropertyExpression pe = members.get(pos);
			if (first == null)
				first = pe.accept(dualConverter_);
			else {
				ModifiableIndexedObjectProperty other = pe
						.accept(dualConverter_);
				axiomInferenceFactory_
						.getElkEquivalentObjectPropertiesAxiomConversion(axiom,
								0, pos, first, other);
				axiomInferenceFactory_
						.getElkEquivalentObjectPropertiesAxiomConversion(axiom,
								pos, 0, other, first);
			}
		}
		return null;
	}

	@Override
	public Void visit(ElkTransitiveObjectPropertyAxiom axiom) {
		ModifiableIndexedObjectProperty p = axiom.getProperty()
				.accept(dualConverter_);
		axiomInferenceFactory_.getElkTransitiveObjectPropertyAxiomConversion(
				axiom, negativeFactory_.getIndexedComplexPropertyChain(p, p),
				p);
		return null;
	}

	@Override
	public Void visit(ElkEquivalentClassesAxiom axiom) {
		// reduces equivalence to class definitions or sub-class axioms
		ModifiableIndexedClassExpression first = null;
		boolean firstIsAClass = false;
		List<? extends ElkClassExpression> members = axiom
				.getClassExpressions();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkClassExpression c = members.get(pos);
			if (first == null) {
				first = c.accept(dualConverter_);
				if (first instanceof IndexedClass) {
					firstIsAClass = true;
				}
			} else {
				ModifiableIndexedClassExpression other = c
						.accept(dualConverter_);
				if (!firstIsAClass && other instanceof ModifiableIndexedClass) {
					axiomInferenceFactory_
							.getElkEquivalentClassesAxiomEquivalenceConversion(
									axiom, pos, 0, other, first);
				} else {
					axiomInferenceFactory_
							.getElkEquivalentClassesAxiomEquivalenceConversion(
									axiom, 0, pos, first, other);
				}
			}
		}
		return null;

	}

	@Override
	public Void visit(ElkSameIndividualAxiom axiom) {
		// reduces equivalence to sub-class axioms
		ModifiableIndexedIndividual first = null;
		List<? extends ElkIndividual> members = axiom.getIndividuals();
		for (int pos = 0; pos < members.size(); pos++) {
			ElkIndividual ind = members.get(pos);
			if (first == null) {
				first = ind.accept(dualConverter_);
			} else {
				ModifiableIndexedIndividual other = ind.accept(dualConverter_);
				axiomInferenceFactory_.getElkSameIndividualAxiomConversion(
						axiom, 0, pos, first, other);
				axiomInferenceFactory_.getElkSameIndividualAxiomConversion(
						axiom, pos, 0, other, first);
			}
		}
		return null;
	}

	@Override
	public Void visit(ElkSubClassOfAxiom axiom) {
		axiomInferenceFactory_.getElkSubClassOfAxiomConversion(axiom,
				axiom.getSubClassExpression().accept(negativeConverter_),
				axiom.getSuperClassExpression().accept(positiveConverter_));
		return null;
	}

	/**
	 * With at most how many disjoint classes transform to binary disjointness
	 * axioms
	 */
	private final static int DISJOINT_AXIOM_BINARIZATION_THRESHOLD = 2;

	private interface DisjointnessIndexer<A extends ElkAxiom, M extends ElkObject> {

		ModifiableIndexedClassExpression convert(M member);

		void indexNary(A axiom, ModifiableIndexedClassExpressionList members);

		void indexBinary(A axiom, int firstMemberPosition,
				int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom);
	}

	private <A extends ElkAxiom, M extends ElkObject> void indexDisjointMembers(
			DisjointnessIndexer<A, M> indexer, A axiom,
			List<? extends M> members) {
		/* index (possibly implicit) positive occurrence of owl:Nothing */
		ModifiableIndexedClass indexedOwlNothing = positiveFactory_
				.getIndexedClass(elkFactory_.getOwlNothing());
		/*
		 * for many disjoint members, convert natively
		 */
		if (members.size() > DISJOINT_AXIOM_BINARIZATION_THRESHOLD) {
			List<ModifiableIndexedClassExpression> indexedMembers = new ArrayList<ModifiableIndexedClassExpression>(
					members.size());
			for (M member : members)
				indexedMembers.add(indexer.convert(member));
			indexer.indexNary(axiom, positiveFactory_
					.getIndexedClassExpressionList(indexedMembers));
			return;
		}
		/*
		 * otherwise create a binary disjointness axioms for all pairs (first,
		 * second) where second occurs after the first in the member list
		 */
		for (int firstPos = 0; firstPos < members.size(); firstPos++) {
			M first = members.get(firstPos);
			ModifiableIndexedClassExpression firstIndexed = indexer
					.convert(first);
			for (int secondPos = firstPos + 1; secondPos < members
					.size(); secondPos++) {
				M second = members.get(secondPos);
				ModifiableIndexedClassExpression secondIndexed = indexer
						.convert(second);
				indexer.indexBinary(axiom, firstPos, secondPos,
						negativeFactory_.getIndexedObjectIntersectionOf(
								firstIndexed, secondIndexed),
						indexedOwlNothing);
			}
		}
	}

	private class ElkDisjointClassesAxiomDisjointnessIndexer implements
			DisjointnessIndexer<ElkDisjointClassesAxiom, ElkClassExpression> {

		@Override
		public ModifiableIndexedClassExpression convert(
				ElkClassExpression member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDisjointClassesAxiom axiom,
				ModifiableIndexedClassExpressionList members) {

			axiomInferenceFactory_
					.getElkDisjointClassesAxiomNaryConversion(axiom, members);
		}

		@Override
		public void indexBinary(ElkDisjointClassesAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomInferenceFactory_.getElkDisjointClassesAxiomBinaryConversion(
					axiom, firstMemberPosition, secondMemberPosition,
					memberConunction, bottom);
		}
	}

	private final ElkDisjointClassesAxiomDisjointnessIndexer elkDisjointClassesAxiomDisjointnessIndexer = new ElkDisjointClassesAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDisjointClassesAxiom axiom) {
		indexDisjointMembers(elkDisjointClassesAxiomDisjointnessIndexer, axiom,
				axiom.getClassExpressions());
		return null;
	}

	private class ElkDisjointUnionAxiomDisjointnessIndexer implements
			DisjointnessIndexer<ElkDisjointUnionAxiom, ElkClassExpression> {

		@Override
		public ModifiableIndexedClassExpression convert(
				ElkClassExpression member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDisjointUnionAxiom axiom,
				ModifiableIndexedClassExpressionList members) {
			axiomInferenceFactory_.getElkDisjointUnionAxiomNaryConversion(axiom,
					members);
		}

		@Override
		public void indexBinary(ElkDisjointUnionAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomInferenceFactory_.getElkDisjointUnionAxiomBinaryConversion(
					axiom, firstMemberPosition, secondMemberPosition,
					memberConunction, bottom);
		}

	}

	private final ElkDisjointUnionAxiomDisjointnessIndexer elkDisjointUnionAxiomDisjointnessIndexer = new ElkDisjointUnionAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDisjointUnionAxiom axiom) {
		List<? extends ElkClassExpression> disjoint = axiom
				.getClassExpressions();
		indexDisjointMembers(elkDisjointUnionAxiomDisjointnessIndexer, axiom,
				disjoint);
		ModifiableIndexedClass defined;
		ModifiableIndexedClassExpression member;
		int size = disjoint.size();
		switch (size) {
		case 0:
			defined = (ModifiableIndexedClass) axiom.getDefinedClass()
					.accept(positiveConverter_);
			axiomInferenceFactory_.getElkDisjointUnionAxiomOwlNothingConversion(
					axiom, defined, positiveFactory_
							.getIndexedClass(elkFactory_.getOwlNothing()));
			break;
		case 1:
			defined = (ModifiableIndexedClass) axiom.getDefinedClass()
					.accept(dualConverter_);
			member = disjoint.iterator().next().accept(dualConverter_);
			axiomInferenceFactory_
					.getElkDisjointUnionAxiomEquivalenceConversion(axiom,
							defined, member);
			break;
		default:
			// indexing only one direction of the equivalence
			indexingListener_
					.onIndexing(Occurrence.OCCURRENCE_OF_DISJOINT_UNION);
			defined = (ModifiableIndexedClass) axiom.getDefinedClass()
					.accept(positiveConverter_);
			for (int pos = 0; pos < size; pos++) {
				ElkClassExpression c = disjoint.get(pos);
				member = c.accept(negativeConverter_);
				axiomInferenceFactory_
						.getElkDisjointUnionAxiomSubClassConversion(axiom, pos,
								member, defined);
			}
			break;
		}
		return null;
	}

	private class ElkDifferentIndividualsAxiomDisjointnessIndexer implements
			DisjointnessIndexer<ElkDifferentIndividualsAxiom, ElkIndividual> {

		@Override
		public ModifiableIndexedClassExpression convert(ElkIndividual member) {
			return member.accept(negativeConverter_);
		}

		@Override
		public void indexNary(ElkDifferentIndividualsAxiom axiom,
				ModifiableIndexedClassExpressionList members) {
			axiomInferenceFactory_
					.getElkDifferentIndividualsAxiomNaryConversion(axiom,
							members);
		}

		@Override
		public void indexBinary(ElkDifferentIndividualsAxiom axiom,
				int firstMemberPosition, int secondMemberPosition,
				ModifiableIndexedObjectIntersectionOf memberConunction,
				ModifiableIndexedClass bottom) {
			axiomInferenceFactory_
					.getElkDifferentIndividualsAxiomBinaryConversion(axiom,
							firstMemberPosition, secondMemberPosition,
							memberConunction, bottom);
		}
	}

	private final ElkDifferentIndividualsAxiomDisjointnessIndexer elkDifferentIndividualsAxiomDisjointnessIndexer = new ElkDifferentIndividualsAxiomDisjointnessIndexer();

	@Override
	public Void visit(ElkDifferentIndividualsAxiom axiom) {
		indexDisjointMembers(elkDifferentIndividualsAxiomDisjointnessIndexer,
				axiom, axiom.getIndividuals());
		return null;
	}

	@Override
	public Void visit(ElkClassAssertionAxiom axiom) {
		axiomInferenceFactory_.getElkClassAssertionAxiomConversion(axiom,
				axiom.getIndividual().accept(negativeConverter_),
				axiom.getClassExpression().accept(positiveConverter_));
		return null;
	}

	@Override
	public Void visit(ElkObjectPropertyAssertionAxiom axiom) {
		indexingListener_
				.onIndexing(Occurrence.OCCURRENCE_OF_OBJECT_PROPERTY_RANGE);
		axiomInferenceFactory_.getElkObjectPropertyAssertionAxiomConversion(
				axiom, axiom.getSubject().accept(negativeConverter_),
				positiveFactory_.getIndexedObjectSomeValuesFrom(
						axiom.getProperty().accept(positiveConverter_), axiom
								.getObject().accept(positiveConverter_)));
		return null;
	}

	@Override
	public Void visit(ElkDeclarationAxiom axiom) {
		axiomInferenceFactory_.getElkDeclarationAxiomConversion(axiom,
				axiom.getEntity().accept(entityConverter_));
		return null;
	}

	@Override
	public ModifiableIndexedPropertyChain visit(ElkObjectProperty expression) {
		return expression.accept(negativeConverter_);
	}

	@Override
	public ModifiableIndexedPropertyChain visit(
			ElkObjectPropertyChain elkObjectPropertyChain) {
		ModifiableIndexedPropertyChain result = null;
		// iterating over the chain backwards
		ListIterator<? extends ElkObjectPropertyExpression> iterator = elkObjectPropertyChain
				.getObjectPropertyExpressions()
				.listIterator(elkObjectPropertyChain
						.getObjectPropertyExpressions().size());
		while (iterator.hasPrevious()) {
			ModifiableIndexedObjectProperty iop = iterator.previous()
					.accept(negativeConverter_);
			if (result == null) {
				result = iop;
				continue;
			}
			result = negativeFactory_.getIndexedComplexPropertyChain(iop,
					result);
		}
		return result;
	}

}
