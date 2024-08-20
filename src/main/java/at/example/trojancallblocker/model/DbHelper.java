package at.example.trojancallblocker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    // Έκδοση της βάσης δεδομένων
    private static final int DB_VERSION = 1;

    // Δημιουργός κλάσης
    public DbHelper(Context context) {
        super(context, "database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία του πίνακα Number στην βάση δεδομένων
        db.execSQL("CREATE TABLE " + Number._TABLE + "(" +
                Number.NUMBER + " TEXT NOT NULL PRIMARY KEY," +
                Number.NAME + " TEXT NULL," +
                Number.LAST_CALL + " INTEGER NULL," +
                Number.TIMES_CALLED + " INTEGER NOT NULL DEFAULT 0" +
        ")");

        ContentValues values = new ContentValues();



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int from, int to) {
    }

}
