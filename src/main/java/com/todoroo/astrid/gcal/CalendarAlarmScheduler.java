package com.todoroo.astrid.gcal;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.todoroo.andlib.utility.DateUtilities;

import org.tasks.R;
import org.tasks.preferences.PermissionChecker;
import org.tasks.preferences.Preferences;
import org.tasks.scheduling.AlarmManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CalendarAlarmScheduler {

    public static final String URI_PREFIX = "cal-reminder";
    public static final String URI_PREFIX_POSTPONE = "cal-postpone";

    private final Preferences preferences;
    private PermissionChecker permissionChecker;
    private AlarmManager alarmManager;

    @Inject
    public CalendarAlarmScheduler(Preferences preferences, PermissionChecker permissionChecker, AlarmManager alarmManager) {
        this.preferences = preferences;
        this.permissionChecker = permissionChecker;
        this.alarmManager = alarmManager;
    }

    public void scheduleCalendarAlarms(final Context context, boolean force) {
        if (!preferences.getBoolean(R.string.p_calendar_reminders, true) && !force) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                scheduleAllCalendarAlarms(context);
            }
        }).start();
    }

    private void scheduleAllCalendarAlarms(Context context) {
        if (!preferences.getBoolean(R.string.p_calendar_reminders, true)) {
            return;
        }
        if (!permissionChecker.canReadCalendar()) {
            return;
        }

        ContentResolver cr = context.getContentResolver();

        long now = DateUtilities.now();

        Cursor events = cr.query(Calendars.getCalendarContentUri(Calendars.CALENDAR_CONTENT_EVENTS),
                new String[] { Calendars.ID_COLUMN_NAME, Calendars.EVENTS_DTSTART_COL },
                Calendars.EVENTS_DTSTART_COL + " > ? AND " + Calendars.EVENTS_DTSTART_COL + " < ?",
                new String[] { Long.toString(now + DateUtilities.ONE_MINUTE * 15), Long.toString(now + DateUtilities.ONE_DAY) },
                null);
        try {
            if (events != null && events.getCount() > 0) {
                int idIndex = events.getColumnIndex(Calendars.ID_COLUMN_NAME);
                int dtstartIndex = events.getColumnIndexOrThrow(Calendars.EVENTS_DTSTART_COL);

                for (events.moveToFirst(); !events.isAfterLast(); events.moveToNext()) {
                    Intent eventAlarm = new Intent(context, CalendarAlarmReceiver.class);
                    eventAlarm.setAction(CalendarAlarmReceiver.BROADCAST_CALENDAR_REMINDER);

                    long start = events.getLong(dtstartIndex);
                    long id = events.getLong(idIndex);

                    eventAlarm.setData(Uri.parse(URI_PREFIX + "://" + id));

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            CalendarAlarmReceiver.REQUEST_CODE_CAL_REMINDER, eventAlarm, 0);

                    alarmManager.cancel(pendingIntent);

                    long alarmTime = start - DateUtilities.ONE_MINUTE * 15;
                    alarmManager.wakeup(alarmTime, pendingIntent);
                }
            }

            // Schedule alarm to recheck and reschedule calendar alarms in 12 hours
            Intent rescheduleAlarm = new Intent(CalendarStartupReceiver.BROADCAST_RESCHEDULE_CAL_ALARMS);
            PendingIntent pendingReschedule = PendingIntent.getBroadcast(context, 0,
                    rescheduleAlarm, 0);
            alarmManager.cancel(pendingReschedule);
            alarmManager.noWakeup(DateUtilities.now() + DateUtilities.ONE_HOUR * 12, pendingReschedule);
        } finally {
            if (events != null) {
                events.close();
            }
        }
    }
}
