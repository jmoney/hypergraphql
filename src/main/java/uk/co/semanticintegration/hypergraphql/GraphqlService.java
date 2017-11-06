package uk.co.semanticintegration.hypergraphql;

import graphql.*;
import graphql.language.SourceLocation;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by szymon on 01/11/2017.
 */
public class GraphqlService {
    private GraphQL graphQL;
    private Config config;
    static Logger logger = Logger.getLogger(GraphqlService.class);



    public GraphqlService(Config config, GraphQL graphQL) {
        this.graphQL = graphQL;
        this.config = config;
    }

    public Map<String, Object> results(String query) {

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        Map<Object, Object> extensions = new HashMap<>();
        List<GraphQLError> errors = new ArrayList<>();

        ExecutionInput executionInput;
        ExecutionResult qlResult;

        List<String> sparqlQueries;

        if (!query.contains("IntrospectionQuery") && !query.contains("__")) {

            Converter converter = new Converter(config);

            sparqlQueries = converter.graphql2sparql(query);

            SparqlClient client = new SparqlClient(sparqlQueries, config.sparqlEndpointsContext());

            executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .context(client)
                    .build();

            qlResult = graphQL.execute(executionInput);
            try {
                data = converter.jsonLDdata(query, qlResult.getData());
            } catch (IOException e) {
                logger.error(e);
            }

        } else {
            qlResult = graphQL.execute(query);
            data = qlResult.getData();
        }

        errors.addAll(qlResult.getErrors());

        result.put("data", data);
        result.put("errors", errors);
        result.put("extensions", extensions);

        return result;
    }
}