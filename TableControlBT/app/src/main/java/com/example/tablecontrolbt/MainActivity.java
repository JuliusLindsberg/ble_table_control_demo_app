package com.example.tablecontrolbt;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.AndroidViewModel;

import android.os.IBinder;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    TableHeightBLEService tableControlService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate()", "A");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent serviceIntent = new Intent(this, TableHeightBLEService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.i("onCreate()", "B");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("onCreateOptionsMenu()", "A");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("onOptionsItemSelected()", "A");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void adjustTable(android.view.View aview)
    {
        Log.i("adjustTable()", "A");
        //SeekBar bar = (SeekBar)findViewById(R.id.tableHeight);
        SeekBar bar = (SeekBar) findViewById(R.id.tableHeight);
        if(bar == null)
        {
            Log.i("adjustTable()", "bar is NULL");
        }
        tableControlService.adjustTableHeight((short) bar.getProgress());
        Log.i("adjustTable()", "B");
    }
    //this is supposed to control binding the tableControlService
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            tableControlService = ((TableHeightBLEService.LocalBinder) service).getService();
            if (!tableControlService.init()) {
                Log.e("onServiceConnected()", "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tableControlService = null;
        }
    };
}