package at.example.trojancallblocker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import at.example.trojancallblocker.model.BlockingModes;
import at.example.trojancallblocker.model.DbHelper;
import at.example.trojancallblocker.model.Number;

    public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "TrojanCallBlocker";

    private static final int NOTIFY_REJECTED = 0;
    private static boolean AlreadyOnCall = false;


    @Override
    public void onReceive(Context context, Intent intent) {

        //Έλεγχος τηλεφώνου
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "Received phone state change to the extra state " + extraState);


            // Αν το τηλέφωνο είναι σε κατάσταση κλήσης
            if (extraState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(TAG, "Setting AlreadyOnCall");
                AlreadyOnCall = true;
            }
            // Αν το τηλέφωνο είναι σε κατάσταση αδράνειας
            else if (extraState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(TAG, "Clearing AlreadyOnCall");
                AlreadyOnCall = false;
            }
            // Αν το τηλέφωνο κουδουνίζει
            else if (extraState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d(TAG, "Handling ring");
                Settings settings = new Settings(context);

                // Ελέγχει αν πρέπει να αποκλειστούν οι απόκρυφοι αριθμοί ή αν η κατάσταση αποκλεισμού κλήσεων δεν είναι "επιτρέπονται όλες"
                if (settings.blockHiddenNumbers() || settings.getCallBlockingMode() != BlockingModes.ALLOW_ALL) {
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);


                    if (incomingNumber == null)
                        return;

                    Log.i(TAG, "Εισερχόμενη κλήση: " + incomingNumber);


                    // Αν ο αριθμός είναι άδειος (απόκρυψη αριθμού)
                    if (TextUtils.isEmpty(incomingNumber)) {
                        // private number (no caller ID)
                        if (settings.blockHiddenNumbers())
                            rejectCall(context, null, context.getString(R.string.receiver_notify_private_number));

                    }
                    // Αν η ρύθμιση είναι να αποκλειστούν όλες οι κλήσεις
                    else if (settings.getCallBlockingMode() == BlockingModes.BLOCK_ALL) {
                        Log.i(TAG, "Μπλοκ όλες τις κλήσεις: " + incomingNumber);
                        Number number;
                        if(isNumberPresentInContacts(context, incomingNumber)){
                            String name = getCallerID(context, incomingNumber);
                            number = new Number(incomingNumber, name);
                        }
                        else{
                            number = new Number(incomingNumber);
                        }
                        rejectCall(context, number,context.getString(R.string.receiver_notify_no_call_allowed));
                    }
                    // Αν η ρύθμιση είναι να επιτρέπονται μόνο κλήσεις από επαφές
                    else if (settings.getCallBlockingMode() == BlockingModes.ALLOW_CONTACTS) {
                        if (!isNumberPresentInContacts(context, incomingNumber)) {
                            Log.i(TAG, "Αριθμός εκτός επαφών: " + incomingNumber);
                            rejectCall(context, new Number(incomingNumber), context.getString(R.string.receiver_notify_not_found_in_contacts));
                        }
                    }
                    else {
                        DbHelper dbHelper = new DbHelper(context);
                        try {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            Cursor c = db.query(Number._TABLE, null, "? LIKE " + Number.NUMBER, new String[]{incomingNumber}, null, null, null);
                            boolean inList = c.moveToNext();
                            // block calls from the numbers stored in list
                            if (inList && settings.getCallBlockingMode() == BlockingModes.BLOCK_LIST) {
                                Log.i(TAG, "Αριθμός εντός λίστας: " + incomingNumber);
                                ContentValues values = new ContentValues();
                                DatabaseUtils.cursorRowToContentValues(c, values);
                                Number number = Number.fromValues(values);

                                rejectCall(context, number, context.getString(R.string.receiver_notify_number_was_in_list));

                                values.clear();
                                values.put(Number.LAST_CALL, System.currentTimeMillis());
                                values.put(Number.TIMES_CALLED, number.timesCalled + 1);
                                db.update(Number._TABLE, values, Number.NUMBER + "=?", new String[]{number.number});

                                BlacklistObserver.notifyUpdated();

                            }
                            // allow calls only from numbers stored in list
                            else if (!inList && settings.getCallBlockingMode() == BlockingModes.ALLOW_ONLY_LIST_CALLS) {
                                Log.i(TAG, "Αριθμός εκτός λίσταςt: " + incomingNumber);

                                Number number;
                                if(isNumberPresentInContacts(context, incomingNumber)){
                                    String name = getCallerID(context, incomingNumber);
                                    number = new Number(incomingNumber, name);
                                }
                                else{
                                    number = new Number(incomingNumber);
                                }

                                rejectCall(context, number, context.getString(R.string.receiver_notify_number_was_not_in_list));
                                BlacklistObserver.notifyUpdated();
                            }
                            c.close();
                        } finally {
                            dbHelper.close();
                        }
                    }


                }
            }
            else {
                Log.d(TAG, "Did not match " + extraState + " with " + TelephonyManager.EXTRA_STATE_RINGING + ", " + TelephonyManager.EXTRA_STATE_OFFHOOK + ", or " + TelephonyManager.EXTRA_STATE_IDLE);
            }
        }
    }

    @SuppressLint("Ελλειπής άδεια")
    protected void rejectCall(@NonNull Context context, Number number, String reason) {

        if (!AlreadyOnCall) {
            boolean failed = false;
            // Για Android 9 και νεότερες εκδόσεις
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                try {
                    telecomManager.endCall();
                    Log.d(TAG, "Invoked 'endCall' on TelecomManager");
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't end call with TelecomManager", e);
                    failed = true;
                }
            }
            // Για παλαιότερες εκδόσεις Android
            else {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Method m = tm.getClass().getDeclaredMethod("getITelephony");
                    m.setAccessible(true);

                    ITelephony telephony = (ITelephony) m.invoke(tm);

                    telephony.endCall();
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't end call with TelephonyManager", e);
                    failed = true;
                }
            }
            if (failed) {
                Toast.makeText(context, context.getString(R.string.call_blocking_unsupported), Toast.LENGTH_LONG).show();
            }
        }

        Settings settings = new Settings(context);
        if (settings.showNotifications()) {
            // Δημιουργία καναλιού ειδοποίησης για Android 8 και νεότερες εκδόσεις
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel(
                        "default", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(reason);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notify = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher_small)
                    .setContentTitle(reason)
                    .setContentText(number != null ? (number.name != null ? number.name : number.number) : context.getString(R.string.receiver_notify_private_number))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setShowWhen(true)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, BlacklistActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .addPerson("tel:" + number)
                    .setGroup("rejected")
                    .setChannelId("default")
                    .setGroupSummary(true) /* fix notifications not appearing on kitkat: https://stackoverflow.com/a/37070917/674685 */
                    .build();

            String tag = number != null ? number.number : "private";
            NotificationManagerCompat.from(context).notify(tag, NOTIFY_REJECTED, notify);


        }

    }

    @SuppressLint("MissingPermission")
    private boolean isNumberPresentInContacts(Context context, String incomingNumber) {
        return getCallerID(context, incomingNumber) != null;
    }

    private String getCallerID(Context context, String incomingNumber){
        Cursor cursor = null;
        String name = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
            cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.i(TAG, "Received call from contact: " + name);

            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name;
    }

}
