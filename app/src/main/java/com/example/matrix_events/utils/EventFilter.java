package com.example.matrix_events.utils;

import com.example.matrix_events.entities.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Utility class for filtering event lists based on search query, availability, and date
 */
public class EventFilter {

    // Nested class to hold the current query, availability and date filters
    public static class FilterCriteria {
        public String query = "";
        public String availability = "All"; // All is Default. Options: "All", "Filled", "Available"
        public Date date = null;
    }

    /**
     * Filters events according to the provided criteria
     *
     * @param allEvents  All events to consider
     * @param criteria   The current filter criteria (search, availability, date)
     * @return A new filtered list of events
     */
    public static List<Event> filterEvents(List<Event> allEvents, FilterCriteria criteria) {
        List<Event> result = new ArrayList<>();

        if (allEvents == null || allEvents.isEmpty()) return result;
        if (criteria == null) criteria = new FilterCriteria();

        // Goated Ternary Operations | S/O Rob Hackman
        String query = criteria.query != null ? criteria.query.trim().toLowerCase() : "";
        String availability = criteria.availability != null ? criteria.availability : "All";
        Date selectedDate = criteria.date;

        for (Event event: allEvents) {
            if (event == null) continue;

            // Search Filter
            boolean matchesSearch = true;
            if(!query.isEmpty()) {
                String title = event.getName() != null ? event.getName().toLowerCase() : "";
                String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                matchesSearch = title.contains(query) || desc.contains(query);
            }
            if (!matchesSearch) continue;

            // Availability Filter
            boolean matchesAvailability = true;
            if ("Filled".equalsIgnoreCase(availability)) {
                matchesAvailability = event.isWaitlistFull();
            } else if ("Available".equalsIgnoreCase(availability)) {
                matchesAvailability = !event.isWaitlistFull();
            }
            if (!matchesAvailability) continue;

            // Date filter
            boolean matchesDate = true;
            if (selectedDate != null) {
                Date eventDate = event.getEventStartDateTime() != null ? event.getEventStartDateTime().toDate() : null;
                matchesDate = eventDate != null && isSameDay(eventDate, selectedDate);
            }
            if (!matchesDate) continue;

            result.add(event);
        }
        return result;
    }

    private static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

/*
displayedEvents.clear();
displayedEvents.addAll(EventFilter.filterEvents(allEvents, criteria));
*/