import java.io.File;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.SimpleRenderer;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import eu.trowl.owlapi3.rel.reasoner.el.RELReasoner;

public class EntailmentChecking {

    public static void main(String[] args) throws Exception {
        // First, we create an OWLOntologyManager object. The manager will load and 
        // save ontologies. 
        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory=manager.getOWLDataFactory();
        // Now, we create the file from which the ontology will be loaded. 
        // Here the ontology is stored in a file locally in the ontologies subfolder
        // of the examples folder.
        
//        File inputOntologyFile = new File("/home/ajkamal/Desktop/Import/pizza.owl");
//        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
//        OWLClass margherita=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita"));
//        OWLObjectProperty hasTopping=dataFactory.getOWLObjectProperty(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping"));
//        OWLClass mozzarellaTopping=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MozzarellaTopping"));
//        OWLClass goatsCheeseTopping=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#GoatsCheeseTopping"));
//        OWLClassExpression mozarellaOrGoatsCheese=dataFactory.getOWLObjectUnionOf(mozzarellaTopping, goatsCheeseTopping);
//        OWLClassExpression four=dataFactory.getOWLObjectSomeValuesFrom(hasTopping, mozarellaOrGoatsCheese);
//        OWLAxiom five=dataFactory.getOWLSubClassOfAxiom(margherita, four);
        
        File inputOntologyFile = new File("/home/ajkamal/Desktop/Import/wine.rdf");
        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        OWLClass Wine=dataFactory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
        OWLClass White=dataFactory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#White"));
        OWLClass OffDry=dataFactory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#OffDry"));
        OWLClass Dry=dataFactory.getOWLClass(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Dry"));
       
        OWLClassExpression offanddry=dataFactory.getOWLObjectUnionOf(OffDry,Dry);
        OWLObjectProperty hasSugar=dataFactory.getOWLObjectProperty(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasSugar"));
        OWLObjectProperty hasColor=dataFactory.getOWLObjectProperty(IRI.create("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasColor"));
        OWLClassExpression one=dataFactory.getOWLObjectAllValuesFrom(hasSugar,offanddry);
        OWLClassExpression two=dataFactory.getOWLObjectSomeValuesFrom(hasColor,White);
        OWLClassExpression four=dataFactory.getOWLObjectIntersectionOf(one,two);
        OWLAxiom five=dataFactory.getOWLSubClassOfAxiom(Wine,four);

        RELReasonerFactory relfactory = new RELReasonerFactory();
		RELReasoner reasoner = relfactory.createReasoner(ontology);
        // Let us check whether the axiom is entailed:
//        System.out.println("Do margherita pizzas have a topping that is morzarella or goats cheese? "+reasoner.isEntailed(five)); 

        System.out.println("Is there any non sweet white wine?  "+reasoner.isEntailed(five)); 
        
        // Let us now also see what other (named) subclasses the complex superclass has
        // Setting the boolean flag to false means we are not only interested in direct subclasses
        // but also indirect ones
        // For printing the classes we want to make use of the abbreviations defined in the
        // ontology. We can set a suitable renderer in the OWL API that will then abbreviate 
        // long IRIs for which there as a prefix declared in the ontology.
        SimpleRenderer renderer=new SimpleRenderer();
        renderer.setPrefixesFromOntologyFormat(ontology, manager, true);
        ToStringRenderer.getInstance().setRenderer(renderer);
        
        NodeSet<OWLClass> subs=reasoner.getSubClasses(four, false);
        System.out.println("Subclasses of the complex class: ");
        for (Node<OWLClass> equivalents : subs.getNodes()) {
            // The node set contains several sets of classes
            // Each set contains classes that are equivalent to each other 
            // (if there are any otherwise it is a singleton set)
            // here only owl:Nothing, which is a subclass of every class
            // has some eqivalents (other unsatisfiable classes) 
            for (OWLClass equivalent : equivalents) {
                System.out.print(equivalent+" ");
            }
            System.out.println();
        }
    }
}