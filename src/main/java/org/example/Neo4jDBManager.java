package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

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

    private void createDriver() {
        driver = GraphDatabase.driver(this.getUrl(), AuthTokens.basic(this.getUsername(), this.getPassword()));
        driver.verifyConnectivity();
    }

    public void closeDriver() {
        getDriver().close();
    }

    public int createNode(String nodeLabel, Property[] properties) {
        // Connection to the Neo4j database
        this.createDriver();
        int nodeId = -1;

        // preparing and add node
        try (Session session = driver.session()) {
            // Create a query to add a node
            StringBuilder query = new StringBuilder("CREATE (p:" + nodeLabel + " {");
            for (Property p : properties) {
                query.append(p.getName()).append(": $").append(p.getName()).append(" ,");
            }
            query.deleteCharAt(query.length() - 1);
            query.append("}) RETURN id(p) as nodeId");

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
            Result result = session.run(query.toString(), parameters);
            Record record = result.next();
            System.out.println(result);
            nodeId = (int)record.get("nodeId").asLong();
            System.out.println("Node added successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDriver();
        }
        return nodeId;
    }

    public boolean createNodeWithMultiLabels(String []nodeLabels, Property[] properties) {
        try {
            int nodeId = this.createNode(nodeLabels[0], properties);
            this.addLabelsToNode(nodeId, nodeLabels);
            System.out.println(nodeId);
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public boolean deleteNode(Node node) {
        try {
            return this.deleteNodeById(node.id());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNodeById(long nodeId) {
        this.deleteRelationships(this.getRelationshipsByStartOrEndNodeId(nodeId,nodeId));
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
        try {
            return this.updatePropertyInNode(nodeId, new Property(propertyName, newPropertyVal));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePropertyInNode(long nodeId, Property newProperty) {
        try {
            return this.addPropertyToNode(nodeId, newProperty);
        } catch (Exception e) {
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
            return this.deletePropertyFromNode(nodeId, new Property(propertyName, null));
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

    public boolean addLabelsToNode(long nodeId, String []labels) {
        try {
            for(String label: labels)
                this.addLabelToNode(nodeId, label);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean addLabelToNode(long nodeId, String label) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to add a label to the node
            String query = "MATCH (n) WHERE id(n) = $nodeId SET n:" + label;
            Value parameters = Values.parameters("nodeId", nodeId);

            Result result = session.run(query, parameters);

            // Check if any nodes were updated
            return result.consume().counters().labelsAdded() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean deleteLabelsFromNode(long nodeId, String []labels) {
        try {
            for(String label: labels)
                this.deleteLabelFromNode(nodeId, label);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteLabelFromNode(long nodeId, String label) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to remove the label from the node
            String query = "MATCH (n) WHERE id(n) = $nodeId REMOVE n:" + label;
            Value parameters = Values.parameters("nodeId", nodeId);

            Result result = session.run(query, parameters);

            // Check if any nodes were updated
            return result.consume().counters().labelsRemoved() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public List<String> getAllLabels() {
        List<String> labels = new ArrayList<>();
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve all distinct labels
            String query = "CALL db.labels()";
            Result result = session.run(query);

            // Process the result and add labels to the list
            while (result.hasNext()) {
                Record record = result.next();
                String label = record.get(0).asString();
                labels.add(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDriver();
        }
        return labels;
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

    public List<Node> getNodesByLabel(String label) {
        List<Node> nodes = new ArrayList<>();
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve nodes by label
            String query = "MATCH (n:`" + label + "`) RETURN n";
            Result result = session.run(query);

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
        // Return the list of nodes
        return nodes;
    }


    public List<Node> getNodesByProperty(Property property) {
        try {
            return this.getNodesByProperty(property.getName(), property.getVal());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Node> getNodesByProperty(String propertyName, Object propertyValue) {
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

    public List<Node> getNodesByPropertyValue(Object propertyValue) {
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
        System.out.println("All Nodes:");
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
            Iterable<String> label = node.labels();
            System.out.println("Node ID: " + node.id() + ",  Label: " + label + ", Properties: " + node.asMap());
        } else {
            System.out.println("Node is null.");
        }
    }
    public boolean createRelationship(int startNodeId, int endNodeId, String relationshipType, Property[] properties) {
        this.createDriver();
        Map<String, Object> propertiesMap = new HashMap<>();
        for (Property p : properties)
            propertiesMap.put(p.getName(), p.getVal());
        try (Session session = driver.session()) {
            Node startNode = this.getNodeById(startNodeId);
            Node endNode = this.getNodeById(endNodeId);
            // Check if the start and end nodes exist
            if (startNode != null && endNode != null) {
                // Execute a query to create the relationship between the nodes with properties
                String query = "MATCH (start), (end) WHERE id(start) = $startNodeId AND id(end) = $endNodeId " +
                        "CREATE (start)-[r:`" + relationshipType + "`]->(end) SET r += $properties";
                Value parameters = Values.parameters(
                        "startNodeId", startNodeId,
                        "endNodeId", endNodeId,
                        "properties", propertiesMap
                );
                Result result = session.run(query, parameters);
                // Check if the relationship was created
                return result.consume().counters().relationshipsCreated() > 0;
            } else {
                // Nodes not found
                System.out.println("Start or end node not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }


    public boolean deleteRelationships(List<Relationship>  relationships) {
        try {
            for(Relationship relationship: relationships)
                this.deleteRelationship(relationship);
            return true;
        }catch (Exception e){
            return false;
        }
    }

        public boolean deleteRelationshipById(long relationshipId) {
        try {
            return this.deleteRelationship(this.getRelationshipById(relationshipId));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }
        public boolean deleteRelationship(Relationship relationship) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to delete the edge by its ID
            String query = "MATCH ()-[r]->() WHERE id(r) = $relationshipId DELETE r";
            Value parameters = Values.parameters("relationshipId", relationship.id());

            Result result = session.run(query, parameters);

            // Check if any relationships were deleted
            return result.consume().counters().relationshipsDeleted() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean updatePropertyInRelationship(long relationshipId, Property newProperty) {
        try{
           return this.addPropertyInRelationship(relationshipId, newProperty);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean addPropertyInRelationship(long relationshipId, Property newProperty) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to update the property in the relationship
            String query = "MATCH ()-[r]->() WHERE id(r) = $relationshipId SET r." + newProperty.getName() + " = $propertyValue";
            Value parameters = Values.parameters(
                    "relationshipId", relationshipId,
                    "propertyValue", newProperty.getVal()
            );

            Result result = session.run(query, parameters);

            // Check if any relationships were updated
            return result.consume().counters().propertiesSet() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean addPropertyInRelationship(long relationshipId, String propertyName, String propertyVal) {
        try {
            return this.addPropertyInRelationship(relationshipId, new Property(propertyName, propertyVal));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean updatePropertyInRelationship(long relationshipId, String propertyName, String propertyVal) {
        try {
            return this.addPropertyInRelationship(relationshipId, new Property(propertyName, propertyVal));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean deletePropertyFromRelationship(long relationshipId, String propertyName) {
        try {
            return this.addPropertyInRelationship(relationshipId, new Property(propertyName, null));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }

    public boolean deletePropertyFromRelationship(long relationshipId, Property property) {
        // Connection to the Neo4j database
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to delete the property from the relationship
            String query = "MATCH ()-[r]->() WHERE id(r) = $relationshipId REMOVE r." + property.getName();
            Value parameters = Values.parameters("relationshipId", relationshipId);

            Result result = session.run(query, parameters);

            // Check if any relationships were modified
            return result.consume().counters().relationshipsDeleted() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            this.closeDriver();
        }
    }



    public List<Relationship> getAllRelationships() {
        this.createDriver();
        List<Relationship> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            // Execute a query to retrieve all relationships
            String query = "MATCH ()-[r]->() RETURN r";
            Result result = session.run(query);

            // Process the result and display relationships
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                relationships.add(relationship);
            }
            return relationships;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }

    public List <Relationship> getRelationshipsByStartOrEndNodeId(long startNodeId, long endNodeId) {
        this.createDriver();
        List <Relationship> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            // Execute a query to retrieve the edge by start and end node IDs
            String query = "MATCH (start)-[r]->(end) WHERE id(start) = $startNodeId Or id(end) = $endNodeId RETURN r";
            Value parameters = Values.parameters("startNodeId", startNodeId, "endNodeId", endNodeId);

            Result result = session.run(query, parameters);

            // Check if any relationships were found
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                relationships.add(relationship);
            }
            return relationships;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }

    public List <Relationship> getRelationshipsByStartAndEndNodeId(long startNodeId, long endNodeId) {
        this.createDriver();
        List <Relationship> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            // Execute a query to retrieve the edge by start and end node IDs
            String query = "MATCH (start)-[r]->(end) WHERE id(start) = $startNodeId AND id(end) = $endNodeId RETURN r";
            Value parameters = Values.parameters("startNodeId", startNodeId, "endNodeId", endNodeId);

            Result result = session.run(query, parameters);

            // Check if any relationships were found
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                relationships.add(relationship);
            }
            return relationships;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }

    public Relationship getRelationshipById(long edgeId) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve the edge by its ID
            String query = "MATCH ()-[r]->() WHERE id(r) = $edgeId RETURN r";
            Value parameters = Values.parameters("edgeId", edgeId);

            Result result = session.run(query, parameters);

            // Check if any relationships were found
            if (result.hasNext()) {
                Record record = result.next();
                return record.get("r").asRelationship();
            } else {
                // No edge found with the specified ID
                System.out.println("No edge found with ID: " + edgeId);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }

    public List <Relationship> getRelationshipsByType(String type) {
        this.createDriver();
        List <Relationship> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            // Execute a query to retrieve relationships by type
            String query = "MATCH ()-[r:`" + type + "`]->() RETURN r";
            Result result = session.run(query);

            // Check if any relationships were found
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                relationships.add(relationship);
                return relationships;
            }
            return relationships;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }


    public void displayAllRelationships() {
        System.out.println("All Relationships:");
        for (Relationship relationship: this.getAllRelationships())
            this.displayRelationship(relationship);
    }

    public void displayRelationships(List<Relationship>  relationships) {
        for (Relationship relationship: relationships)
            this.displayRelationship(relationship);
    }

    public void displayRelationshipById(int edgeId) {
        Relationship relationship = this.getRelationshipById(edgeId);
        this.displayRelationship(relationship);
    }

    public void displayRelationship(Relationship relationship) {
        if (relationship != null) {
            System.out.println("Relationship ID: " + relationship.id() + ", Type: " + relationship.type() +
                    ", Start Node ID: " + relationship.startNodeId() + ", End Node ID: " + relationship.endNodeId() +
                    ", Properties: " + relationship.asMap());
        } else {
            System.out.println("Edge is null.");
        }
    }


}
