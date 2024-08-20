package at.example.trojancallblocker.model;

import android.content.ContentValues;

public class Number {
    // Σταθερές για το όνομα του πίνακα και των στηλών στη βάση δεδομένων
    public static final String
            _TABLE = "numbers",
            NUMBER = "number",
            NAME = "name",
            LAST_CALL = "lastCall",
            TIMES_CALLED = "timesCalled";

    // Ιδιότητες της κλάσης
    public String number;
    public String name;
    public Long lastCall;
    public int timesCalled;
    // Κενός κατασκευαστής
    public Number(){}

    // Κατασκευαστής με μόνο αριθμό
    public Number(String number){
        this(number, null);
    }


    // Κατασκευαστής με αριθμό και όνομα
    public Number(String number, String name){
        this.number = number;
        this.name = name;
    }


    // Δημιουργία αντικειμένου Number από ContentValues
    public static Number fromValues(ContentValues values) {
        Number number = new Number();
        number.number = values.getAsString(NUMBER);
        number.name = values.getAsString(NAME);
        number.lastCall = values.getAsLong(LAST_CALL);
        number.timesCalled = values.getAsInteger(TIMES_CALLED);
        return number;
    }

    // Μετατροπή αριθμού από τη φορμά της βάσης δεδομένων σε μορφή εμφάνισης
    public static String wildcardsDbToView(String number) {
        return number
                .replace('%','*')
                .replace('_','#');
    }

    // Μετατροπή αριθμού από τη φορμά εμφάνισης σε μορφή βάσης δεδομένων
    public static String wildcardsViewToDb(String number) {
        return number
                .replaceAll("[^+#*%_0-9]", "")  // Αφαίρεση όλων των χαρακτήρων εκτός των επιτρεπόμενων
                .replace('*','%')
                .replace('#','_');
    }

}
