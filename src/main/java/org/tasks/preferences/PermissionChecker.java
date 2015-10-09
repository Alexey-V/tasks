package org.tasks.preferences;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tasks.injection.ForApplication;

import javax.inject.Inject;

public class PermissionChecker {

    private static final Logger log = LoggerFactory.getLogger(PermissionChecker.class);

    private final Context context;

    @Inject
    public PermissionChecker(@ForApplication Context context) {
        this.context = context;
    }

    public boolean canReadCalendar() {
        return checkPermission(Manifest.permission.READ_CALENDAR);
    }

    public boolean canWriteToExternalStorage() {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean checkPermission(String permission) {
        boolean result = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        if (!result) {
            log.warn("Request for {} denied", permission);
        }
        return result;
    }
}
