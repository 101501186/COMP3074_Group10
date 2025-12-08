package ca.gbc.foodspot.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ca.gbc.foodspot.model.Restaurant;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "foodspot.db";
    private static final int DB_VERSION = 4;

    public static final String TABLE_RESTAURANTS = "restaurants";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PHONE = "phone";
    public static final String COL_RATING = "rating";
    public static final String COL_PRICE = "price";
    public static final String COL_DISTANCE = "distance";
    public static final String COL_TAGS = "tags";
    public static final String COL_DESC = "description";
    public static final String COL_FAVORITE = "favorite";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    private static DbHelper instance;

    public static synchronized DbHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DbHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_RESTAURANTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_ADDRESS + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_RATING + " REAL, " +
                COL_PRICE + " TEXT, " +
                COL_DISTANCE + " TEXT, " +
                COL_TAGS + " TEXT, " +
                COL_DESC + " TEXT, " +
                COL_FAVORITE + " INTEGER DEFAULT 0, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL" +
                ")";
        db.execSQL(sql);

        seedSampleData(db);
    }

    private void seedSampleData(SQLiteDatabase db) {
        insertSeed(db, "The Golden Spoon", "123 Main Street, Toronto, ON",
                "(416) 555-0123", 4.8f, "$$$", "750 m away",
                "Italian, Fine Dining, Romantic",
                "Experience authentic Italian cuisine in an elegant atmosphere. Perfect spot for date nights and celebrations.",
                1, 43.6532, -79.3832);

        insertSeed(db, "Sunset Bistro", "89 Lakeshore Blvd, Toronto, ON",
                "(416) 555-0177", 4.5f, "$$", "1.2 km away",
                "Bistro, Brunch, Patio",
                "Casual bistro with brunch specials, lakeside views, and a relaxed patio.",
                1, 43.6387, -79.3817);

        insertSeed(db, "Spice Route", "716 King Street, Toronto, ON",
                "(416) 555-0199", 4.6f, "$$", "2.1 km away",
                "Indian, Vegetarian, Takeout",
                "Modern Indian restaurant offering tandoori dishes, curries, and vegetarian options.",
                0, 43.6440, -79.3957);

        insertSeed(db, "Ocean Breeze", "45 Harbour St, Toronto, ON",
                "(416) 555-0210", 4.3f, "$$", "2.5 km away",
                "Seafood, Family, Casual",
                "Seafood-focused menu with daily catches and family-friendly seating.",
                0, 43.6407, -79.3802);
    }

    private void insertSeed(SQLiteDatabase db,
                            String name, String address, String phone,
                            float rating, String price, String distance,
                            String tags, String desc, int fav,
                            double lat, double lng) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ADDRESS, address);
        cv.put(COL_PHONE, phone);
        cv.put(COL_RATING, rating);
        cv.put(COL_PRICE, price);
        cv.put(COL_DISTANCE, distance);
        cv.put(COL_TAGS, tags);
        cv.put(COL_DESC, desc);
        cv.put(COL_FAVORITE, fav);
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);
        db.insert(TABLE_RESTAURANTS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANTS);
        onCreate(db);
    }

    public long insertRestaurant(Restaurant r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toValues(r);
        return db.insert(TABLE_RESTAURANTS, null, cv);
    }

    public void updateRestaurant(Restaurant r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toValues(r);
        db.update(TABLE_RESTAURANTS, cv, COL_ID + "=?",
                new String[]{String.valueOf(r.getId())});
    }

    public void deleteRestaurant(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RESTAURANTS, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Restaurant r) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, r.getName());
        cv.put(COL_ADDRESS, r.getAddress());
        cv.put(COL_PHONE, r.getPhone());
        cv.put(COL_RATING, r.getRating());
        cv.put(COL_PRICE, r.getPriceLevel());
        cv.put(COL_DISTANCE, r.getDistanceText());
        cv.put(COL_TAGS, r.getTags());
        cv.put(COL_DESC, r.getDescription());
        cv.put(COL_FAVORITE, r.isFavorite() ? 1 : 0);
        cv.put(COL_LATITUDE, r.getLatitude());
        cv.put(COL_LONGITUDE, r.getLongitude());
        return cv;
    }

    public Restaurant getRestaurantById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RESTAURANTS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Restaurant r = null;
        if (c != null) {
            if (c.moveToFirst()) {
                r = cursorToRestaurant(c);
            }
            c.close();
        }
        return r;
    }

    public List<Restaurant> getRestaurants(boolean favoritesOnly,
                                           boolean reviewsOnly,
                                           String query) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> args = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (favoritesOnly) {
            where.append(COL_FAVORITE).append("=1");
        }
        if (reviewsOnly) {
            if (where.length() > 0) where.append(" AND ");
            where.append(COL_RATING).append(">0");
        }
        if (query != null && !query.isEmpty()) {
            String like = "%" + query + "%";
            if (where.length() > 0) where.append(" AND ");
            where.append("(")
                    .append(COL_NAME).append(" LIKE ? OR ")
                    .append(COL_TAGS).append(" LIKE ?")
                    .append(")");
            args.add(like);
            args.add(like);
        }

        String selection = where.length() == 0 ? null : where.toString();
        String[] selectionArgs = args.isEmpty()
                ? null
                : args.toArray(new String[0]);

        Cursor c = db.query(TABLE_RESTAURANTS, null, selection,
                selectionArgs, null, null, COL_NAME + " ASC");

        List<Restaurant> list = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToRestaurant(c));
            }
            c.close();
        }
        return list;
    }

    private Restaurant cursorToRestaurant(Cursor c) {
        Restaurant r = new Restaurant();
        r.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        r.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        r.setAddress(c.getString(c.getColumnIndexOrThrow(COL_ADDRESS)));
        r.setPhone(c.getString(c.getColumnIndexOrThrow(COL_PHONE)));
        r.setRating(c.getFloat(c.getColumnIndexOrThrow(COL_RATING)));
        r.setPriceLevel(c.getString(c.getColumnIndexOrThrow(COL_PRICE)));
        r.setDistanceText(c.getString(c.getColumnIndexOrThrow(COL_DISTANCE)));
        r.setTags(c.getString(c.getColumnIndexOrThrow(COL_TAGS)));
        r.setDescription(c.getString(c.getColumnIndexOrThrow(COL_DESC)));
        r.setFavorite(c.getInt(c.getColumnIndexOrThrow(COL_FAVORITE)) == 1);
        r.setLatitude(c.getDouble(c.getColumnIndexOrThrow(COL_LATITUDE)));
        r.setLongitude(c.getDouble(c.getColumnIndexOrThrow(COL_LONGITUDE)));
        return r;
    }
}
