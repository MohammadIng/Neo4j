package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.util.HashMap;
import java.util.Map;


public class Neo4jExample {
    public static Driver driver = null;

    public static String url="bolt://localhost:7687";
    public static String username="neo4j";
    public static String password="test1234";

    public static void createDriver(){
        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
        driver.verifyConnectivity();
    }

    public static void closeDriver(){
        driver.close();
    }

    public static void displaySingleNode() {
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve a single node
            String query = "MATCH (n) RETURN n LIMIT 1";
            Result result = session.run(query);

            // Process the result and display the node
            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                System.out.println("Node ID: " + node.id() + ", Properties: " + node.asMap());
            } else {
                System.out.println("No nodes found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDriver();
    }

    public static void displaySingleNodeById(long nodeId) {
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch a single node by its ID
            String query = "MATCH (n) WHERE ID(n) = $nodeId RETURN n LIMIT 1";
            Value parameters = Values.parameters("nodeId", nodeId);

            Result result = session.run(query, parameters);

            // Process the result and display the node
            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                System.out.println("Node ID: " + node.id() + ", Properties: " + node.asMap());
            } else {
                System.out.println("No node found with ID: " + nodeId);
            }
        }
        closeDriver();
    }

    public static void displaySingleNodeByProperty(String propertyName, Object propertyValue) {
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch a single node by property name and value
            String query = "MATCH (n) WHERE n." + propertyName + " = $propertyValue RETURN n LIMIT 1";
            Value parameters = Values.parameters("propertyValue", propertyValue);

            Result result = session.run(query, parameters);

            // Process the result and display the node
            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                System.out.println("Node ID: " + node.id() + ", Properties: " + node.asMap());
            } else {
                System.out.println("No node found with property '" + propertyName + "' and value '" + propertyValue + "'");
            }
        }
        closeDriver();

    }

    public static void displaySingleNodeByValue(Object value) {
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch a single node by property value
            String query = "MATCH (n) WHERE any(prop in keys(n) WHERE n[prop] = $value) RETURN n LIMIT 1";
            Value parameters = Values.parameters("value", value);

            Result result = session.run(query, parameters);

            // Process the result and display the node
            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                System.out.println("Node ID: " + node.id() + ", Properties: " + node.asMap());
            } else {
                System.out.println("No node found with value '" + value + "' in any property.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDriver();
        }
    }



    public static void addNode(String nodeName, Property[] properties) {
        // Connection to the Neo4j database
        createDriver();

        // preparing and add node
        try (Session session = driver.session()) {
            // Create a query to add a node
            StringBuilder query = new StringBuilder("CREATE (p:" + nodeName + " {");
            for (Property p : properties) {
                query.append(p.getName()).append(": $").append(p.getName()).append(" ,");
            }
            query.deleteCharAt(query.length() - 1);
            query.append("})");

            // Create parameters to add a node
            Map<String, Object> values = new HashMap<>();
            for (Property p : properties) {
                values.put(p.getName(), p.getVal());
            }
            Object[] params = new Object[values.size() * 2];
            int i = 0;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                params[i++] = entry.getKey();
                params[i++] = entry.getValue();
            }
            Value parameters = Values.parameters(params);

            // add node
            session.run(query.toString(), parameters);

            System.out.println("Node added successfully.");
        }
            closeDriver();
    }

    public static void displayNodes() {
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve nodes
            String query = "MATCH (n) RETURN n";
            Result result = session.run(query);

            // Process the results and display nodes
            while (result.hasNext()) {
                Record record = result.next();
                // Assuming 'n' is a node, you can get its properties and display them
                Node node = record.get("n").asNode();
                System.out.println("Node ID: " + node.id() + ", Properties: " + node.asMap());
            }
        }
        closeDriver();
    }

    public static void main(String[] args) {
//        Property[] properties = {
//                new Property("name", "qw"),
//                new Property("age", 43)
//        };
//        addNode("Person", properties);
//        displayNodes();
//        displaySingleNodeById(1);

//        displaySingleNodeByProperty("name", "qw");
        displaySingleNodeByValue(43);

    }


}
