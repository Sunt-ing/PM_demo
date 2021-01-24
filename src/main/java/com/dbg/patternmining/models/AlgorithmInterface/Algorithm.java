/**
 * created May 2, 2006
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
package com.dbg.patternmining.models.AlgorithmInterface;

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
import com.dbg.patternmining.models.search.Extender;
import com.dbg.patternmining.models.search.Generic;
import com.dbg.patternmining.models.search.SearchLatticeNode;

/**
 * This interface encapsulate the required abilities of a mining algorithm.
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
public interface Algorithm<NodeType, EdgeType> extends
		Generic<NodeType, EdgeType>, Serializable {

	/**
	 * @param threadIdx
	 * @return a (new) Extender Object for the given thread (index)
	 */
	public Extender<NodeType, EdgeType> getExtender(HashMap<String, Integer> oneEdgeMNImap, int[][] maxDegreeMatrix,
                                                    ArrayList<Integer> sortedNodeLabels, Graph kGraph);
	
	public Extender<NodeType, EdgeType> getCoreExtender(CorePattern inPattern,
                                                        List<String> coreOrginEdges, int[][] maxDegreeMatrix,
                                                        ArrayList<Integer> sortedNodeLabels);

	/**
	 * Initialize the algorithm
	 * 
	 * @param graphs
	 *            the set of graphs that will be search for frequent fragments
	 * @param factory
	 *            the factory new graphs will be created with
	 * @param settings
	 *            the settings for the search
	 * @return a collection with all fragments that will not be found by the
	 *         algorithm
	 */
//	public Collection<Fragment<NodeType, EdgeType>> initialize(
//			final Collection<Graph<NodeType, EdgeType>> graphs,
//			final GraphFactory<NodeType, EdgeType> factory,
//			final Settings<NodeType, EdgeType> settings);

	/**
	 * @return an iterator over the initial nodes for the search
	 */
//	public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes();
	public Iterator<SearchLatticeNode<NodeType, EdgeType>> oneEdgeTwoNodes();

	public Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> getOneEdgePatterns();

}
