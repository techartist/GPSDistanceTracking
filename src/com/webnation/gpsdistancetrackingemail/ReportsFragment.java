package com.webnation.gpsdistancetrackingemail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.webnation.gpsdistancetrackingemail.DistanceFragment.OnCoordinatesAddedListener;

public class ReportsFragment extends SherlockFragment {

	Button btnStartDate;
	Button btnEndDate;
	Button btnSendEmail;
	TextView tvSummaryTitle;
	TextView tvStartDate;
	TextView tvEndDate;
	TextView tvTotalDistance;
	//set 0 for miles, 1 for kilometers
    int mMilesOrKilometers = 0;
    
	GPSTracker gps;
	GPSDateCoordinates mGPSDC;
	static String TAG = "ReportsFragment";
	private DataBaseHelper myDbHelper;
	long insertedID = 0;
	protected Calendar StartDate;
	protected Calendar EndDate;
	//random numbers for the dialogs
	static final int START_DATE_DIALOG_ID = 888;
	static final int END_DATE_DIALOG_ID = 999;
	//universal formatter
	NumberFormat nf = NumberFormat.getInstance();
    

	static ReportsFragment newInstance() {
		// Create a new fragment instance
		ReportsFragment reportFrag = new ReportsFragment();

		return reportFrag;
	}
	
	//interface to the GPSTrackingActivity activity class
    private ReportStartDateListener listener;
	
	/*
	 * Way to communicate with the activity
	 * http://www.vogella.com/articles/AndroidFragments/article.html
	 * GPSTrackingActivity must implement this interface
	 * 
	 * messages pass this way
	 * ReportsFragment --> GPSTrackingActivity
	 */
	public interface ReportStartDateListener {
	      public void getCurrentIdOfFragment();

	}

	/*creates all the views for this fragment */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		//reports tab
		View distance = inflater.inflate(R.layout.reports_frag, container,
				false);
		//inflates the views
		tvSummaryTitle = (TextView) distance.findViewById(R.id.tvSummaryTitle);
		tvEndDate = (TextView) distance.findViewById(R.id.tvEndDate);
		tvStartDate = (TextView) distance.findViewById(R.id.tvStartDate);
		tvTotalDistance = (TextView) distance
				.findViewById(R.id.tvTotalDistance);
		btnStartDate = (Button) distance.findViewById(R.id.btnStartDate);
		btnEndDate = (Button) distance.findViewById(R.id.btnEndDate);
		btnSendEmail = (Button) distance.findViewById(R.id.btnSendEmail);
		
		//instantiate the database
		myDbHelper = new DataBaseHelper(getActivity());
		
		//set up universal fractions for digits
		nf.setMinimumFractionDigits(3);
	    nf.setMaximumFractionDigits(3);
	    
