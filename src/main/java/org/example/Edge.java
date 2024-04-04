package org.example;


import org.neo4j.driver.types.Node;

import java.util.HashMap;
import java.util.Map;

public class Edge {
    private Node startNode;
    private Node endNode;
    private String relationshipType;

    private Property []properties;

    private Map<String, Object> propertiesMap;


    public Edge(Node startNode, Node endNode, String relationshipType) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.relationshipType = relationshipType;
    }

    public Edge(Node startNode, Node endNode, String relationshipType, Property[] properties) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.relationshipType = relationshipType;
        this.properties = properties;
        this.updatePropertiesMap();
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public Property[] getProperties() {
        return properties;
    }

    public Map<String, Object> getPropertiesMap() {
        return propertiesMap;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public void setPropertiesMap(Map<String, Object> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    public void updatePropertiesMap() {
        propertiesMap = new HashMap<>();
        for (Property p: this.getProperties())
            propertiesMap.put(p.getName(), p.getVal());
    }


}
