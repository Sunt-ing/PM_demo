package com.dbg.patternmining.models.dataStructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.dbg.patternmining.models.CSP.Variable;

public class PatternHyperGraph {

	private Variable[] variables;
	private Query qry;
	private int UB;

	public PatternHyperGraph(Graph graph, Query qry, int kthFreq) {
		this.qry = qry;
		UB = 0;
		UBHyperGraph ub = new UBHyperGraph();
		HashMap<Integer, HashMap<Integer, myNode>> supperNodeMap = ub.getSupperNodeList(graph, qry, kthFreq);
		if (supperNodeMap != null) {
			int min = supperNodeMap.get(0).size();
			for (Entry<Integer, HashMap<Integer, myNode>> entry : supperNodeMap.entrySet()) {
				if (min > entry.getValue().size()) {
					min = entry.getValue().size();
				}
			}
			UB = min;
		}
		variables = ub.getVariables();
	}

	
	public Query getQuery()
	{
		return qry;
	}

	public Variable[] getVariables() {
		return variables;
	}
	
	public IntFrequency getUpperBound() {
		// TODO Auto-generated method stub
		return new IntFrequency(UB);
	}
}
