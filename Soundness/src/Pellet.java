import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;




  
public class Pellet {
	public static void main(String[] args) throws Exception {

		File c=new File(args[0]);
		IRI physicalIRI = IRI.create(c);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(physicalIRI);
        
        
        
        // Now we can start and create the reasoner. Lets this time use HermiT's native interface.
        // For this we don't need a factory.
        // The OWLReasoner interface is very similar though, it just has fewer methods
//		OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
//		OWLReasoner reasoner2 = reasonerFactory.createReasoner(ontology);
		
		
		OWLReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);
		
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
//	        reasoner.printHierarchies(output, true, true, true);
	
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
