/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------------
 * MolecularShortestPath.java
 * 
 * Modified from: DijkstraShortestPath.java
 * -------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer, Scott Gigante
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Sep-2003 : Initial revision (JVS);
 * 29-May-2005 : Make non-static and add radius support (JVS);
 * 07-Jun-2005 : Made generic (CH);
 * 07-Jan-2015 : Modified to Molecular Shortest Path
 */
package minimumFreeEnergyPath.core;

import java.util.ArrayList;
import java.util.List;

import minimumFreeEnergyPath.weightedVertexGraph.WeightedVertex;
import minimumFreeEnergyPath.weightedVertexGraph.WeightedVertexCycle;
import minimumFreeEnergyPath.weightedVertexGraph.WeightedVertexGraph;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphPathImpl;



/**
 * An implementation of Molecular Shortest Path 
 * Follows lowest edge weight until target node is reached, ignoring all dead ends
 *
 * @author John V. Sichi
 * @since Sep 2, 2003
 */
public final class MolecularShortestPath
{
    

    private GraphPath<WeightedVertex, DefaultWeightedEdge> path;

    

    /**
     * Creates and executes a new MolecularShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     */
    public MolecularShortestPath(WeightedVertexGraph graph,
        WeightedVertex startVertex,
        WeightedVertex endVertex)
    {
        this(graph, startVertex, endVertex, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates and executes a new MolecularShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     * @param radius limit on weighted path length, or
     * Double.POSITIVE_INFINITY for unbounded search
     */
    public MolecularShortestPath(
        WeightedVertexGraph graph,
        WeightedVertex startVertex,
        WeightedVertex endVertex,
        double radius)
    {
        if (!graph.containsVertex(endVertex)) {
            throw new IllegalArgumentException(
                "graph must contain the end vertex");
        }

        List<DefaultWeightedEdge> edgeList = createEdgeList(graph, startVertex, endVertex, radius);
        
        path =
                new GraphPathImpl<WeightedVertex, DefaultWeightedEdge>(
                    graph,
                    startVertex,
                    endVertex,
                    edgeList,
                    0);
    }

    

    /**
     * Return the edges making up the path found.
     *
     * @return List of Edges, or null if no path exists
     */
    public List<DefaultWeightedEdge> getPathEdgeList()
    {
        if (path == null) {
            return null;
        } else {
            return path.getEdgeList();
        }
    }

    /**
     * Return the path found.
     *
     * @return path representation, or null if no path exists
     */
    public GraphPath<WeightedVertex, DefaultWeightedEdge> getPath()
    {
        return path;
    }

    /**
     * Return the weighted length of the path found.
     *
     * @return path length, or Double.POSITIVE_INFINITY if no path exists
     */
    public double getPathLength()
    {
        if (path == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return path.getWeight();
        }
    }

    /**
     * Convenience method to find the shortest path via a single static method
     * call. If you need a more advanced search (e.g. limited by radius, or
     * computation of the path length), use the constructor instead.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     *
     * @return List of Edges, or null if no path exists
     */
    public static List<DefaultWeightedEdge> findPathBetween(
        WeightedVertexGraph graph,
        WeightedVertex startVertex,
        WeightedVertex endVertex)
    {
        MolecularShortestPath alg =
            new MolecularShortestPath(
                graph,
                startVertex,
                endVertex);

        return alg.getPathEdgeList();
    }

    private List<DefaultWeightedEdge> createEdgeList(
        WeightedVertexGraph graph,
        WeightedVertex startVertex,
        WeightedVertex endVertex, double radius)
    {
    	
    	// iterate through graph
        List<DefaultWeightedEdge> edgeList = new ArrayList<DefaultWeightedEdge>();
        List<WeightedVertex> visited = new ArrayList<WeightedVertex>();
        WeightedVertex currentVertex = startVertex;
        
        while (currentVertex != endVertex) {
        	visited.add(currentVertex);
        	
        	DefaultWeightedEdge minEdge = graph.getMinEdge(currentVertex);
        	DefaultWeightedEdge minCycleEdge = graph.getMinEdgeInCycle(currentVertex);
        	
        	if (minEdge != null && visited.contains(graph.getEdgeTarget(minEdge))) {
        		// we've found a cycle!
        		currentVertex.setCycle(new WeightedVertexCycle(graph, currentVertex));
        	} else if (minEdge == null) {
        		// reached a dead end
        		DefaultWeightedEdge previousEdge = edgeList.remove(edgeList.size()-1);
        		currentVertex = graph.getEdgeSource(previousEdge);
        	} else if (minEdge != minCycleEdge) {
        		// a previous node within the cycle is a better choice
        		DefaultWeightedEdge previousEdge = edgeList.get(edgeList.size()-1);
        		WeightedVertex outVertex = graph.getEdgeSource(minCycleEdge);
        		if (outVertex.getCycle().contains(graph.getEdgeSource(previousEdge))) {
        			// we're still within the cycle, step back
        			edgeList.remove(edgeList.size()-1);
            		currentVertex = graph.getEdgeSource(previousEdge);
        		} else {
        			// going back would cause us to leave the cycle, find a new path
        			edgeList.addAll(this.createEdgeList(graph, currentVertex, outVertex, radius));
        			currentVertex = outVertex;
        		}
        	} else {        	
	        	// proceed to the next node
	        	edgeList.add(minEdge);
	        	currentVertex = graph.getEdgeTarget(minEdge);
        	}
        }

    	return edgeList;
        
    }
}

// End MolecularShortestPath.java