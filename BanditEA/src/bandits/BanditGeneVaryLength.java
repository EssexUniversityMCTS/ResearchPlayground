package bandits;

import java.util.ArrayList;
import java.util.Random;

import utilities.Picker;
import utilities.StatSummary;

/**
 * Created by jliu on 23/08/2016.
 */
public class BanditGeneVaryLength {
    static Random random = new Random();

    static double eps = 0.01;

    int nArms;

    ArrayList<Double> deltaRewards;

    // note the number of times each arm has been pulled
    ArrayList<Integer> nPullsByArm;
    int nPulls;

    // current value
    double x;
    // backup of value
    double xPrevious;

    // start all at one to avoid div zero
    int nMutations = 1;
    static double k = 10;

//    public BanditGeneVaryLength(int nArms) {
//        this.nArms = nArms;
//        armPulls = new int[nArms];
//        deltaRewards = new double[nArms];
//        randomize();
//    }
//
//    public void randomize() {
//        // x = 0 ; // random.nextInt(nArms);
//        x = random.nextInt(nArms);
//        armPulls[x]++;
//        nPulls++;
//
//    }
//
//    public void mutate() {
//        banditMutate();
//    }
//
//    public void banditMutate() {
//        // having chosen this bandit, only the UCB terms
//        // within this bandit are relevant (not the total across all bandits)
//        Picker<Integer> picker = new Picker<>(Picker.MAX_FIRST);
//
//        for (int i = 0; i < nArms; i++) {
//            // never choose the current value of x
//            // that would not be a mutation!!!
//            if (i != x) {
//                double exploit = exploit(i);
//                double explore = explore(armPulls[i]);
//                // small random numbers: break ties in unexpanded nodes
//                double noise = random.nextDouble() * eps;
//                // System.out.format("%d\t %.2f\t %.2f\n", i, exploit, explore);
//                picker.add(exploit + explore + noise, i);
//            }
//        }
//        xPrevious = x;
//        x = picker.getBest();
//        armPulls[x]++;
//        nPulls++;
//    }
//
//
//    public double maxDelta() {
//        StatSummary ss = new StatSummary();
//        for (double d : deltaRewards) ss.add(d);
//        return ss.max();
//    }
//
//    public double urgency(int nEvals) {
//        return rescue() + explore(nEvals);
//    }
//
//
//    // in bandit terms this would normally be called the exploit term
//    // but in an EA we need to use it in the opposite sense
//    // since we need to stick with values that are already thought to be
//    // good and instead modify ones that need to be rescued
//    public double rescue() {
//        return -maxDelta() / nMutations;
//    }
//
//    // standard UCB Explore term
//    // consider modifying a value that's not been changed much yet
//    public double explore(int ithPulls) {
//        return k * Math.sqrt(Math.log(nPulls + 1) / (ithPulls + 1));
//    }
//
//    public double exploit(int i) {
//        return deltaRewards[i] / (armPulls[i] + 1);
//    }
//
//
//    public void applyReward(double delta) {
//        deltaRewards[x] += delta;
//        deltaRewards[xPrevious] -= delta;
//        // System.out.println("Applied: " + delta);
//    }
//
//    public void revertOrKeep(double delta) {
//        if (delta < 0) {
//            x = xPrevious;
//        }
//    }
//
//    public String statusString(int nEvals) {
//        return String.format("%d\t rescue: %.2f\t explore: %.2f\t urgency: %.2f",
//                x, rescue(), explore(nEvals), urgency(nEvals));
//    }
}
