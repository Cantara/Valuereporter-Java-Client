package org.valuereporter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.Observation;
import org.valuereporter.ObservedMethod;

import java.time.Instant;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public abstract class ObservationDistributer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ObservationDistributer.class);
    protected static final int MAX_CACHE_SIZE = 500;
    private static final int MAX_WAIT_PERIOD_MS = 1000;

    private static final long DEFAULT_SLEEP_PERIOD_MS = 100;
    private final String reporterHost;
    private final String reporterPort;
    private final String serviceName;

    private static MonitorRepository monitorRepository;
    private final long sleepPeriod;
    private Instant nextForwardAt = null;
    private int maxWaitInterval = -1;

    public ObservationDistributer(String reporterHost, String reporterPort, String serviceName) {
        this(reporterHost,reporterPort,serviceName, MAX_WAIT_PERIOD_MS);
    }

    public ObservationDistributer(String reporterHost, String reporterPort, String serviceName, int maxWaitInterval) {
        this.reporterHost = reporterHost;
        this.reporterPort = reporterPort;
        this.serviceName = serviceName;
        updateNextForwardAt();
        monitorRepository = MonitorRepository.getInstance();
        this.maxWaitInterval = maxWaitInterval;
        sleepPeriod = DEFAULT_SLEEP_PERIOD_MS;
    }

    @Override
    public void run() {
        log.info("Starting ObservationDistributer");
        do {
            while (monitorRepository.hasObservations()) {
                ObservedMethod observedMethod = monitorRepository.takeFirst();
                updateObservation(observedMethod);
            }
            try {
                Thread.sleep(sleepPeriod);
            } catch (InterruptedException e) {
                //Interupted sleep. No probblem, and ignored.
            }
        } while (true);

    }

    public void updateNextForwardAt() {
        nextForwardAt = Instant.now().plusMillis(maxWaitInterval);
    }

    protected boolean waitedLongEnough() {
        boolean doForward = Instant.now().isAfter(nextForwardAt);
        log.trace("doForward {}", doForward);
        return  doForward;
    }

    public String getReporterHost() {
        return reporterHost;
    }

    public String getReporterPort() {
        return reporterPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Instant getNextForwardAt() {
        return nextForwardAt;
    }

    public int getMaxWaitInterval() {
        return maxWaitInterval;
    }

    protected abstract void updateObservation(Observation observation);

}
