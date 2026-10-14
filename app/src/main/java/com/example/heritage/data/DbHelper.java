package com.example.heritage.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database with two tables:
 * sites (read only, seeded on first launch) and trips (full CRUD).
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "virasat.db";
    private static final int DB_VERSION = 1;

    private static final String T_SITES = "sites";
    private static final String T_TRIPS = "trips";

    private static DbHelper instance;

    public static synchronized DbHelper get(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_SITES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "city TEXT NOT NULL,"
                + "state TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "built TEXT,"
                + "about TEXT,"
                + "timings TEXT,"
                + "closed_day INTEGER NOT NULL DEFAULT 0,"
                + "fee_indian INTEGER NOT NULL,"
                + "fee_foreign INTEGER NOT NULL,"
                + "lat REAL,"
                + "lng REAL,"
                + "wiki_url TEXT,"
                + "image_key TEXT)");

        db.execSQL("CREATE TABLE " + T_TRIPS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "site_id INTEGER NOT NULL REFERENCES " + T_SITES + "(id),"
                + "visitor_name TEXT NOT NULL,"
                + "visit_date INTEGER NOT NULL,"
                + "visitors INTEGER NOT NULL,"
                + "ticket_type TEXT NOT NULL,"
                + "guide INTEGER NOT NULL DEFAULT 0,"
                + "audio INTEGER NOT NULL DEFAULT 0,"
                + "total REAL NOT NULL,"
                + "status TEXT NOT NULL DEFAULT '" + Trip.STATUS_PLANNED + "',"
                + "rating REAL NOT NULL DEFAULT 0)");

        for (Site s : SiteSeed.all()) {
            ContentValues v = new ContentValues();
            v.put("name", s.name);
            v.put("city", s.city);
            v.put("state", s.state);
            v.put("category", s.category);
            v.put("built", s.built);
            v.put("about", s.about);
            v.put("timings", s.timings);
            v.put("closed_day", s.closedDay);
            v.put("fee_indian", s.feeIndian);
            v.put("fee_foreign", s.feeForeign);
            v.put("lat", s.lat);
            v.put("lng", s.lng);
            v.put("wiki_url", s.wikiUrl);
            v.put("image_key", s.imageKey);
            db.insert(T_SITES, null, v);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + T_SITES);
        onCreate(db);
    }

    // ---------- Sites ----------

    /** @param query text to match against name, city or state; @param category null for all */
    public List<Site> getSites(String query, String category) {
        StringBuilder where = new StringBuilder("(name LIKE ? OR city LIKE ? OR state LIKE ?)");
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        List<String> args = new ArrayList<>();
        args.add(like);
        args.add(like);
        args.add(like);
        if (category != null) {
            where.append(" AND category = ?");
            args.add(category);
        }

        List<Site> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_SITES, null, where.toString(),
                args.toArray(new String[0]), null, null, "name")) {
            while (c.moveToNext()) list.add(readSite(c));
        }
        return list;
    }

    public Site getSite(long id) {
        try (Cursor c = getReadableDatabase().query(T_SITES, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readSite(c) : null;
        }
    }

    private Site readSite(Cursor c) {
        Site s = new Site();
        s.id = c.getLong(c.getColumnIndexOrThrow("id"));
        s.name = c.getString(c.getColumnIndexOrThrow("name"));
        s.city = c.getString(c.getColumnIndexOrThrow("city"));
        s.state = c.getString(c.getColumnIndexOrThrow("state"));
        s.category = c.getString(c.getColumnIndexOrThrow("category"));
        s.built = c.getString(c.getColumnIndexOrThrow("built"));
        s.about = c.getString(c.getColumnIndexOrThrow("about"));
        s.timings = c.getString(c.getColumnIndexOrThrow("timings"));
        s.closedDay = c.getInt(c.getColumnIndexOrThrow("closed_day"));
        s.feeIndian = c.getInt(c.getColumnIndexOrThrow("fee_indian"));
        s.feeForeign = c.getInt(c.getColumnIndexOrThrow("fee_foreign"));
        s.lat = c.getDouble(c.getColumnIndexOrThrow("lat"));
        s.lng = c.getDouble(c.getColumnIndexOrThrow("lng"));
        s.wikiUrl = c.getString(c.getColumnIndexOrThrow("wiki_url"));
        s.imageKey = c.getString(c.getColumnIndexOrThrow("image_key"));
        return s;
    }

    // ---------- Trips: Create, Read, Update, Delete ----------

    public long insertTrip(Trip t) {
        ContentValues v = tripValues(t);
        // id is set when restoring a deleted trip (undo), so it keeps its original row id
        if (t.id > 0) v.put("id", t.id);
        return getWritableDatabase().insert(T_TRIPS, null, v);
    }

    public int updateTrip(Trip t) {
        return getWritableDatabase().update(T_TRIPS, tripValues(t), "id = ?",
                new String[]{String.valueOf(t.id)});
    }

    public int deleteTrip(long id) {
        return getWritableDatabase().delete(T_TRIPS, "id = ?", new String[]{String.valueOf(id)});
    }

    public void markVisited(long id, float rating) {
        ContentValues v = new ContentValues();
        v.put("status", Trip.STATUS_VISITED);
        v.put("rating", rating);
        getWritableDatabase().update(T_TRIPS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public Trip getTrip(long id) {
        List<Trip> list = queryTrips("t.id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    /** @param status null for all trips, or Trip.STATUS_PLANNED / STATUS_VISITED */
    public List<Trip> getTrips(String status) {
        if (status == null) return queryTrips(null, null);
        return queryTrips("t.status = ?", new String[]{status});
    }

    public int countTrips(String status) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + T_TRIPS + " WHERE status = ?", new String[]{status})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    private List<Trip> queryTrips(String where, String[] args) {
        String sql = "SELECT t.*, s.name AS site_name, s.city AS site_city, s.image_key AS site_image"
                + " FROM " + T_TRIPS + " t JOIN " + T_SITES + " s ON s.id = t.site_id"
                + (where != null ? " WHERE " + where : "")
                + " ORDER BY t.status = '" + Trip.STATUS_VISITED + "', t.visit_date";
        List<Trip> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Trip t = new Trip();
                t.id = c.getLong(c.getColumnIndexOrThrow("id"));
                t.siteId = c.getLong(c.getColumnIndexOrThrow("site_id"));
                t.visitorName = c.getString(c.getColumnIndexOrThrow("visitor_name"));
                t.visitDate = c.getLong(c.getColumnIndexOrThrow("visit_date"));
                t.visitors = c.getInt(c.getColumnIndexOrThrow("visitors"));
                t.ticketType = c.getString(c.getColumnIndexOrThrow("ticket_type"));
                t.guide = c.getInt(c.getColumnIndexOrThrow("guide")) == 1;
                t.audio = c.getInt(c.getColumnIndexOrThrow("audio")) == 1;
                t.total = c.getDouble(c.getColumnIndexOrThrow("total"));
                t.status = c.getString(c.getColumnIndexOrThrow("status"));
                t.rating = c.getFloat(c.getColumnIndexOrThrow("rating"));
                t.siteName = c.getString(c.getColumnIndexOrThrow("site_name"));
                t.siteCity = c.getString(c.getColumnIndexOrThrow("site_city"));
                t.siteImageKey = c.getString(c.getColumnIndexOrThrow("site_image"));
                list.add(t);
            }
        }
        return list;
    }

    private ContentValues tripValues(Trip t) {
        ContentValues v = new ContentValues();
        v.put("site_id", t.siteId);
        v.put("visitor_name", t.visitorName);
        v.put("visit_date", t.visitDate);
        v.put("visitors", t.visitors);
        v.put("ticket_type", t.ticketType);
        v.put("guide", t.guide ? 1 : 0);
        v.put("audio", t.audio ? 1 : 0);
        v.put("total", t.total);
        v.put("status", t.status);
        v.put("rating", t.rating);
        return v;
    }
}
