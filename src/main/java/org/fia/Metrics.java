package org.fia;

import java.util.TreeMap;

public class Metrics {
    String name;
    private TreeMap<String,String> labels;
    String value;

    public Metrics(String n) {
        this.name = n;
        labels = new TreeMap<>();
    }
    public void setLabels(String[] k,String[] v){
        for ( int i=0; i<k.length;i++){
                        labels.put(k[i],v[i]);
        }
    }
    public TreeMap<String,String> getLabels(){
        return this.labels;
    }
    public void setValue(String s){
        this.value = s;
    }
    public String getValue(){
        return this.value;
    }
}