	    //get the date range for the app.  Shows current year
		CalcDate();
		//go to the datbase and calculate the total distance
		//for the calculated time period
		GetDistanceTraveledForTime();
		return distance;
	}

	//implement the interface allowing the fragment to talk to the activity
	//used for debugging 
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof ReportStartDateListener) {
	        listener = (ReportStartDateListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implemenet ReportFragment.ReportStartDateListener");
	      }
	}

	//sets up buttons, onClick events
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		SetTextDates();
		
		/* picks the start date for the time range the user wants */
		btnStartDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.getCurrentIdOfFragment();
				DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						//check if valid date range
						//true means StartDate
						if (DatesValid(true, year, monthOfYear, dayOfMonth)) {
							StartDate.set(Calendar.YEAR, year);
							StartDate.set(Calendar.MONTH, monthOfYear);
							StartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							SetTextDates();
						} else {
							Toast.makeText(
									getActivity(),
									"Start Date cannot be greater than End Date",
									Toast.LENGTH_SHORT).show();
						}
						//update fragment with new Start Date
						GetDistanceTraveledForTime();

					}
				};

				//unfortunately, this does not use the GPSTheme, but uses the 
				//dialog style from the style.xml file
				DatePickerDialog d = new DatePickerDialog(getActivity(),
						R.style.GPSTheme, mDateSetListener,
						StartDate.get(Calendar.YEAR), StartDate
								.get(Calendar.MONTH), StartDate
								.get(Calendar.DAY_OF_MONTH));
				
				d.setIcon(R.drawable.calendar_2_icon);
				d.show();

			}

		});

		/* picks the end date for the time range the user wants */
		btnEndDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						//check if valid date range
						//false means EndDate
						if (DatesValid(false, year, monthOfYear, dayOfMonth)) {
							EndDate.set(Calendar.YEAR, year);
							EndDate.set(Calendar.MONTH, monthOfYear);
							EndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							SetTextDates();
						} else {
							Toast.makeText(getActivity(),
									"End Date cannot be less than Start Date",
									Toast.LENGTH_SHORT).show();
						}
						//update fragment for date range selected
						GetDistanceTraveledForTime();

					}
				};

				//unfortunately, this does not use the GPSTheme, but uses the 
				//dialog style from the style.xml file
				DatePickerDialog d = new DatePickerDialog(getActivity(),
						R.style.GPSTheme, mDateSetListener,
						EndDate.get(Calendar.YEAR), EndDate
								.get(Calendar.MONTH), EndDate
								.get(Calendar.DAY_OF_MONTH));
				d.setIcon(R.drawable.calendar_2_icon);
				d.show();

			}

		});
		
		//allows user to send themselves an email with the date range currently selected
		btnSendEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// FormatEmail();
				sendEmail();
			}
		});
	}
	
	/* gets called from the GPSTrackingActivity */
	public void setMileOrKilometers(int mMilesOrKm) { 
		mMilesOrKilometers = mMilesOrKm;
		GetDistanceTraveledForTime();
	}

	/*
	 * Get the total distance traveled between Start and End Times
	 * Updates the display
	 */
	public void GetDistanceTraveledForTime() {
		String UnitsOfMeasure = "miles";
		if (mMilesOrKilometers == 1) { 
			UnitsOfMeasure = "km";
			
		}
		
		float[] distances = myDbHelper.GetDistancesForDateRange(StartDate,
				EndDate,mMilesOrKilometers);
		String text = "Total Distance Traveled:\n";
		if (distances.length > 0) {
			text += nf.format(distances[0]) + " " + UnitsOfMeasure;
			text += "\nAverage Distance per Trip:\n";
			text += nf.format(distances[1]) + " " + UnitsOfMeasure;
			text += "\nNumber of Trips:";
			text += String.valueOf((int) distances[2]);
			
			
		} else {
			text += "0.";
		}
		tvTotalDistance.setText(text);

	}

	/*
	 * Formats the email to send to the user
	 */
	@SuppressWarnings("static-access")
	public void sendEmail() {
		Calendar today = new GregorianCalendar();
		Log.d(TAG, "Path:"
				+ Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/GPSTracking/" + MakeTextDate(today) + ".csv");
		File tempFile = null;

		try {
			//create temp file for the email attachment
			tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()  + "/" +  MakeTextDate(today) + ".csv");
			tempFile.createTempFile(MakeTextDate(today), ".csv");
			boolean fileExists = tempFile.exists();
			Log.d(TAG, "file exists:" + fileExists);
			
		} //catch (IOException e) {
			// error
			//Log.d(TAG, "create temp file:" + e.toString());
		//}
		catch (Exception e) { 
			Log.d(TAG, "file exists:" + e.toString());
		}
		//get the actual text of the email
		FormatEmail(today, tempFile);

		try {
			//Find an installed email program on user's device. 
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 
			//Email Subject
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Trip report");
			//Text of email
			emailIntent
					.putExtra(Intent.EXTRA_TEXT, "Here is your Trips Report");
			//add attachment
			emailIntent.putExtra(Intent.EXTRA_STREAM, Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/GPSTracking/" + MakeTextDate(today) + ".csv");
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
			emailIntent.setType("plain/text");
			//create a chooser for user to pick email messages
			startActivity(Intent.createChooser(emailIntent, "Send email..."));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * as the name Implies
	 * uses Bytecode Pty Ltd.'s csv writer
	 */
	public void FormatEmail(Calendar today, File tempFile) {
		CSVWriter writer = null;
		String entryUnits = "Date#Distance Miles";
		if (mMilesOrKilometers == 1) { 
			entryUnits = "Date#Distance Kms";
		}
		String[] blankLine = { "", "" };

		String[] Title = { "Trips for Date Range " + MakeTextDate(StartDate)
				+ " to " + MakeTextDate(EndDate) };
		ArrayList<String[]> distancesDatesCSV = myDbHelper
				.GetDistancesForDateRangeCSV(StartDate, EndDate, mMilesOrKilometers);
		float[] distances = myDbHelper.GetDistancesForDateRange(StartDate, EndDate, mMilesOrKilometers);
		if (distancesDatesCSV.size() > 0) {
			if (tempFile != null) {
				try {
					
					writer = new CSVWriter(new FileWriter(tempFile), ',');
					String[] entries = entryUnits.split("#"); // array of
																	// your
					writer.writeNext(Title);											// values
					writer.writeNext(entries);
					// getDatesAndDistances
					int i = 0;
					
					for (int j=0;j<distancesDatesCSV.size();j++) { 
						
						writer.writeNext(distancesDatesCSV.get(j));
						
					}

					writer.writeNext(blankLine);
					writer.writeNext(blankLine);

					String[] Totals = "Total Distance Traveled#Average Distance#Total Number of Trips"
							.split("#");
					writer.writeNext(Totals);
					String[] strDistances = { "", "", "" };
					for (i = 0; i < distances.length; i++) {
						strDistances[i] = String.valueOf(distances[i]);
					}
					writer.writeNext(strDistances);

					writer.close();
				} catch (IOException e) {
					// error
					Log.d(TAG, "FormatEmail:" + e.toString());
				} catch (Exception e) {
					Log.d(TAG, "FormatEmail:" + e.toString());
				}
			}
			else { 
				Toast.makeText(getActivity(),
						"File is null", 1).show();
			}
		} else {
			Toast.makeText(getActivity(),
					"There are no entries for the time period", 1).show();
		}
	}

	

	/*
	 * delete old files from the sdcard In order to always make a cleanup of the
	 * user's storage (SDCard), you can check the lastModified() date of the
	 * file for a givend age and delete it.
	 */
	private void checkTempFiles() {
		Log.d(TAG, "--> checkTempFiles");

		// Check if directory 'YourTempDirectory' exists and delete all files
		String tempDirectoryPath = Environment.getExternalStorageDirectory()
				.toString() + "/YourTempDirectory";
		File dir = new File(tempDirectoryPath);
		// Delete all existing files older than 24 hours
		if (dir.exists() && dir.isDirectory()) {
			String[] fileToBeDeleted = dir.list();
			for (int i = 0; i < fileToBeDeleted.length; i++) {
				File deleteFile = new File(tempDirectoryPath + "/"
						+ fileToBeDeleted[i]);
				Long lastmodified = deleteFile.lastModified();
				if (lastmodified + 86400000L < System.currentTimeMillis()) {
					if (deleteFile.isFile()) {
						deleteFile.delete();
					}
				}
			}
		}
	}

	//just a quick and dirty way to return a text date from the Calendar Date
	private String MakeTextDate(Calendar theDate) {
		int Year = theDate.get(Calendar.YEAR);
		int Month = theDate.get(Calendar.MONTH);
		int Day = theDate.get(Calendar.DAY_OF_MONTH);
		return (new StringBuilder()
		// Month is 0 based, just add 1
				.append(Month + 1).append("-").append(Day).append("-")
				.append(Year)).toString();
	}

	//sets View Dates for fragment
	private void SetTextDates() {
		// set current date into textview
		tvStartDate.setText(MakeTextDate(StartDate));
		// set end date into textview
		tvEndDate.setText(MakeTextDate(EndDate));
	}

	/*
	 * Figures out what year we're in and defaults to display
	 * Jan 1 - Dec 31 of that year
	 */
	protected void CalcDate() {
		StartDate = new GregorianCalendar();
		EndDate = new GregorianCalendar();
		Log.d(TAG, "We're here");
		// figure out which year to show.
		// starting date will always be less than "now"
		mGPSDC = myDbHelper.GetClosestEntryToNow();
		if ((mGPSDC.getStartLatitude() != 0) && (mGPSDC.getEndLatitude() != 0)) {
			StartDate.setTimeInMillis(mGPSDC.getStartUnixEpoch() * 1000);
		}

		int Year = StartDate.get(Calendar.YEAR);
		// set to January 1 of the closest entry date, or this year if there is
		// no entries
		StartDate.set(Year, 0, 1);
		StartDate.set(Calendar.MINUTE, 0);
		StartDate.set(Calendar.HOUR, 0);
		// set to December 31 of the closest entry date year or this year if
		// there is no entries
		EndDate.set(Year, 11, 31);
		EndDate.set(Calendar.HOUR, 11);
		EndDate.set(Calendar.MINUTE, 59);
		EndDate.set(Calendar.SECOND, 59);

	}

	//upon selection of new start and end dates, figures out if dates are valid
	public boolean DatesValid(boolean StartOrEndDate, int year, int month,
			int day) {
		Calendar testDate = new GregorianCalendar();
		testDate.set(year, month, day);
		if (StartOrEndDate) {
			return (EndDate.getTimeInMillis() < testDate.getTimeInMillis()) ? false
					: true;
		} else {
			return (StartDate.getTimeInMillis() > testDate.getTimeInMillis()) ? false
					: true;
		}
	}

	//found this online and thought it would be good to include, but not used
	public static boolean checkEmail(String email) {
		String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
				+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
				+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
				+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
				+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
				+ "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches())
			return true;
		else
			return false;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}



}
