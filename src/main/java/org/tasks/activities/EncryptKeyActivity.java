package org.tasks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.preferences.Preferences;

/**
 * Created by Jiayu on 11/17/15.
 */

public class EncryptKeyActivity extends InjectingAppCompatActivity{

    @Inject
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(EncryptKeyActivity.this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.encryption_key, null);
        builder.setTitle(R.string.encryption_password);
        builder.setView(textEntryView);
        builder.setPositiveButton(R.string.encryption_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText userName = (EditText) textEntryView.findViewById(R.id.etUserName);
                String test = userName.getText().toString();

                preferences.setString(R.string.p_encryp_key, test);
                Toast.makeText(EncryptKeyActivity.this, "The password is set to "
                        + test + "!", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
        builder.setNegativeButton(R.string.encryption_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        builder.create().show();

    }

}
