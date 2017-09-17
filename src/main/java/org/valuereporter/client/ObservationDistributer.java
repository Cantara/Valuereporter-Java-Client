package org.valuereporter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public abstract class ObservationDistributer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ObservationDistributer.class);
    private static final long DEFAULT_SLEEP = 1000;

    private static MonitorRepository monitorRepository;
    public String prefix = "PREFIX-NOT-SET";
    private final long sleepPeriod;

    public ObservationDistributer() {
         monitorRepository = MonitorRepository.getInstance();
        sleepPeriod = DEFAULT_SLEEP;
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

    protected abstract void updateObservation(ObservedMethod observedMethod);
}
