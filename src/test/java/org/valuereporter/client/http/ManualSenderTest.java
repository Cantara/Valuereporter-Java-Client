package org.valuereporter.client.http;

import org.slf4j.Logger;
import org.valuereporter.ObservedMethod;
import org.valuereporter.http.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 31.07.17.
 */
public class ManualSenderTest {
    private static final Logger log = getLogger(ManualSenderTest.class);
    private static String host = "localhost";
    private static String port = "4901";
    private static String serviceName = "manual-test";

    //Manual testing
    public static void main(String[] args) {

        List<ObservedMethod> observedMethods = new ArrayList<>();
        CommandSender commandSender = new CommandSender(host, port, serviceName, observedMethods);

    }

}