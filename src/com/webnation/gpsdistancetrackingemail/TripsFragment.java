package com.webnation.gpsdistancetrackingemail;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class TripsFragment extends SherlockListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {
	
	Button btnNext;
	Button btnBack;
	TextView tvDate;
	TextView tvDistance;
	ListView lvTrips;
    GPSTracker gps;
    GPSDateCoordinates mGPSDC;
    //set 0 for miles, 1 for kilometers
    int mMilesOrKilometers = 0;
    //get shared preferences for units
    private static final String KEY_UNITS = "units";
    static String TAG = "TripsFragment";
    private DataBaseHelper myDbHelper;
    long insertedID = 0;
    Date mToday; 
    Calendar mcalToday;
    int BaseMonth = 0;
    int BaseYear = 0;
    long deleteid = 0;
    boolean mPreviousMonthsExists = false;
    boolean mFollowingMonthsExist = false;
    private static final int LOADER_ID = 0x02;
    private SimpleCursorAdapter adapter=null;
    private SQLiteCursorLoader loader=null;

	
	public static TripsFragment newInstance(int count) {
		// Create a new fragment instance
		TripsFragment detail = new TripsFragment();

        return detail;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		myDbHelper = new DataBaseHelper(getActivity());
		View trips =inflater.inflate(R.layout.trips_date_frag, null);
		tvDate = (TextView)trips.findViewById(R.id.tvDate);
		tvDistance = (TextView)trips.findViewById(R.id.distance);
		lvTrips = (ListView) trips.findViewById(android.R.id.list);
        btnNext = (Button) trips.findViewById(R.id.btnNext);
        btnBack = (Button) trips.findViewById(R.id.btnBack);
        
        //tvDate.setEnabled(false);
        btnNext.setEnabled(false);
	    btnBack.setEnabled(false);
        
		return trips;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		
		gps = new GPSTracker(getActivity());
		mGPSDC = new GPSDateCoordinates();
		Log.d(TAG, "onActivityCreated");
		CalcDate();
		if (mMilesOrKilometers == 0) { 
			tvDistance.setText("Distance (miles)");
		}
		else { 
			tvDistance.setText("Distance (km)");
		}
		//enables buttons for next and previous months
		GetPreviousFollowingMonths();
		adapter= new SimpleCursorAdapter(getActivity(), R.layout.row, null, new String[] {
        		myDbHelper.COLUMN_DISTANCE, myDbHelper.COLUMN_FORMATTEDSTART }, 
        		new int[] { R.id.title, R.id.value },0);
        lvTrips.setAdapter(adapter);
		
		tvDate.setText(getMonth(BaseMonth) + " " + String.valueOf(BaseYear));
		getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		
		
		//Go to Next month's data
		btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	    CalculatePreviousNextMonths(false);
            	    ReloadListView();
            	    GetPreviousFollowingMonths();  
            	    tvDate.setText(getMonth(BaseMonth) + " " + String.valueOf(BaseYear));
            	    
            	 
            }
        });
		
		//go to Previous month's data
		btnBack.setOnClickListener(new View.OnClickListener() {
		     @Override
		     public void onClick(View arg0) {
		             CalculatePreviousNextMonths(true);
		             ReloadListView();
		             GetPreviousFollowingMonths();  
		             tvDate.setText(getMonth(BaseMonth) + " " + String.valueOf(BaseYear));
		 
		     }
		});
		
		//set a way to delete the entries
		lvTrips.setOnItemClickListener(new OnItemClickListener() {

	        //ListView item is clicked
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        	deleteid = id;
	        	final AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
	        	
	
	        	Log.d("OnClickDelete", "id= " + String.valueOf(id));
	            // set the message to display
	            alertbox.setMessage("This entry will be permanently deleted");
	            alertbox.setCancelable(true);
	            alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) { 
	                	boolean success = myDbHelper.deleteEntryFromDatabase((int) deleteid);
	                	if (success){ 
	                		adapter.notifyDataSetChanged(); //need this if you disabled auto notify
	                		ReloadListView();
	                		
	                	}
	            		dialog.dismiss();
	            		
	                }
	            })
	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });
	            // show it
	            alertbox.show();

	        }
	    });
        
	} 
	
	/*
	 * as the name implies....
	 * also called from the main activity via DistanceFragment
	 */
	public void ReloadListView() { 
		if (mMilesOrKilometers == 0) { 
			tvDistance.setText("Distance (miles)");
		}
		else { 
			tvDistance.setText("Distance (km)");
		}
		
		getSherlockActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, TripsFragment.this);
		getSherlockActivity().getSupportLoaderManager().getLoader(LOADER_ID).forceLoad();
	}
	
	/*
	 * Calculates which month should show in the beginning of the view.
	 */
	private void CalcDate() { 
		mToday = new Date();
		mcalToday = new GregorianCalendar();
		int todayMonth = mcalToday.get(Calendar.MONTH);
		int todayDay = mcalToday.get(Calendar.DAY_OF_MONTH);
		int todayYear = mcalToday.get(Calendar.YEAR);
		Calendar StartDate = new GregorianCalendar();
		Log.d(TAG, "We're here");
		//figure out which month to show. 
		//starting date will always be less than "now"
		mGPSDC = myDbHelper.GetClosestEntryToNow();
	    if ((mGPSDC.getStartLatitude() != 0) && (mGPSDC.getEndLatitude() != 0)) { 
	    	Log.d("Start Lat ", "value =" + mGPSDC.getStartLatitude());
			Log.d("Start Long ", "value =" + mGPSDC.getStartLongitude());
	    	
			StartDate.setTimeInMillis(mGPSDC.getStartUnixEpoch() * 1000);
	    	Log.d("Start Year", "value =" + StartDate.get(Calendar.YEAR));
	    	Log.d("Start Month", "value =" + StartDate.get(Calendar.MONTH));
	    	if (StartDate.get(Calendar.YEAR) == todayYear) { 
	    		if (StartDate.get(Calendar.MONTH) ==todayMonth) { 
	    			BaseMonth = todayMonth;
	    		}
	    		else { 
	    			BaseMonth = StartDate.get(Calendar.MONTH);	
	    		}
	    		BaseYear = todayYear;
	    	}
	    	else { 
	    		BaseYear = StartDate.get(Calendar.YEAR);
	    		BaseMonth = StartDate.get(Calendar.MONTH);
	    	}
	    	
	    }
	    else { 
	    	Log.d(TAG, "Coordinates not received");
	    	BaseYear = StartDate.get(Calendar.YEAR);
    		BaseMonth = StartDate.get(Calendar.MONTH);
	    }
		
	}
	
	//figure out if previous or following month's data exists.  Used 
	//to enable Next or Previous buttons. 
	//true does next month
	//false does previous month
	public void GetPreviousFollowingMonths() { 
		if (myDbHelper.IsDataInPreviousOrNextMonth(BaseMonth, BaseYear,true)) { 
			btnNext.setEnabled(true);
		}
		else { 
			btnNext.setEnabled(false);
			//btnNext.
		}
		if (myDbHelper.IsDataInPreviousOrNextMonth(BaseMonth, BaseYear,false)) { 
			btnBack.setEnabled(true);
		}
		else { 
			btnBack.setEnabled(false);
		}
		
	}
	
	//figure out if previous or following month's data exists.  Used 
	//to enable Next or Previous buttons.  
	//true if Previous, false if next
	public void CalculatePreviousNextMonths(boolean PreviousOrNext) { 
		Calendar MonthDate = new GregorianCalendar();
		int value;
		MonthDate.set(BaseYear, BaseMonth, 1);
		value = PreviousOrNext ? -1 : 1;

		MonthDate.add(Calendar.MONTH, value);
		
		BaseMonth = MonthDate.get(Calendar.MONTH);
		BaseYear = MonthDate.get(Calendar.YEAR);
	}
	
	
	public String getMonth(int month) {
	    return new DateFormatSymbols().getMonths()[month];
	}
	
	/* gets called from the GPSTrackingActivity */
	public void setMileOrKilometers(int mMilesOrKm) { 
		mMilesOrKilometers = mMilesOrKm;
	}
	
	/*
	 * as the name implies
	 * gets preferences for automatic and difficulty
	 */
	public void getSharedPrefs() { 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMilesOrKilometers = prefs.getInt(KEY_UNITS,0);
		      
	}
	
	/*
	 * as the name implies
	 * saves preferences for automatic and difficulty
	 */
	public void PopulateSharedPrefs() { 
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean Success = settings.edit().putInt(KEY_UNITS, mMilesOrKilometers).commit();
		
		
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		loader = new SQLiteCursorLoader(getActivity().getApplicationContext(), myDbHelper,
				myDbHelper.getSQLArrayOfDatesDistances(BaseYear,BaseMonth,mMilesOrKilometers),null);
		return loader;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		adapter.changeCursor(cursor);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(null);
		
	}

	@Override
	public void onDestroy() {
		getSharedPrefs();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		getSharedPrefs();
		super.onPause();
	}

	@Override
	public void onResume() {
		getSharedPrefs();
		super.onResume();
	}

	@Override
	public void onStart() {
		getSharedPrefs();
		super.onStart();
	}

	@Override
	public void onStop() {
		getSharedPrefs();
		super.onStop();
	}



}
