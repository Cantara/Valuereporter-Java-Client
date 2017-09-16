package org.valuereporter.client.activity;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.valuereporter.activity.ObservedActivity;

import static org.testng.Assert.*;
import static org.valuereporter.activity.ObservedActivity.Builder.observe;

/**
 * Created by baardl on 16.09.17.
 */
public class ActivityRepositoryTest {
    private ActivityRepository activityRepository;
    private ObservedActivity observedActivity;
    @BeforeMethod
    public void setUp() throws Exception {
        activityRepository = new ActivityRepository(100);
        observedActivity = observe("testActivity").build();
    }

    @Test
    public void testObserved() throws Exception {
        boolean isObserved = activityRepository.observed(observedActivity);
        assertTrue(isObserved);
    }

    @Test
    public void testHasObservations() throws Exception {
        activityRepository.observed(observedActivity);
        assertTrue(activityRepository.hasObservations());
    }

    @Test
    public void testTakeFirst() throws Exception {
        activityRepository.observed(observedActivity);
        ObservedActivity fromQueueActvity = activityRepository.takeFirst();
        assertNotNull(fromQueueActvity);
        assertEquals(fromQueueActvity.getActivityName(),observedActivity.getActivityName());
    }

}