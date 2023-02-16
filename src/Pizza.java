import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.*;
import org.apache.jena.riot.RDFDataMgr;

public class Pizza {

    static final String pizzaFileName = "pizza.ttl";
    static final String pizzaOntologyFileName = "pizza_ontology.rdf";
    static final String pizza_NS = "http://example.com/pizzas.owl#";


    public static void main(String[] args) {
        // create an empty model
        Model model = RDFDataMgr.loadModel(pizzaFileName) ;

        System.out.println( "--- In raw input pizza data ---" );
        // select all the statements with a hasIngredient property
        Property hasIng = model.getProperty(pizza_NS + "hasIngredient");
        System.out.println( "hasIngredient property" );
        StmtIterator stmtHasIng = model.listStatements(null, hasIng, (RDFNode) null);
        while (stmtHasIng.hasNext()) {
            System.out.println(stmtHasIng.next());
        }
        // select all the statements with a isIngredientOf property
        Property ingOf = model.getProperty(pizza_NS + "isIngredientOf");
        System.out.println( "isIngredientOf property" );
        StmtIterator stmtIngOf = model.listStatements(null, ingOf, (RDFNode) null);
        while (stmtIngOf.hasNext()) {
            System.out.println(stmtIngOf.next());
        }

        // SPARQL query
        String queryString = "SELECT DISTINCT * WHERE {?s <" + pizza_NS + "hasIngredient> ?o .} ORDER BY ?s ?o" ;
        Query query = QueryFactory.create(queryString) ;
        // execution
        System.out.println( "\n--- Results of SPARQL on raw model ---" ) ;
        try (QueryExecution qExc = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qExc.execSelect() ;
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution() ;
                System.out.println(sol) ;
            }
        }

        // OWL inference
        Model schema = RDFDataMgr.loadModel(pizzaOntologyFileName) ;
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner() ;
        System.out.println( "\nBinding ontology schema" ) ;
        System.out.println("    reasoner: " + reasoner);
        reasoner = reasoner.bindSchema(schema) ;
        // creates an inference model using the rules of reasoner over the raw model
        InfModel infmodel = ModelFactory.createInfModel(reasoner, model) ;
        // SPARQL execution
        System.out.println( "--- Results of SPARQL on inferred model ---" ) ;
        try (QueryExecution qExc = QueryExecutionFactory.create(query, infmodel)) {
            ResultSet results = qExc.execSelect() ;
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution() ;
                System.out.println(sol) ;
            }
        }
    }
}