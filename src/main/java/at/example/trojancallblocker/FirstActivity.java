package at.example.trojancallblocker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import at.example.trojancallblocker.model.BlacklistFile;
import at.example.trojancallblocker.model.BlockingModes;
import at.example.trojancallblocker.model.Number;

public class FirstActivity extends AppCompatActivity {

    private boolean isOn = false;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CallBlockerPrefs";
    private static final String BLOCKING_ENABLED_KEY = "BlockingEnabled";
    private Settings settings;
    private static final int DIALOG_LOAD_FILE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String basePath;

    private ArrayAdapter<Number> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        settings = new Settings(this);
        basePath = getFilesDir().getPath();

        final Button toggleButton = findViewById(R.id.toggleButton);
        TextView instructionsText = findViewById(R.id.instructionsText);
        Button openMainButton = findViewById(R.id.openMainButton);

        instructionsText.setSelected(true); // Enable marquee effect

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isOn = sharedPreferences.getBoolean(BLOCKING_ENABLED_KEY, false);
        updateButton(toggleButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new LinkedList<Number>());

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOn = !isOn;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(BLOCKING_ENABLED_KEY, isOn);
                editor.apply();
                updateButton(toggleButton);
            }
        });

        openMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this, BlacklistActivity.class);
                startActivity(intent);
            }
        });

        // Έλεγχος και αίτηση permissions
        if (!checkPermission()) {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (!writeAccepted || !readAccepted) {
                    Toast.makeText(this, "Η άδεια για πρόσβαση στα αρχεία είναι απαραίτητη για τη σωστή λειτουργία της εφαρμογής.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_blacklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.block_hidden_numbers:
                onBlockHiddenNumbers(item);
                return true;
            case R.id.notifications:
                onShowNotifications(item);
                return true;
            case R.id.allow_all:
            case R.id.allow_only_list:
            case R.id.block_list:
            case R.id.block_all:
                selectCallBlockingMode(item);
                return true;
            case R.id.import_blacklist:
                onImportBlacklist(item);
                return true;
            case R.id.export_blacklist:
                onExportBlacklist(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBlockHiddenNumbers(MenuItem item) {
        settings.blockHiddenNumbers(!item.isChecked());
        item.setChecked(!item.isChecked());
    }

    public void onShowNotifications(MenuItem item) {
        settings.showNotifications(!item.isChecked());
        item.setChecked(!item.isChecked());
    }

    public boolean selectCallBlockingMode(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.allow_all:
                settings.setCallBlockingMode(BlockingModes.ALLOW_ALL);
                break;
            case R.id.allow_only_list:
                settings.setCallBlockingMode(BlockingModes.ALLOW_ONLY_LIST_CALLS);
                break;
            case R.id.block_list:
                settings.setCallBlockingMode(BlockingModes.BLOCK_LIST);
                break;
            case R.id.block_all:
                settings.setCallBlockingMode(BlockingModes.BLOCK_ALL);
                break;
            default:
                return false;
        }
        item.setChecked(true);
        return true;
    }

    public void onImportBlacklist(MenuItem item) {
        showDialog(DIALOG_LOAD_FILE);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_LOAD_FILE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Load File");
                builder.setMessage("This is a sample file load dialog.");
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;
            default:
                dialog = super.onCreateDialog(id);
        }
        return dialog;
    }

    public void onExportBlacklist(MenuItem item) {
        BlacklistFile blacklistFile = new BlacklistFile(new File(basePath), BlacklistFile.DEFAULT_FILENAME);

        List<Number> numbers = new LinkedList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            numbers.add(adapter.getItem(i));
        }

        blacklistFile.store(numbers, this);

        Toast.makeText(this, getString(R.string.blacklist_exported_to) + " " + BlacklistFile.DEFAULT_FILENAME, Toast.LENGTH_LONG).show();
    }

    private void updateButton(Button toggleButton) {
        if (isOn) {
            toggleButton.setText("ON");
            toggleButton.setBackgroundResource(R.drawable.button_background_on);
            settings.setCallBlockingMode(BlockingModes.ALLOW_CONTACTS);
            Toast.makeText(FirstActivity.this, "Μπλοκάρισμα άγνωστων κλήσεων [ Ενεργό ]", Toast.LENGTH_SHORT).show();
        } else {
            toggleButton.setText("OFF");
            toggleButton.setBackgroundResource(R.drawable.button_background_off);
            settings.setCallBlockingMode(BlockingModes.ALLOW_ALL);
            Toast.makeText(FirstActivity.this, "Μπλοκάρισμα άγνωστων κλήσεων [ Ανενεργό ]", Toast.LENGTH_SHORT).show();
        }
    }
}
