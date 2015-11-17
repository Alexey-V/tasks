package org.tasks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.preferences.Preferences;

/**
 * Created by Jiayu on 11/17/15.
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

                        preferences.setString(R.string.p_encryp_mode, String.valueOf(mode));
                        Toast.makeText(EncryptModeActivity.this, "Encryption mode "
                                + encrypt_mode[mode] + "!", Toast.LENGTH_SHORT).show();
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
