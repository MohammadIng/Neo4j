package org.example;


import org.neo4j.driver.types.Node;

import java.util.Properties;

public class Main {

    public  static Neo4jDBManager manager = new Neo4jDBManager();

    public static void deleteAll(int l){
        for (int i=0;i<l; i++){
            manager.deleteNodeById(i);
            manager.deleteRelationshipById(i);
        }
    }

    public static void main(String[] args) {

//        deleteAll(20);

        Property []properties2 = new Property[]{
                                                new Property("name","rostock"),
                                                new Property("bl","mv")
                                                };
        Property []properties1 = new Property[]{
                new Property("name","judy"),
                new Property("age",29),

        };







//        manager.insertNode("person",properties1);
//        manager.insertNode("stadt",properties2);

//        manager.insertRelationship(0,1, "t1",propertiest);
//
//        manager.insertRelationship(0,1, "t1",propertiest);
//        manager.insertNodeWithOutDuplicate("person", properties1);
//        manager.insertRelationshipWithoutDuplicate(4,5, "besucht",propertiest);

//        manager.insertRelationshipWithoutDuplicate(0,1, "stduiret_in",propertiest);

        Property []propertiesR = new Property[]{
                new Property("seit",2000),
                new Property("with","freind"),
        };
        Property []propertiesN = new Property[]{
                new Property("x","y"),
                new Property("c","d"),
        };

//            manager.deleteRelationshipById(4);
//        int id = manager.insertNode("person", propertiesN);
//        manager.insertRelationship(17, 2,"arbeitet", propertiesR);

        manager.mergeRelationships(4,2);



        manager.displayAllNodes();
        manager.displayAllRelationships();


    }
}