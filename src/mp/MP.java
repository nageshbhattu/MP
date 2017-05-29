/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mp;

import java.io.IOException;
import static java.lang.System.exit;

/**
 *
 * @author nageshbhattu
 */
public class MP {
    int numInstances;
    int numLabeledInstances;
    int numUnlabeledInstances;
    int numLabels;
    int numIterations;
    Graph g;
    final double SMALL=1E-10;
    final double CONVERGENCE_CRITERIA = 1E-4;
    double mu, nu;
    double[] beta;
    private double[] convergence;
    
    void getMPObjectiveValue(){
        double term1 = 0;
        for (int i=0; i < numInstances; i++) {
            
            if (i<numLabeledInstances) { 
                double[] qDist = g.getQDist(i);
                term1 += -Math.log(qDist[g.getTargetLabel(i)]);
            }
        }
        

        double term2 = 0;
        for (int i=0; i < numInstances; i++) {
            int[] neighbors = g.getNeighbors(i);
            double[] weights = g.getNeighborWeights(i);
            double[] nodePDist = g.getPDist(i);
            
            for (int ni=0; ni < neighbors.length; ni++) {
            
                double[] neighborQDist = g.getQDist(neighbors[ni]);

                term2 += weights[ni] * KLDivergence(nodePDist,neighborQDist);
        
            }
        }
        term2 = term2 * mu;

        double term3 = 0;
        for (int ni=0; ni < numInstances; ni++) {
            double[] nodePDist = g.getPDist(ni);
            term3 +=Entropy(nodePDist);
        }
        term3 = term3 * nu;
        
    }    
    public void init(String graphFile,int numInstances,int numLabels,int numLabeledInstances,double nu, double mu)throws IOException{
        CorpusReader cr = new CorpusReader(graphFile,numInstances,numLabels);
        g = cr.getGraph();
        this.numLabeledInstances = numLabeledInstances;
        this.numInstances = g.numInstances;
        this.numLabels = g.numLabels;
        this.numUnlabeledInstances = this.numInstances - this.numLabeledInstances;
        this.nu = nu;
        this.mu = mu;
    }
    public void AM() {
        
        beta = new double[numLabels];
        convergence = new double[numIterations];
        for(int iter = 0;iter < numIterations;iter++){
            updateP();
            convergence[iter] = updateQ();
            System.out.println(" Accuracy for Iteration " + iter +" is " + computeAccuracy());
            if(iter>0){
                double change = (convergence[iter-1]-convergence[iter])/convergence[iter];
                if(change<CONVERGENCE_CRITERIA){
                    System.out.println("Converged in "+iter + " Steps ");
                    iter = numIterations +1;
                    break;
                }
            }
        }
    }
    
    public void updateP(){
        
        for (int ni = 0; ni < numInstances; ni++){
            double[] neighborWeights = g.getNeighborWeights(ni);
            int[] neighbors = g.getNeighbors(ni);
            
            double sumWeight = 0.0;
            for(int nei=0;nei<neighborWeights.length;nei++){
                sumWeight+=neighborWeights[nei];
            }
            double gamma_i = nu + mu * sumWeight + 1.0;
            double[] selfQDist = g.getQDist(ni);
            double sumBeta =  0.0;
            for(int li = 0;li<numLabels;li++){
                double weightedLogQ = 0.0;
                for(int nei=0;nei<neighborWeights.length;nei++){
                    double[] neighborQ = g.getQDist(neighbors[nei]);

                    weightedLogQ += neighborWeights[nei] * Math.log(neighborQ[li]+SMALL);
                }
                
                weightedLogQ += Math.log(selfQDist[li]);
                beta[li] = Math.exp(mu/gamma_i * weightedLogQ);
                sumBeta += beta[li];
            }
            
            // Update the p_distibution of the current node ni
            double[] nodePDist = g.getPDist(ni);
            for(int li = 0;li<numLabels;li++){
                nodePDist[li] = beta[li]/sumBeta;
            }
        } 
    }
    
    public double updateQ(){
        
        double convergence = 0.0;
        for (int ni = 0; ni < numInstances; ni++){
            
            double max = 0.0;
            double change = 0.0;
            double[] neighborWeights = g.getNeighborWeights(ni);
            int[] neighbors = g.getNeighbors(ni);
            
            double[] selfPDist = g.getPDist(ni);
            
            double sumWeight = 0.0;
            for(int nei=0;nei<neighborWeights.length;nei++){
                sumWeight+=neighborWeights[nei];
            }
            sumWeight += 1.0;
            double[] selfQDist = g.getQDist(ni);
            
            for(int li = 0;li<numLabels;li++){
                
                double numerator = 0.0;
                double denominator = 0.0;
                if(ni<numLabeledInstances){
                    if(li==g.getTargetLabel(ni))
                        numerator = 1.0;
                    denominator = 1.0;
                }
                
                
                double weightedP = 0.0;
                for(int nei=0;nei<neighborWeights.length;nei++){
                    double[] neighborP = g.getPDist(neighbors[nei]);
                    weightedP += neighborWeights[nei] * neighborP[li];
                }
                weightedP += selfPDist[li];
                numerator += weightedP * mu;
                denominator += mu*sumWeight;
                double oldQProb = selfQDist[li];
                selfQDist[li] = numerator/denominator;
                change = selfQDist[li]/(oldQProb+SMALL);
                if(change>max)
                    max = change;
            }
            convergence += (ni<numLabeledInstances?1:0) + sumWeight * Math.log(max+SMALL);
        }
        return convergence;
    }
    public double computeAccuracy(){
        int nCorrect = 0;
        for(int ii = numLabeledInstances;ii<numInstances;ii++){
            if(g.getMaxProbLabel(ii)== g.getTargetLabel(ii)) nCorrect++;
        }
        return (double) (nCorrect)/numUnlabeledInstances; 
    }
    public double KLDivergence(double[] dist1, double[] dist2){
        double kldivergence = 0.0;
        for(int di = 0;di<dist1.length;di++){
            kldivergence += (dist1[di]+SMALL) * (Math.log(dist1[di]+SMALL) - Math.log(dist2[di]+SMALL));
        }
        return kldivergence;
    }
    
    public double Entropy(double[] dist){
        double entropy =0.0;
        for(int di = 0;di<dist.length;di++){
            entropy += (dist[di]+SMALL) * (Math.log(dist[di]+SMALL) );
        }
        return entropy;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        if(args.length<6){
            System.out.println("Usage: \n MP graphfile numInstances numLabels numLabeledInstances nu mu");
            exit(0);
        }
        // TODO code application logic here
        MP mp = new MP();
        mp.numIterations = 100;
        String graphFile = args[0];
        int numInstances = Integer.parseInt(args[1]);
        int numLabels = Integer.parseInt(args[2]);
        int numLabeledInstances = Integer.parseInt(args[3]);
        double nu = Double.parseDouble(args[4]);
        double mu = Double.parseDouble(args[5]);
        mp.init(graphFile, numInstances, numLabels, numLabeledInstances,nu,mu);
        mp.AM();
    }
    
}
