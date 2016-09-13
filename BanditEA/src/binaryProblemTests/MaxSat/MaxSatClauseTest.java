package binaryProblemTests.MaxSat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bandits.MBanditEA;
import bandits.MBanditGene;
import benchmarks.maxSAT.MaxSAT;
import utilities.StatSummary;

/**
 * Created by Jialin Liu on 06/09/2016.
 * CSEE, University of Essex
 * jialin.liu@essex.ac.uk
 */
public class MaxSatClauseTest {
    public MaxSatBanditInstance problem;
    boolean success;
    int evalsSoFar;
    int nBandits;
    double bestYet;
    MBanditEA genome;
    static double bestYets[][];

    public static void main(String[] args) {
        int nTrials = 100;
        int nEvals = 100000;

/*        final File dir = new File("benchmarks/MaxSAT/ms_random/abrame-habet/max2sat/120v");
        String[] everythingInThisDir = dir.list();
        for (String fileName : everythingInThisDir) {
            if(fileName.endsWith("cnf")) {
                System.out.println(dir+"/"+fileName);
                StatSummary[] ssArray = runTrials(dir+"/"+fileName, nTrials, nEvals);
                System.out.println(ssArray[0]);
                System.out.println(ssArray[1]);
            }
        }*/
        String fileName = "benchmarks/MaxSAT/ms_random/abrame-habet/max2sat/120v/s2v120c1200-1.cnf";
        System.out.println(fileName);
        StatSummary[] ssArray = runTrials(fileName, nTrials, nEvals);
        System.out.println(ssArray[0]);
        System.out.println(ssArray[1]);
        dump(bestYets, String.join("","mbandit_UCB_UCB_MAXSAT_C", Integer.toString((int)MBanditGene.k), "_", Integer.toString(nEvals), "evals_", Integer.toString(nTrials), "runs.txt"));
    }

    public MaxSatClauseTest(String fileName) {
        // Load problem
        setSATProblem(fileName);
        // Initialise the optimiser
        setupBandit();
    }

    // Load problem
    public void setSATProblem(String fileName) {
        MaxSAT sat = new MaxSAT(fileName);
        this.problem = new MaxSatBanditInstance(sat);
    }

    // Initialise the optimiser
    public void setupBandit() {
        // reset variable values and clause values
        this.problem.initVars();
        // create a new banditEA
        this.genome = new MBanditEA();
        this.genome.init(problem);
        this.nBandits = this.genome.getNBandit();
        // get the current variable indices in the truth table -> current values of the bandits
        int[] indices = this.problem.getIndicesInTable();
        for(int i=0; i<this.nBandits; i++) {
            this.genome.getGenome().getGene(i).resetX(indices[i]); // setup the current value
        }
    }

    // TODO this is the part to play with
    public double evaluate(double[] solution) {
        evalsSoFar++;
        //double fitness = this.problem.sumClauseValue(solution);
        double fitness = this.problem.getNbTrueClauses(solution);
        return fitness;
    }

    public double evaluate2(double[] vars) {
        evalsSoFar++;
        double fitness = this.problem.evaluate(vars);
        return fitness;
    }

    public int getOptimalValue() {
        return problem.optimalValue();
    }

    // Mutate the related genes
    public ArrayList<Integer> mutateRelatedGenes(int idxClause) {
        // The selected mutated gene
        MBanditGene gene = genome.getGenome().getGene(idxClause);
        // Modified variables indices
        ArrayList<Integer> modifiedVars = this.problem.getModifiedVarIdx(idxClause, gene.getPreviousX(), gene.getX());
        // Update the modified variables
        //double[] newSolution = this.problem.getVariables();
        //System.out.println("modifiedVars" + modifiedVars.toString());

        for(Integer idxVar: modifiedVars) {
            //System.out.println("newSolution[" + idxVar+ "]=" + this.problem.getNewVariables()[idxVar]);
            this.problem.flip(this.problem.getNewVariables(), idxVar);
            //System.out.println("newSolution[" + idxVar+ "]=" + this.problem.getNewVariables()[idxVar]);
        }

        // Save the updated variables, newClauseValues is updated
        this.problem.setNewVariables(this.problem.getNewVariables());

        // Find the related clauses
        ArrayList<Integer> modifiedGenes = this.problem.getModifiedClauses(idxClause, modifiedVars);
        // Mutate the related genes (clauses)
        for(int idxGene: modifiedGenes) {
            //System.out.println("modifiedGene idx=" + idxGene);
            List<Integer> currentClause = this.problem.getClauseAt(idxGene);
            double[] newPair = new double[2];
            assert(currentClause.size()==2);
            for (int i = 0; i < 2; i++) {
                int value = currentClause.get(i);
                if (value > 0)
                    newPair[i] = this.problem.getNewVariables()[value-1];
                else if (value < 0)
                    newPair[i] = 1-this.problem.getNewVariables()[-value-1];
                else
                    System.err.println("ERROR: Variable " + value + " with index 0.");
            }

            double[] pair = new double[2];
            assert(currentClause.size()==2);
            for (int i = 0; i < 2; i++) {
                int value = currentClause.get(i);
                if (value > 0)
                    pair[i] = this.problem.getVariables()[value-1];
                else if (value < 0)
                    pair[i] = 1-this.problem.getVariables()[-value-1];
                else
                    System.err.println("ERROR: Variable " + value + " with index 0.");
            }

            //System.out.println(pair[0] + " " + pair[1] + "    " + newPair[0] + " " + newPair[1]);
            // Find the index of new entry
            //int mutateFrom = this.problem.getIdxInTable(pair);
            int mutateTo = this.problem.getIdxInTable(newPair);
            //System.out.println("Mutate from "+ mutateFrom + " to " + mutateTo);
            MBanditGene currentGene = genome.getGenome().getGene(idxGene);
            currentGene.mutateTo(mutateTo);
        }
        return modifiedGenes;
    }

