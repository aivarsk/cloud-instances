package lv.jug.rubylightcontest2.simulator;

import lv.jug.rubylightcontest2.InstanceManager;
import lv.jug.rubylightcontest2.impl.InstanceManagerImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Simulator {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: " + Simulator.class.getSimpleName() + " datafile days");
            System.exit(1);
        }

        long[] data = loadData(Integer.parseInt(args[1]), args[0]);
        new Simulator().run(data);
    }

    private void run(long[] data) {
        CloudAPIImpl cloudApi = new CloudAPIImpl();
        InstanceManager instanceManager = new InstanceManagerImpl();

        long requestEarnings = 0;
        long totalFines = 0;

        for (long requestCount : data) {
            cloudApi.updateInstanceHours();
            long capacity = InstanceManager.MAX_REQUEST_PER_INSTANCE * cloudApi.getInstanceCount();

            long processedRequests = Math.min(requestCount, capacity);

            requestEarnings += (processedRequests * InstanceManager.PROFIT_PER_MILLION_REQUESTS / 1_000_000);
            totalFines += calcFines(requestCount, processedRequests);

            System.out.format("Requests %d, processed %d, instances: %d, lost %d\n",
                    requestCount / InstanceManager.MAX_REQUEST_PER_INSTANCE,
                    processedRequests / InstanceManager.MAX_REQUEST_PER_INSTANCE,
                    cloudApi.getInstanceCount(),
                    requestCount - processedRequests);

            instanceManager.loadNotification(requestCount, cloudApi);
        }

        long instanceExpenses = cloudApi.getInstanceHours() * InstanceManager.INSTANCE_COST_PER_HOUR;
        System.out.println("Instance hours = " + cloudApi.getInstanceHours());
        long result = requestEarnings - instanceExpenses - totalFines;
        System.out.format("Requests earnings: %d, instance expenses: %d, fines: %d, result: %d\n",
                requestEarnings, instanceExpenses, totalFines, result);
    }

    private long calcFines(long requestCount, long processedRequests) {
        if (requestCount == processedRequests) {
            // all requests processed - no fines
            return 0;
        }

        long lostRequests = requestCount - processedRequests;
        double lostRequestRatio = (double) lostRequests / requestCount;
        return (long) (lostRequestRatio * lostRequestRatio * 10_000_000);
    }

    private static long[] loadData(int days, String path) throws IOException {
        long[] data = new long[days * 24];
        int i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strData = line.split(",");
                int countInLine = 0;
                for (String s : strData) {
                    if (countInLine++ == 24 || s.length() == 0) {
                        break;
                    }
                    data[i++] = Long.parseLong(s);
                }
            }
        }
        return data;
    }
}
