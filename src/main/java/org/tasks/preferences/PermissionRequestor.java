package org.tasks.preferences;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import javax.inject.Inject;

public class PermissionRequestor {

    public static final int REQUEST_FILE_WRITE = 50;

    private final Activity activity;
    private final PermissionChecker permissionChecker;

    @Inject
    public PermissionRequestor(Activity activity, PermissionChecker permissionChecker) {
        this.activity = activity;
        this.permissionChecker = permissionChecker;
    }

    public boolean requestFileWritePermission() {
        if (permissionChecker.canWriteToExternalStorage()) {
            return true;
        }
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_FILE_WRITE);
        return false;
    }

    private void requestPermission(String permission, int rc) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, rc);
    }
}
