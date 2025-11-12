# 🔗 Java Neo4j Graph Database Manager

## 📘 Overview
This project provides a **Java-based framework for managing and interacting with Neo4j graph databases**.  
It demonstrates how to model graph entities (nodes, relationships, and properties), execute Cypher queries, and manage database connections through a structured object-oriented architecture.

The project enables developers to build, query, and visualize graph structures efficiently — combining Java’s robustness with the flexibility of the Neo4j graph model.

---

## ⚙️ Technology Stack
| Component          | Description                                     |
|--------------------|-------------------------------------------------|
| **Language**       | Java 17+                                        |
| **Database**       | Neo4j (Community or Enterprise Edition)         |
| **Driver**         | Official Neo4j Java Driver (`org.neo4j.driver`) |
| **Query Language** | Cypher                                          |
| **Execution**      | Command-line Java application                   |

---

## 🧱 System Architecture

```
+-------------------------------------------------------------------+
|                   Java – Neo4j Graph System                       |
+-------------------------------------------------------------------+
|  Main.java             → entry point of the application           |
|  Neo4jManager.java     → handles Neo4j driver connection          |
|  Neo4jDBManager.java   → manages transactions & queries           |
|  Neo4jExample.java     → builds and demonstrates example graphs   |
|  Edge.java             → represents relationships (edges)         |
|  Property.java         → stores node properties (key-value pairs) |
+-------------------------------------------------------------------+
```

---

## 🧩 Class Overview

| Class                   | Description                                                                                 |
|-------------------------|---------------------------------------------------------------------------------------------|
| **Main.java**           | Entry point; initializes database connection and runs example operations.                   |
| **Neo4jManager.java**   | Responsible for creating, maintaining, and closing Neo4j driver sessions.                   |
| **Neo4jDBManager.java** | Provides methods for CRUD operations, Cypher execution, and transaction control.            |
| **Neo4jExample.java**   | Contains predefined examples demonstrating how to create nodes, relationships, and queries. |
| **Edge.java**           | Represents relationships between nodes, including type and direction.                       |
| **Property.java**       | Defines key-value pairs for nodes or relationships (metadata).                              |

---

## 🧠 Workflow

1. **Initialization** – `Neo4jManager` establishes a connection with the Neo4j database using the driver URI, username, and password.  
2. **Graph Creation** – `Neo4jExample` creates nodes and relationships using Cypher queries.  
3. **Data Manipulation** – `Neo4jDBManager` executes CRUD operations and commits transactions.  
4. **Visualization / Debug** – results can be inspected directly in the Neo4j Browser or through returned query results.  
5. **Termination** – sessions and driver connections are safely closed to release resources.  

---

## 🧰 Installation & Usage

### 1️⃣ Prerequisites
- Java JDK 17 or newer installed.  
- Neo4j Desktop or Neo4j Server running locally.  
- The official Neo4j Java Driver added to your project dependencies.

Example Maven dependency:
```xml
<dependency>
  <groupId>org.neo4j.driver</groupId>
  <artifactId>neo4j-java-driver</artifactId>
  <version>5.16.0</version>
</dependency>
```

---

### 2️⃣ Configure Database Connection
In `Neo4jManager.java`, adjust the connection URI and credentials:
```java
private static final String URI = "bolt://localhost:7687";
private static final String USER = "neo4j";
private static final String PASSWORD = "your_password";
```

---

### 3️⃣ Compile the Project
```bash
javac -cp "path/to/neo4j-java-driver.jar" *.java
```

---

### 4️⃣ Run the Program
```bash
java -cp ".:path/to/neo4j-java-driver.jar" Main
```

---

### 5️⃣ Example Output
```
Connected to Neo4j successfully.
Creating nodes: Person {name: 'Alice'}, Person {name: 'Bob'}
Creating relationship: (Alice)-[:KNOWS]->(Bob)
Query result: MATCH (n) RETURN n
Closing connection...
```

---

## 🧪 Example Code Snippet

```java
try (Session session = driver.session()) {
    session.run("CREATE (a:Person {name:'Alice'})");
    session.run("CREATE (b:Person {name:'Bob'})");
    session.run("MATCH (a:Person {name:'Alice'}), (b:Person {name:'Bob'}) " +
                "CREATE (a)-[:KNOWS]->(b)");
}
```

---

## 📊 Use Cases
- Educational demonstrations of Neo4j and graph modeling.  
- Rapid prototyping of graph-based data structures.  
- Testing of Cypher queries directly from Java.  
- Integration of Neo4j into Java enterprise applications.

---

## 📚 Citation
> Mohammad Matar, *Neo4j Graph Database Manager in Java*,  
> University of Rostock, 2025.
