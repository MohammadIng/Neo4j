package org.example;


import org.neo4j.driver.types.Node;

public class Edge {
    Node startNode;
    Node endNode;
    String relationshipType;

    public Edge(Node startNode, Node endNode, String relationshipType) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.relationshipType = relationshipType;
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

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
