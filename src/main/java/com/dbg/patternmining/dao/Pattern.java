package com.dbg.patternmining.dao;

import java.util.ArrayList;

public class Pattern {
    private ArrayList<PatternInstance> patternInstances;
    private int frequency;

    public Pattern() {
        patternInstances = new ArrayList<>();
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public ArrayList<PatternInstance> getPatternInstance() {
        return patternInstances;
    }

    public void addPatternInstance(PatternInstance patternInstance) {
        patternInstances.add(patternInstance);
    }

    public int getFrequency() {
        return frequency;
    }
}
