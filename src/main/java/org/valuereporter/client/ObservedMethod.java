package org.valuereporter.client;

/**
 * This class will represent each of the methods the instrumentation has detected as beeing used..
 *
 * These methods will be forwarded to the Valuereporter analyzer, as a method that has been be used.
 *
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ObservedMethod {
    //The name is the identification of a method. Typically the name is the full name, including class, and package.
    private final String name;
    private final long startTime;
    private final long endTime;

    public ObservedMethod(String name, long startTime, long endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "ObservedMethod{" +
                "name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

}
