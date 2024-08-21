![eikonaapp](https://github.com/user-attachments/assets/1c92cde8-4159-4065-b656-5a7423ba56f5)
# TrojanCallBlocker

Αυτή είναι η πτυχιακή μου εργασία για το τμήμα πληροφορικής ΑΠΘ. Το **TrojanCallBlocker** είναι μια εφαρμογή Android που έχει σχεδιαστεί για να αποτρέπει εισερχόμενες κλήσεις από αγνώστους και spam αριθμούς. Η εφαρμογή δίνει τη δυνατότητα στους χρήστες να διαχειρίζονται τις εισερχόμενες κλήσεις σύμφωνα με τις προτιμήσεις τους, ενώ είναι συμβατή με συσκευές που τρέχουν Android 4 έως Android 10.

Εμπνευσμένο απο την αρχαία Τροία με τον Δούρειο Ίππο να γίνεται το λογότυπο της εφαρμογής αποτρέποντας να μην έρθει σε επαφή μαζί σας οποιοδήποτε τηλεφώνημα (σπαμ,κακόβουλο),έχοντας το κεφάλι σας ήσυχο.
Απλό Design με χρήσιμες λειτουργίες, ιδανική εφαρμογή για χρήστες μεγαλύτερης ηλικίας, όπου γίνονται στόχος ολοένα και περισσότερο τα τελευταία χρόνια απο τηλεφωνήματα εξαπάτησης, με κάθε τεχνική προς απόσπαση χρημάτων / κρίσιμων πληροφοριών. 

## Περιεχόμενα

- [Περίληψη](#περίληψη)
- [Προαπαιτούμενα](#προαπαιτούμενα)
- [Εγκατάσταση](#εγκατάσταση)
- [Χρήση](#χρήση)
- [Αρχιτεκτονική Συστήματος](#αρχιτεκτονική-συστήματος)
- [Τεχνολογίες](#τεχνολογίες)
- [Συγγραφέας](#συγγραφέας)
- [Άδεια](#άδεια)

## Περίληψη

Η παρούσα πτυχιακή εργασία έχει ως κύριο στόχο τη σχεδίαση και την ανάπτυξη μιας εφαρμογής για την αποτροπή εισερχόμενων κλήσεων από αγνώστους-spam αριθμούς και τη διαχείρισή τους σύμφωνα με τις προτιμήσεις του χρήστη. Η εφαρμογή είναι συμβατή με παλιότερες κινητές συσκευές smartphone που χρησιμοποιούν Android 4 έως Android 10, καθώς σε αυτές τις εκδόσεις του λειτουργικού συστήματος απουσίαζε η δυνατότητα διαχείρισης τέτοιων κλήσεων.

Η εφαρμογή επιτρέπει στους χρήστες να καταχωρούν αριθμούς τηλεφώνων σε μια λίστα και να επιλέγουν μέσα από το μενού πώς θα διαχειριστούν αυτή τη λίστα. Επιπλέον, δίνει τη δυνατότητα στον χρήστη να ενεργοποιήσει μια λειτουργία που αποτρέπει οποιαδήποτε κλήση από αριθμούς που δεν είναι αποθηκευμένοι ως επαφές στη συσκευή του. Ένα σημαντικό πλεονέκτημα της εφαρμογής είναι ότι επιτρέπει στους χρήστες να μπλοκάρουν αριθμούς πριν λάβουν κλήση, αποτρέποντας έτσι μελλοντικές ανεπιθύμητες επικοινωνίες.

## Προαπαιτούμενα

Για να μπορέσετε να τρέξετε την εφαρμογή τοπικά, θα χρειαστείτε:

- Android Studio
- Ελάχιστη υποστήριξη SDK για Android 4.0 (API 14)
- Εγκατεστημένο Android SDK και εικονική συσκευή ή πραγματική συσκευή με Android 4 έως 10

## Εγκατάσταση

Για να εγκαταστήσετε και να τρέξετε την εφαρμογή τοπικά, ακολουθήστε τα παρακάτω βήματα:

1. **Κλωνοποίηση του repository**:
   ```bash
   git clone https://github.com/geord1on/TrojanCallBlocker.git
   cd TrojanCallBlocker
   
## Χρήση

Μετά την εγκατάσταση και εκκίνηση της εφαρμογής, μπορείτε να:

Προσθέσετε αριθμούς στη "μαύρη λίστα": Ανοίξτε την εφαρμογή και καταχωρήστε τους αριθμούς που θέλετε να μπλοκάρετε.
Ενεργοποιήσετε τη λειτουργία αποτροπής άγνωστων αριθμών: Μέσα από το κύριο μενού, επιλέξτε την επιλογή που αποτρέπει τις κλήσεις από μη αποθηκευμένες επαφές.
Διαχείριση λίστας: Μπορείτε να επεξεργαστείτε ή να διαγράψετε αριθμούς από τη λίστα οποιαδήποτε στιγμή.

## Αρχιτεκτονική Συστήματος

Η εφαρμογή έχει σχεδιαστεί με γνώμονα την υποστήριξη παλιότερων εκδόσεων Android. Χρησιμοποιεί το Android Telephony API για τη διαχείριση των κλήσεων και επιτρέπει τη δημιουργία προσαρμοσμένων λιστών τηλεφώνων. Ο χρήστης μπορεί να ενεργοποιήσει ή να απενεργοποιήσει λειτουργίες της εφαρμογής μέσω ενός απλού και κατανοητού UI.

Ο λόγος που επικεντρώνεται σε παλιότερες εκδόσεις android είναι γιατί σε αυτές δεν υπήρχε η δυνατότητα φραγής/αποτροπής κλήσεων απο αγνώστους αριθμούς εκτός επαφών, ούτε η δημιουργία μιας "μαύρης λίστας" την οποία ο χρήστης θα μπορεί να την χειριστεί ανάλογα (import,export,block numbers,allow numbers)

## Τεχνολογίες
Οι κύριες τεχνολογίες και εργαλεία που χρησιμοποιήθηκαν είναι:

- Android Studio
- Java
- Android Telephony API

## Συγγραφέας

Όνομα: Γεώργιος Διονυσίου / George Dionysiou

Email: georgediongr@gmail.com

LinkedIn: https://www.linkedin.com/in/george-dionysiou-944125278/

GitHub: geord1on

## Άδεια

Αυτό το έργο είναι αδειοδοτημένο κάτω από την άδεια MIT - δείτε το αρχείο LICENSE για λεπτομέρειες.