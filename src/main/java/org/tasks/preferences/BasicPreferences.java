package org.tasks.preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.todoroo.andlib.utility.AndroidUtilities;
import com.todoroo.astrid.core.OldTaskPreferences;
import com.todoroo.astrid.gcal.CalendarAlarmScheduler;
import com.todoroo.astrid.gcal.GCalHelper;
import com.todoroo.astrid.reminders.ReminderPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tasks.R;
import org.tasks.injection.InjectingPreferenceActivity;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import static com.todoroo.andlib.utility.AndroidUtilities.preFroyo;

public class BasicPreferences extends InjectingPreferenceActivity {

    private static final Logger log = LoggerFactory.getLogger(BasicPreferences.class);

    private static final String EXTRA_RESULT = "extra_result";
    private static final int RC_PREFS = 10001;

    @Inject CalendarAlarmScheduler calendarAlarmScheduler;
    @Inject PermissionChecker permissionChecker;
    @Inject PermissionRequestor permissionRequestor;
    @Inject Preferences preferences;
    @Inject GCalHelper calendarHelper;

    private CheckBoxPreference calendarIntegrationPreference;
    private Bundle result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        result = savedInstanceState == null ? new Bundle() : savedInstanceState.getBundle(EXTRA_RESULT);

        addPreferencesFromResource(R.xml.preferences);
        if (!getResources().getBoolean(R.bool.sync_enabled)) {
            getPreferenceScreen().removePreference(findPreference(getString(R.string.synchronization)));
        }
        if (getResources().getBoolean(R.bool.tasker_available)) {
            addPreferencesFromResource(R.xml.preferences_addons);
        }
        setupActivity(R.string.EPr_appearance_header, AppearancePreferences.class);
        setupActivity(R.string.notifications, ReminderPreferences.class);
        setupActivity(R.string.EPr_manage_header, OldTaskPreferences.class);

        calendarIntegrationPreference = (CheckBoxPreference) findPreference(getString(R.string.p_calendar_enabled));
        calendarIntegrationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null && (boolean) newValue) {
                    if (permissionRequestor.requestCalendarPermissions()) {
                        enableCalendarIntegration(true);
                    }
                }
                return true;
            }
        });
        enableCalendarIntegration(
                preferences.getBoolean(R.string.p_calendar_enabled, false) &&
                permissionChecker.canAccessCalendars());
    }

    private void enableCalendarIntegration(boolean enabled) {
        if (enabled) {
            preferences.setBoolean(R.string.p_calendar_enabled, true);
            initializeCalendarReminderPreference();
        } else {
            preferences.setBoolean(R.string.p_calendar_enabled, false);
            calendarIntegrationPreference.setChecked(false);
        }
    }

    private void setupActivity(int key, final Class<?> target) {
        findPreference(getString(key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(BasicPreferences.this, target), RC_PREFS);
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(EXTRA_RESULT, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PREFS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                result.putAll(data.getExtras());
                setResult(Activity.RESULT_OK, new Intent() {{
                    putExtras(result);
                }});
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionRequestor.REQUEST_CALENDAR) {
            for (int i = 0 ; i < permissions.length ; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    enableCalendarIntegration(false);
                    return;
                }
            }
            enableCalendarIntegration(true);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initializeCalendarReminderPreference() {
        Preference defaultCalendarPref = findPreference(getString(R.string.gcal_p_default));
        try {
            initCalendarsPreference((ListPreference) defaultCalendarPref);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        Preference calendarReminderPreference = findPreference(getString(R.string.p_calendar_reminders));
        if (preFroyo()) {
            getPreferenceScreen().removePreference(calendarReminderPreference);
        } else {
            calendarReminderPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null && ((Boolean) newValue)) {
                        calendarAlarmScheduler.scheduleCalendarAlarms(BasicPreferences.this, true);
                    }
                    return true;
                }
            });
        }
    }

    private void initCalendarsPreference(ListPreference listPreference) {
        Resources r = getResources();
        GCalHelper.CalendarResult calendars = calendarHelper.getCalendars();

        // Fetch the current setting. Invalid calendar id will
        // be changed to default value.
        String currentSetting = preferences.getStringValue(R.string.gcal_p_default);

        int currentSettingIndex = -1;

        ArrayList<CharSequence> entries = new ArrayList<>();
        entries.addAll(Arrays.asList(r.getStringArray(R.array.EPr_default_addtocalendar)));
        entries.addAll(Arrays.asList(calendars.calendars));

        ArrayList<CharSequence> entryValues = new ArrayList<>();
        entryValues.addAll(Arrays.asList(r.getStringArray(R.array.EPr_default_addtocalendar_values)));
        entryValues.addAll(Arrays.asList(calendars.calendarIds));

        listPreference.setEntries(entries.toArray(new CharSequence[entries.size()]));
        listPreference.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));

        listPreference.setValueIndex(0);
        listPreference.setEnabled(true);

        if (calendars.calendarIds.length == 0 || calendars.calendars.length == 0) {
            // Something went wrong when querying calendars
            // Leave the preference at disabled.
            return;
        }

        // Iterate calendars one by one, and fill up the list preference
        if (currentSetting != null) {
            for (int i=0; i<calendars.calendarIds.length; i++) {
                // We found currently selected calendar
                if (currentSetting.equals(calendars.calendarIds[i])) {
                    currentSettingIndex = i+1; // +1 correction for disabled-entry
                    break;
                }
            }
        }

        if(currentSettingIndex == -1 || currentSettingIndex > calendars.calendarIds.length+1) {
            // Should not happen!
            // Leave the preference at disabled.
            log.debug("initCalendarsPreference: Unknown calendar.");
            currentSettingIndex = 0;
        }

        listPreference.setValueIndex(currentSettingIndex);
        listPreference.setEnabled(true);

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setCalendarSummary(newValue);
                return true;
            }
        });
        setCalendarSummary(listPreference.getValue());
    }

    private void setCalendarSummary(Object value) {
        ListPreference listPreference = (ListPreference) findPreference(getString(R.string.gcal_p_default));
        int index = AndroidUtilities.indexOf(listPreference.getEntryValues(), value);
        String setting = listPreference.getEntries()[index].toString();
        listPreference.setSummary(setting);
    }
}
