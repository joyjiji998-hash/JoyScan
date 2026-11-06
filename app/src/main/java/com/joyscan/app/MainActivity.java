
package com.joyscan.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends Activity {

    private UsbManager usbManager;
    private TextView status;
    private TextView deviceInfo;

    private static final String ACTION_USB_PERMISSION =
            "com.joyscan.app.USB_PERMISSION";

    private final BroadcastReceiver permissionReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                boolean granted = intent.getBooleanExtra(
                        UsbManager.EXTRA_PERMISSION_GRANTED, false);

                if (granted) {
                    status.setText("USB permission granted!");
                } else {
                    status.setText("USB permission denied.");
                }
                checkUSB();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usbManager = (UsbManager) getSystemService(
                Context.USB_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 32);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("JoyScan");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setTextSize(18);
        status.setText("Checking USB connection...");

        deviceInfo = new TextView(this);
        deviceInfo.setTextSize(16);

        Button connectButton = new Button(this);
        connectButton.setText("Check USB Connection");
        connectButton.setOnClickListener(v -> checkUSB());

        Button scanButton = new Button(this);
        scanButton.setText("Scan Document");
        scanButton.setEnabled(false);
        scanButton.setOnClickListener(v ->
                status.setText(
                    "Scanner engine is not installed yet."));

        layout.addView(title);
        layout.addView(status);
        layout.addView(deviceInfo);
        layout.addView(connectButton);
        layout.addView(scanButton);

        setContentView(layout);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                    permissionReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(permissionReceiver, filter);
        }

        checkUSB();
    }

    private void checkUSB() {

        HashMap<String, UsbDevice> devices =
                usbManager.getDeviceList();

        if (devices.isEmpty()) {
            status.setText("No USB device detected.");
            deviceInfo.setText(
                    "Connect your printer using USB OTG.");
            return;
        }

        for (UsbDevice device : devices.values()) {

            String name = device.getManufacturerName();

            if (name == null) {
                name = "Unknown manufacturer";
            }

            deviceInfo.setText(
                    "Device: " + name +
                    "\nProduct: " + device.getProductName() +
                    "\nVendor ID: " + device.getVendorId() +
                    "\nProduct ID: " + device.getProductId()
            );

            if (usbManager.hasPermission(device)) {
                status.setText("USB device detected and permitted.");
            } else {
                status.setText("USB device detected. Requesting permission...");

                PendingIntent permissionIntent =
                        PendingIntent.getBroadcast(
                                this,
                                0,
                                new Intent(ACTION_USB_PERMISSION)
                                        .setPackage(getPackageName()),
                                PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                        );

                usbManager.requestPermission(
                        device, permissionIntent);
            }

            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usbManager != null && status != null) {
            checkUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(permissionReceiver);
    }
          }
          
