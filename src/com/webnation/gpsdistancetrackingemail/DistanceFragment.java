package com.webnation.gpsdistancetrackingemail;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class DistanceFragment extends SherlockFragment {
	
	//views to be used in this framgment
	Button btnShowLocation;
    Button btnShowEndLocation;
    Button btnClearData;
    TextView tvIntialLocation;
    TextView tvEndLocation;
    TextView tvDistance;
    
    //gps class that does all the heavy lifting for this fragment
    GPSTracker gps;
    
    //class to hold all information about the trip
    GPSDateCoordinates mGPSDC;
    
    //for Logcat messages
    static String TAG = "DistanceFragment";
    
    //does all the communicating with the database
    private DataBaseHelper myDbHelper;
    
    //get shared preferences for units
    private static final String KEY_UNITS = "units";
    //if we had some coordinates before rotate or destroy, save them
    private static final String KEY_START_POSITION_LONG = "startLong";
    private static final String KEY_START_POSITION_LAT = "startLat";
    
    //id of row of inserted trip
    long insertedID = 0;
    
    //set 0 for miles, 1 for kilometers
    int mMilesOrKilometers = 0;
    
    /* for emulator: 
     * telnet localhost 5554
     * geo fix (longitude) (latitude)
     */

	
    /*
     * factory instantiation method
     */
	public static DistanceFragment newInstance() {
		// Create a new fragment instance
		DistanceFragment detail = new DistanceFragment();

        return detail;
	}
	
	//interface to the GPSTrackingActivity activity class
    private OnCoordinatesAddedListener listener;
	
	/*
	 * Way to communicate with the activity
	 * http://www.vogella.com/articles/AndroidFragments/article.html
	 * GPSTrackingActivity must implement this interface
	 * 
	 * messages pass this way
	 * DistanceFragment --> GPSTrackingActivity --> TripsFragment
	 */
	public interface OnCoordinatesAddedListener {
	      public void onNewItemsAdded();

	}
	

	//inflates layouts and instantiates all views
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View distance =inflater.inflate(R.layout.distance_frag, container, false);
		tvIntialLocation = (TextView)distance.findViewById(R.id.tvIntialLocation);
        tvEndLocation = (TextView)distance.findViewById(R.id.tvEndLocation);
        tvDistance = (TextView)distance.findViewById(R.id.tvDistance);
        btnShowLocation = (Button) distance.findViewById(R.id.btnShowLocation);
        btnShowEndLocation = (Button)distance.findViewById(R.id.btnEndLocation);
        btnClearData = (Button)distance.findViewById(R.id.btnClearData);
        myDbHelper = new DataBaseHelper(getActivity());
		return distance;
	}
	
	
	//sets up interface so this fragment can talk to the Activity Fragment
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (activity instanceof OnCoordinatesAddedListener) {
	        listener = (OnCoordinatesAddedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implemenet DistanceFragment.OnCoordinatesAddedListener");
	      }
	}
	
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onActivityCreated(savedInstanceState);
		//instantiate object for use
		mGPSDC = new GPSDateCoordinates();
		
		//instantiate GPS
		gps = new GPSTracker(getActivity());
		Log.d(TAG, "onActivityCreated");
		
		//if there is a Start Coordinate from last session, read it into fragment
		getSharedPrefs(); 
				
		
		// show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	btnShowLocation.setEnabled(false);
            	btnShowEndLocation.setEnabled(true);
            	if ((!tvIntialLocation.getText().equals("")) && (!tvIntialLocation.getText().equals("Start Location"))) { 
            		ClearData();
            	}
 
                // check if GPS enabled
                if(gps.canGetLocation()){
 
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    //format output
                    NumberFormat format = NumberFormat.getInstance();
                    format.setMaximumFractionDigits(4);
                    tvIntialLocation.setText("Start Location is - \nLat: " + format.format(latitude) + "\nLong: " + format.format(longitude));
                    
                    //we've got first coordinates, store them in the database
                    insertedID = myDbHelper.InsertCoordinates(latitude, longitude, true, 0);
                    Log.d("InsertedID ", "value =" + insertedID);
                    
                    //read trip data into GPSDateCoordinate Object
                    mGPSDC = new GPSDateCoordinates(insertedID, latitude, longitude);
                    
                                       
        			Log.d("Start Lat onCLick", "value =" + mGPSDC.getStartLatitude());
        			Log.d("Start Long onClick", "value =" + mGPSDC.getStartLongitude());

                    // \n is for new line
                    Toast.makeText(getActivity().getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
 
            }
        });
        
     // show location button click event
        btnShowEndLocation.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
            	btnShowLocation.setEnabled(true);
            	btnShowEndLocation.setEnabled(false);
                // check if GPS enabled
                if(gps.canGetLocation()){
 
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    //format output
                    NumberFormat format = NumberFormat.getInstance();
                    format.setMaximumFractionDigits(4);
                    
                    //update fragment display
                    tvEndLocation.setText("End Location is - \nLat: " + format.format(latitude) + "\nLong: " + format.format(longitude));
                    //insert end coordinates into database
                    insertedID = myDbHelper.InsertCoordinates(latitude, longitude, false, insertedID);
                    //read all the information into the GPSDateCoordinate object
                    mGPSDC = myDbHelper.SelectStartEndCoordinates(insertedID);

                    Log.d("InsertedID ", "value =" + insertedID);
                    Log.d("End Lat onCLick", "value =" + mGPSDC.getEndLatitude());
        			Log.d("End Long onClick", "value =" + mGPSDC.getEndLongitude());
        			
        			//as the name implies
        			mGPSDC.CalcDistance();
        			
        			//updates the distance in the trip database row
        			myDbHelper.InsertDistance(mGPSDC);
        			
                    //updates distance to fragment display
                    WriteDistance();
                    
                    //routes call to the activity so the trips fragment can be updated with new trip
                    listener.onNewItemsAdded();
                    
                    // \n is for new line
                    Toast.makeText(getActivity().getApplicationContext(), "End Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
 
            }
        });
        
       // show location button click event
        btnClearData.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View arg0) {
        		ClearData();
        	}
        });
		
	}
	
	//if coordinates exists, update StartText view
	public void SetStartText(double latitude, double longitude) { 
		NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(4);
        tvIntialLocation.setText("Start Location is - \nLat: " + format.format(latitude) + "\nLong: " + format.format(longitude));
        
	}
	
	//as the name implies
	public void ClearData() { 
    	tvEndLocation.setText("End Location");
		tvIntialLocation.setText("Start Location");
		tvDistance.setText("");
		insertedID = 0;
		//zeros out object
		mGPSDC = new GPSDateCoordinates();
    }
	
	/* gets called from the GPSTrackingActivity */
	public void setMileOrKilometers(int MilesOrKm) { 
		mMilesOrKilometers = MilesOrKm;
	}
	
	/* writes out the distance between GPS coordinates 
	 * can be called from the GPSTrackingActivity 
	 */
	public void WriteDistance() { 
		NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
		String UnitsOfMeasure = "miles";
		//conversion from meters to whatever the default units of measurement is
		int Divider = 1604;
		if (mMilesOrKilometers == 1) { 
			UnitsOfMeasure = "km";
			Divider = 1000;
		}
		
		tvDistance.setText(String.valueOf(format.format(mGPSDC.getDistance()/Divider)) + " " + UnitsOfMeasure);
	}
	
	/*
	 * as the name implies
	 * saves preferences for automatic and difficulty
	 */
	public void PopulateSharedPrefs() { 
		try { 
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		boolean Success = settings.edit().putInt(KEY_UNITS, mMilesOrKilometers).commit();
		settings.edit().putFloat(KEY_START_POSITION_LAT, (float)mGPSDC.getStartLatitude());
		settings.edit().putFloat(KEY_START_POSITION_LONG, (float)mGPSDC.getStartLongitude());
		}
		catch (Exception e) { 
			Log.d(TAG,e.toString());
		}
		
	}
    
    /*
	 * as the name implies
	 * gets preferences for automatic and difficulty
	 */
	public void getSharedPrefs() { 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		   mMilesOrKilometers = prefs.getInt(KEY_UNITS,0);
		   if (prefs.getLong(KEY_START_POSITION_LONG, 0) > 0) { 
		       mGPSDC.setStartLatitude((double)prefs.getFloat(KEY_START_POSITION_LONG,0));
		   }
		   if (prefs.getLong(KEY_START_POSITION_LAT, 0) > 0) { 
		       mGPSDC.setStartLatitude((double)prefs.getFloat(KEY_START_POSITION_LAT,0));
		   }
		   if ( (prefs.getLong(KEY_START_POSITION_LONG, 0) > 0) || (prefs.getLong(KEY_START_POSITION_LAT, 0) > 0)) { 
			   SetStartText(mGPSDC.getStartLatitude(), mGPSDC.getStartLongitude());
			   btnShowEndLocation.setEnabled(true);
			   btnShowLocation.setEnabled(false);
		   }
		   
	   
	      
	}


	@Override
	public void onDestroy() {
		PopulateSharedPrefs();
		Log.d(TAG,"On Destroy");
		super.onDestroy();
		
	}

	@Override
	public void onPause() {
		PopulateSharedPrefs();
		Log.d(TAG,"On Pause");
		super.onPause();
	}

	@Override
	public void onResume() {
		getSharedPrefs();
		Log.d(TAG,"On Resume");
		super.onResume();
	}

	@Override
	public void onStart() {
		getSharedPrefs();
		Log.d(TAG,"On Start");
		super.onStart();
	}

	@Override
	public void onStop() {
		PopulateSharedPrefs();
		Log.d(TAG,"On Stop");
		super.onStop();
	}

}
