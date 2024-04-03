package org.example;


import org.neo4j.driver.types.Node;

public class Main {




    public static void main(String[] args) {
        Neo4jManager neo4jManager = new Neo4jManager();
//        neo4jManager.displayAllNodes();
//        neo4jManager.displayNodeById(4);
//        neo4jManager.displayNodes(neo4jManager.getNodesByValue(30));

//        Property[] properties = {
//            new Property("x", "1"),
//            new Property("y", 2)
//        };
//        neo4jManager.addNode("MyNode", properties);

//        Node node = neo4jManager.getNodeById(3);
//        neo4jManager.deleteNode(node);

        neo4jManager.displayNodeById(1);
        Property p = new Property("p12",12);
        neo4jManager.addPropertyToNode(1, p);
        neo4jManager.displayNodeById(1);

        p = new Property("p12",122);
        neo4jManager.addPropertyToNode(1, p);
        neo4jManager.displayNodeById(1);




    }
}