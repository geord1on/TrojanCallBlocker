package at.example.trojancallblocker;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import at.example.trojancallblocker.model.DbHelper;
import at.example.trojancallblocker.model.Number;

public class EditNumberActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Number> {

    static final String EXTRA_NUMBER = "number";

    TextView tvName, tvNumber; // Δημιουργία TextView για το όνομα και τον αριθμό


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String intentNumber = getIntentNumber();// Λήψη του αριθμού από το Intent
        setTitle(intentNumber == null ? R.string.edit_add_number : R.string.edit_edit_number);
        setContentView(R.layout.activity_edit_number);

        tvName = (TextView)findViewById(R.id.name); // Ανάθεση του TextView για το όνομα
        tvNumber = (TextView)findViewById(R.id.number); // Ανάθεση του TextView για τον αριθμό

        if (intentNumber != null)
            getLoaderManager().initLoader(0, null, this); // Αν υπάρχει αριθμός, αρχικοποιεί το Loader για φόρτωση δεδομένων
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_number, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void onSave(MenuItem item) {
        if (validate()) { // Ελέγχει αν τα δεδομένα είναι έγκυρα
            ContentValues values = new ContentValues(2);
            values.put(Number.NAME, tvName.getText().toString());
            values.put(Number.NUMBER, Number.wildcardsViewToDb(tvNumber.getText().toString()));

            DbHelper dbHelper = new DbHelper(this);
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (getIntentNumber() != null)
                    db.update(Number._TABLE, values, Number.NUMBER + "=?", new String[] { getIntentNumber() });  // Ενημερώνει την εγγραφή αν υπάρχει ήδη
                else
                    db.insert(Number._TABLE, null, values);

                Toast.makeText(this, R.string.edit_changes_saved, Toast.LENGTH_SHORT).show(); // Εμφανίζει μήνυμα επιτυχίας

                finish();  // Κλείνει τη δραστηριότητα
            } finally {
                dbHelper.close(); // Κλείνει τη βάση δεδομένων
            }
        }
    }

    public void onCancel(MenuItem item) {
        finish(); // Κλείνει τη δραστηριότητα χωρίς αποθήκευση
    }

    @Override
    public void onBackPressed() {
        if (getIntentNumber() == null && TextUtils.isEmpty(tvName.getText()) && TextUtils.isEmpty(tvNumber.getText()))
            onCancel(null);
        else
            onSave(null);
    }


    protected String getIntentNumber() {
        return getIntent().getStringExtra(EXTRA_NUMBER);
    }

    protected boolean validate() {
        boolean ok = true;
        if (TextUtils.isEmpty(tvName.getText())) {
            tvName.setError(getString(R.string.edit_must_not_be_empty));
            ok = false;
        }
        if (TextUtils.isEmpty(tvNumber.getText())) {
            tvNumber.setError(getString(R.string.edit_must_not_be_empty));
            ok = false;
        }
        return ok;
    }


    @Override
    public Loader<Number> onCreateLoader(int i, Bundle bundle) {
        return new NumberLoader(this, getIntentNumber());
    }

    @Override
    public void onLoadFinished(Loader<Number> loader, Number number) {
        if (number != null) {
            tvName.setText(number.name);
            tvNumber.setText(Number.wildcardsDbToView(number.number));
        }
    }

    @Override
    public void onLoaderReset(Loader<Number> loader) {
    }


    protected static class NumberLoader extends AsyncTaskLoader<Number> {

        final String number;

        public NumberLoader(Context context, String number) {
            super(context);
            this.number = number;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Number loadInBackground() {
            DbHelper dbHelper = new DbHelper(getContext());
            Cursor c = null;
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                c = db.query(Number._TABLE, null, Number.NUMBER + "=?", new String[] { number }, null, null, null);
                if (c.moveToNext()) {
                    ContentValues values = new ContentValues(c.getColumnCount());
                    DatabaseUtils.cursorRowToContentValues(c, values); // Μετατρέπει τη γραμμή σε ContentValues
                    return Number.fromValues(values); // Επιστρέφει το αντικείμενο Number
                }
            } finally {
                if (c != null)
                    c.close();  // Κλείνει τον Cursor
                dbHelper.close(); // Κλείνει τη βάση δεδομένων
            }
            return null;
        }

    }

}
