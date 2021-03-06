package org.valuereporter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.ObservedMethod;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class MonitorRepository {
    private static final Logger log = LoggerFactory.getLogger(MonitorRepository.class);
    private static MonitorRepository instance = null;
    private static LinkedBlockingQueue<ObservedMethod> observedQueue;
    private MonitorRepository() {
        observedQueue = new LinkedBlockingQueue<>(10000);
    }

    /**
     * Should be used for testing only.
     * @param maxSize
     */
    protected MonitorRepository(int maxSize) {
        log.info("MonitorRepository initiated with max sixe of ObservedMethod {}", maxSize);
        observedQueue = new LinkedBlockingQueue<>(maxSize);
    }

    public static MonitorRepository getInstance(int maxSize) {
        if(instance == null) {
            instance = new MonitorRepository(maxSize);
        } else {
            log.warn("Tried set capacity of obeservedQueue to {}. This is not possible. Max capacity remains at {}", maxSize, observedQueue.size() + observedQueue.remainingCapacity());
        }
        return instance;
    }
    public static MonitorRepository getInstance() {
        if(instance == null) {
            instance = new MonitorRepository();
        }
        return instance;
    }
    public boolean observed(String name, long startTimeMillis, long endTimeMillis) {
        boolean isObserved = false;
        if (name != null) {
            try {
                log.trace("Add to observedQueue {}", name);
                isObserved = observedQueue.offer(new ObservedMethod(name, startTimeMillis,endTimeMillis), 1, TimeUnit.MILLISECONDS);
                log.trace("Attempt to add {}, estimated totalSize {}, was added [{}]", name, observedQueue.size(), isObserved);
            } catch (InterruptedException e) {
                log.warn("Could not add observation Name {}, startTime {}, endTime {}",name, startTimeMillis, endTimeMillis, e);
            }
        }
        return isObserved;
    }

    public boolean hasObservations() {
        boolean hasObservations = observedQueue.size() > 0;
        //log.debug("hasObservations {}", hasObservations);
        return hasObservations;
    }

    public ObservedMethod takeFirst() {
        try {
            ObservedMethod observedMethod = observedQueue.poll(1,TimeUnit.MILLISECONDS);
            //log.debug("takeFirst-observedMethod {}", observedMethod.toString());
            return observedMethod;
        } catch (InterruptedException e) {
            log.warn("Nothing to take {}", e.getMessage());
            return null;
        }
    }
}
