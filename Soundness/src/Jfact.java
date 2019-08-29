import java.io.File;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
 
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;


public class Jfact {
	public static void main(String[] args) throws OWLOntologyCreationException {

		File c=new File(args[0]);
		IRI physicalIRI = IRI.create(c);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(physicalIRI);	
		
		
		OWLReasonerFactory reasonerFactory = new JFactFactory();
		OWLReasoner reasoner2 = reasonerFactory.createReasoner(ontology);
		
		long startTime = System.nanoTime();
		reasoner2.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		long endTime = System.nanoTime();
        long duration = ((endTime - startTime));        
//        System.out.println(duration/1000000000);
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); 
		long memory = (runtime.totalMemory() - runtime.freeMemory());
        long tmemory=runtime.totalMemory();
//        System.out.println("Total memory is bytes: " + tmemory/(1024L * 1024L));
        
//        System.out.println("Used memory is megabytes: "+ (memory/(1024L * 1024L)));
        System.out.print(duration+" ");
        System.out.print(tmemory/(1024L * 1024L)+" ");
        System.out.print((memory/(1024L * 1024L))+" ");
        

	}
}

//reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//reasoner.getSubClasses(df.getOWLClass("http://www.co−ode.org/ontologies/pizza/pizza.owl#RealItalianPizza",true), false).forEach(System.out::println);;