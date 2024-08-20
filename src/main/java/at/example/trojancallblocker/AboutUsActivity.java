package at.example.trojancallblocker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView aboutUsText = findViewById(R.id.about_us_text);
        TextView githubLink = findViewById(R.id.github_link);

        // Εισαγάγετε τις πληροφορίες σας για την πτυχιακή εργασία
        aboutUsText.setText("Αυτό είναι το application της πτυχιακής εργασίας " +
                "με θέμα 'Νομική Πληροφορική και Spam κλήσεις'.");

        // Ρυθμίστε το σύνδεσμο GitHub
        githubLink.setText("https://github.com/your-repository");
        githubLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
