package com.strands.interviews.eventsystem;

import com.strands.interviews.eventsystem.events.SimpleEvent;
import com.strands.interviews.eventsystem.events.SubEvent;
import com.strands.interviews.eventsystem.impl.DefaultEventManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultEventManagerTest
{
    public static final String SOME_KEY = "some.key";
    public static final String ANOTHER_KEY = "another.key";
    private EventManager eventManager = new DefaultEventManager();

    @Test
    public void testPublishNullEvent()
    {
        eventManager.publishEvent(null);
    }

    @Test
    public void testRegisterListenerAndPublishEvent()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        eventManager.registerListener(SOME_KEY, eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertTrue(eventListenerMock.isCalled());
    }

    @Test
    public void testListenerWithoutMatchingEventClass()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SubEvent.class});
        eventManager.registerListener(SOME_KEY, eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
    }

    @Test
    public void testUnregisterListener()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener(SOME_KEY, eventListenerMock);
        eventManager.registerListener(ANOTHER_KEY, eventListenerMock2);
        eventManager.unregisterListener(SOME_KEY);

        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
        assertTrue(eventListenerMock2.isCalled());
    }

    @Test
    public void testWhenSubEventThatOnlySubEventListenerIsCalled()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock subEventListenerMock  = new EventListenerMock(new Class[]{SubEvent.class});

        eventManager.registerListener(SOME_KEY, eventListenerMock);
        eventManager.registerListener(ANOTHER_KEY, subEventListenerMock );

        eventManager.publishEvent(new SubEvent(this));
        assertFalse(eventListenerMock.isCalled());
        assertTrue(subEventListenerMock .isCalled());
    }

    /**
     * Check that registering and unregistering listeners behaves properly.
     */
    @Test
    public void testRemoveNonexistentListener()
    {
        DefaultEventManager dem = (DefaultEventManager)eventManager;
        assertEquals(0, dem.getListeners().size());
        eventManager.registerListener(SOME_KEY, new EventListenerMock(new Class[]{SimpleEvent.class}));
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener("this.key.is.not.registered");
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener(SOME_KEY);
        assertEquals(0, dem.getListeners().size());
    }

    /**
     * Registering duplicate keys on different listeners should only fire the most recently added.
     */
    @Test
    public void testDuplicateKeysForListeners()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener(SOME_KEY, eventListenerMock);
        eventManager.registerListener(SOME_KEY, eventListenerMock2);

        eventManager.publishEvent(new SimpleEvent(this));

        assertTrue(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());

        eventListenerMock.resetCalled();
        eventListenerMock2.resetCalled();

        eventManager.unregisterListener(SOME_KEY);
        eventManager.publishEvent(new SimpleEvent(this));

        assertFalse(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());
    }

    /**
     * Attempting to register a null with a valid key should result in an illegal argument exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddValidKeyWithNullListener()
    {
        eventManager.registerListener("bogus.key", null);
    }
}
