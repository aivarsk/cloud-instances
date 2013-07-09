package lv.jug.rubylightcontest2;

public interface InstanceManager {

    /**
     * Maximum number of request per hour that could be handled by a single instance.
     * If the number of incoming requests is higher than this limit, all exceeding requests
     * will be discarded
     */
    public static final long MAX_REQUEST_PER_INSTANCE = 100_000_000;

    /**
     * Instance const per hour, in some fictional currency
     */
    public static final long INSTANCE_COST_PER_HOUR = 50;

    /**
     * The amount company earns on each million requests that are successfully processed, in some fictional currency
     */
    public static final long PROFIT_PER_MILLION_REQUESTS = 1;

    /**
     * Called every hour, use cloudApi parameter to allocate/deallocate instances
     * @param requestCount number of request that came into the system during the last hour (since the
     *                     previous invocation of this method). The value is non-negative.
     * @param cloudApi interface for stopping/starting instances
     */
    void loadNotification(long requestCount, CloudAPI cloudApi);
}
