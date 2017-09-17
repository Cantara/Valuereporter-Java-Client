package org.valuereporter.client.http;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.testng.Assert.assertTrue;

/**
 * Created by baardl on 26.07.15.
 */
public class HttpObservationDistributerTest {

    private HttpObservationDistributer observationDistributer;

    @BeforeMethod
    public void setUp() throws Exception {
        observationDistributer = new HttpObservationDistributer("","","");

    }

    @Test
    public void testUpdateLatestTimeForwarding() throws Exception {
        Instant currentTime = Instant.now();
        observationDistributer.updateNextForwardAt();
        Instant nextForwardAtLatest = observationDistributer.getNextForwardAt();
        long maxWaitInterval = new Long(observationDistributer.getMaxWaitInterval());
        assertTrue(currentTime.plusMillis(1L).plusMillis(maxWaitInterval).isAfter(nextForwardAtLatest));

    }

    @Test
    public void testWaitedLongEnough() throws Exception {

    }
}