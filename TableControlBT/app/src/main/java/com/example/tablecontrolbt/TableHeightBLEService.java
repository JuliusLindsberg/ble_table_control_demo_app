package com.example.tablecontrolbt;

import android.app.Activity;

//import libraries required for bluetooth low energy GATT client

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Filter;

public final class TableHeightBLEService extends Service
{

    public final static UUID UUID_DESK_CONTROL_SERVICE = UUID.fromString("6495c7ad-e0cc-4c2c-87c8-e8ad2f964b03");

    public class LocalBinder extends Binder {
        TableHeightBLEService getService() {
            return TableHeightBLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder binder = new LocalBinder();


    private short tableHeight;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private String deviceAddress;
    public final boolean init()
    {
        tableHeight = 0;
        Log.i("init()", "BEGIN");
        //manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager == null)
        {
            Log.i("asd", "error");
        }
        Log.i("init()", "MID");
        adapter = manager.getAdapter();
        scanner = adapter.getBluetoothLeScanner();
        if(scanner == null)
        {
            Log.i("asdasd", "SCANNER WAS NULL");
        }
        Log.i("init()", "END");
        return true;
    }

    private final BluetoothGattCallback GATTCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange()", "BEGIN");
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //inform the user that the device has been disconnected
            }
            Log.i("onConnectionStateChange()", "END");
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered()", "BEGIN");
            List<BluetoothGattService> services = gatt.getServices();
            for(int i = 0; i < services.size(); i++)
            {
                Log.i("Service",services.get(i).getUuid().toString());
            }
            BluetoothGattService controlService = gatt.getService(UUID_DESK_CONTROL_SERVICE);
            if(controlService == null)
            {
                Log.i("onServicesDiscovered()","No gatt service found");
            }
            List<BluetoothGattCharacteristic> gattCharacteristics = controlService.getCharacteristics();
            //only one characteristic in this service
            if(gattCharacteristics == null)
            {
                Log.i("onServicesDiscovered()","No characteristics found");
            }
            //BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
            BluetoothGattCharacteristic characteristic = controlService.getCharacteristic(UUID_DESK_CONTROL_SERVICE);
            if(characteristic == null)
            {
                Log.i("onServicesDiscovered()","No characteristic of index 0 found");
            }
            //desk height saved as little-endian bytes
            byte[] gattCharacteristicData = {(byte) (char)(tableHeight)};//, (byte) ((tableHeight >> 8) & 0xff)};
            characteristic.setValue(gattCharacteristicData);
            gatt.writeCharacteristic(characteristic);
            Log.i("CHARACTERS", String.valueOf((short)gattCharacteristicData[0]));// + " " + " " + (short)gattCharacteristicData[1]);
            Log.i("onServicesDiscovered()", "END");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead()", "EMPTY");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("onCharacteristicChanged()", "EMPTY");
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite()", "EMPTY");
        }
    };
    ScanCallback scanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("onScanResult()", "BEGIN");
            if(!result.isConnectable())
            {
                //results that are not connectable are not useful
                Log.i("onScanResult()", "NOT CONNECTABLE");
                return;
            }
            //connect to device
            ScanRecord record = result.getScanRecord();
            String deviceName = record.getDeviceName();
            List<ParcelUuid> UUIDs = record.getServiceUuids();
            if(UUIDs != null) {
                //we found the correct UUID
                for (int i = 0; i < UUIDs.size(); i++) {
                    Log.i("UUID", UUIDs.get(i).toString());
                    if(UUIDs.get(i).getUuid().equals(UUID_DESK_CONTROL_SERVICE)) {
                        Log.i("onScanResult()","CONNECTING");
                        BluetoothDevice device = adapter.getRemoteDevice(result.getDevice().getAddress());
                        BluetoothGatt gattDevice = device.connectGatt(null, false, GATTCallBack);
                        scanner.stopScan(scanCallBack);
                    }
                }
            }
            /*final BluetoothDevice device = adapter.getRemoteDevice(result.getDevice().getAddress());
            if(device == null)
            {
                //connection failed!
                Log.i("onScanResult()", "CONNECTION FAILED");
                return;
            }
            //WARNING: no idea what the "null" context is gonna do here. Probably will destroy everything...
            device.connectGatt(null, false, GATTCallBack);*/
            Log.i("onScanResult()", "END");
            return;
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //no idea if this would ever even happen with our filter so...
            Log.i("onBatchScanResults()", "EMPTY");
            return;
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("onScanFailed()", "EMPTY");
            //display failed to find device message to app user
        }
    };
    public void adjustTableHeight(short targetHeight)
    {
        Log.i("TARGET HEIGHT:", String.valueOf(targetHeight));
        tableHeight = targetHeight;
        Log.i("adjustTableHeight()", "A");
        ParcelUuid tableUUID = new ParcelUuid(UUID_DESK_CONTROL_SERVICE);
        //this must not be the right way that the builder pattern is supposed to be used...
        Log.i("adjustTableHeight()", "B");
        ScanFilter.Builder builder = new ScanFilter.Builder()
                //.setServiceUuid(tableUUID)
                .setDeviceName("Test beacon");
        Log.i("adjustTableHeight()", "C");
        ScanFilter filter = builder.build();
        Log.i("adjustTableHeight()", "D");

        ScanSettings.Builder settingBuilder = new ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT);
        ScanSettings scanSettings = settingBuilder.build();
        Log.i("adjustTableHeight()", "E");

        Log.i("adjustTableHeight()", "F");
        //scanner.startScan(Collections.singletonList(filter), scanSettings, scanCallBack);
        scanner.startScan(scanCallBack);
        Log.i("adjustTableHeight()", "G");

    }
};
