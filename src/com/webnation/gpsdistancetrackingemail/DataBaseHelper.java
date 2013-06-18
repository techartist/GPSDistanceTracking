package com.webnation.gpsdistancetrackingemail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	 
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.webnation.gpsdistancetrackingemail/databases/";
    private static String DB_NAME = "mileagetracker"; 
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATETIMESTAMP = "dateTS";
	public static final String COLUMN_DATESTART = "dateStart";
	public static final String COLUMN_DATEEND = "dateEnd";
	public static final String COLUMN_LATSTART = "latStart";
	public static final String COLUMN_LATEND = "latEnd";
	public static final String COLUMN_LONGSTART = "longStart";
	public static final String COLUMN_LONGEND = "longEnd";
	public static final String COLUMN_STARTID = "start_id";
	public static final String COLUMN_ENDID = "end_id";
	public static final String COLUMN_DISTANCE = "distance";
	public static final String COLUMN_FORMATTEDSTART = "start_date";
	public static final String TABLE_DATECOORDINATES = "dateCoordinates";
	public static final String TABLE_STARTEND = "startEnd";
	private static final String TAG = "DataBaseHelper";
	
	private static final int SKIP = 1;
	private static final int START = 0;
	private static final int END = 1;
	private static final int START_OR_END = START;
    public SQLiteDatabase myDataBase; 
	protected int numOfRows = 0;
	private String[] allColumns = { DataBaseHelper.COLUMN_ID,
			DataBaseHelper.COLUMN_DATETIMESTAMP,
			DataBaseHelper.COLUMN_DATESTART,DataBaseHelper.COLUMN_LATSTART,
			DataBaseHelper.COLUMN_LONGSTART,DataBaseHelper.COLUMN_DATEEND, 
			DataBaseHelper.COLUMN_LATEND, DataBaseHelper.COLUMN_LONGEND, 
			DataBaseHelper.COLUMN_DISTANCE};


    private String CreateTableDateCoordinates = "CREATE TABLE IF NOT EXISTS dateCoordinates (_id INTEGER PRIMARY KEY, dateTS TIMESTAMP NOT NULL DEFAULT current_timestamp,dateStart INTEGER, latStart NUMERIC, longStart NUMERIC, dateEnd INTEGER,  latEnd NUMERIC, longEnd NUMERIC, distance NUMERIC)";

	private String SQL_MainQuery = "";
	
 
    private final Context myContext;
 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {
 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
        //deleteDatabase(context); 
       	
        createDataBase();
    	
        SQL_MainQuery = "select * from  " + TABLE_DATECOORDINATES;
    }	
    
    public boolean deleteDatabase(Context context) {
    	boolean successfulDelete = false; 
    	try { 
    		successfulDelete = context.deleteDatabase(DB_NAME);
    	}
    	catch (Exception e) { 
    		Log.d("Exception in createDatabase", e.toString());
    		
    	}
    	return successfulDelete;
    }

 
  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() {
 
    	boolean dbExist = checkDataBase();
    	
 
    	if(dbExist){
    		//do nothing - database already exists
    		Log.d("Database Check", "Database Exists");
    		createDateCoor();

    	}else{
    		Log.d("Database Check", "Database Doesn't Exist");
    		//this.close();
    		//By calling this method an empty database will be created into the default system path
               //of your application 
        	this.getReadableDatabase();
        	this.close();
        	try {
        		createDateCoor();
    			Log.d("Database Check", "Table created");
 
    		} catch (Exception e) { 
        		Log.d("Exception in createDatabase", e.toString());
        		
        	}
    	}
 
    }
    
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
    	Log.d("We're Here", "DatabaseHelper.checkDataBase()");
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		Log.d("We're Here", "Database doesn't exist");
    		Log.d("Database Error", e.toString());
 
    	}
    	catch (Exception e) { 
    		Log.d("Exception in checkDatabase", e.toString());
    		
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
    	//checkDB.close();
 
    	return checkDB != null;
    }
 
    
    /*
     * Create Table
     */
    public void createDateCoor() { 
    	String sql = CreateTableDateCoordinates;
    	this.openDataBase();
    	try {
			Log.d("Create Table DateCoor", sql);
			
			myDataBase.execSQL(sql);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.d("SQL Exception", e.toString());
		}
    	catch (Exception e) {
			Log.d("Exception", "Create Table DateCoor " + e.toString());
		}
    	this.close();
    	
    }
    
    //selects entry for a certain row    
    /*
     * Param: id is the id of the row. 
     */
    public GPSDateCoordinates SelectStartEndCoordinates(long ID) { 
    	String sql = SQL_MainQuery + " where " +  COLUMN_ID  + "=" + String.valueOf(ID);
    	GPSDateCoordinates gps = new GPSDateCoordinates();
    	this.openDataBase();
		try { 
			Log.d("Database sql", sql);
			Cursor mcursor = myDataBase.rawQuery(sql, null);
		    if (mcursor.moveToFirst()) { 
		       gps = CursorToDistance(mcursor);
		    }
		    mcursor.close();
		}
		catch (SQLException sqle) {
			Log.d("Database Error", "Error getting number of rows");
			Log.d("SQL EXception", sqle.toString());
		}
		
		this.close();
		return gps;
        
    }
    
    /* read info from the database into the cursor object */
    public GPSDateCoordinates CursorToDistance(Cursor cursor) { 
    	GPSDateCoordinates gps = new GPSDateCoordinates();
        /*CREATE TABLE dateCoordinates (_id INTEGER, dateTS TIMESTAMP,dateStart INTEGER, latStart NUMERIC,
         *  longStart NUMERIC, dateEnd INTEGER,  latEnd NUMERIC, longEnd NUMERIC, distance NUMERIC)
         */
    	try { 
    		if (cursor.moveToFirst()) { 
    			gps.setId((long)cursor.getInt(0));
    			gps.setTimestamp(new Date(cursor.getLong(1)));
    			gps.setStartUnixEpoch(cursor.getLong(2));
    			gps.setStartLatitude(cursor.getDouble(3));
    			gps.setStartLongitude(cursor.getDouble(4));
    			gps.setEndUnixEpoch(cursor.getLong(5));
    			gps.setEndLatitude(cursor.getDouble(6));
    			gps.setEndLongitude(cursor.getDouble(7));
    			gps.setDistance(cursor.getDouble(7));
    		}
    		
    	}
    	catch (Exception e) { 
    		Log.d("Cursor Error", e.toString());
    	}
    	return gps;
    	
    }
       
    
    //gets closest start time where end time is not null
    //reads data into the GPSDateCoordinates object
    //figures out which month we should show in the opening
    //trips fragment
    public GPSDateCoordinates GetClosestEntryToNow() {
    	
    	String sql3 = "select * from " + TABLE_DATECOORDINATES;
    		   sql3 += " where " + COLUMN_LATEND + " is not null and "; 
    	       sql3 += COLUMN_LONGEND + " is not null order by ABS(strftime('%s', 'now') - ";
    	       sql3 += " strftime('%s', " + COLUMN_DATETIMESTAMP + ")) ASC";
    	long id = 0;

    	this.openDataBase();
    	GPSDateCoordinates gps = new GPSDateCoordinates();
		try { 
			Log.d("GetClosestEntryToNow", sql3);
			Cursor mcursor = myDataBase.rawQuery(sql3, null);
			gps = CursorToDistance(mcursor);
		    mcursor.close();
		}
		catch (SQLException sqle) {
			Log.d("Database Error", "Error getting closest entries");
			Log.d("SQL EXception", sqle.toString());
		}
		this.close();

		return gps; 
    	
    }
    
    //determines if there is data in month preceding or following
    //the inputted month.
    //called from trips fragment
    /* Params: Month - as the (0-11)
     *         Year - as the name implies as int
     *         PreviousOrNext - are we looking in previous month or next? 
     *         false PreviousMonth, true for NextMonth
     */
    public boolean IsDataInPreviousOrNextMonth(int Month, int Year, boolean PreviousOrNext) {
    	String sql3 = "select * from " + TABLE_DATECOORDINATES;
		   sql3 += " where " + COLUMN_LATEND + " is not null and "; 
	       sql3 += COLUMN_LONGEND + " is not null and " +  COLUMN_DATESTART;
	    int rowCount = 0;
	    Cursor mcursor = null; 
    	Calendar mEndDate = new GregorianCalendar();
    	Calendar mStartDate = new GregorianCalendar();
    	//set to first Day of inputed Month
		mEndDate.set(Year, Month, 1);
		mStartDate.set(Year,  Month, 1);
		mEndDate.set(Calendar.HOUR, 23);
		mEndDate.set(Calendar.MINUTE, 59);
		mEndDate.set(Calendar.SECOND, 59);
		mStartDate.set(Calendar.HOUR, 0);
		mStartDate.set(Calendar.MINUTE, 0);
    	if (PreviousOrNext) { 
    		//increase end month by two, start month by one.
            mEndDate.roll(Calendar.MONTH, true);
            mStartDate.roll(Calendar.MONTH, true);
            mEndDate.roll(Calendar.MONTH, true);
    	}
    	else { 
    		mStartDate.roll(Calendar.MONTH, false);
    	}
    	Log.d("CurrentMonth End", String.valueOf(mEndDate.get(Calendar.MONTH)));
    	Log.d("CurrentMonth Start", String.valueOf(mStartDate.get(Calendar.MONTH)));
    	    	
    	long UnixEpochStart = mStartDate.getTimeInMillis()/1000;
    	long UnixEpochEnd = mEndDate.getTimeInMillis()/1000;
    	sql3 += " > " + String.valueOf(UnixEpochStart);
    	sql3 += " and " +  COLUMN_DATESTART + " < " + String.valueOf(UnixEpochEnd);

    	
		try { 
			this.openDataBase();
			Log.d("IsDataInPreviousOrNextMonth sql", sql3);
			mcursor = myDataBase.rawQuery(sql3, null);
			rowCount = mcursor.getCount();
			Log.d("Row Count", String.valueOf(rowCount));
			//this.close();
		    
		}
		catch (SQLException sqle) {
			Log.d("Database Error", "Error getting closest entries");
			Log.d("SQL EXception", sqle.toString());
		}
		
		if (!mcursor.isClosed()) 
			mcursor.close();
		

		if (rowCount > 0) return true;
		else return false;
    	
    }
    
    
    //returns an array of floats
    //distance[0] is total distance
    //distance[1] is average distance
    //distance[2] is number of trips
    /* Params: StartDate - as the name implies 
     *         EndDate - as the name implies
     *         mMilesOrKilometers - miles or kilometers.  0 for miles, 1 for kilometers
     */
    public float[] GetDistancesForDateRange(Calendar StartDate, Calendar EndDate, int mMilesOrKilometers) {
    	float[] distances = { 0,0,0 } ;
    	double Divider = 1;
    	if (mMilesOrKilometers == 0) { 
    		Divider = 1609.34;
    	}
    	else { 
    		Divider = 1000;
    	}
    	Log.d("DateRange Multiplier", String.valueOf(Divider));
    	String sql3 = "select round((sum(" + COLUMN_DISTANCE + ")/" + String.valueOf(Divider) + "),2),"; 
    		   sql3 += " round((avg(" + COLUMN_DISTANCE + ")/" + String.valueOf(Divider) + "),2),"; 
    		   sql3 += " count(*) "; 
    	       sql3 += "from " + TABLE_DATECOORDINATES;
    		   sql3 += " where " + COLUMN_DISTANCE + " is not null ";
    	       sql3 += " and " + COLUMN_DATESTART + "> " + StartDate.getTimeInMillis()/1000;
    	       sql3 += " and " + COLUMN_DATESTART + "<" + EndDate.getTimeInMillis()/1000;

    	this.openDataBase();
    	
		try { 
			Log.d("GetDistancesForDateRange", sql3);
			Cursor mcursor = myDataBase.rawQuery(sql3, null);
			if (mcursor.moveToFirst()) { 
			   distances[0] = mcursor.getFloat(0);
			   distances[1] = mcursor.getFloat(1);
			   distances[2] = mcursor.getFloat(2);
			}
		    mcursor.close();
		}
		catch (SQLException sqle) {
			Log.d("Database Error", "Error getting closest entries");
			Log.d("SQL EXception", sqle.toString());
		}
		this.close();

		return distances; 
    	
    }
    
    
    //returns an array of floats
    //called from reports fragment used to populate email with dates and distances
    /* Params: StartDate - as the name implies 
    *         EndDate - as the name implies
    *         mMilesOrKilometers - miles or kilometers.  0 for miles, 1 for kilometers
    */
    public ArrayList<String[]> GetDistancesForDateRangeCSV(Calendar StartDate, Calendar EndDate, int mMilesOrKilometers) {
    	ArrayList<String[]> stringCSVList = new ArrayList<String[]>();
    	
    	double Divider = 1;
    	if (mMilesOrKilometers == 0) { 
    		Divider = 1609.34;
    	}
    	else { 
    		Divider = 1000;
    	}
    	    	
    	String sql3 = "select " + COLUMN_DATETIMESTAMP + ",";
    	       sql3 += "round((" + COLUMN_DISTANCE + "/" + String.valueOf(Divider) + "),2) "; 
    	       sql3 += "from " + TABLE_DATECOORDINATES;
    		   sql3 += " where " + COLUMN_DISTANCE + " is not null ";
    	       sql3 += " and " + COLUMN_DATESTART + "> " + StartDate.getTimeInMillis()/1000;
    	       sql3 += " and " + COLUMN_DATESTART + "<" + EndDate.getTimeInMillis()/1000;

    	this.openDataBase();
    	
		try { 
			Log.d("GetDistancesForDateRangeCSV", sql3);
			Cursor mcursor = myDataBase.rawQuery(sql3, null);
			while (mcursor.moveToNext()) { 
				//Log.d("Database data", mcursor.getString(0) + " " + mcursor.getString(1));
				String[] csvRow = { "", ""};
				csvRow[0] = mcursor.getString(0);
			    csvRow[1] = mcursor.getString(1);
			    stringCSVList.add(csvRow);
			}
		    mcursor.close();
		}
		catch (SQLException sqle) {
			Log.d("SQL EXception", sqle.toString());
		}
		catch (Exception e) { 
			Log.d("Exception", e.toString());
		}
		this.close();

		return stringCSVList; 
    	
    }
    
    
    /*Sets up the string for the raw query to be displayed in the ListView in trips fragment
     * Params: StartMonth - as the name implies (0-11)
     *         StartYear - current year as int
     *         mMilesOrKilometers - miles or kilometers.  0 for miles, 1 for kilometers
     */
    public String getSQLArrayOfDatesDistances(int StartYear, int StartMonth, int mMilesOrKilometers) { 
    	
    	double Divider = 1;
    	if (mMilesOrKilometers == 0) { 
    		Divider = 1609.34;
    	}
    	else { 
    		Divider = 1000;
    	}
        String SQL = "select " + COLUMN_ID + ",round((" + COLUMN_DISTANCE + "/" + String.valueOf(Divider) + "),2) as " + COLUMN_DISTANCE + " ,";
        SQL +=        "strftime('%Y-%m-%d  '," + COLUMN_DATETIMESTAMP + ")  as " + COLUMN_FORMATTEDSTART;
        SQL +=		" from " + TABLE_DATECOORDINATES + " where " + COLUMN_DATESTART;
    	
        /*Month is 0-11 */
    	Calendar UnixEpochStart = new GregorianCalendar();
    	UnixEpochStart.set(StartYear,  StartMonth, 1);
    	UnixEpochStart.set(Calendar.HOUR, 0);
    	UnixEpochStart.set(Calendar.MINUTE, 0);
    	Calendar UnixEpochEnd = new GregorianCalendar();
    	UnixEpochEnd.set(StartYear, StartMonth, 1);
    	UnixEpochEnd.set(Calendar.HOUR, 0);
    	UnixEpochEnd.set(Calendar.MINUTE, 0);
    	UnixEpochEnd.roll(Calendar.MONTH, true);
    	
    	
    	Log.d(TAG, "StartDate Month:" + StartMonth);
    	Log.d(TAG, "StartDate Year:" + StartYear);
    	Log.d(TAG, "StartDate Millis:" + UnixEpochStart.getTimeInMillis()/1000);
    	Log.d(TAG, "EndDate Millis:" + UnixEpochEnd.getTimeInMillis()/1000);
    	SQL += ">" +  String.valueOf(UnixEpochStart.getTimeInMillis()/1000) + " and ";
    	SQL += COLUMN_DATESTART + " <" + String.valueOf(UnixEpochEnd.getTimeInMillis()/1000);
    	Log.d(TAG, "SQL=" + SQL);
    	return SQL;
    	
    }
   
    
   /*
    * inserts coordinates from the device
    * called from distance frag when user selects start or end dates
    * params: latitude   //  current latitude
    *         longitude  // current longitude
    *         Start      // true if starting coordinate, false if ending
    *         StartEndId // id if previous record, -1 if starting id
    */     
   
    public long InsertCoordinates(double latitude, double longitude, boolean Start, long StartEndId) { 
    	
    	long idCoord = StartEndId;
    	String sql = "";
    	this.openDataBase();
    	try {
    		

    		if (Start) { 
    			sql =  "insert into " + TABLE_DATECOORDINATES + " (" + COLUMN_ID + ",";
    			sql += COLUMN_DATESTART + "," +  COLUMN_LATSTART + "," +  COLUMN_LONGSTART + ") ";
    			sql += "values (null,strftime('%s'),";
    			sql += String.valueOf(latitude) + "," + String.valueOf(longitude) + ")";
    			Log.d("Insert coordinates", sql);
    			ContentValues cv = new ContentValues();
    			long timestamp = System.currentTimeMillis()/ 1000;
    			cv.put(COLUMN_DATESTART, timestamp);
    			cv.put(COLUMN_LATSTART, latitude);
    			cv.put(COLUMN_LONGSTART, longitude);
    			idCoord= myDataBase.insert(TABLE_DATECOORDINATES,null, cv);

    		}
    		else { 
    			 
    			String where = "_id=?";
    			String[] whereArgs = {String.valueOf(idCoord)};
    			sql = "update " + TABLE_DATECOORDINATES + " set " + COLUMN_DATEEND + "= strftime('%s'),";
    			sql += COLUMN_LATEND + "=" + String.valueOf(latitude) + ",";
    			sql += COLUMN_LONGEND + "=" + String.valueOf(longitude);
    			sql += " where " + COLUMN_ID + "=" + String.valueOf(StartEndId);
    			Log.d("Update coordinates", sql);
    			ContentValues cv = new ContentValues();
    			long timestamp = System.currentTimeMillis()/ 1000;
    			cv.put(COLUMN_DATEEND, timestamp);
    			cv.put(COLUMN_LATEND, latitude);
    			cv.put(COLUMN_LONGEND, longitude);
    			if (myDataBase.update(TABLE_DATECOORDINATES, cv, where, whereArgs) >= 1) { 
    				;
    			}
    		}
			
		} // TODO Auto-generated catch block
			catch (SQLException e) {
			Log.d("SQL Exception", "InsertCoordinates " + e.toString());
		}
    	    catch (Exception e) {
			Log.d("Exception", "InsertCoordinates " + e.toString());
		}
    	this.close();
    	return idCoord; 
    	
    }
    
    /*
     * Update the table to reflect the distance. 
     * All Distances stored in meters
     * params:  mGPSDC which is a GPSDateCoordinate holding all the 
     *          infomration about a trip. 
     */
    public void InsertDistance(GPSDateCoordinates mGPSDC) {
		if (mGPSDC.getId() > 0) { 
    	  String sql = "";
    	  String where = "_id=?";
		  String[] whereArgs = {String.valueOf(mGPSDC.getId())};
		  sql = "update " + TABLE_DATECOORDINATES + " set " + COLUMN_DISTANCE + "=";
		  sql += String.valueOf(mGPSDC.getDistance());
		  sql += " where " + COLUMN_ID + "=" + String.valueOf(mGPSDC.getId());
		  Log.d("Update coordinates", sql);
		  this.openDataBase();
		  try {
			  
			  ContentValues cv = new ContentValues();
			  long timestamp = System.currentTimeMillis()/ 1000;
			  cv.put(COLUMN_DISTANCE, mGPSDC.getDistance());
			  if (myDataBase.update(TABLE_DATECOORDINATES, cv, where, whereArgs) >= 1) { 
				  Log.d("Update coordinates", "Success!");
			  }
              
		  }
		  catch (SQLException e) {
					Log.d("SQL Exception", "Update Distance " + e.toString());
		   }
		  catch (Exception e) {
			  Log.d("Exception", "Update Distance " + e.toString());
			  e.printStackTrace();
		   }
		  this.close();
		}
		
	}
    
    /*
     * Deletes rows from the table in the list view
     * param:  id of row to be deleted
     */
    public boolean deleteEntryFromDatabase(int id) {
    	String[] values = { String.valueOf(id) };
    	int deleted = 1;
    	String sql = "delete from " + TABLE_DATECOORDINATES + " where " + COLUMN_ID + "=";
		sql += String.valueOf(id);
		Log.d("Delete entries coordinates", sql);
    	this.openDataBase();
    	try { 
    		Log.d("deleteEntryFromDatabase", "_id= " + String.valueOf(id));
    	    deleted = myDataBase.delete(TABLE_DATECOORDINATES,  COLUMN_ID + "=?", values);
    		//myDataBase.rawQuery(sql,null);
    	}
    	catch (Exception e) { 
    		Log.d("Exception", e.toString());
    	}
    	this.close();
    	return deleted>0 ? true : false;
    	
    }
    

    /*
     * As the name implies
     */
    public void openDataBase() {
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
        try { 
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } 
        catch (SQLException sqle){ 
        	Log.d("Database Error", "Could not Open DB");
        	throw sqle;
        }
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}
    
    /*for debug*/
	public int getNumberOfRowsDateCoordinates() {
		this.openDataBase();
		int icount = 0;
		try { 
			Cursor mcursor = myDataBase.rawQuery("Select Count(*) from " + TABLE_DATECOORDINATES, null);
		    mcursor.moveToFirst();
		    icount = mcursor.getInt(0);
		    mcursor.close();
		}
		catch (SQLException sqle) {
			Log.d("Database Error", "Error getting number of rows");
			Log.d("SQL EXception", sqle.toString());
		}
		
		this.close();
        return icount;
	} 
	
	 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}

	
	
	
}
