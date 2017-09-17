package org.valuereporter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valuereporter.activity.ObservedActivity;
import org.valuereporter.client.activity.ActivityRepository;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class MonitorReporter {
    private final static Logger log = LoggerFactory.getLogger(MonitorReporter.class);

    // called by instrumented methods
    public static void reportTime(String name, long startTimeMillis, long endTimeMillis) {

       MonitorRepository.getInstance().observed(name, startTimeMillis, endTimeMillis);
    }

    public static void reportActivity(ObservedActivity observedActivity) {
        ActivityRepository.getInstance().observed(observedActivity);
    }

    public static void startHttpReporter(String reporterHost, int reporterPort, String reporterPrefix) {
        log.info("Init Http reporter to host {}, port {}, prefix {}", reporterHost, reporterPort, reporterPrefix);
        log.warn("Not Implemented http reporter");
        MonitorRepository.getInstance();

    }
}
