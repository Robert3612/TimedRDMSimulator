package org.lrdm.examples;

import org.lrdm.Data;
import org.lrdm.DataPackage;
import org.lrdm.DirtyFlag;
import org.lrdm.TimedRDMSim;
import org.lrdm.dirty_flag_update_strategy.HighestFlagAllAtOnce;
import org.lrdm.effectors.Effector;
import org.lrdm.probes.Probe;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExampleDatapackage {

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        TimedRDMSim sim = new TimedRDMSim("resources/sim-data.conf");
        sim.initialize(new BalancedTreeTopologyStrategy());
        Effector effector = sim.getEffector();

        DirtyFlag dirty1 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(1,2,4)));
        Data d = new Data(10, 34);
        Data d2 = new Data(15, 5);
        Data d3 = new Data(5, 6);
        d.increaseReceived(10);
        d2.increaseReceived(15);
        d3.increaseReceived(5);

        DataPackage package1 = new DataPackage(new ArrayList<>(Arrays.asList(d,d2,d3)), dirty1);

        effector.setDataPackage(4,package1, 20);
        //effector.setDirtyFlagUpdateStrategy(new HighestFlagAllAtOnce(), 30);


        List<Probe> probes = sim.getProbes();
        int simTime = sim.getSimTime();
        for (int t = 1; t <= simTime; t++) {
            for(Probe p : probes) p.print(t);

            sim.runStep(t);
        }
        sim.plotLinks();
    }
}
