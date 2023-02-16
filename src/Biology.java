import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import java.util.concurrent.TimeUnit;

public class Biology {

    static final String bioFileName = "biology.ttl";
    static final String biolinkOntologyFileName = "biolink-model.owl.ttl";
    static final String biolink_NS = "https://w3id.org/biolink/vocab/";

    public static String getDuration(long start, long end){
        long duration = end - start ;
        long seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static void main(String[] args) {
        // create an empty model
        Model model = RDFDataMgr.loadModel(bioFileName) ;

        System.out.println( "--- In raw input biology data ---" );
        // select all the statements with a has_catalyst property
        Property hasCatalyst = model.getProperty(biolink_NS + "has_catalyst");
        System.out.println( "has_catalyst property" );
        StmtIterator stmtHasCatalyst = model.listStatements(null, hasCatalyst, (RDFNode) null);
        while (stmtHasCatalyst.hasNext()) {
            System.out.println(stmtHasCatalyst.next());
        }
        // select all the statements with a catalyzes property
        Property catalyzes = model.getProperty(biolink_NS + "catalyzes");
        System.out.println( "catalyzes property" );
        StmtIterator stmtCatalyzes = model.listStatements(null, catalyzes, (RDFNode) null);
        while (stmtCatalyzes.hasNext()) {
            System.out.println(stmtCatalyzes.next());
        }

        // SPARQL query
        String queryString = "SELECT DISTINCT * WHERE {?s <" + biolink_NS + "has_catalyst> ?o .} ORDER BY ?s ?o" ;
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
        Model schema = RDFDataMgr.loadModel(biolinkOntologyFileName) ;
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner() ;
        System.out.println( "\nBinding ontology schema" ) ;
        long startTime1 = System.nanoTime();
        reasoner = reasoner.bindSchema(schema) ;
        long endTime1 = System.nanoTime();
        String duration1 = getDuration(startTime1, endTime1);
        System.out.println("    duration: " + duration1);

        // creates an inference model using the rules of reasoner over the raw model
        System.out.println( "Creating inference model" ) ;
        long startTime2 = System.nanoTime();
        InfModel infmodel = ModelFactory.createInfModel(reasoner, model) ;
        long endTime2   = System.nanoTime();
        String duration2 = getDuration(startTime2, endTime2);
        System.out.println("    duration: " + duration2);

        // SPARQL execution
        long startTime3 = System.nanoTime();
        System.out.println( "--- Results of SPARQL on inferred model ---" ) ;
        try (QueryExecution qExc = QueryExecutionFactory.create(query, infmodel)) {
            ResultSet results = qExc.execSelect() ;
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution() ;
                System.out.println(sol) ;
            }
        }
        long endTime3   = System.nanoTime();
        String duration3 = getDuration(startTime2, endTime2);
        System.out.println("    duration: " + duration3);
    }
}
