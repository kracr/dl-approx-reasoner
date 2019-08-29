import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.owl.owlapi.tutorial.LabelExtractor;
public class Hermit {

	public static void main(String[] args) throws Exception {

		File c=new File(args[0]);
		IRI physicalIRI = IRI.create(c);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(physicalIRI);
        System.out.println(ontology.getLogicalAxiomCount());
        
        
        // Now we can start and create the reasoner. Lets this time use HermiT's native interface.
        // For this we don't need a factory.
        // The OWLReasoner interface is very similar though, it just has fewer methods
        Reasoner reasoner=new Reasoner(ontology);
        long startTime = System.nanoTime();
        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
        
        long endTime = System.nanoTime();
        long duration = ((endTime - startTime));        
        
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); 
		long memory = (runtime.totalMemory() - runtime.freeMemory());
        long tmemory=runtime.totalMemory();
        
        
        System.out.print(duration+" ");
        System.out.print(tmemory/(1024L * 1024L)+" ");
        System.out.print((memory/(1024L * 1024L))+" ");
        
        
        
        
//        String reasonerFactoryClassName = null;
//        OWLDataFactory df = manager.getOWLDataFactory();


     
    
		if (reasoner.isConsistent()) {
	
	        // Now we create an output stream that HermiT can use to write the axioms. The output stream is
	        // a wrapper around the file into which the axioms are written.
	        File prettyPrintHierarchyFile=new File("/home/ajkamal/Desktop/fma.owl");
	        if (!prettyPrintHierarchyFile.exists())
	            prettyPrintHierarchyFile.createNewFile();
	        // turn to an absolute file, so that we can write to it
	        prettyPrintHierarchyFile=prettyPrintHierarchyFile.getAbsoluteFile();
	        BufferedOutputStream prettyPrintHierarchyStreamOut=new BufferedOutputStream(new FileOutputStream(prettyPrintHierarchyFile));
	        // The output stream is wrapped into a print write with autoflush.
	        PrintWriter output=new PrintWriter(prettyPrintHierarchyStreamOut,true);
	        // Now we let HermiT pretty print the hierarchies. Since all parameters are set to true,
	        // HermiT will print the class and the object property and the data property hierarchy.
	        long t=System.currentTimeMillis();
	        t=System.currentTimeMillis()-t;
	        reasoner.printHierarchies(output, true, true, true);
	
	        // Now save a copy in OWL/XML format
	        //File f = new File("example.xml");                         
	        //IRI documentIRI2 = IRI.create(f);
	        //manager.saveOntology(school, new OWLXMLOntologyFormat(), documentIRI2);
	        //System.out.println("Ontology saved in XML format.");
	    } else 
	    {
	        System.out.println("Ontology malformed.");
	    }
	}
}





class vcse {
    private static int INDENT = 4;
    private final OWLReasonerFactory reasonerFactory;
    private final OWLOntology ontology;
    private final PrintStream out;

    public vcse(OWLReasonerFactory reasonerFactory,
            OWLOntology _ontology) {
        this.reasonerFactory = reasonerFactory;
        ontology = _ontology;
        out = System.out;
    }

    /** Print the class hierarchy for the given ontology from this class down,
     * assuming this class is at the given level. Makes no attempt to deal
     * sensibly with multiple inheritance. */
    public void printHierarchy(OWLClass clazz) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, clazz, 0);
        /* Now print out any unsatisfiable classes */
        for (OWLClass cl : ontology.getClassesInSignature()) {
            if (!reasoner.isSatisfiable(cl)) {
                out.println("XXX: " + labelFor(cl));
            }
        }
        reasoner.dispose();
    }

    public String labelFor(OWLClass clazz) {
        /*
         * Use a visitor to extract label annotations
         */
        LabelExtractor le = new LabelExtractor();
        Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology);
        for (OWLAnnotation anno : annotations) {
            anno.accept(le);
        }
        /* Print out the label if there is one. If not, just use the class URI */
        if (le.getResult() != null) {
            return le.getResult().toString();
        } else {
            return clazz.getIRI().toString();
        }
    }

    /** Print the class hierarchy from this class down, assuming this class is at
     * the given level. Makes no attempt to deal sensibly with multiple
     * inheritance. */
    public void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level)
            throws OWLException {
        /*
         * Only print satisfiable classes -- otherwise we end up with bottom
         * everywhere
         */
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * INDENT; i++) {
                out.print(" ");
            }
            out.println(labelFor(clazz));
            /* Find the children and recurse */
            for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1);
                }
            }
        }
    }

