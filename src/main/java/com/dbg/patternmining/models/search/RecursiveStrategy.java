/**
 * created May 16, 2006
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * <p>
 * Copyright 2006 Marc Woerlein
 * <p>
 * This file is part of parsemis.
 * <p>
 * Licence:
 * LGPL: http://www.gnu.org/licenses/lgpl.html
 * EPL: http://www.eclipse.org/org/documents/epl-v10.php
 * See the LICENSE file in the project's top-level directory for details.
 */
package com.dbg.patternmining.models.search;


import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dbg.patternmining.models.AlgorithmInterface.Algorithm;
import com.dbg.patternmining.models.CSP.AssignmentInstance;
import com.dbg.patternmining.models.CSP.DFSSearch;
import com.dbg.patternmining.models.VF2check.CheckIso;
import com.dbg.patternmining.models.dataStructures.Canonizable;
import com.dbg.patternmining.models.dataStructures.CorePattern;
import com.dbg.patternmining.models.dataStructures.DFSCode;
import com.dbg.patternmining.models.dataStructures.DFScodeSerializer;
import com.dbg.patternmining.models.dataStructures.Frequency;
import com.dbg.patternmining.models.dataStructures.Frequented;
import com.dbg.patternmining.models.dataStructures.GSpanEdge;
import com.dbg.patternmining.models.dataStructures.Graph;
import com.dbg.patternmining.models.dataStructures.HPListGraph;
import com.dbg.patternmining.models.dataStructures.IntFrequency;
import com.dbg.patternmining.models.dataStructures.IntIterator;
import com.dbg.patternmining.models.dataStructures.Query;
import com.dbg.patternmining.models.dataStructures.myNode;
import com.dbg.patternmining.models.joinAlgorithm.InstanceTable;
import com.dbg.patternmining.models.joinAlgorithm.JoinOperation;
import com.dbg.patternmining.models.topKresults.MaxHeap;
import com.dbg.patternmining.models.topKresults.MinHeap;
import com.dbg.patternmining.models.utilities.Settings;
import com.dbg.patternmining.models.utilities.StopWatch;


