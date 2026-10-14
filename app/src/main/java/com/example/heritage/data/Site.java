package com.example.heritage.data;

import java.text.DateFormatSymbols;
import java.util.Objects;

/** A heritage site. Rows come from the "sites" table, seeded from {@link SiteSeed}. */
public class Site {

    public long id;
    public String name;
    public String city;
    public String state;
    public String category;
    public String built;
    public String about;
    public String timings;
    /** Weekly closed day as a {@link java.util.Calendar} DAY_OF_WEEK value, or 0 if open every day. */
    public int closedDay;
    public int feeIndian;
    public int feeForeign;
    public double lat;
    public double lng;
    public String wikiUrl;
    public String imageKey;

    public String location() {
        return city + ", " + state;
    }

    public int imageRes() {
        return SiteSeed.imageFor(imageKey);
    }

    /** "Fridays", "Mondays", or null when the site is open every day. */
    public String closedDayPlural() {
        if (closedDay == 0) return null;
        return new DateFormatSymbols().getWeekdays()[closedDay] + "s";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Site)) return false;
        Site s = (Site) o;
        return id == s.id && Objects.equals(name, s.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
