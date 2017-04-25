package adamoconnor.informe.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import adamoconnor.informe.R;

public class CheckConnectivity {

    // declare booleans.
    private static boolean internetOn;
    private static boolean locationOn;

    /**
     * check if internet is on.
     * @return
     * return the boolean if internet is connected.
     */
    public static boolean isInternetOn() {
        return internetOn;
    }

    /**
     * check if location is on.
     * @return
     * return the boolean if location is connected.
     */
    public static boolean isLocationOn() {
        return locationOn;
    }

    /**
     * used to check if internet is connected if not
     * user is sent to the settings menu to turn internet
     * on.
     * @param context
     * get the activity to display
     */
    public static void startInternetEnabled(final Context context) {

        // check the internets connectivity
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean network_enabled = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(myIntent);
                    //go to internet settings.
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    ((Activity)context).finish();
                }
            });
            dialog.show();
        }

        network_enabled = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(network_enabled) {
            internetOn = true;
        }
        else {
            internetOn = false;
        }

    }

    /**
     * used to check if location is connected
     * @param context
     * get the activity to display
     */
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    /**
     * used to check if location is connected if not
     * user is sent to the settings menu to turn internet
     * on.
     * @param context
     * get the activity to display
     */
    public static void startLocationEnabled(final Context context) {

        if(!isLocationEnabled(context)) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    ((Activity)context).finish();

                }
            });
            dialog.show();
        }

        if(isLocationEnabled(context)) {
            locationOn = true;
        }
        else {
            locationOn = false;
        }

    }



}
