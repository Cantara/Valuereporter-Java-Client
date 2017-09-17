package org.valuereporter.client;

import org.testng.annotations.Test;
import org.valuereporter.ObservedMethod;

import static org.testng.Assert.*;


/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class MonitorRepositoryTest {


    @Test
    public void testObservedMoreThanCapacity() throws Exception {
        MonitorRepository repository = new MonitorRepository(1);
        assertTrue(repository.observed("ok",System.currentTimeMillis()-2000, System.currentTimeMillis()));
        assertFalse(repository.observed("timeout",System.currentTimeMillis()-2000, System.currentTimeMillis()));
        ObservedMethod observedMethod = repository.takeFirst();
        assertNotNull(observedMethod);
        assertEquals(observedMethod.getName(), "ok");
    }

    @Test
    public void testHasObservations() throws Exception {
        MonitorRepository repository = MonitorRepository.getInstance(1);
        repository.observed("ok",System.currentTimeMillis()-2000, System.currentTimeMillis());
        ObservedMethod observedMethod = repository.takeFirst();
        assertNotNull(observedMethod);

    }
    @Test
    public void testNoMoreObservations() throws Exception {
        MonitorRepository repository = MonitorRepository.getInstance(1);
        repository.observed("ok",System.currentTimeMillis()-2000, System.currentTimeMillis());
        ObservedMethod observedMethod = repository.takeFirst();
        assertNotNull(observedMethod);
        assertNull(repository.takeFirst());

    }


    public static void main(String[] args) {
        MonitorRepository repository = MonitorRepository.getInstance();
        repository.observed("first", System.currentTimeMillis()-2000, System.currentTimeMillis());
        //repository.takeFirst();
        repository.observed("second", System.currentTimeMillis()-1000, System.currentTimeMillis());
        repository.observed("third", System.currentTimeMillis()-500, System.currentTimeMillis());
    }
}