//    @SuppressWarnings("javadoc")
//    public static void main(String[] args) throws OWLException, InstantiationException,
//            IllegalAccessException, ClassNotFoundException {
//        String reasonerFactoryClassName = null;
//        // We first need to obtain a copy of an
//        // OWLOntologyManager, which, as the name
//        // suggests, manages a set of ontologies.
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//        // We load an ontology from the URI specified
//        // on the command line
//        System.out.println(args[0]);
//        IRI documentIRI = IRI.create(args[0]);
//        // Now load the ontology.
//        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
//        // Report information about the ontology
//        System.out.println("Ontology Loaded...");
//        System.out.println("Document IRI: " + documentIRI);
//        System.out.println("Ontology : " + ontology.getOntologyID());
//        System.out.println("Format      : " + manager.getOntologyFormat(ontology));
//        // / Create a new SimpleHierarchy object with the given reasoner.
//        vcse simpleHierarchy = new vcse(
//                (OWLReasonerFactory) Class.forName(reasonerFactoryClassName)
//                        .newInstance(), ontology);
//        // Get Thing
//        OWLClass clazz = manager.getOWLDataFactory().getOWLThing();
//        System.out.println("Class       : " + clazz);
//        // Print the hierarchy below thing
//        simpleHierarchy.printHierarchy(clazz);
//    }
}
//public static void main(String[] args) {
//    try {
//        // First, we create an OWLOntologyManager object. The manager will load and
//        // save ontologies.
//        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
//        // Now, we create the file from which the ontology will be loaded.
//        File inputOntologyFile = new File("SchoolESTGExample.owl");
//        // We use the OWL API to load the ontology.
//        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
//        // Now we can start and create the reasoner. Here we create an instance of HermiT
//        // OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();        
//        //OWLReasoner owlReasoner = reasonerFactory.createReasoner(ontology);
//        Reasoner reasoner = new Reasoner(ontology);
//        reasoner.precomputeInferences();
//        // We can determine if the ontology is actually consistent
//        if (reasoner.isConsistent()) {
//
//            // Now we create an output stream that HermiT can use to write the axioms. The output stream is
//            // a wrapper around the file into which the axioms are written.
//            File prettyPrintHierarchyFile=new File("prettyPrint.owl");
//            if (!prettyPrintHierarchyFile.exists())
//                prettyPrintHierarchyFile.createNewFile();
//            // turn to an absolute file, so that we can write to it
//            prettyPrintHierarchyFile=prettyPrintHierarchyFile.getAbsoluteFile();
//            BufferedOutputStream prettyPrintHierarchyStreamOut=new BufferedOutputStream(new FileOutputStream(prettyPrintHierarchyFile));
//            // The output stream is wrapped into a print write with autoflush.
//            PrintWriter output=new PrintWriter(prettyPrintHierarchyStreamOut,true);
//            // Now we let HermiT pretty print the hierarchies. Since all parameters are set to true,
//            // HermiT will print the class and the object property and the data property hierarchy.
//            long t=System.currentTimeMillis();
//            t=System.currentTimeMillis()-t;
//            reasoner.printHierarchies(output, true, true, true);
//
//            // Now save a copy in OWL/XML format
//            //File f = new File("example.xml");                         
//            //IRI documentIRI2 = IRI.create(f);
//            //manager.saveOntology(school, new OWLXMLOntologyFormat(), documentIRI2);
//            //System.out.println("Ontology saved in XML format.");
//        } else {
//            System.out.println("Ontology malformed.");
//        }
//
//        // Remove the ontology from the manager
//        //manager.removeOntology(ontology);
//
//    } catch (OWLOntologyCreationException | IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//
//}


//@SuppressWarnings("deprecation")
//Set<OWLClass> classes = ontology.getClassesInSignature();
//for(OWLClass g: classes) 
//{
////	vcse simpleHierarchy = new vcse((OWLReasonerFactory) Class.forName(g.toString())
////        .newInstance(), ontology);
////	 OWLClass clazz = manager.getOWLDataFactory().getOWLThing();
//// System.out.println("Class       : " + clazz);
//// // Print the hierarchy below thing
//// simpleHierarchy.printHierarchy(clazz);
//	OWLClass clsA = df.getOWLClass(IRI.create(g.toString()));
//  Set<OWLClassExpression> superClasses = clsA.getSuperClasses(ontology);
//  //System.out.println("Hello World\n"+superClasses.iterator().toString());
//  for (OWLClassExpression gg : superClasses) {
//       System.out.println("The superclasses are:"+gg);
//  }
//}

//
//
//
