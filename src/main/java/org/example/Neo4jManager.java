package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jManager {


    private Driver driver;
    private String url;
    private String username;
    private String password;



    public Neo4jManager(){
        this.url="bolt://localhost:7687";
        this.username="neo4j";
        this.password="test1234";
        this.createDriver();
    }

    public Neo4jManager(String url, String username, String password){
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

    public boolean createNode(String nodeName, Property[] properties) {
        // Connection to the Neo4j database
        this.createDriver();

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
        // Convert list to array
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
        // Convert list to array and return
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
        // Return the list of nodes
        return nodes;
    }


    public boolean addEdge(Edge edge) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Check if the start and end nodes exist
            if (edge.getStartNode() != null && edge.getEndNode() != null) {
                // Execute a query to create the relationship between the nodes with properties
                String query = "MATCH (start), (end) WHERE id(start) = $startNodeId AND id(end) = $endNodeId " +
                        "CREATE (start)-[r:" + edge.getRelationshipType() + "]->(end) SET r += $properties";
                Value parameters = Values.parameters(
                        "startNodeId", edge.getStartNode().id(),
                        "endNodeId", edge.getEndNode().id(),
                        "properties", edge.getPropertiesMap()
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


    public boolean deleteEdgeById(long edgeId) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to delete the edge by its ID
            String query = "MATCH ()-[r]->() WHERE id(r) = $edgeId DELETE r";
            Value parameters = Values.parameters("edgeId", edgeId);

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

    public Edge getEdgeById(long edgeId) {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve the edge by its ID
            String query = "MATCH ()-[r]->() WHERE id(r) = $edgeId RETURN r";
            Value parameters = Values.parameters("edgeId", edgeId);

            Result result = session.run(query, parameters);

            // Check if any relationships were found
            if (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();

                return this.createEdgeFromRelationship(relationship);
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

    public List <Edge> getEdgesByStartAndEndNodeId(int startNodeId, int endNodeId) {
        this.createDriver();
        List <Edge> edges = new ArrayList<>();
        try (Session session = driver.session()) {
            // Execute a query to retrieve the edge by start and end node IDs
            String query = "MATCH (start)-[r]->(end) WHERE id(start) = $startNodeId AND id(end) = $endNodeId RETURN r";
            Value parameters = Values.parameters("startNodeId", startNodeId, "endNodeId", endNodeId);

            Result result = session.run(query, parameters);

            // Check if any relationships were found
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                edges.add(createEdgeFromRelationship(relationship));
            }
            return edges;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            this.closeDriver();
        }
    }

    private Edge createEdgeFromRelationship(Relationship relationship) {
        return new Edge(relationship.id(), this.getNodeById(relationship.startNodeId()),
                this.getNodeById(relationship.endNodeId()),
                relationship.type(),
                relationship.asMap());
    }

    public void displayAllEdges() {
        this.createDriver();
        try (Session session = driver.session()) {
            // Execute a query to retrieve all relationships
            String query = "MATCH ()-[r]->() RETURN r";
            Result result = session.run(query);

            // Process the result and display relationships
            while (result.hasNext()) {
                Record record = result.next();
                Relationship relationship = record.get("r").asRelationship();
                System.out.println("Relationship ID: " + relationship.id() + ", Type: " + relationship.type() +
                        ", Start Node ID: " + relationship.startNodeId() + ", End Node ID: " + relationship.endNodeId() +
                        ", Properties: " + relationship.asMap());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDriver();
        }
    }

    public void displayEdgeById(int edgeId) {
        Edge edge = this.getEdgeById(edgeId);
        if (edge != null) {
            System.out.println("Edge ID: " + edge.getEdgeId() + ", Type: " + edge.getRelationshipType() +
                    ", Start Node ID: " + edge.getStartNode().id() + ", End Node ID: " + edge.getEndNode().id() +
                    ", Properties: " + edge.getPropertiesMap());
        } else {
            System.out.println("Edge is null.");
        }
    }

    public void displayEdges(List<Edge>  edges) {
        for (Edge edge: edges)
            this.displayEdge(edge);
    }


    public void displayEdge(Edge edge) {
        if (edge != null) {
            System.out.println("Edge ID: " + edge.getEdgeId() + ", Type: " + edge.getRelationshipType() +
                    ", Start Node ID: " + edge.getStartNode().id() + ", End Node ID: " + edge.getEndNode().id() +
                    ", Properties: " + edge.getPropertiesMap());
        } else {
            System.out.println("Edge is null.");
        }
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
