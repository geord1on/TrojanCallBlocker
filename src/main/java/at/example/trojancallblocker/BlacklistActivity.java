package at.example.trojancallblocker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.example.trojancallblocker.model.BlacklistFile;
import at.example.trojancallblocker.model.DbHelper;
import at.example.trojancallblocker.model.Number;

public class BlacklistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Set<Number>>, AdapterView.OnItemClickListener {

    // Δήλωση μεταβλητών για ρυθμίσεις και διεπαφή χρήστη
    protected Settings settings;
    CoordinatorLayout coordinatorLayout;

    ListView list;
    ArrayAdapter<Number> adapter;


    // Δήλωση μεταβλητών για διαχείριση αρχείων
    protected String[] fileList;
    protected static final File basePath = Environment.getExternalStorageDirectory();
    protected static final int DIALOG_LOAD_FILE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        // Αρχικοποίηση ρυθμίσεων και διεπαφής χρήστη
        settings = new Settings(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        list = (ListView)findViewById(R.id.numbers);
        list.setAdapter(adapter = new NumberAdapter(this));
        list.setOnItemClickListener(this);

        // Ενεργοποίηση λειτουργίας πολλαπλής επιλογής
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                // Κενή μέθοδος για διαχείριση αλλαγών στην επιλογή στοιχείων

            }


            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Εμφάνιση μενού διαγραφής
                getMenuInflater().inflate(R.menu.blacklist_delete_numbers, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                // Διαγραφή επιλεγμένων αριθμών όταν πατηθεί η επιλογή διαγραφής

                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        deleteSelectedNumbers();
                        actionMode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                // Κενή μέθοδος για καθαρισμό μετά την έξοδο από τη λειτουργία πολλαπλής επιλογής
            }

        });

        // Ζήτηση αδειών χρήσης
        requestPermissions();

        // Αρχικοποίηση του loader για τη φόρτωση των αριθμών της λίστας αποκλεισμού
        getLoaderManager().initLoader(0, null, this);
    }

    protected void requestPermissions() {
        // Λίστα απαιτούμενων αδειών
        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.CALL_PHONE);
        requiredPermissions.add(Manifest.permission.READ_PHONE_STATE);
        requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredPermissions.add(Manifest.permission.READ_CONTACTS);

        // Προσθήκη αδειών ανάλογα με την έκδοση του Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            requiredPermissions.add(Manifest.permission.READ_CALL_LOG);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }

        // Έλεγχος για ελλιπείς άδειες
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        // Ζήτηση ελλιπών αδειών
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[0]), 0);
        }
    }

    protected void deleteSelectedNumbers() {
        // Λήψη των επιλεγμένων αριθμών για διαγραφή
        final List<String> numbers = new LinkedList<>();

        SparseBooleanArray checked = list.getCheckedItemPositions();
        for (int i = checked.size() - 1; i >= 0; i--)
            if (checked.valueAt(i)) {
                int position = checked.keyAt(i);
                numbers.add(adapter.getItem(position).number);
            }

        // Εκτέλεση διαγραφής σε background thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbHelper dbHelper = new DbHelper(BlacklistActivity.this);
                try {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    for (String number : numbers)
                        db.delete(Number._TABLE, Number.NUMBER + "=?", new String[] { number });
                } finally {
                    dbHelper.close();
                }

                // Επανεκκίνηση του loader μετά τη διαγραφή
                getLoaderManager().restartLoader(0, null, BlacklistActivity.this);
                return null;
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Έλεγχος αν οι άδειες χορηγήθηκαν
        boolean ok = true;
        if (grantResults.length != 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    ok = false;
                    break;
                }
            }
        } else {
            // Αν ακυρώθηκε η ζήτηση αδειών, θεωρείται αποτυχία
            ok = false;
        }

        // Εμφάνιση μηνύματος εάν δεν δόθηκαν οι άδειες
        if (!ok)
            Snackbar.make(coordinatorLayout, R.string.blacklist_permissions_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.blacklist_request_permissions, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions();
                        }
                    })
                    .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Εμφάνιση του μενού επιλογών
        getMenuInflater().inflate(R.menu.activity_blacklist2, menu);
        return true;
    }



    // Εμφάνιση διαλόγου για εισαγωγή λίστας αποκλεισμού
    public void onImportBlacklist(MenuItem item) {
        showDialog(DIALOG_LOAD_FILE);
    }

    public Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
        case DIALOG_LOAD_FILE:

            // Φιλτράρισμα αρχείων για την εισαγωγή λίστας αποκλεισμού
            final int lastDot = BlacklistFile.DEFAULT_FILENAME.lastIndexOf(".");
            final String ext = BlacklistFile.DEFAULT_FILENAME.substring(lastDot);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(ext);
                }
            };

            fileList = basePath.list(filter);

            builder.setTitle(R.string.blacklist_import);
            builder.setItems(fileList, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    commitBlacklist(new BlacklistFile(basePath, fileList[which]));
                }
            });
            break;
        }

        dialog = builder.show();
        return dialog;
    }

    public void commitBlacklist(@NonNull BlacklistFile blacklist) {
        // Εισαγωγή της λίστας αποκλεισμού στη βάση δεδομένων
        DbHelper dbHelper = new DbHelper(BlacklistActivity.this);
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values;
            Boolean exists;
            for (Number number : blacklist.load()) {

                values = new ContentValues(4);
                values.put(Number.NAME, number.name);
                values.put(Number.NUMBER, Number.wildcardsViewToDb(number.number));

                exists = db.query(Number._TABLE, null, Number.NUMBER + "=?", new String[]{number.number}, null, null, null).moveToNext();
                if (exists)
                    db.update(Number._TABLE, values, Number.NUMBER + "=?", new String[]{number.number});
                else
                    db.insert(Number._TABLE, null, values);
            }
        } finally {
            dbHelper.close();
        }

        // Επανεκκίνηση του loader μετά την εισαγωγή
        getLoaderManager().restartLoader(0, null, BlacklistActivity.this);
        return;
    }

    public void onExportBlacklist(MenuItem item) {
        // Εξαγωγή της λίστας αποκλεισμού σε αρχείο
        BlacklistFile f = new BlacklistFile(basePath, BlacklistFile.DEFAULT_FILENAME);

        List<Number> numbers = new LinkedList<>();
        for (int i = 0; i < adapter.getCount(); i++)
            numbers.add(adapter.getItem(i));

        f.store(numbers, this);

        Toast.makeText(
                getApplicationContext(),
                getResources().getText(R.string.blacklist_exported_to) + " " + BlacklistFile.DEFAULT_FILENAME,
                Toast.LENGTH_LONG
        ).show();
    }


    public void addNumber(View view) {
        // Εναρξη του Activity για προσθήκη νέου αριθμού
        startActivity(new Intent(this, EditNumberActivity.class));
    }


    @Override
    public Loader<Set<Number>> onCreateLoader(int i, Bundle bundle) {
        // Δημιουργία νέου loader για τη φόρτωση των αριθμών
        return new NumberLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Set<Number>> loader, Set<Number> numbers) {
        // Εκκαθάριση και ενημέρωση του adapter με τους αριθμούς
        adapter.clear();
        adapter.addAll(numbers);
    }

    @Override
    public void onLoaderReset(Loader<Set<Number>> loader) {
        // Εκκαθάριση του adapter
        adapter.clear();
    }

    public void onAbooutus(MenuItem item) {
        //sxetika me to app
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }


    private static class NumberAdapter extends ArrayAdapter<Number> {
        // Προσαρμοστής για την εμφάνιση των αριθμών στη λίστα

        public NumberAdapter(Context context) {
            super(context, R.layout.blacklist_item);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = View.inflate(getContext(), R.layout.blacklist_item, null);

            Number number = getItem(position);

            TextView tv = (TextView)view.findViewById(R.id.number);
            tv.setText(Number.wildcardsDbToView(number.number));

            tv = (TextView)view.findViewById(R.id.name);
            tv.setText(number.name);

            tv = (TextView)view.findViewById(R.id.stats);
            if (number.lastCall != null) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(getContext().getResources().getQuantityString(R.plurals.blacklist_call_details, number.timesCalled,
                        number.timesCalled, SimpleDateFormat.getDateTimeInstance().format(new Date(number.lastCall))));
            } else
                tv.setVisibility(View.GONE);

            return view;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        // Εκκίνηση του Activity για επεξεργασία του αριθμού όταν γίνει κλικ σε έναν αριθμό
        Number number = adapter.getItem(position);

        Intent intent = new Intent(this, EditNumberActivity.class);
        intent.putExtra(EditNumberActivity.EXTRA_NUMBER, number.number);
        startActivity(intent);
    }


    protected static class NumberLoader extends AsyncTaskLoader<Set<Number>> implements BlacklistObserver.Observer {

        // Loader για τη φόρτωση των αριθμών από τη βάση δεδομένων
        public NumberLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {

            // Προσθήκη παρατηρητή για ενημερώσεις της λίστας αποκλεισμού
            BlacklistObserver.addObserver(this, true);
        }

        // Προσθήκη παρατηρητή για ενημερώσεις της λίστας αποκλεισμού
        @Override
        public Set<Number> loadInBackground() {
            // Φόρτωση των αριθμών από τη βάση δεδομένων
            DbHelper dbHelper = new DbHelper(getContext());
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Set<Number> numbers = new LinkedHashSet<>();
                Cursor c = db.query(Number._TABLE, null, null, null, null, null, Number.NUMBER);
                while (c.moveToNext()) {
                    ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(c, values);
                    numbers.add(Number.fromValues(values));
                }
                c.close();

                return numbers;
            } finally {
                dbHelper.close();
            }
        }

        @Override
        public void onBlacklistUpdate() {
            // Επαναφόρτωση της λίστας όταν γίνει ενημέρωση
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            // Αφαίρεση του παρατηρητή όταν σταματήσει το loading
            BlacklistObserver.removeObserver(this);
        }

    }

}
