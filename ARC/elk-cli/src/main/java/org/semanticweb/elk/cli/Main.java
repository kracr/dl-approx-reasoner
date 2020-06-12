/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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
/**
 * @author Yevgeny Kazakov, Jun 11, 2011
 */
package org.semanticweb.elk.cli;

import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.semanticweb.elk.exceptions.ElkException;
import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.loading.Owl2StreamLoader;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkNamedIndividual;
import org.semanticweb.elk.owl.parsing.Owl2ParserFactory;
import org.semanticweb.elk.owl.parsing.javacc.Owl2FunctionalStyleParserFactory;
import org.semanticweb.elk.reasoner.ElkInconsistentOntologyException;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.elk.reasoner.taxonomy.TaxonomyPrinter;
import org.semanticweb.elk.reasoner.taxonomy.hashing.InstanceTaxonomyHasher;
import org.semanticweb.elk.reasoner.taxonomy.hashing.TaxonomyHasher;
import org.semanticweb.elk.reasoner.taxonomy.model.InstanceTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import org.semanticweb.elk.util.logging.Statistics;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.semanticweb.elk.util.logging.Statistics;


/**
 * 
 * The command line interface for Elk reasoner. Typically for the usage within
 * stand-alone executables.
 * 
 * @author Yevgeny Kazakov
 * @author Frantisek Simancik
 * 
 */
public class Main {
	// logger for this class
	public static Logger LOGGER_ = LoggerFactory.getLogger(Main.class);
	/**
	 * @param args3 
	 * @param args2 
	 * @param args 
	 * @param args
	 * @throws ElkException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws OWLOntologyCreationException 
	 * @throws Exception
	 */
public static long test_drive(String args, String args2, String args3) throws ElkException, IOException, InterruptedException, OWLOntologyCreationException 
{	
	System.out.println("ELK Started");
	ReasonerConfiguration configuration = ReasonerConfiguration.getConfiguration();
	configuration.setParameter(ReasonerConfiguration.NUM_OF_WORKING_THREADS, args2);
	ReasonerFactory reasoningFactory = new ReasonerFactory();
	Owl2ParserFactory parserFactory = new Owl2FunctionalStyleParserFactory();
	AxiomLoader.Factory loader = new Owl2StreamLoader.Factory(parserFactory,new File(args));
	
	Reasoner reasoner = reasoningFactory.createReasoner(loader,configuration);
	

	try {
		
		
		Taxonomy<ElkClass> taxonomy = reasoner.getTaxonomyQuietly();		

		System.out.println("Execution time of ELK: " + Statistics.rettimeinforprint());
		writeClassTaxonomyToFile(new File(args3),taxonomy, false);

	} finally {
		reasoner.shutdown();
	}
	if(reasoner.shutdown()) 
	{
		AfterC.prc(new File(args),new File(args3));
	}
	return Long.parseLong(Statistics.rettimeinforprint());
	
}
public static int countLinesOld(String filename) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(filename));
    try {
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        return (count == 0 && !empty) ? 1 : count;
    } finally {
        is.close();
    }
}
public static void main(String[] args) throws Exception 
{
//	File c=new File(args[0]);
//	IRI physicalIRI = IRI.create(c);
//	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(physicalIRI);
	
	int i=-1;
	int m=countLinesOld(args[0]);
	System.out.println("Initial Lines Count: "+m);
	int iter=0;
	long ttime=0;
	while(i<m) 
	{
		i=m;
		long c2= test_drive(args[0],args[1],args[2]);
		System.gc();
		int tempcounter1=countLinesOld(args[0]);
		System.out.println("Elk Completed adding "+(tempcounter1-m)+ " Axioms.");
		m=tempcounter1;
		
		A_PreProcess mncv=new A_PreProcess();
		long c1=mncv.test_drive2(args[0],args[1],args[2],iter);
		System.gc();
		int tempcounter2=countLinesOld(args[0]);
		System.out.println("New System Completed adding "+(tempcounter2-m)+ " Axioms.");
		m=tempcounter2;
		iter+=1;	
		ttime=ttime+c1+c2;
	}
	System.out.println("******************************************************************************");
	System.out.println("Saturation after "+iter+" Iterations in "+ttime +" ms with "+m+" lines.");
	System.out.println("******************************************************************************");
	System.gc();
		
}

	static void writeConsistencyToFile(File file, Boolean consistent)
			throws IOException, ElkException {
		LOGGER_.info("Writing consistency to {}", file);

		FileWriter fstream = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(fstream);
		writer.write(consistent.toString() + "\n");
		writer.write("# The ontology is "
				+ (consistent ? "consistent" : "inconsistent") + ".\n");
		writer.close();
	}

	static void writeClassTaxonomyToFile(File file, Taxonomy<ElkClass> taxonomy, boolean printHash)
			throws IOException, ElkInconsistentOntologyException, ElkException {
		LOGGER_.info("Writing taxonomy to {}", file);

		Statistics.logOperationStart("Writing taxonomy", LOGGER_);
		TaxonomyPrinter.dumpTaxomomyToFile(taxonomy, file.getPath(), printHash);
		Statistics.logOperationFinish("Writing taxonomy", LOGGER_);
	}

	static void writeInstanceTaxonomyToFile(File file,
			InstanceTaxonomy<ElkClass, ElkNamedIndividual> taxonomy, boolean printHash)
			throws IOException, ElkInconsistentOntologyException, ElkException {
		LOGGER_.info("Writing taxonomy with instances to {}", file);

		Statistics
				.logOperationStart("Writing taxonomy with instances", LOGGER_);
		TaxonomyPrinter.dumpInstanceTaxomomyToFile(taxonomy, file.getPath(),
				printHash);
		Statistics.logOperationFinish("Writing taxonomy with instances",
				LOGGER_);
	}

	static void printTaxonomyHash(Taxonomy<ElkClass> taxonomy) {System.out.println("Main.java printTaxonomyHash");System.out.println("Main.java printTaxonomyHash");
		if (LOGGER_.isInfoEnabled()) {
			LOGGER_.info("Taxonomy hash: "
					+ Integer.toHexString(TaxonomyHasher.hash(taxonomy)));
		}
	}

	static void printTaxonomyHash(
			InstanceTaxonomy<ElkClass, ElkNamedIndividual> taxonomy) {
		if (LOGGER_.isInfoEnabled()) {
			LOGGER_.info("Taxonomy hash: "
					+ Integer.toHexString(InstanceTaxonomyHasher.hash(taxonomy)));
		}
	}

	static int countOptions(OptionSet options, OptionSpec<?>... specs) 
{
		int count = 0;
		for (OptionSpec<?> s : specs)
			if (options.has(s))
				count++;
		return count;
	}
}