package za.co.topitup.suppliers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class UsbDeviceReceiver extends BroadcastReceiver {

    private static final String ACTION_USB_PERMISSION = "com.example.packagename.USB_PERMISSION";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    public static boolean isUsbPermission = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        Log.e("ask permission","receiver.....");
        if (ACTION_USB_ATTACHED.equals(action)) {
            // Handle USB device attachment
            showToast(context, "USB Device Attached");


            Log.d("USB PERMISSION","has permission:- "+usbManager.hasPermission(device));
            // Check if permission is already granted for this device
            synchronized (this) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        Log.d("USB PERMISSION", "permission exists");
                        isUsbPermission = true;
                        // Permission granted, open the device

//                        SerialPortManagerSingleton.getInstance(context, null).openSerialPort((UsbManager) context.getSystemService(Context.USB_SERVICE));
                    }
                } else {
                    Log.d("USB PERMISSION", "permission does not exist");
                    isUsbPermission= false;
                    // Permission denied
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            new Intent(ACTION_USB_PERMISSION),
                            PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0)
                    );
                    usbManager.requestPermission(device, permissionIntent);
                }
            }

        } else if (ACTION_USB_DETACHED.equals(action)) {
            // Handle USB device detachment
            showToast(context, "USB Device Detached");
//            SerialPortManagerSingleton.releaseInstance();
        } else if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        Log.d("USB PERMISSION", "granted permission");
                        Log.d("USB PERMISSION","has permission:- "+usbManager.hasPermission(device));
                        // Permission granted, open the device
//                        SerialPortManagerSingleton.getInstance(context, null).openSerialPort((UsbManager) context.getSystemService(Context.USB_SERVICE));
                    }
                } else {
                    Log.d("USB PERMISSION", "Permission denied for device " + device);
                    // Handle permission denial
                }
            }
        }
    }
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}