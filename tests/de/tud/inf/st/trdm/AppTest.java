package de.tud.inf.st.trdm;

import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    private TimedRDMSim sim;
    private static final String config = "resources/sim-test-1.conf";
    private static final Properties props = new Properties();

    int startup_time_min;
    int startup_time_max;
    int ready_time_min;
    int ready_time_max;
    int link_activation_time_min;
    int link_activation_time_max;

    public void initSimulator() throws IOException {
        props.load(new FileReader(config));
        startup_time_min = Integer.parseInt(props.get("startup_time_min").toString());
        startup_time_max = Integer.parseInt(props.get("startup_time_max").toString());
        ready_time_min = Integer.parseInt(props.get("ready_time_min").toString());
        ready_time_max = Integer.parseInt(props.get("ready_time_max").toString());
        link_activation_time_min = Integer.parseInt(props.get("link_activation_time_min").toString());
        link_activation_time_max = Integer.parseInt(props.get("link_activation_time_max").toString());
        sim = new TimedRDMSim(config);
        sim.setHeadless(true);
    }
    @Test()
    void testInitializeHasToBeCalled() throws IOException {
        initSimulator();
        assertThrows(RuntimeException.class, () -> sim.run());
    }
    @Test
    void testMirrorChange() throws IOException {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        sim.getEffector().setMirrors(20, 10);
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof  MirrorProbe) {
                mp = (MirrorProbe) p;
            }
        }
        assert(mp != null);
        for(int t = 1; t < sim.getSimTime(); t++) {
            System.out.println("timestep: "+t+" mirrors: "+mp.getNumMirrors());
            sim.runStep(t);
            if(t < 10) assertEquals(5, mp.getNumMirrors());
            else if(t >= 30) assertEquals(20, mp.getNumMirrors());
        }
    }

    @Test
    void testMirrorStartupTime() throws IOException {
        MirrorProbe mp = initTimeTest();
        Map<Integer,Integer> startupTimes = getTimeToStateForMirrorFromSimulation(mp, Mirror.State.UP);
        double avg = getAvg(startupTimes);
        assertTrue(avg > startup_time_min && avg < startup_time_max);
        for(Mirror m : mp.getMirrors()) {
            assertEquals(m.getStartupTime(), startupTimes.get(m.getID()));
        }
    }
    @Test
    void testMirrorReadyTime() throws IOException {
        MirrorProbe mp = initTimeTest();
        Map<Integer, Integer> readyTimes = getTimeToStateForMirrorFromSimulation(mp, Mirror.State.READY);
        double avg = getAvg(readyTimes);
        assertTrue(avg > ready_time_min+startup_time_min && avg < ready_time_max+startup_time_max);
        for(Mirror m : mp.getMirrors()) {
            assertEquals(m.getReadyTime()+m.getStartupTime(), readyTimes.get(m.getID()));
        }
    }
    @Test
    void testLinkActiveTime() throws IOException {
        initTimeTest();
        LinkProbe lp = getLinkProbe();
        Map<Integer,Integer> activeTimes = new HashMap<>();
        for(int i = 1; i < sim.getSimTime(); i++) {
            for(Link l : lp.getLinks()) {
                if(l.getState().equals(Link.State.ACTIVE) && activeTimes.get(l.getID()) == null) {
                    activeTimes.put(l.getID(), i);
                }
            }
            sim.runStep(i);
        }
        double avg = getAvg(activeTimes);
        assertTrue(avg > startup_time_min+ready_time_min+link_activation_time_min && avg < startup_time_max+ready_time_max+link_activation_time_max);
        for(Link l : lp.getLinks()) {
            int expected = l.getActivationTime() + Math.max(l.getSource().getStartupTime() + l.getSource().getReadyTime(), l.getTarget().getStartupTime() + l.getTarget().getReadyTime());
            assertEquals(expected, activeTimes.get(l.getID()));
        }
    }
    @Test
    void testTopologyChange() throws IOException {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),10);
        sim.getEffector().setStrategy(new NextNTopologyStrategy(), 20);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),30);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),40);
        assertDoesNotThrow(() -> sim.run());
    }

    private MirrorProbe initTimeTest() throws IOException {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        MirrorProbe mp = getMirrorProbe();
        assertNotNull(mp);
        return mp;
    }
    private Map<Integer, Integer> getTimeToStateForMirrorFromSimulation(MirrorProbe mp, Mirror.State state) {
        Map<Integer,Integer> stateTimes = new HashMap<>();
        for(int i = 1; i < sim.getSimTime(); i++) {
            for(Mirror m : mp.getMirrors()) {
                if(m.getState().equals(state) && stateTimes.get(m.getID()) == null) {
                    stateTimes.put(m.getID(), i);
                }
            }
            sim.runStep(i);
        }
        return stateTimes;
    }
    private static double getAvg(Map<Integer, Integer> times) {
        int total = 0;
        for(int t : times.values()) total += t;
        return (double)total / times.size();
    }
    private MirrorProbe getMirrorProbe() {
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof MirrorProbe) mp = (MirrorProbe)p;
        }
        return mp;
    }
    private LinkProbe getLinkProbe() {
        LinkProbe lp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof LinkProbe) lp = (LinkProbe)p;
        }
        return lp;
    }

}
