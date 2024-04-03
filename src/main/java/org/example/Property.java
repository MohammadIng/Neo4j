package org.example;


public class Property {
    private String name;
    private Object val;

    Property(String name, Object val){
        this.name = name;
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public Object getVal() {
        return val;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVal(Object val) {
        this.val = val;
    }
}
