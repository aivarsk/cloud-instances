package lv.jug.rubylightcontest2;

import lv.jug.rubylightcontest2.impl.InstanceManagerImpl;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.*;

public class InstanceManagerTest {

    private InstanceManager instanceManager;

    private CloudAPI cloudApi;

    @Before
    public void setUp() throws Exception {
        instanceManager = new InstanceManagerImpl();
        cloudApi = mock(CloudAPI.class);
    }

    @Test
    public void loadNotification_StartsInstances() throws Exception {
        // we start with 100 instances
        // then load increases
        instanceManager.loadNotification(InstanceManager.MAX_REQUEST_PER_INSTANCE * 1000, cloudApi);

        // instances must be started in response to load increase
        verify(cloudApi).startInstances(intThat(greaterThan(0)));
    }
}
