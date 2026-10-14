package com.example.heritage.data;

import java.util.Objects;

/** A planned or completed visit. Stored in the "trips" table. */
public class Trip {

    public static final String TICKET_INDIAN = "INDIAN";
    public static final String TICKET_FOREIGN = "FOREIGN";
    public static final String STATUS_PLANNED = "PLANNED";
    public static final String STATUS_VISITED = "VISITED";

    public long id;
    public long siteId;
    public String visitorName;
    /** Local midnight of the visit day, in millis. */
    public long visitDate;
    public int visitors;
    public String ticketType;
    public boolean guide;
    public boolean audio;
    public double total;
    public String status = STATUS_PLANNED;
    public float rating;

    // Joined from the sites table for display, not stored in trips.
    public String siteName;
    public String siteCity;
    public String siteImageKey;

    public boolean isVisited() {
        return STATUS_VISITED.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip)) return false;
        Trip t = (Trip) o;
        return id == t.id && siteId == t.siteId && visitDate == t.visitDate
                && visitors == t.visitors && guide == t.guide && audio == t.audio
                && Double.compare(total, t.total) == 0 && Float.compare(rating, t.rating) == 0
                && Objects.equals(visitorName, t.visitorName)
                && Objects.equals(ticketType, t.ticketType)
                && Objects.equals(status, t.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, visitDate, visitors, status, rating);
    }
}
