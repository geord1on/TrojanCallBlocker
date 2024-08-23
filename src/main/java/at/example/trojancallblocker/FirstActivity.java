package at.example.trojancallblocker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import at.example.trojancallblocker.model.BlacklistFile;
import at.example.trojancallblocker.model.BlockingModes;
import at.example.trojancallblocker.model.Number;

public class FirstActivity extends AppCompatActivity {

    // Μεταβλητή που ελέγχει αν το μπλοκάρισμα είναι ενεργοποιημένο ή όχι
    private boolean isOn = false;

    // SharedPreferences για αποθήκευση των προτιμήσεων της εφαρμογής
    private SharedPreferences sharedPreferences;

    // Όνομα αρχείου προτιμήσεων
    private static final String PREFS_NAME = "CallBlockerPrefs";

    // Κλειδί για την αποθήκευση της κατάστασης του μπλοκαρίσματος
    private static final String BLOCKING_ENABLED_KEY = "BlockingEnabled";

    private static final String BLOCK_HIDDEN_NUMBERS_KEY = "BlockHiddenNumbers"; // Νέο κλειδί για τον αποκλεισμό απόκρυψης αριθμών

    private Settings settings; // Ρυθμίσεις της εφαρμογής

    private static final int DIALOG_LOAD_FILE = 1; // Κωδικός για το διάλογο επιλογής αρχείου

    private static final int PERMISSION_REQUEST_CODE = 100; // Κωδικός για την αίτηση άδειας χρήσης

    private String basePath; // Διαδρομή βασικού καταλόγου για αρχεία της εφαρμογής

    // Adapter για την εμφάνιση αριθμών στη λίστα
    private ArrayAdapter<Number> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // Αρχικοποίηση των ρυθμίσεων
        settings = new Settings(this);
        basePath = getFilesDir().getPath();

        // Αναφορά στα UI στοιχεία
        final Button toggleButton = findViewById(R.id.toggleButton);
        TextView instructionsText = findViewById(R.id.instructionsText);
        Button openMainButton = findViewById(R.id.openMainButton);

        instructionsText.setSelected(true); // Enable marquee effect

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isOn = sharedPreferences.getBoolean(BLOCKING_ENABLED_KEY, false);
        // Ενημέρωση της εμφάνισης του κουμπιού με βάση την κατάσταση μπλοκαρίσματος
        updateButton(toggleButton);

