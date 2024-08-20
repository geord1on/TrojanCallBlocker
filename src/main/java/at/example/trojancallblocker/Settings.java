package at.example.trojancallblocker;

import android.content.Context;
import android.content.SharedPreferences;

import at.example.trojancallblocker.model.BlockingModes;

public class Settings {

    private static final String
            PREF_BLOCK_HIDDEN_NUMBERS = "blockHiddenNumbers", // Κλειδί για την απόκρυψη κλήσεων από κρυφούς αριθμούς
            PREF_NOTIFICATIONS = "notifications", // Κλειδί για τις ειδοποιήσεις
            PREF_BLOCKING_MODE = "callBlockingMode";  // Κλειδί για τη λειτουργία αποκλεισμού κλήσεων



    private final SharedPreferences pref; // Αντικείμενο SharedPreferences για την αποθήκευση των ρυθμίσεων


    public Settings(Context context) {
        pref = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }


    public boolean blockHiddenNumbers() {     // Μέθοδος για την απόκρυψη κλήσεων από κρυφούς αριθμούς

        return pref.getBoolean(PREF_BLOCK_HIDDEN_NUMBERS, false);
    }

    public void blockHiddenNumbers(boolean block) {     // Μέθοδος για την αλλαγή της ρύθμισης αποκλεισμού κρυφών αριθμών

        pref.edit()
                .putBoolean(PREF_BLOCK_HIDDEN_NUMBERS, block)
                .apply();
    }


    // Μέθοδος για την εμφάνιση ειδοποιήσεων
    public boolean showNotifications() {
        return pref.getBoolean(PREF_NOTIFICATIONS, true);
    }

    // Μέθοδος για την αλλαγή της ρύθμισης ειδοποιήσεων
    public void showNotifications(boolean show) {
        pref.edit()
                .putBoolean(PREF_NOTIFICATIONS, show)
                .apply();
    }


    // Μέθοδος για την λήψη της λειτουργίας αποκλεισμού κλήσεων
    public int getCallBlockingMode() {
        return pref.getInt(PREF_BLOCKING_MODE, BlockingModes.BLOCK_LIST);
    }

    // Μέθοδος για την αλλαγή της λειτουργίας αποκλεισμού κλήσεων
    public void setCallBlockingMode(int value) {
        pref.edit()
                .putInt(PREF_BLOCKING_MODE, value)
                .apply();
    }



}