    // Run several trials
    public static StatSummary[] runTrials(String fileName, int nTrials, int nEvals) {
        // Initialise the statistic containers
        StatSummary[] ssArray = new StatSummary[2];
        StatSummary ss = new StatSummary();
        StatSummary ssTime = new StatSummary();
        bestYets = new double[nTrials][nEvals];

        // Create a test
        // The problem is load only once
        MaxSatClauseTest test = new MaxSatClauseTest(fileName);

        // Run several trials
        for (int i=0; i<nTrials; i++) {
            // Init best yet at each evaluation / iteration
            for(int j = 0; j < nEvals; ++j) {
                bestYets[i][j] = 0;
            }

            long startTime = System.nanoTime(); // Starting time
            // Run the i^th trial using nEvals as budget
            test.run(nEvals, i);

            //if (test.success) {
            //    ss.add(test.evalsSoFar);
            //}

            long endTime = System.nanoTime();   // End time

            // Optimum = nbClauses - nbTrueClauses
            ss.add(test.getOptimalValue());
            ssTime.add((endTime-startTime)/1000000);
        }
        ssArray[0] = ss;
        ssArray[1] = ssTime;
        return ssArray;
    }

    // Run one trial
    public MBanditEA run(int nEvals, int nTrial) {
        // Reset the optimiser and initial solution
        setupBandit();

        this.success = false;
        this.evalsSoFar = 0;
        this.bestYet = this.evaluate(this.problem.getVariables());
        //System.out.println("nbClauses=" + this.problem.getNbClauses());
        //System.out.println("bestYet=" + bestYet);
        //MaxSATTest newtest = new MaxSATTest(this.problem.getProblem());
        //System.out.println("bestYet=" + newtest.problem.evaluate(this.problem.getVariables()));
        System.out.println("Initialised with y=" + (this.problem.getNbClauses()-this.bestYet) );

        if (evalsSoFar != 1) {
            System.err.println("ERROR: The current evaluation number is wrongly counted.");
        }

        int iterations = 0;
        bestYets[nTrial][iterations] = getOptimalValue();

        while (evalsSoFar < nEvals) {
            iterations++;
            // Select the gene to mutate
            int idxGene = genome.getGenome().selectOneGeneIdxToMutate(evalsSoFar);
            //int idxGene = genome.getGenome().selectRandomGeneIdx();
            MBanditGene gene = genome.getGenome().getGene(idxGene);
            assert(genome.getGenome().selectOneGeneToMutate(evalsSoFar) == gene);

            // Mutate the selected gene
            gene.mutate();
            // Find out the related genes (clauses) and mutate
            ArrayList<Integer> relatedGenesIndices = mutateRelatedGenes(idxGene);

            double after = evaluate(this.problem.getNewVariables());

            double delta = after - bestYet;

            // Replace
            if (delta >= 0) {
                bestYet = after;
                this.problem.replaceVariables();
                // TODO This is somewhere we can play with
                gene.applyReward(delta);
                // gene.applyReward(delta/(1+relatedGenesIndices.size()));

                for(int idx: relatedGenesIndices) {
                    genome.getGenome().getGene(idx).applyReward(delta);
                    //genome.getGenome().getGene(idx).applyReward(delta/(1+relatedGenesIndices.size()));
                }
            } else {
                this.problem.resetNewVariables();
            }

            gene.revertOrKeep(delta);
            for(int idx: relatedGenesIndices) {
                genome.getGenome().getGene(idx).revertOrKeep(delta);
            }
            bestYets[nTrial][iterations] = getOptimalValue();
        }

        return genome;
    }

    private static void dump(double[][] results, String filename)
    {
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            for (int i = 0; i < results.length; ++i) {
                for (int j = 0; j < results[i].length; ++j) {
                    writer.write(results[i][j] + ",");
                }
                writer.write("\n");
            }
            writer.close();
        }catch(Exception e)
        {
            System.out.println("MEH: " + e.toString());
            e.printStackTrace();
        }
    }
}
