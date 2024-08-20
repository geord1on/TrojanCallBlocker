package com.android.internal.telephony;

// Δημόσια διεπαφή για τη διαχείριση τηλεφωνικών λειτουργιών
public interface ITelephony {
    // Μέθοδος για τον τερματισμό μιας κλήσης
    boolean endCall();

    // Μέθοδος για την απάντηση σε μια εισερχόμενη κλήση
    void answerRingingCall();

    // Μέθοδος για την σίγαση του κουδουνίσματος
    void silenceRinger();
}
