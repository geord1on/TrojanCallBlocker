package at.example.trojancallblocker.model;

import android.app.Activity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
// Κλάση που επεκτείνει την File για την αποθήκευση και ανάκτηση λίστας αριθμών τηλεφώνου
public class BlacklistFile extends File {

    public static final String END_NUMBER_DELIMETER = ": ";
    public static final String DEFAULT_FILENAME = "TrojanCallBlocker_blacklist.txt";

    public BlacklistFile(File parent, String child) {
        super(parent, child);
    }
    // Μέθοδος για την ανάκτηση των αριθμών τηλεφώνου από το αρχείο
    public List<Number> load() {
        List<Number> numbers = new LinkedList<>();

        FileInputStream fin;
        BufferedReader reader;
        String line;
        Number n;
        int sep;

        try {
            fin = new FileInputStream(this);
            reader = new BufferedReader(new InputStreamReader(fin));
            while ((line = reader.readLine()) != null &&
                    (sep = line.indexOf(END_NUMBER_DELIMETER)) != -1) {
                n = new Number();
                n.number = line.substring(0, sep);
                n.name = line.substring(sep + END_NUMBER_DELIMETER.length());
                numbers.add(n);
            }
            fin.close();
            return numbers;
        } catch (IOException exception) {
            return numbers;
        }
    }

    // Μέθοδος για την αποθήκευση των αριθμών τηλεφώνου στο αρχείο
    public boolean store(List<Number> numbers, Activity activity) {
        try {
            FileOutputStream fout = new FileOutputStream(this);
            for (Number n : numbers) {
                fout.write(n.number.getBytes());
                fout.write(END_NUMBER_DELIMETER.getBytes());
                fout.write(n.name.getBytes());
                fout.write("\n".getBytes());
            }
            fout.close();
            return true;


        } catch (IOException exception) {
            return false;             // Σε περίπτωση σφάλματος επιστρέφεται false
        }
    }
}