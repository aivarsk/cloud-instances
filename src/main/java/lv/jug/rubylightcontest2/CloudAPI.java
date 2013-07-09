package lv.jug.rubylightcontest2;

/**
 * Starts and stops server instances.
 */
public interface CloudAPI {

    /**
     * Starts n instances
     * @throws  IllegalArgumentException
     *          if the value of {@code n} is negative
     */
    void startInstances(int n);

    /**
     * Stops n instances
     * @throws  IllegalArgumentException
     *          if the value of {@code n} is negative
     */
    void stopInstances(int n);
}
