package edu.psu;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import edu.psu.database.DatabaseHandler;
import edu.psu.database.Gulp;
import edu.psu.graph.DayBarGraph;
import edu.psu.graph.MonthBarGraph;
import edu.psu.graph.WeekBarGraph;

public class HarrisWaterBottleActivity extends Activity {
	private static final String BLUETOOTH_LOG_TAG = "Bluetooth";
	
	private static final double DEVICE_CONSTANT = 7.5;
	private static final double OZ_PER_LITER = 33.814;
	private static final double SEC_PER_MIN = 60.0;

	private static final int BUFFER_SIZE = 128;
	private static final int TOAST_DURATION = 2;
	
	private static final int DATE_PICKER_ID = 990;
	private static final int WEEK_PICKER_ID = 991;
	private static final int MONTH_PICKER_ID = 992;
	private static final int BLUETOOTH_PICKER_ID = 993;
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private static int currentDevice;
	
	private BluetoothAdapter bluetooth;
	private List<BluetoothDevice> pairedDevices;
	
	private TextView deviceName;
	private TextView drinkRate;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        deviceName = (TextView)findViewById(R.id.device_name);
        drinkRate = (TextView)findViewById(R.id.drink_rate);
    }
    
    public void bluetoothConnectionHandler(View v) {
	    showDialog(BLUETOOTH_PICKER_ID);
    }
    
	public void dayGraphHandler(View v) {
		showDialog(DATE_PICKER_ID);
    }
	
	public void weekGraphHandler(View v) {
		showDialog(WEEK_PICKER_ID);
	}
	
	public void monthGraphHandler(View v) {
		showDialog(MONTH_PICKER_ID);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	Calendar c;
    	int year, month, date;
    	
    	c = Calendar.getInstance();
    	
    	year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		date = c.get(Calendar.DATE);
    	
        switch (id) {
            case DATE_PICKER_ID:
                return new DatePickerDialog(this, dateSetListener, year, month, date);
            case WEEK_PICKER_ID:
            	return new DatePickerDialog(this, weekSetListener, year, month, date);
            case MONTH_PICKER_ID:
                return new DatePickerDialog(this, monthSetListener, year, month, date);
            case BLUETOOTH_PICKER_ID:
            	Builder builder;
            	List<String> deviceNames;
            	
            	try {
            		connectToBluetooth();
            	} catch(IOException ioe) {
            		Toast.makeText(this, ioe.getMessage(), TOAST_DURATION).show();
            		return null;
            	}
            	
    		    pairedDevices = new ArrayList<BluetoothDevice>(bluetooth.getBondedDevices());
    		    
    		    if (pairedDevices.size() == 0) {
    		    	Toast.makeText(this, "No paired devices found!", TOAST_DURATION).show();
    		    	return null;
    		    }
    		    
    		    deviceNames = new ArrayList<String>();
		    	for (BluetoothDevice bd : pairedDevices) {
		    		deviceNames.add(bd.getName());
		    	}
            	
            	builder = new AlertDialog.Builder(this);
            	builder.setTitle("Pick A Device");
            	builder.setItems(deviceNames.toArray(new CharSequence[deviceNames.size()]), bluetoothlistener);
            	return builder.create();
            default:
            	return super.onCreateDialog(id);
        }
    }
    
	private void switchToDateGraph(final int month, final int date, final int year) {
		Intent intent;
		DayBarGraph dayBarGraph;
		
		dayBarGraph = new DayBarGraph();
		intent = dayBarGraph.getIntent(this, month, date, year);
		startActivity(intent);
	}
	
	private void switchToWeekGraph(final int week, final int year) {
		Intent intent;
		WeekBarGraph weekBarGraph;
		
		weekBarGraph = new WeekBarGraph();
		intent = weekBarGraph.getIntent(this, week, year);
		startActivity(intent);
	}
	
	private void switchToMonthGraph(final int month, final int year) {
		Intent intent;
		MonthBarGraph monthBarGraph;
		
		monthBarGraph = new MonthBarGraph();
		intent = monthBarGraph.getIntent(this, month, year);
		startActivity(intent);
	}
	
	private android.content.DialogInterface.OnClickListener bluetoothlistener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			BluetoothSocket socket;
			
			currentDevice = which;
			
			try {
				socket = pairedDevices.get(currentDevice).createRfcommSocketToServiceRecord(MY_UUID);
				
				if (socket != null) {
					new BluetoothTask().execute(socket);
					deviceName.setText(pairedDevices.get(currentDevice).getName()
										+ " (" + pairedDevices.get(currentDevice).getAddress() + ")");
				}
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Could not create socket!", TOAST_DURATION);
				Log.e(BLUETOOTH_LOG_TAG, e.getMessage(), e);
			}
		}
	};
	
    private DatePickerDialog.OnDateSetListener dateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    switchToDateGraph(monthOfYear, dayOfMonth, year);
                }
            };
            
    private DatePickerDialog.OnDateSetListener weekSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                	Calendar cal;
                	cal = Calendar.getInstance();
                	cal.set(year, monthOfYear, dayOfMonth);
                	
                	int week = cal.get(Calendar.WEEK_OF_YEAR);
                    switchToWeekGraph(week, year);
                }
            };
            
    private DatePickerDialog.OnDateSetListener monthSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    switchToMonthGraph(monthOfYear, year);
                }
            };
    
    private void connectToBluetooth() throws IOException {
    	bluetooth = BluetoothAdapter.getDefaultAdapter();
    	
    	if (bluetooth == null) {			// No bluetooth adapter found
    		throw new IOException("No bluetooth adapter found!");
    	} else {							// Bluetooth adapter found
    		if (!bluetooth.isEnabled()) {
    			Toast.makeText(this, "Please enable Bluetooth", TOAST_DURATION);
    			Intent enableBtIntent = new Intent(bluetooth.ACTION_REQUEST_ENABLE);
    	        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    		}
    	}
    }
    
    private class BluetoothTask extends AsyncTask<BluetoothSocket, LinkedList<Byte>, String> {
    	private static final String SUCCESS = "Succeeded";
    	private static final String FAILURE = "Failed";
    	
    	private BluetoothSocket socket;
    	
		@Override
		protected String doInBackground(BluetoothSocket... sockets) {
			InputStream in;
			byte[] buffer;
			int bytesRead;
			
			LinkedList<Byte> bufferList;
			
			socket = sockets[0];
			
			try {
				socket.connect();
				in = socket.getInputStream();
			} catch (IOException e) {
				Log.e(BLUETOOTH_LOG_TAG, e.getMessage(), e);
				Toast.makeText(getApplicationContext(), "Could not open connection to device", TOAST_DURATION);
				return FAILURE;
			}
			
			if (in != null) {
				buffer = new byte[BUFFER_SIZE];
				Arrays.fill(buffer, (byte) 0);
				bufferList = new LinkedList<Byte>();
				
		        while (true) {
		            try { 
		            	bytesRead = in.read(buffer);

		            	for (int i = 0; i < 0 + bytesRead; i++) {
		            		if ((buffer[i] & 0xFF) == 10) {
		            			publishProgress(new LinkedList<Byte>(bufferList));
		            			bufferList.clear();
		            		} else {
		            			bufferList.addLast(buffer[i]);
		            		}
		            	}
		            } catch (IOException e) {
		                Log.e(BLUETOOTH_LOG_TAG, "disconnected", e);
		                Toast.makeText(getApplicationContext(), "Disconnected from device", TOAST_DURATION);
		                return FAILURE;
	                }
		        }
			}
			
			return SUCCESS;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					Log.e(BLUETOOTH_LOG_TAG, "Error disconnecting", e);
				}
			}
		}
		
		@Override
		protected void onProgressUpdate(LinkedList<Byte>... values) {
			double ounces;
			StringBuffer s;
			DatabaseHandler handler;
			
			super.onProgressUpdate(values);
			
			s = new StringBuffer();
			for (Iterator<Byte> iter = values[0].iterator(); iter.hasNext();) {
				s.append(iter.next().intValue() - 48);
			}

			ounces = Double.parseDouble(s.toString()) / DEVICE_CONSTANT * OZ_PER_LITER / SEC_PER_MIN;
			
			drinkRate.setText(Double.toString(ounces));
			
			if (ounces != 0) {
				handler = new DatabaseHandler(getApplicationContext());
				handler.addGulp(new Gulp(new Date(), ounces));
			}
		}
    }
}