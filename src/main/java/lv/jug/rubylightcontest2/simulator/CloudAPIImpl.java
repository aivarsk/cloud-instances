package lv.jug.rubylightcontest2.simulator;

import lv.jug.rubylightcontest2.CloudAPI;

public class CloudAPIImpl implements CloudAPI {

    private int instanceHours = 0;

    private int instanceCount = 100;

    @Override
    public void startInstances(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        //System.out.format("Allocating %d instances...\n", n);
        instanceCount += n;
    }

    @Override
    public void stopInstances(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        //System.out.format("Deallocating %d instances...\n", n);
        instanceCount = Math.max(instanceCount - n, 0);
    }

    public int getInstanceHours() {
        return instanceHours;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void updateInstanceHours() {
        instanceHours += instanceCount;
    }
}
