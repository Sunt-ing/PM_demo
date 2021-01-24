package com.dbg.patternmining.dao;

public class Node implements Cloneable {
    private int id;
    private int typeId;
    private String type;
    private String label;
    private String virusType;
    private double divergence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVirusType() {
        return virusType;
    }

    public void setVirusType(String virusType) {
        this.virusType = virusType;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public double getDivergence() {
        return divergence;
    }

    public void setDivergence(double divergence) {
        this.divergence = divergence;
    }

    public Node(int id, int typeId, String type) {
        this.id = id;
        this.typeId = typeId;
        this.type = type;
    }

    public Node() {

    }

    @Override
    public Node clone() throws CloneNotSupportedException{
        return (Node) super.clone();
    }
}
