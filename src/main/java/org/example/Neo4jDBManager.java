package org.example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

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

}
