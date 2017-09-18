package org.valuereporter.client.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.activity.ObservedActivity;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ActivityRepository {
    private static final Logger log = LoggerFactory.getLogger(ActivityRepository.class);
    private static ActivityRepository instance = null;
    private static LinkedBlockingQueue<ObservedActivity> observedQueue;
    private ActivityRepository() {
        observedQueue = new LinkedBlockingQueue<>(10000);
    }

    /**
     * Should be used for testing only.
     * @param maxSize
     */
    protected ActivityRepository(int maxSize) {
        log.info("MonitorRepository initiated with max sixe of ObservedActivity {}", maxSize);
        observedQueue = new LinkedBlockingQueue<>(maxSize);
    }

    public static ActivityRepository getInstance(int maxSize) {
        if(instance == null) {
            instance = new ActivityRepository(maxSize);
        } else {
            log.warn("Tried set capacity of obeservedQueue to {}. This is not possible. Max capacity remains at {}", maxSize, observedQueue.size() + observedQueue.remainingCapacity());
        }
        return instance;
    }
    public static ActivityRepository getInstance() {
        if(instance == null) {
            instance = new ActivityRepository();
        }
        return instance;
    }

    public boolean observed(ObservedActivity observedActivity) {
        boolean isObserved = false;
        if (observedActivity != null) {
            try {
                log.trace("Add to observedQueue {}", observedActivity);
                isObserved = observedQueue.offer(observedActivity, 1, TimeUnit.MILLISECONDS);
                log.trace("Attempt to add {}, estimated totalSize {}, was added [{}]", observedActivity, observedQueue.size(), isObserved);
            } catch (InterruptedException e) {
                log.warn("Could not add observation {}",observedActivity, e);
            }
        }
        return isObserved;
    }

    /*
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
    */

    public boolean hasObservations() {
        boolean hasObservations = observedQueue.size() > 0;
        //log.debug("hasObservations {}", hasObservations);
        return hasObservations;
    }

    public ObservedActivity takeFirst() {
        try {
            ObservedActivity observedActivity = observedQueue.poll(1,TimeUnit.MILLISECONDS);
            //log.debug("takeFirst-observedMethod {}", observedMethod.toString());
            return observedActivity;
        } catch (InterruptedException e) {
            log.warn("Nothing to take {}", e.getMessage());
            return null;
        }
    }
}