/**
 * This class represents the local recursive strategy.
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
public class RecursiveStrategy<NodeType, EdgeType> implements
        Strategy<NodeType, EdgeType> {

    private Extender<NodeType, EdgeType> extender;
    private Extender<NodeType, EdgeType> coreExtender;
    private Graph kGraph;
    private ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> TopKList;
    private IntFrequency minHeapThreshold;
    private int extendedPatternNumber;//the number of child patterns
    private static int[][] maxDegreeMatrix;
    private HashMap<String, Integer> pureTable;
    private MinHeap<NodeType, EdgeType> minHeap;
    //	private MaxHeap coreMaxHeap;
    private HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> coreSet;
    private CorePattern inPattern;
    private int insTableSize;

    public RecursiveStrategy(ArrayList<Integer> sortedNodeLabels) {

    }

    /*
     * (non-Javadoc)
     *
     * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
     *      int)
     */
    @Override
    public ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> search(
            final Algorithm<NodeType, EdgeType> algo, Graph singleGraph,
            ArrayList<SearchLatticeNode<NodeType, EdgeType>> coreOrginEdgeList, CorePattern inPattern,
            Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> patternEdges) {
        this.kGraph = singleGraph;
        this.inPattern = inPattern;
        minHeap = new MinHeap<NodeType, EdgeType>();
        StopWatch watch = new StopWatch();
        watch.start();
        minHeapThreshold = new IntFrequency(0);
        ArrayList<Integer> sortedNodeLabels = kGraph.getSortedFreqLabels();

        /*
         * (1) construct pure table for all pattern edges
         */
        pureTable = constructPureTable(patternEdges);
        /*
         * (2) generate core-patterns S_core
         */
        List<String> coreHMT = createCoreHMT(coreOrginEdgeList); //pattern edges that contain edges of P
        CheckIso.changeInpattern(inPattern);
        maxDegreeMatrix = kGraph.getMaxDegreeMatrix();
        coreExtender = algo.getCoreExtender(inPattern, coreHMT, maxDegreeMatrix, sortedNodeLabels);
        coreSet = new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
        while (coreOrginEdgeList != null && coreOrginEdgeList.size() != 0) {
            coreOrginEdgeList = getList(coreOrginEdgeList);
        }

        /*
         * (3) pure table extension based on core patterns
         *
         */
        ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> extendFreqList = new ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>>();
        extender = algo.getExtender(pureTable, maxDegreeMatrix, sortedNodeLabels, kGraph);
        extendFreqList.add(coreSet);
        while (extendFreqList != null && extendFreqList.size() != 0) {
            extendFreqList = extendFreq(extendFreqList);
        }

        watch.stop();
        System.out.println("Top-" + Settings.k + " List:\n" + minHeap.getTopKList());
        System.out.println("K-th frequency:" + minHeap.getMinFrequency());
        System.out.println("inelapsedTime:" + watch.getElapsedTime() / 1000.0);
        TopKList = minHeap.getTopKList();

        // write instances of these top-k patterns
        Iterator its = TopKList.iterator();
        int topkCount = 0;
        while (its.hasNext()) {
            topkCount++;
            HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> map = (HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>) its
                    .next();
            for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry : map.entrySet()) {
                SearchLatticeNode<NodeType, EdgeType> pattern = entry.getKey();
                Frequency freq = entry.getValue();
                ArrayList<int[]> insList = pattern.getInsList();
                writeInsTxtFiles(topkCount, insList, pattern.getHPlistGraph(), freq);
            }
        }

        return TopKList;
    }

    private ArrayList<SearchLatticeNode<NodeType, EdgeType>> getList(
            ArrayList<SearchLatticeNode<NodeType, EdgeType>> coreOrginEdgeList) {
        ArrayList<SearchLatticeNode<NodeType, EdgeType>> retSet = new ArrayList<SearchLatticeNode<NodeType, EdgeType>>();
        Iterator it = coreOrginEdgeList.iterator();
        while (it.hasNext()) {
            SearchLatticeNode<NodeType, EdgeType> pattern = (SearchLatticeNode<NodeType, EdgeType>) it.next();
            // if the pattern has same topology as core pattern
            if (pattern.getHPlistGraph().getNodeCount() == inPattern.getNodeNum()
                    && pattern.getHPlistGraph().getEdgeCount() == inPattern.getEdgeNum()) {
                // p is isomorphic with P
                boolean iso = CheckIso.checkIso(pattern);
                if (iso) {
//					Frequency UB = ((Frequented) pattern).upperBound(minHeapThreshold.intValue());
//					Frequency UB = ((Frequented) pattern).frequency(minHeapThreshold.intValue());

                    Query qry = new Query((HPListGraph<Integer, Double>) pattern.getHPlistGraph());
                    Frequency childFreq = null;
                    pattern.intitialInsList();
                    if (Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph())) {
                        JoinOperation join = new JoinOperation(pattern);
                        int JoinFreq = join.getMNIfrqByBasicJoin(pattern);
                        childFreq = new IntFrequency(JoinFreq);

                    } else {
                        childFreq = ((Frequented) pattern).frequency(minHeapThreshold.intValue());
                        ArrayList<int[]> list = ((DFSCode<NodeType, EdgeType>) pattern).getDFSInsList();
                        if (list != null) {
                            Iterator its = list.iterator();
                            while (its.hasNext()) {
                                int[] arr = (int[]) its.next();
                                pattern.setInstances(arr);
                            }
                        }
                    }

                    if (childFreq.compareTo(minHeapThreshold) > 0) {
//						Frequency MNIFreq = ((Frequented) pattern).aftUBFrequency(minHeapThreshold.intValue());
                        coreSet.put(pattern, childFreq);
                        Boolean ifAdd = minHeap.addTopk(pattern, childFreq);
                        if (ifAdd) {// the minimum frequency of minheap was changed
                            minHeapThreshold = minHeap.getMinFrequency();
                        }
                    }
                }
            } else if (pattern.getHPlistGraph().getNodeCount() <= inPattern.getNodeNum()
                    && pattern.getHPlistGraph().getEdgeCount() < inPattern.getEdgeNum()) {
                // PatExtension HMT'
                final Collection<SearchLatticeNode<NodeType, EdgeType>> children = coreExtender
                        .getCoreChildren(pattern);
                for (SearchLatticeNode<NodeType, EdgeType> child : children) {
                    final Canonizable can = (Canonizable) child;
                    if (!can.isCanonical()) {// if the child pattern is minimum DFSCode
                        System.out.println("Not Canonizable!!!");
                        continue;
                    }
                    retSet.add(child);
                }
            } else if (inPattern.getEdgeNum() == 0 && pattern.getHPlistGraph().getNodeCount() == 2) {// core: a node
                System.out.println("core pattern: Node !!!!!!!!");
                NodeType nodeA = pattern.getHPlistGraph().getNodeLabel(0);
                NodeType nodeB = pattern.getHPlistGraph().getNodeLabel(1);
                int nodeLabelA = Integer.valueOf(nodeA.toString());
                int nodeLabelB = Integer.valueOf(nodeB.toString());
                Set<Integer> nodeLabelSet = inPattern.getNodeLabelSet();
                if (nodeLabelSet.contains(nodeLabelA) || nodeLabelSet.contains(nodeLabelB)) {
//					Frequency UB = ((Frequented) pattern).upperBound(minHeapThreshold.intValue());
//					Frequency UB = ((Frequented) pattern).frequency(minHeapThreshold.intValue());
                    Query qry = new Query((HPListGraph<Integer, Double>) pattern.getHPlistGraph());
                    Frequency childFreq = null;
                    pattern.intitialInsList();
                    if (Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph())) {
                        JoinOperation join = new JoinOperation(pattern);
                        int JoinFreq = join.getMNIfrqByBasicJoin(pattern);
                        childFreq = new IntFrequency(JoinFreq);

                    } else {
                        childFreq = ((Frequented) pattern).frequency(minHeapThreshold.intValue());
                        ArrayList<int[]> list = ((DFSCode<NodeType, EdgeType>) pattern).getDFSInsList();
                        if (list != null) {
                            Iterator its = list.iterator();
                            while (its.hasNext()) {
                                int[] arr = (int[]) its.next();
                                pattern.setInstances(arr);
                            }
                        }
                    }
                    if (childFreq.compareTo(minHeapThreshold) > 0) {
//						Frequency MNIFreq = ((Frequented) pattern).aftUBFrequency(minHeapThreshold.intValue());
                        coreSet.put(pattern, childFreq);
                        Boolean ifAdd = minHeap.addTopk(pattern, childFreq);
                        if (ifAdd) {// the minimum frequency of minheap was changed
                            minHeapThreshold = minHeap.getMinFrequency();
                        }
                    }
                } else
                    continue;
            }
            it.remove();
        }
        return retSet;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> extendFreq(ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> extendFreqList) { // RECURSIVE NODES SEARCH
        ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> retSet = new ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>>();
        Iterator it = extendFreqList.iterator();
        while (it.hasNext()) {
            HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> patternMap = (HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>) it.next();

            for (Entry<SearchLatticeNode<NodeType, EdgeType>, Frequency> entry : patternMap.entrySet()) {
                SearchLatticeNode<NodeType, EdgeType> pattern = entry.getKey();
                final Collection<SearchLatticeNode<NodeType, EdgeType>> children = extender.getChildren(pattern);
                for (SearchLatticeNode<NodeType, EdgeType> child : children) {
                    extendedPatternNumber++;
                    final Canonizable can = (Canonizable) child;
                    if (!can.isCanonical()) {// if the child pattern is minimum DFSCode
                        System.out.println("Not Canonizable!!!");
                        continue;
                    }

//					Frequency childFreq = ((Frequented) child).frequency(minHeapThreshold.intValue());
                    System.out.print("Count MNI:\n" + child);
                    Query qry = new Query((HPListGraph<Integer, Double>) child.getHPlistGraph());
                    Frequency childFreq = null;
                    child.intitialInsList();
                    if (Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph())) {
                        JoinOperation join = new JoinOperation(child);
                        int JoinFreq = join.getMNIfrqByBasicJoin(child);
                        childFreq = new IntFrequency(JoinFreq);

                    } else {
                        childFreq = ((Frequented) child).frequency(minHeapThreshold.intValue());
                        ArrayList<int[]> list = ((DFSCode<NodeType, EdgeType>) child).getDFSInsList();
                        if (list != null) {
                            Iterator its = list.iterator();
                            while (its.hasNext()) {
                                int[] a = (int[]) its.next();
                                child.setInstances(a);
                            }
                        }
                    }

                    // Basic join operation
//					JoinOperation join = new JoinOperation(child);
//					int JoinFreq = join.getMNIfrqByBasicJoin();
//					Frequency childFreq = new IntFrequency(JoinFreq);
//					System.out.println("Basic Join MNI = "+childFreq);
                    //add a judge for equal to 0
                    if (childFreq.compareTo(new IntFrequency(0)) > 0) {
                        Boolean ifAdd = minHeap.addTopk(child, childFreq);

                        if (ifAdd) {// the minimum frequency of minheap was changed
                            IntFrequency minFreq = minHeap.getMinFrequency();
                            if (minFreq.compareTo(new IntFrequency(1)) > 0) {
                                minHeapThreshold = minFreq;
                                System.out.println("Change the minThreshold to:" + minFreq);
                            }

                            HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency> midMap = new HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>();
                            midMap.put(child, childFreq);
                            retSet.add(midMap);
                        } else {
                            System.out.println("The pattern is valid!");
                        }
                    }
                }

                pattern.finalizeIt();
            }
            it.remove();
        }
        return retSet;
    }

    private boolean areAllLabelsDistinct(HPListGraph<Integer, Double> me) {
        for (int i = 0; i < me.getNodeCount(); i++) {
            int labelChecker = (me.getNodeLabel(i));
            for (int j = i + 1; j < me.getNodeCount(); j++) {
                int label = me.getNodeLabel(j);
                if (labelChecker == label) {
                    return false;
                }
            }
        }
        return true;
    }

    public static <NodeType, EdgeType> void writeInsTxtFiles(int topkCount, ArrayList<int[]> result,
                                                             HPListGraph<NodeType, EdgeType> hpListGraph, Frequency freq) {
        FileWriter fw;
        String fName = "No_" + topkCount + "_Instances.txt";
        try {
            fw = new FileWriter(fName);
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			fw.write("Date:" + df.format(day) + "\n");
//			fw.write("--------------------- Pattern ---------------------"+ "\n");
            fw.write(hpListGraph.toString());
            fw.write("Frequency:" + freq + "\n");
//			fw.write("--------------------- Instances -------------------"+ "\n");
            for (int i = 0; i < result.size(); i++) {
                int[] arr = result.get(i);
                for (int j = 0; j < arr.length; j++) {
                    fw.write(arr[j] + " ");
                    if (j < arr.length - 1)
                        fw.write(",");
                }
                if (i < result.size() - 1)
                    fw.write(";");
            }
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> constructPureTable(Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> patternEdges) {
        HashMap<String, Integer> rePureTable = new HashMap<String, Integer>();
        for (Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> entry : patternEdges.entrySet()) {
            SearchLatticeNode<NodeType, EdgeType> pattern = entry.getValue();
            String patternString = DFScodeSerializer.serializePatternToString(pattern);
//			Frequency freq = ((Frequented) pattern).frequency(minHeapThreshold.intValue());
            rePureTable.put(patternString, 0);
        }

        return rePureTable;
    }

    public List<String> createCoreHMT(List<SearchLatticeNode<NodeType, EdgeType>> list) {
        List<String> coreHMT = new ArrayList<String>();
        // copy the items into HMT
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SearchLatticeNode<NodeType, EdgeType> pattern = (SearchLatticeNode<NodeType, EdgeType>) it.next();
            String patternString = DFScodeSerializer.serializePatternToString(pattern);
            coreHMT.add(patternString);
        }
        return coreHMT;
    }

}
