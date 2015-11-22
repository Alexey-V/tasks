package org.tasks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.preferences.Preferences;

/**
 * This class handle the actions of switching encryption mode.
 * @author Jiayu
 */

public class EncryptModeActivity extends InjectingAppCompatActivity{

    @Inject
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String[] encrypt_mode = new String[]{"ON", "OFF"};
        new AlertDialog.Builder(this).setTitle(R.string.switch_encryption_mode).setItems(
                encrypt_mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int mode) {
                        // Save the value
                        preferences.setString(R.string.p_encrypt_mode, String.valueOf(mode));
                        Toast toast = Toast.makeText(EncryptModeActivity.this, "Encryption mode "
                                + encrypt_mode[mode] +
                                "\n\nEncrypted file shall be imported while mode ON", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
    }

}