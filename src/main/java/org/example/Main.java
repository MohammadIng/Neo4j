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

//        neo4jManager.displayNodeById(1);
//        Property p = new Property("p12",12);
//        neo4jManager.addPropertyToNode(1, p);
//        neo4jManager.displayNodeById(1);

//        Property p = new Property("p12",122);
//        neo4jManager.addPropertyToNode(1, p);
//        neo4jManager.displayNodeById(1);
//        p = new Property("p12",1224);
//        neo4jManager.updatePropertyInNode(1, p);
//        neo4jManager.displayNodeById(1);
//        neo4jManager.deletePropertyFromNode(1, p);
//        neo4jManager.displayNodeById(1);



        Property[] properties = {
            new Property("x", "1"),
            new Property("y", 2)
        };
        Edge edge = new Edge(neo4jManager.getNodeById(2), neo4jManager.getNodeById(5),"Test", properties);
        neo4jManager.addEdge2(edge);

//        neo4jManager.displayAllNodes();
        neo4jManager.displayAllEdges();






    }
}