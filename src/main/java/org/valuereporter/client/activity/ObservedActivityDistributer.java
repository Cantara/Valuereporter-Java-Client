package org.valuereporter.client.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.activity.CommandActivitySender;
import org.valuereporter.activity.ObservedActivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * There must be only one Observation Distributer in the JVM.
 * Created by baardl on 07.05.14.
 */
public class ObservedActivityDistributer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ObservedActivityDistributer.class);
    private static final int MAX_CACHE_SIZE = 500;
    private static final int MAX_WAIT_PERIOD_MS = 1000;
    private static final  int THREAD_POOL_DEFAULT_SIZE = 10;
    private final String reporterHost;
    private final String reporterPort;
    private int maxWaitInterval = MAX_WAIT_PERIOD_MS;
    private int cacheSize = MAX_CACHE_SIZE;

    private static ActivityRepository activityRepository;
    public String prefix = "PREFIX-NOT-SET";
    private final long sleepPeriod;

    List<ObservedActivity> observedActivities = new ArrayList<>();
    private Instant nextForwardAt = null;

    private ThreadPoolExecutor executor = null;

    public ObservedActivityDistributer(String reporterHost, String reporterPort, String prefix, int fowardInterval) {
        super();
        this.reporterHost = reporterHost;
        this.reporterPort = reporterPort;
        this.prefix = prefix;
        updateNextForwardTimestamp();
        int threadPoolSize = THREAD_POOL_DEFAULT_SIZE;
        executor = new ThreadPoolExecutor(threadPoolSize,threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        this.sleepPeriod = fowardInterval;
        activityRepository = ActivityRepository.getInstance();
    }
    public ObservedActivityDistributer(String reporterHost, String reporterPort, String prefix, int batchSize, int forwardInterval) {
        this(reporterHost, reporterPort, prefix, forwardInterval);
        this.cacheSize = batchSize;
        this.maxWaitInterval = forwardInterval;
        updateNextForwardTimestamp();
    }

    protected void updateNextForwardTimestamp() {
        nextForwardAt = Instant.now().plusMillis(maxWaitInterval);
    }

    @Override
    public void run() {
        log.info("Starting ObservationDistributer");
        do {
            while (activityRepository.hasObservations()) {
                ObservedActivity observedActivity = activityRepository.takeFirst();
                updateObservation(observedActivity);
            }
            try {
                Thread.sleep(sleepPeriod);
            } catch (InterruptedException e) {
                //Interupted sleep. No probblem, and ignored.
            }
        } while (true);

    }

    protected void updateObservation(ObservedActivity observedActivity) {
        if (observedActivity != null) {
            //log.info("Observed {}", observedActivity.toString());
            observedActivities.add(observedActivity);
            if (observedActivities.size() >= cacheSize ||waitedLongEnough()) {
                forwardOutput();
                updateNextForwardTimestamp();
            }
        } else {
            log.warn("Observed Method is null");
        }

    }

    boolean waitedLongEnough() {
        boolean doForward = Instant.now().isAfter(nextForwardAt);
        log.trace("doForward {}", doForward);
        return  doForward;
    }

    /**
     * This worker will call a Command in Valuereporter_Java-SDK to forward the payload.
     */
    private void forwardOutput() {
        //Forward to Valuereporter via HTTP
        log.trace("Forwarding ObservedMethods. Local cache size {}", observedActivities.size());
        if (executor.getActiveCount() < executor.getMaximumPoolSize()) {
            List<ObservedActivity> activitiesToSend = new ArrayList<>(observedActivities);
            if (activitiesToSend != null && activitiesToSend.size() > 0) {

                CommandActivitySender commandSender = new CommandActivitySender(reporterHost, reporterPort, prefix, activitiesToSend);
                executor.submit(commandSender);
            }
        }else {
            log.info("No threads available for HttpSender. Will discard content {}", observedActivities.size());
        }
        observedActivities.clear();
    }

    public int getMaxCacheSize() {
        return MAX_CACHE_SIZE;
    }

    public int getMaxWaitPeriodMs() {
        return MAX_WAIT_PERIOD_MS;
    }
}
