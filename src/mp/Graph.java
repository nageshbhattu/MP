/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mp;

import cc.mallet.types.Dirichlet;

/**
 *
 * @author nageshbhattu
 */
public class Graph {
    double[][] targetLabelDists; // numInstances X numLabels
    double[][] pDists; // numInstances X numLabels
    double[][] qDists; // numInstances X numLabels
    double[][] weights; // Graph in Adjacency List form
    int[][] edges; // Graph Edges
    
    int numLabels;
    int numInstances;
    public Graph(int numInstances, int numLabels, double[][] targetLabelDists, double[][] weights, int[][] edges){
        this.numInstances = numInstances;
        this.numLabels = numLabels;
        this.targetLabelDists = targetLabelDists;
        this.weights = weights;
        this.edges = edges;
        pDists = new double[numInstances][];
        qDists = new double[numInstances][];
        initPQDists();
    }   
    
    public void initPQDists(){
        Dirichlet d = new Dirichlet(numLabels);
        for(int ni = 0;ni<numInstances;ni++){
            pDists[ni] = d.nextDistribution();
            qDists[ni] = d.nextDistribution();
        }
    }
    
    public double[] getPDist(int nodeId){
        return pDists[nodeId];
    }
    
    public double[] getQDist(int nodeId){
        return qDists[nodeId];
    }
    public double[] getTargetDist(int nodeId){
        return targetLabelDists[nodeId];
    }
    public int[] getNeighbors(int nodeId){
        return edges[nodeId];
    }
    public double[] getNeighborWeights(int nodeId){
        return weights[nodeId];
    }
}
