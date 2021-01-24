package com.dbg.patternmining.dao;

public class Link {
    private int source;

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private int target;
    private String label;

    public Link(int source, int target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public Link(String label) {
        this.label = label;
    }

    public Link() {

    }

}
