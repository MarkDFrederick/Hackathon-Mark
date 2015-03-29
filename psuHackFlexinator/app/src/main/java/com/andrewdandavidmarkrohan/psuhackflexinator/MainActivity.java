package com.andrewdandavidmarkrohan.psuhackflexinator;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;



public class MainActivity extends ActionBarActivity {

    int[] data = new int[100];
    int commandCount = 0;

    private static final int REQUEST_ENABLE_BT = 5;
    // Enter random UUID from https://www.uuidgenerator.net/
    private static UUID MY_UUID = UUID.fromString("791850d6-9b1f-42d3-af0e-24a1afa33038");
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothSocket fallbackSocket;
    private InputStream mBluetoothInputStream;
    private Set<BluetoothDevice> pairedDevices;

    private Camera camera = Camera.open();
    private boolean isFlashlightOn = false;
    Parameters params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.out.println("NO BLUETOOTH SUPPORT");
        }

        // Enable Bluetooth adapter
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            List pairedDeviceList = new ArrayList();
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in the alert dialog
                pairedDeviceList.add(device.getName() + "\n" + device.getAddress());
            }

            // Build an alert dialog to select the Bluetooth adapter from a list of paired devices
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select the attiny85 Bluetooth adapter");
            builder.setItems((CharSequence[]) pairedDeviceList.toArray(new
                    CharSequence[pairedDeviceList.size()]), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Get selected paired Bluetooth device
                    mBluetoothDevice = (BluetoothDevice)pairedDevices.toArray()[which];
                    System.out.println(mBluetoothDevice.getAddress());
                    try {
                        // Try to connect to Bluetooth device
                        mBluetoothSocket =
                                mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        mBluetoothSocket.connect();
                        mBluetoothInputStream = mBluetoothSocket.getInputStream();
                    } catch (IOException e) {
                        try {
                            // Try to connect to Bluetooth device again in case of error
                            Class<?> clazz = mBluetoothSocket.getRemoteDevice().getClass();
                            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                            Object[] params = new Object[]{Integer.valueOf(1)};

                            fallbackSocket = (BluetoothSocket)
                                    m.invoke(mBluetoothSocket.getRemoteDevice(), params);
                            fallbackSocket.connect();
                            mBluetoothInputStream = fallbackSocket.getInputStream();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (InvocationTargetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchMethodException e1) {
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();

                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        }



        // Handle button click to toggle LED
        // Output input into written thing
        //public class EventListenerActivity implements ActionListener {
        //}
        //}
        this.findViewById(R.id.receiveData).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {

                    int count = 0;
                    boolean isOnetoZero = false;
                    int test=0;
                    //EditText myText = (EditText) findViewById(R.id.blueIn);
                    while (mBluetoothInputStream.available() > 0 && count < 100 && isOnetoZero == false) {
                        //myText.setText(Integer.toString(mBluetoothInputStream.read()));

                        int value1 = mBluetoothInputStream.read()-'0';
                        int value2 = mBluetoothInputStream.read()-'0';
                        mBluetoothInputStream.read();

                        int value = 10*value1 + value2;
                        //System.out.println(Integer.toString(value));

                        if (value > 70)
                        {
                            test = 0;
                            //myText.setText(Integer.toString(test));
                            data[count] = test;

                        }
                        else if (value <= 70)
                        {
                            while (value<=70) {
                                test = 1;
                                //myText.setText(Integer.toString(test));
                                data[count] = test;
                            }

                            isOnetoZero = true;
                        }
                        count++;

                    }

                    for (int i = 0; i<100; i++)
                    {
                        if (data[i] == 1)
                            commandCount++;
                    }

                    if (commandCount >0)
                    {
                        toggleFlashlight();
                        System.out.println("works");
                    }
                /*else if (commandCount < 26)
                {
                    sendSMSMessage();
                }
                else if (commandCount < 51)
                {
                    Uri number = Uri.parse("tel:4843189828");
                    Intent callIntent = new Intent(Intent.ACTION_CALL, number);
                    startActivity(callIntent);
                }*/
                }

                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }



            }
        });



    Button button1 = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);



        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri number = Uri.parse("tel:4843189828");
                Intent callIntent = new Intent(Intent.ACTION_CALL, number);
                startActivity(callIntent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { //flashlight
            public void onClick(View v) {

               toggleFlashlight();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

               sendSMSMessage();
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void sendSMSMessage(){
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+14843189828", null, "Fuck Yeah Mother Fucker", null, null);
            Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "SMS failed try again", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }

    private void toggleFlashlight() {
        if (!isFlashlightOn) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashlightOn = true;
        }
        else {
            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashlightOn = false;
        }
    }

    private void setFlashlightOff() {
        //camera = Camera.open();
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
        isFlashlightOn = false;

    }

    @Override
    public void onPause() {
        super.onPause();

/*
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {


        }*/

    }





}
