package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jDBManager {

    private Driver driver;
    private String url;
    private String username;
    private String password;

    public Neo4jDBManager(){
        this.url="bolt://localhost:7687";
        this.username="neo4j";
        this.password="test1234";
        this.createDriver();
    }

    public Neo4jDBManager(String url, String username, String password){
        this.url=url;
        this.username=username;
        this.password=password;
        this.createDriver();
    }



    public Driver getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void createDriver(){
        driver = GraphDatabase.driver(this.getUrl(), AuthTokens.basic(this.getUsername(), this.getPassword()));
        driver.verifyConnectivity();
    }

    public void closeDriver(){
        getDriver().close();
    }

    public boolean createNode(String nodeLabel, Property[] properties) {
        // Connection to the Neo4j database
        this.createDriver();

        // preparing and add node
        try (Session session = driver.session()) {
            // Create a query to add a node
            StringBuilder query = new StringBuilder("CREATE (p:" + nodeLabel + " {");
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
            this.closeDriver();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public boolean deleteNode(Node node){
        try {
            return this.deleteNodeById(node.id());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNodeById(long nodeId) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Retrieve the node by its ID
            Node node = this.getNodeById(nodeId);
            if (node != null) {
                // Execute a query to delete the node
                String query = "MATCH (n) WHERE id(n) = $nodeId DELETE n";
                Value parameters = Values.parameters("nodeId", nodeId);

                Result result = session.run(query, parameters);

                // Check if the node was deleted
                return result.consume().counters().nodesDeleted() > 0;
            } else {
                // Node not found
                System.out.println("Node with ID " + nodeId + " not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean updatePropertyInNode(long nodeId, String propertyName, String newPropertyVal) {
        try{
            return this.updatePropertyInNode(nodeId, new Property(propertyName, newPropertyVal));
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePropertyInNode(long nodeId, Property newProperty) {
        try{
            return this.addPropertyToNode(nodeId, newProperty);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPropertyToNode(long nodeId, Property newProperty) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Check if the node exists
            Node node = this.getNodeById(nodeId);
            if (node != null) {
                // Execute a query to add the new property to the node
                String query = "MATCH (n) WHERE id(n) = $nodeId SET n += $newProperties";
                Value parameters = Values.parameters(
                        "nodeId", nodeId,
                        "newProperties", Values.parameters(newProperty.getName(), newProperty.getVal())
                );

                Result result = session.run(query, parameters);

                // Check if the property was added
                return result.consume().counters().propertiesSet() > 0;
            } else {
                // Node not found
                System.out.println("Node with ID " + nodeId + " not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean deletePropertyFromNode(long nodeId, String propertyName) {
        try {
            return this.deletePropertyFromNode(nodeId,new Property(propertyName,null));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
}
        public boolean deletePropertyFromNode(long nodeId, Property property) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Check if the node exists
            Node node = this.getNodeById(nodeId);
            if (node != null) {
                // Execute a query to remove the property from the node
                String query = "MATCH (n) WHERE id(n) = $nodeId REMOVE n." + property.getName();
                Value parameters = Values.parameters(
                        "nodeId", nodeId,
                        "propertyName", property.getName()
                );

                session.run(query, parameters);

                // Check if the property was deleted
                return true;
            } else {
                // Node not found
                System.out.println("Node with ID " + nodeId + " not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve nodes
            String query = "MATCH (n) RETURN n";
            Result result = session.run(query);

            // Process the results and add nodes to the list
            while (result.hasNext()) {
                Record record = result.next();
                // Assuming 'n' is a node, you can get its properties and add it to the list
                Node node = record.get("n").asNode();
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDriver();
        }
        return nodes;
    }

    public Node getNodeById(long nodeId) {
        Node node = null;
        // Connection to the Neo4j database
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch the node by ID
            String query = "MATCH (n) WHERE id(n) = $nodeId RETURN n";
            Value parameters = Values.parameters("nodeId", nodeId);

            Result result = session.run(query, parameters);

            // Process the result and get the node
            if (result.hasNext()) {
                Record record = result.next();
                node = record.get("n").asNode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDriver();
        }
        return node;
    }
    public  List<Node> getNodesByProperty(Property property) {
        try {
            return this.getNodesByProperty(property.getName(), property.getVal());
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public  List<Node> getNodesByProperty(String propertyName, Object propertyValue) {
        List<Node> nodes = new ArrayList<>();
        // Connection to the Neo4j database
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch nodes by property name and value
            String query = "MATCH (n) WHERE n." + propertyName + " = $propertyValue RETURN n";
            Value parameters = Values.parameters("propertyValue", propertyValue);

            Result result = session.run(query, parameters);

            // Process the result and add nodes to the list
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDriver();
        }
        return nodes;
    }

    public List<Node> getNodesByValue(Object propertyValue) {
        List<Node> nodes = new ArrayList<>();
        // Connection to the Neo4j database
        createDriver();
        try (Session session = driver.session()) {
            // Execute a query to fetch nodes by property value
            String query = "MATCH (n) WHERE any(prop in keys(n) WHERE n[prop] = $value) RETURN n LIMIT 1";
            Value parameters = Values.parameters("value", propertyValue);


            Result result = session.run(query, parameters);

            // Process the result and add nodes to the list
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("n").asNode();
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDriver();
        }
        return nodes;
    }

    public void displayAllNodes() {
        for (Node node: this.getAllNodes())
            this.displayNode(node);
    }

    public void displayNodes(List<Node> nodes) {
        for (Node node: nodes)
            this.displayNode(node);
    }

    public void displayNodeById(int nodeId) {
        this.displayNode(this.getNodeById(nodeId));
    }

    public void displayNode(Node node) {
        if (node != null) {
            String label = node.labels().iterator().next();
            System.out.println("ID: " + node.id() + ",  Name: " + label + ", Properties: " + node.asMap());
        } else {
            System.out.println("Node is null.");
        }
    }

}
