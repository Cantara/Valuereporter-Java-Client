package org.valuereporter.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.Observation;
import org.valuereporter.ObservedMethod;
import org.valuereporter.client.ObservationDistributer;
import org.valuereporter.http.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * There must be only one Observation Distributer in the JVM.
 * Created by baardl on 07.05.14.
 */
public class HttpObservationDistributer extends ObservationDistributer {
    private static final Logger log = LoggerFactory.getLogger(HttpObservationDistributer.class);
    private static final int MAX_CACHE_SIZE = 500;
    private static final int MAX_WAIT_PERIOD_MS = 60000;
    private static final  int THREAD_POOL_DEFAULT_SIZE = 10;

    List<ObservedMethod> observedMethods = new ArrayList<>();

    private ThreadPoolExecutor executor = null;

    public HttpObservationDistributer(String reporterHost, String reporterPort, String serviceName) {
        super(reporterHost, reporterPort, serviceName, MAX_WAIT_PERIOD_MS);
        log.info("Starting HttpObservationDistributer. reporterHost: {}, reporterPort {}, serviceName {}", reporterHost, reporterPort, serviceName);

        int threadPoolSize = THREAD_POOL_DEFAULT_SIZE;
        executor = new ThreadPoolExecutor(threadPoolSize,threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    }



    @Override
    public void run() {
        super.run();
    }

    protected void updateObservation(ObservedMethod observedMethod) {
        if (observedMethod != null) {
            //log.info("Observed {}", observedMethod.toString());
            observedMethods.add(observedMethod);
            if (observedMethods.size() >= MAX_CACHE_SIZE ||waitedLongEnough()) {
                forwardOutput();
                updateNextForwardAt();
            }
        } else {
            log.warn("Observed Method is null");
        }

    }

    @Override
    protected void updateObservation(Observation observation) {
        if (observation instanceof ObservedMethod) {
            updateObservation((ObservedMethod)observation);
        }

    }

    /**
     * This worker will call a Hystrix Command to forward the payload.
     */
    private void forwardOutput() {
        //Forward to Valuereporter via HTTP
        log.trace("Forwarding ObservedMethods. Local cache size {}", observedMethods.size());
//        HttpSender httpSender = new HttpSender(reporterHost, reporterPort, serviceName, observedMethods);
        if (executor.getActiveCount() < executor.getMaximumPoolSize()) {
//            executor.submit(httpSender);
            //Prepare for Hystrix
            CommandSender commandSender = new CommandSender(getReporterHost(),getReporterPort(),getServiceName(),observedMethods);
            executor.submit(commandSender);
        }else {
            log.info("No threads available for HttpSender. Will discard content {}", observedMethods.size());
        }
        observedMethods.clear();
    }


    public int getMaxCacheSize() {
        return MAX_CACHE_SIZE;
    }


}
