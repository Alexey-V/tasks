package org.tasks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.preferences.Preferences;

/**
 * This class handle the actions of setting encryption keys.
 * @author Jiayu
 */

public class EncryptKeyActivity extends InjectingAppCompatActivity{

    @Inject
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String te = preferences.getStringValue(R.string.p_encrypt_mode);
        // When the encryption mode is on
        if (te.equals("0")){
            AlertDialog.Builder builder = new AlertDialog.Builder(EncryptKeyActivity.this);
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.encryption_key, null);
            builder.setTitle(R.string.encryption_password);
            builder.setView(textEntryView);
            builder.setPositiveButton(R.string.encryption_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            EditText userName = (EditText) textEntryView.findViewById(R.id.editKey);
                            String test = userName.getText().toString();
                            // Save the value
                            preferences.setString(R.string.p_encrypt_key, test);
                            Toast toast = Toast.makeText(EncryptKeyActivity.this, "Password is set to \""
                                    + test + "\"\n\nPlease remember it for decryption" +
                                    "\n\nDefault password is \"123456\"", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            finish();
                        }
                    }
            );
            builder.setNegativeButton(R.string.encryption_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }
            );
            builder.create().
                    show();
        // When the encryption mode is off
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(EncryptKeyActivity.this);
                builder.setTitle("Encryption mode is off");
                builder.setMessage("Please turn on encryption mode first");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        }
                );
                builder.create().
                        show();
                builder.setNegativeButton(R.string.encryption_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }
            );
        }
        }
    }