        // Αρχικοποίηση του adapter για εμφάνιση αριθμών σε λίστα
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new LinkedList<Number>());

        // Αποκατάσταση της κατάστασης αποκλεισμού απόκρυψης αριθμών κατά την εκκίνηση
        boolean blockHiddenNumbers = sharedPreferences.getBoolean(BLOCK_HIDDEN_NUMBERS_KEY, false);
        if (blockHiddenNumbers) {
            blockHiddenNumbers(true); // Ενεργοποίηση της λειτουργίας αν ήταν ενεργή
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOn = !isOn;  // Αλλάζει την κατάσταση του μπλοκαρίσματος (ενεργοποιημένο/απενεργοποιημένο)
                SharedPreferences.Editor editor = sharedPreferences.edit(); // Αποθήκευση της νέας κατάστασης στο SharedPreferences
                editor.putBoolean(BLOCKING_ENABLED_KEY, isOn);
                editor.apply();
                updateButton(toggleButton); // Ενημέρωση του κουμπιού για τη νέα κατάσταση μπλοκαρίσματος
            }
        });

        // Listener για το κουμπί που ανοίγει τη δραστηριότητα διαχείρισης μαύρης λίστας
        openMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Δημιουργία και εκκίνηση νέου Intent για μετάβαση στη BlacklistActivity
                Intent intent = new Intent(FirstActivity.this, BlacklistActivity.class);
                startActivity(intent);
            }
        });

        // Έλεγχος και αίτηση permissions
        if (!checkPermission()) {
            requestPermission();
        }
    }

    // Μέθοδος για έλεγχο αν έχουν δοθεί οι άδειες για εγγραφή και ανάγνωση από εξωτερικό αποθηκευτικό χώρο
    private boolean checkPermission() {
        // Έλεγχος αν έχει δοθεί η άδεια για εγγραφή σε εξωτερικό χώρο αποθήκευσης
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Έλεγχος αν έχει δοθεί η άδεια για ανάγνωση από εξωτερικό χώρο αποθήκευσης
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // Επιστρέφει true μόνο αν και οι δύο άδειες έχουν δοθεί
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    // Μέθοδος για να ζητήσει από τον χρήστη τις απαιτούμενες άδειες
    private void requestPermission() {
        // Ζήτηση των permissions WRITE_EXTERNAL_STORAGE και READ_EXTERNAL_STORAGE
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Ελέγχει αν το requestCode ταιριάζει με τον κωδικό που χρησιμοποιήθηκε για την αίτηση άδειας
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                // Αν κάποια από τις άδειες δεν έχει δοθεί, εμφανίζει ένα μήνυμα στον χρήστη
                if (!writeAccepted || !readAccepted) {
                    Toast.makeText(this, "Η άδεια για πρόσβαση στα αρχεία είναι απαραίτητη για τη σωστή λειτουργία της εφαρμογής.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Δημιουργεί το μενού από το XML αρχείο που βρίσκεται στο res/menu/activity_blacklist.xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_blacklist, menu);

        // Επαναφορά της κατάστασης των μενού
        MenuItem blockHiddenItem = menu.findItem(R.id.block_hidden_numbers);
        boolean blockHiddenNumbers = sharedPreferences.getBoolean(BLOCK_HIDDEN_NUMBERS_KEY, false);
        blockHiddenItem.setChecked(blockHiddenNumbers);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Χειρισμός επιλογών από το μενού
        switch (item.getItemId()) {
            case R.id.block_hidden_numbers:
                // Χειρισμός του αποκλεισμού απόκρυψης αριθμών
                onBlockHiddenNumbers(item);
                return true;
            case R.id.notifications:
                // Χειρισμός επιλογής για ειδοποιήσεις
                onShowNotifications(item);
                return true;
            case R.id.allow_all:
            case R.id.allow_only_list:
            case R.id.block_list:
            case R.id.block_all:
                // Χειρισμός επιλογής της κατάστασης μπλοκαρίσματος κλήσεων
                selectCallBlockingMode(item);
                return true;
            case R.id.import_blacklist:
                // Χειρισμός επιλογής για εισαγωγή μαύρης λίστας
                onImportBlacklist(item);
                return true;
            case R.id.export_blacklist:
                // Χειρισμός επιλογής για εξαγωγή μαύρης λίστας
                onExportBlacklist(item);
                return true;
            default:
                return super.onOptionsItemSelected(item); // Για άλλες επιλογές χρησιμοποιείται η βασική μέθοδος
        }
    }

    public void onBlockHiddenNumbers(MenuItem item) {
        boolean newCheckedStatus = !item.isChecked();
        item.setChecked(newCheckedStatus);

        // Αποθήκευση της επιλογής του χρήστη
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BLOCK_HIDDEN_NUMBERS_KEY, newCheckedStatus);
        editor.apply();

        blockHiddenNumbers(newCheckedStatus);
    }

    private void blockHiddenNumbers(boolean shouldBlock) {
        if (shouldBlock) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);

                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (incomingNumber == null || incomingNumber.isEmpty()) {
                            // Μπλοκάρει την κλήση με απόκρυψη αριθμού
                            endCall();
                            Toast.makeText(getApplicationContext(), "Μπλοκάρισμα Απόκρυψης", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    // Μέθοδος για να τερματίσεις την κλήση (χρειάζεται ειδικά permissions)
    private void endCall() {

        try {
            // Απόκτηση του TelephonyManager για πρόσβαση στις τηλεφωνικές λειτουργίες της συσκευής
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            // Απόκτηση της κλάσης του telephonyManager
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            // Απόκτηση της μεθόδου "endCall" μέσω reflection
            Method endCall = telephonyClass.getDeclaredMethod("endCall");

            // Κάνει τη μέθοδο προσβάσιμη
            endCall.setAccessible(true);
            // Καλεί τη μέθοδο "endCall" για να τερματίσει την κλήση
            endCall.invoke(telephonyManager);
        } catch (Exception e) {
            e.printStackTrace(); // Εκτύπωση της εξαίρεσης σε περίπτωση αποτυχίας
        }
    }

    public void onShowNotifications(MenuItem item) {

        // Αλλαγή της ρύθμισης για την εμφάνιση ειδοποιήσεων (ενεργοποίηση/απενεργοποίηση)
        settings.showNotifications(!item.isChecked());
        // Ενημέρωση του checkbox στο μενού για να αντικατοπτρίζει τη νέα κατάσταση
        item.setChecked(!item.isChecked());
    }

    public boolean selectCallBlockingMode(MenuItem item) {
        // Επιλέγει τη λειτουργία μπλοκαρίσματος κλήσεων με βάση το στοιχείο μενού που επιλέχθηκε
        switch (item.getItemId()) {

            case R.id.allow_all:
                // Επιλογή λειτουργίας που επιτρέπει όλες τις κλήσεις
                settings.setCallBlockingMode(BlockingModes.ALLOW_ALL);
                break;
            case R.id.allow_only_list:
                // Επιλογή λειτουργίας που επιτρέπει μόνο τις κλήσεις από τη λίστα
                settings.setCallBlockingMode(BlockingModes.ALLOW_ONLY_LIST_CALLS);
                break;
            case R.id.block_list:
                // Επιλογή λειτουργίας που αποκλείει τις κλήσεις από τη λίστα
                settings.setCallBlockingMode(BlockingModes.BLOCK_LIST);
                break;
            case R.id.block_all:
                // Επιλογή λειτουργίας που αποκλείει όλες τις κλήσεις
                settings.setCallBlockingMode(BlockingModes.BLOCK_ALL);
                break;
            default:
                return false; // Αν δεν βρεθεί ταίριασμα, επιστρέφει false
        }
        item.setChecked(true);  // Ορίζει το στοιχείο μενού ως επιλεγμένο
        return true;
    }

    public void onImportBlacklist(MenuItem item) {
        showDialog(DIALOG_LOAD_FILE); // Εμφανίζει ένα διάλογο για την εισαγωγή της μαύρης λίστας
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_LOAD_FILE:
                // Δημιουργεί έναν διάλογο φόρτωσης αρχείου
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Load File");
                builder.setMessage("This is a sample file load dialog.");// Μήνυμα που εμφανίζεται στο διάλογο
                builder.setPositiveButton("OK", null);// Κουμπί επιβεβαίωσης
                dialog = builder.create();
                break;
            default:
                dialog = super.onCreateDialog(id); //Μήνυμα που εμφανιζει αν δεν ταιριάξει το id
        }
        return dialog;
    }

    public void onExportBlacklist(MenuItem item) {
        // Δημιουργία αντικειμένου BlacklistFile για τη διαχείριση της μαύρης λίστας
        BlacklistFile blacklistFile = new BlacklistFile(new File(basePath), BlacklistFile.DEFAULT_FILENAME);


        // Δημιουργία λίστας αριθμών που θα αποθηκευτούν
        List<Number> numbers = new LinkedList<>();
        // Προσθήκη όλων των αριθμών από το adapter στη λίστα
        for (int i = 0; i < adapter.getCount(); i++) {
            numbers.add(adapter.getItem(i));
        }

        // Αποθήκευση της μαύρης λίστας στο αρχείο
        blacklistFile.store(numbers, this);


        // Εμφάνιση μηνύματος επιβεβαίωσης ότι η μαύρη λίστα αποθηκεύτηκε επιτυχώς
        Toast.makeText(this, getString(R.string.blacklist_exported_to) + " " + BlacklistFile.DEFAULT_FILENAME, Toast.LENGTH_LONG).show();
    }

    private void updateButton(Button toggleButton) {
        if (isOn) {
            // Αν το μπλοκάρισμα είναι ενεργό, αλλάζει το κείμενο και το υπόβαθρο του κουμπιού
            toggleButton.setText("ON");
            toggleButton.setBackgroundResource(R.drawable.button_background_on);

            // Ορίζει τη λειτουργία μπλοκαρίσματος να επιτρέπει μόνο κλήσεις από επαφές
            settings.setCallBlockingMode(BlockingModes.ALLOW_CONTACTS);

            // Εμφανίζει ένα μήνυμα ότι το μπλοκάρισμα αγνώστων κλήσεων είναι ενεργό
            Toast.makeText(FirstActivity.this, "Μπλοκάρισμα άγνωστων κλήσεων [ Ενεργό ]", Toast.LENGTH_SHORT).show();
        } else {
            // Αν το μπλοκάρισμα είναι ανενεργό, αλλάζει το κείμενο και το υπόβαθρο του κουμπιού
            toggleButton.setText("OFF");
            toggleButton.setBackgroundResource(R.drawable.button_background_off);
            // Ορίζει τη λειτουργία μπλοκαρίσματος να επιτρέπει όλες τις κλήσεις
            settings.setCallBlockingMode(BlockingModes.ALLOW_ALL);
            // Εμφανίζει ένα μήνυμα ότι το μπλοκάρισμα αγνώστων κλήσεων είναι ανενεργό
            Toast.makeText(FirstActivity.this, "Μπλοκάρισμα άγνωστων κλήσεων [ Ανενεργό ]", Toast.LENGTH_SHORT).show();
        }
    }
}

