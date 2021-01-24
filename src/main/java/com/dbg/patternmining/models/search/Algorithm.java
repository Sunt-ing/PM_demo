/**
 * created May 16, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package com.dbg.patternmining.models.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dbg.patternmining.models.dataStructures.CorePattern;
import com.dbg.patternmining.models.dataStructures.DFSCode;
import com.dbg.patternmining.models.dataStructures.Frequency;
import com.dbg.patternmining.models.dataStructures.GSpanEdge;
import com.dbg.patternmining.models.dataStructures.Graph;
/**
 * Creates a mining chain according to the gSpan algorithm, extended by
 * different options.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class Algorithm<NodeType, EdgeType> implements
	 	com.dbg.patternmining.models.AlgorithmInterface.Algorithm<NodeType, EdgeType>,
		Generic<NodeType, EdgeType> {

	/**
	 * Inner class to iterate over the initial edges
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 */
	private class MyIterator implements
			Iterator<SearchLatticeNode<NodeType, EdgeType>> {
		final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> entryit;

		final boolean del;

		Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> last = null;

		MyIterator(
				final Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials,
				final boolean del) {
			entryit = initials.entrySet().iterator();
			this.del = del;
		}

		@Override
		public boolean hasNext() {
			return entryit.hasNext();
		}

		@Override
		public SearchLatticeNode<NodeType, EdgeType> next() {
			last = entryit.next();
			return last.getValue();
		}

		@Override
		public void remove() {
			if (del) {
				//removeEdge(last.getValue());
			}
			entryit.remove();
		}
	}

	/**
	 * This class represents a Label that is used for the pseudo node required
	 * for unconnected fragemtn search in gSpan
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 */
	static class PseudoLabel implements Serializable {
		/**	 */
		private static final long serialVersionUID = -4215112903761599420L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			return (o instanceof PseudoLabel);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "pseudo";
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean keep;

//	private transient/* final */Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
	private Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> oneEdgePatterns;

	/**
	 * generates a new (GSpan) algorithm
	 */
	public Algorithm() {
		
	}
	
//	public void setInitials(Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials)
//	{
//		this.initials=initials;
//	}
	
	public void setOneEdgePatterns(Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> oneEdgePatterns)
	{
		this.oneEdgePatterns=oneEdgePatterns;
	}
	@Override
	public Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> getOneEdgePatterns() {
		return oneEdgePatterns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#getExtender(int)
	 */
	@Override
	public Extender<NodeType, EdgeType> getExtender(HashMap<String,Integer> oneEdgeMNImap,int[][] maxDegreeMatrix,
			ArrayList<Integer> sortedNodeLabels, Graph kGraph) 
	{
		// configure mining chain
		
		final GSpanExtender<NodeType, EdgeType> extender = new GSpanExtender<NodeType, EdgeType>();
		// from last steps (filters after child computation) ...
		MiningStep<NodeType, EdgeType> curFirst = extender;
		GenerationStep<NodeType, EdgeType> gen;
		

		//if (env.embeddingBased) { //Yes
			// ... over generation ...
			curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(
					curFirst);
			// .. to prefilters
			
//			curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst,
//					new IntFrequency(minFreq), null);
			curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);

		//} 

		// build generation chain
		GenerationPartialStep<NodeType, EdgeType> generationFirst = gen
				.getLast();

		
			//YES
		generationFirst = new OneEdgeExtension<NodeType, EdgeType>(generationFirst,oneEdgePatterns,
				oneEdgeMNImap,maxDegreeMatrix,sortedNodeLabels,kGraph);

		// insert generation chain
		gen.setFirst(generationFirst);

		// insert mining chain
		extender.setFirst(curFirst);
		return extender;
	}
	
	@Override
	public Extender<NodeType, EdgeType> getCoreExtender(CorePattern inPattern, 
			List<String> coreOrginEdges,int[][] maxDegreeMatrix,
			ArrayList<Integer> sortedNodeLabels) 
	{
		// configure mining chain
		
		final GSpanExtender<NodeType, EdgeType> extender = new GSpanExtender<NodeType, EdgeType>();
		// from last steps (filters after child computation) ...
		MiningStep<NodeType, EdgeType> curFirst = extender;
		GenerationStep<NodeType, EdgeType> gen;
		

		//if (env.embeddingBased) { //Yes
			// ... over generation ...
			curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(
					curFirst);
			// .. to prefilters
			
//			curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst,
//					new IntFrequency(minFreq), null);
			curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);

		//} 

		// build generation chain
		GenerationPartialStep<NodeType, EdgeType> generationFirst = gen
				.getLast();

		
			//YES
		generationFirst = new OneCoreEdgeExtension<NodeType, EdgeType>(generationFirst,inPattern,coreOrginEdges,
				maxDegreeMatrix,sortedNodeLabels);

		// insert generation chain
		gen.setFirst(generationFirst);

		// insert mining chain
		extender.setFirst(curFirst);
		return extender;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialNodes()
	 */
	@Override
	public Iterator<SearchLatticeNode<NodeType, EdgeType>> oneEdgeTwoNodes() {
		return new MyIterator(oneEdgePatterns, !keep);
	}
}
