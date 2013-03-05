package edu.mines.robotics.clementinecommandcontrolprotocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class ControlActivity extends Activity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    TextView statusText;
    ArrayList<Button> buttons;
    Button connectButton, disconnectButton;
    SparseArray<byte[]> buttonHash;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		createHash();
		buttons = new ArrayList<Button>();
		addButtons();
		connectButton = (Button) findViewById(R.id.connectButton);
		disconnectButton = (Button) findViewById(R.id.disconnectButton);
		statusText = (TextView) findViewById(R.id.statusText);
		
		//Connect Button
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                	findBT();
    				openBT();
                } catch (Exception e) {
                	Log.e("CCCP", "Failure", e);
                }
            }
        });
        
        // Disconnect Button
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                	closeBT();
                } catch (Exception e) {
                	Log.e("CCCP", "Failure", e);
                }
            }
        });
        
	}
	//send 0xE2 0x68 0x07 to set reverse, 0xE2 0x5C 0x0B for neutral, 0xE2 0x50 0x0F for forward.
	private void createHash() {
		buttonHash = new SparseArray<byte[]>();
		byte pin2 = (byte) 0xE2;
		byte pin3 = (byte) 0xE3;
		byte pin4 = (byte) 0xE4;
		byte pin5 = (byte) 0xE5;
		byte pin6 = (byte) 0xE6;
		byte pin7 = (byte) 0xE7;
		byte pin8 = (byte) 0xE8;
		byte pin9 = (byte) 0xE9;
		byte pin10 = (byte) 0xEA;
		byte pin11 = (byte) 0xEB;
		byte pin12 = (byte) 0xEC;
		byte fwd1 = (byte) 0x50;
		byte fwd2 = (byte) 0x0F;
		byte rev1 = (byte) 0x68;
		byte rev2 = (byte) 0x07;
		
		buttonHash.append(R.id.driveFwd, new byte[] {pin2, fwd1, fwd2});
		buttonHash.append(R.id.driveBck, new byte[] {pin2, rev1, rev2});
		buttonHash.append(R.id.baseLeft, new byte[] {pin3, fwd1, fwd2});
		buttonHash.append(R.id.baseRight, new byte[] {pin3, rev1, rev2});
		buttonHash.append(R.id.armUp, new byte[] {pin4, fwd1, fwd2});
		buttonHash.append(R.id.armDown, new byte[] {pin4, rev1, rev2});
		buttonHash.append(R.id.turnRight, new byte[] {pin5, rev1, rev2});
		buttonHash.append(R.id.turnLeft, new byte[] {pin5, fwd1, fwd2});
		buttonHash.append(R.id.wristUp, new byte[] {pin6, fwd1, fwd2});
		buttonHash.append(R.id.wristDown, new byte[] {pin6, rev1, rev2});
		buttonHash.append(R.id.elbowLeft, new byte[] {pin7, fwd1, fwd2});
		buttonHash.append(R.id.elbowRight, new byte[] {pin7, rev1, rev2});
		buttonHash.append(R.id.clawOpen, new byte[] {pin8, fwd1, fwd2});
		buttonHash.append(R.id.clawClose, new byte[] {pin8, rev1, rev2});
		buttonHash.append(R.id.rotateLeft, new byte[] {pin9, fwd1, fwd2});
		buttonHash.append(R.id.rotateRight, new byte[] {pin9, rev1, rev2});
		
	}
	
	// add buttons to array with touch listeners
	void addButtons() {		
		Button driveFwd = (Button) findViewById(R.id.driveFwd);
		Button driveBck = (Button) findViewById(R.id.driveBck);
		Button baseLeft = (Button) findViewById(R.id.baseLeft);
		Button baseRight = (Button) findViewById(R.id.baseRight);
		Button armUp = (Button) findViewById(R.id.armUp);
		Button armDown = (Button) findViewById(R.id.armDown);
		Button turnRight = (Button) findViewById(R.id.turnRight);
		Button turnLeft = (Button) findViewById(R.id.turnLeft);
		Button wristUp = (Button) findViewById(R.id.wristUp);
		Button wristDown = (Button) findViewById(R.id.wristDown);
		Button elbowLeft = (Button) findViewById(R.id.elbowLeft);
		Button elbowRight = (Button) findViewById(R.id.elbowRight);
		Button clawOpen = (Button) findViewById(R.id.clawOpen);
		Button clawClose = (Button) findViewById(R.id.clawClose);
		Button rotateLeft = (Button) findViewById(R.id.rotateLeft);
		Button rotateRight = (Button) findViewById(R.id.rotateRight);
		
		buttons.add(driveFwd);
		buttons.add(driveBck);
		buttons.add(baseLeft);
		buttons.add(baseRight);
		buttons.add(armUp);
		buttons.add(armDown);
		buttons.add(turnRight);
		buttons.add(turnLeft);
		buttons.add(wristUp);
		buttons.add(wristDown);
		buttons.add(elbowLeft);
		buttons.add(elbowRight);
		buttons.add(clawOpen);
		buttons.add(clawClose);
		buttons.add(rotateLeft);
		buttons.add(rotateRight);
		
		// add touch listeners
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setOnTouchListener(listener);
		}
	}
	
	/*
	 * Status of testing:
	 * - bluetooth does not work on emulation
	 * - looking at url below for example Android code
	 * - Added a button to try to connect. Prereq: device has paired with the bluetooth module (pass = 1234)
	 * - Button successfully displays "No bluetooth adapter available" in emulation
	 * 
	 * TODO
	 * - check baud requirements - currently Arduino expects 57600, but that can be easily changed.
	 * - implement openBT(), then send serial bytes.
	 * - for now, ignore receiving data
	 * Example commands:
	 * - send 0xE2 0x68 0x07 to set reverse, 0xE2 0x5C 0x0B for neutral, 0xE2 0x50 0x0F for forward.
	 * - E2 means pin 2, similarly for other pins (pin 4 is E4, etc).
	 */
	// http://bellcode.wordpress.com/2012/01/02/android-and-arduino-bluetooth-communication/
	
	void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            statusText.setText("No bluetooth adapter available");
            return;
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	// name of our adapter
                if (device.getName().equals("BlueClementine-5ACA")) {
                    mmDevice = device;
                    statusText.setText("Bluetooth Device Found");
                    return;
                } else {
                    statusText.setText(device.getName() + " found instead");
                }
            }
        }
    }
	
	void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        
        statusText.setText("Bluetooth Opened");
    }
	
	void closeBT() throws IOException {
        mmOutputStream.close();
        mmSocket.close();
        
        statusText.setText("Bluetooth Closed");
    }
	
	private byte[] getByteArray(int viewId, boolean released) {
		byte[] byteArray = buttonHash.get(viewId);
		byte neutral1 = (byte) 0x5C;
		byte neutral2 = (byte) 0x0B;
		if (released) {
			byteArray[1] = neutral1;
			byteArray[2] = neutral2;
		}
		return byteArray;
	}

	private OnTouchListener listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			try {
				if (null == mmOutputStream) {
					throw new NullPointerException();
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// send full speed signal to pin
					mmOutputStream.write(getByteArray(v.getId(), false));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// send neutral signal to pin 
					mmOutputStream.write(getByteArray(v.getId(), true));
				}
			} catch (NullPointerException e) {
				statusText.setText("No device connected");
			} catch (IOException e) {
				// oh no!
			}
			return false;
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_control, menu);
		return true;
	}

    @Override
    public void onStart() {
        super.onStart();        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();        
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
	
}
