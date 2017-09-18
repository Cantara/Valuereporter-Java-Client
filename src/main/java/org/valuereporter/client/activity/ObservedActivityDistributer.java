package org.valuereporter.client.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.Observation;
import org.valuereporter.activity.CommandActivitySender;
import org.valuereporter.activity.ObservedActivity;
import org.valuereporter.client.ObservationDistributer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * There must be only one Observation Distributer in the JVM.
 * Created by baardl on 07.05.14.
 */
public class ObservedActivityDistributer extends ObservationDistributer {
    private static final Logger log = LoggerFactory.getLogger(ObservedActivityDistributer.class);
    private static ObservedActivityDistributer instance = null;


    private int cacheSize = ObservationDistributer.MAX_CACHE_SIZE;
    private static final  int THREAD_POOL_DEFAULT_SIZE = 10;

    private static ActivityRepository activityRepository;
    private final long sleepPeriod;

    List<ObservedActivity> observedActivities = new ArrayList<>();


    private ThreadPoolExecutor executor = null;

    ObservedActivityDistributer(String reporterHost, String reporterPort, String serviceName, int forwardInterval) {
        super(reporterHost, reporterPort, serviceName, forwardInterval);
        log.info("Starting HttpObservationDistributer. reporterHost: {}, reporterPort {}, serviceName {}, forwardInterval {}", reporterHost, reporterPort, serviceName, forwardInterval);
//        updateNextForwardAt();
        int threadPoolSize = THREAD_POOL_DEFAULT_SIZE;
        executor = new ThreadPoolExecutor(threadPoolSize,threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        this.sleepPeriod = forwardInterval;
        activityRepository = ActivityRepository.getInstance();
    }
    ObservedActivityDistributer(String reporterHost, String reporterPort, String serviceName, int batchSize, int forwardInterval) {
        this(reporterHost, reporterPort, serviceName, forwardInterval);
        this.cacheSize = batchSize;
//        updateNextForwardAt();
    }

    /**
     *
     * @param reporterHost
     * @param reporterPort
     * @param serviceName
     * @param forwardInterval max millsecond between posting updates.
     * @return ObservedActivityDistributer
     */
    public static ObservedActivityDistributer getInstance(String reporterHost, String reporterPort, String serviceName, int forwardInterval) {
        if(instance == null) {
            instance = new ObservedActivityDistributer(reporterHost, reporterPort, serviceName, forwardInterval);
        }
        return instance;
    }

    public static ObservedActivityDistributer getInstance() {
        return instance;
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
                updateNextForwardAt();
            }
        } else {
            log.warn("Observed Method is null");
        }

    }

    @Override
    protected void updateObservation(Observation observation) {
        if (observation instanceof ObservedActivity) {
            updateObservation((ObservedActivity) observation);
        }

    }


    /**
     * This worker will call a Command in Valuereporter_Java-SDK to forward the payload.
     */
    private void forwardOutput() {
        //Forward to Valuereporter via HTTP and observed with Hystrix
        log.trace("Forwarding ObservedActivities. Local cache size {}", observedActivities.size());
        if (executor.getActiveCount() < executor.getMaximumPoolSize()) {
            List<ObservedActivity> activitiesToSend = new ArrayList<>(observedActivities);
            if (activitiesToSend != null && activitiesToSend.size() > 0) {

                CommandActivitySender commandSender = new CommandActivitySender(getReporterHost(), getReporterPort(), getServiceName(), activitiesToSend);
                executor.submit(commandSender);
            }
        }else {
            log.info("No threads available for HttpSender. Will discard content {}", observedActivities.size());
        }
        observedActivities.clear();
    }


}
