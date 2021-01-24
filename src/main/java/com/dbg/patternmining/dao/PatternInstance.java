package com.dbg.patternmining.dao;

import java.util.ArrayList;

public class PatternInstance {
    private ArrayList<Node> nodes;
    private ArrayList<Link> links;

    public PatternInstance () {
        nodes = new ArrayList<>();
        links = new ArrayList<>();
    }

    public PatternInstance(ArrayList<Node> nodes, ArrayList<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void addLink(Link link) {
        this.links.add(link);
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

}
