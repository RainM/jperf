package ru.ms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sergey on 03.12.16.
 */
public class Profile {
    private final Map<String, EventProfile> profiles = new HashMap<>();

    public Set<String> availableEvents() {
        return profiles.keySet();
    }

    public EventProfile getEventProfile(String event) {
        EventProfile result = profiles.get(event);
        if (result == null) {
            result = new EventProfile();
            profiles.put(event, result);
        }
        return result;
    }

    public void addEventProfile(EventProfile eventProfile, String eventName) {
        profiles.put(eventName, eventProfile);
    }

    public static class EventProfile {
        Map<Long, Long> profile = new HashMap<>();

        public long getCounter(long addr) {
            return profile.getOrDefault(addr, 0l);
        }

        public void addCounter(long addr, long counter) {
            long old = getCounter(addr);
            profile.put(addr, counter);
        }
    }
}
