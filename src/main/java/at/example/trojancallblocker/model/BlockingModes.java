package at.example.trojancallblocker.model;


// Σταθερές τιμές που αντιπροσωπεύουν τους διαφορετικούς τρόπους φραγής κλήσεων
public interface BlockingModes {
    // Επιτρέπει όλες τις κλήσεις
    int ALLOW_ALL= 0;

    // Επιτρέπει μόνο τις κλήσεις από επαφές
    int ALLOW_CONTACTS = 1;

    // Επιτρέπει μόνο τις κλήσεις από αριθμούς που είναι στη λίστα αποκλεισμένων
    int ALLOW_ONLY_LIST_CALLS = 2;

    // Φράσσει τις κλήσεις που προέρχονται από αριθμούς στη λίστα αποκλεισμένων
    int BLOCK_LIST = 3;

    // Φράζει όλες τις κλήσεις
    int BLOCK_ALL = 4;
}
