/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mp;

import cc.mallet.types.Dirichlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author nageshbhattu
 */
public class CorpusReader {
    int[] targetLabels; // numInstances
    double[][] targetLabelDists; // numInstances X numLabels
    double[][] weights; // Graph in Adjacency List form
    int[][] edges; // Graph Edges
    
    int numLabels;
    int numInstances;
    public CorpusReader( String graphFile,int numInstances,int numLabels) throws IOException{
        this.numInstances = numInstances;
        this.numLabels = numLabels;
        weights = new double[numInstances][];
        edges = new int[numInstances][];
        targetLabels = new int[numInstances];
        readGraph(graphFile);
    }
    
    public Graph getGraph(){
        return new Graph(numInstances,numLabels,targetLabels, weights,edges);
    }
          
    public void readGraph(String graphFile) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(new File(graphFile)));
        String line = null;
        int nodeId = 0;
        while((line = br.readLine())!=null){ 
        // first number indicates the node-id and remaining items in the list contain 
        // adjacency list encoded as adj_vertex_id:weight_of_the_edge separated by spaces
            String[] adjList = line.split("\\s+");
            targetLabels[nodeId] = Integer.parseInt(adjList[0]);
            weights[nodeId] = new double[adjList.length-1];
            edges[nodeId] = new int[adjList.length-1];
            for(int n=1;n<adjList.length;n++){
                String[] nodeIDWeight= adjList[n].split(":");
                weights[nodeId][n-1] = Double.parseDouble(nodeIDWeight[1]);
                edges[nodeId][n-1] = Integer.parseInt(nodeIDWeight[0]);
            }
            nodeId++;
        }
    }
}
