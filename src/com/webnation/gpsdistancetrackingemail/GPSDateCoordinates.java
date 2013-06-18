package com.webnation.gpsdistancetrackingemail;

import java.sql.Date;
import android.location.Location;
import android.util.Log;

public class GPSDateCoordinates {
    private long id = 0;
    private Date mTimestamp = null;
    private long mStartUnixEpoch = 0;
    private long mEndUnixEpoch = 0;
    private double mStartLatitude = 0;
    private double mStartLongitude = 0;
    private double mEndLatitude = 0;
    private double mEndLongitude = 0;
    private double mDistance = 0;
    
    public GPSDateCoordinates() {
    	id = 0;
        mTimestamp = null;
        mStartUnixEpoch = 0;
        mEndUnixEpoch = 0;
        mStartLatitude = 0;
        mStartLongitude = 0;
        mEndLatitude = 0;
        mEndLongitude = 0;
        mDistance = 0;
    	
    }
    
    public GPSDateCoordinates(long id,
    		          Date date,
    		          long sue,
    		          double slat,
                      double slongit)
     {
    	this.id = id;
    	this.mTimestamp = date;
    	this.mStartUnixEpoch = sue;
    	this.mStartLatitude = slat;
    	this.mStartLongitude = slongit;
    	
     }
    
    public GPSDateCoordinates(long id,
	          Date date,
	          long sue,
	          double slat,
              double slongit,
              long eue,
	          double elat,
              double elongit)
    {
          this.id = id;
          this.mTimestamp = date;
          this.mStartUnixEpoch = sue;
          this.mStartLatitude = slat;
          this.mStartLatitude = slongit;
          this.mEndUnixEpoch = eue;
          this.mEndLatitude = elat;
          this.mEndLatitude = elongit;
          if ((mStartLatitude != 0) && (mStartLongitude != 0) && 
        		  (mEndLatitude != 0) && (mEndLongitude != 0))
        		   { 
        	  CalcDistance();
          }
     }
    
	public GPSDateCoordinates(long id,
	          double slat,
            double slongit)
     {
        this.id = id;
        this.mStartLatitude = slat;
  	    this.mStartLatitude = slongit;

     }
	
	public void CalcDistance() { 
		if ((mStartLatitude != 0) && (mStartLongitude != 0) && 
      		  (mEndLatitude != 0) && (mEndLongitude != 0)) { 
			Location locationA = new Location("point A");  
			Log.d("Start Class Lat ", "value =" + mStartLatitude);
			Log.d("Start Class Long ", "value =" + mStartLongitude);
			locationA.setLatitude(mStartLatitude);  
			locationA.setLongitude(mStartLongitude);  

			Location locationB = new Location("point B");  
			Log.d("End Class Lat ", "value =" + mEndLatitude);
			Log.d("End Class Long ", "value =" + mEndLongitude);
			locationB.setLatitude(mEndLatitude);  
			locationB.setLongitude(mEndLongitude);  

			mDistance = locationA.distanceTo(locationB);
			
		}
		else { 
			String msg = "";

			if ((mStartLatitude == 0) || (mStartLongitude == 0)) { 
				msg = "Start Lat value =" + String.valueOf(mStartLatitude) + "StartLongitude value =" + String.valueOf(mStartLongitude);
				Log.d("Start Values Empty! ", msg);
			}
			if ( (mEndLatitude == 0) || (mEndLongitude == 0)) { 
			     msg = "End Lat value =" + String.valueOf(mEndLatitude) + "EndLongitude value =" + String.valueOf(mEndLongitude);
			     Log.d("End Values Empty! ", msg);
			}
			
		}
	}
    
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
    
    public Date getmTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(Date mTimestamp) {
		this.mTimestamp = mTimestamp;
	}

	public long getStartUnixEpoch() {
		return mStartUnixEpoch;
	}

	public void setStartUnixEpoch(long mStartUnixEpoch) {
		this.mStartUnixEpoch = mStartUnixEpoch;
	}

	public long getEndUnixEpoch() {
		return mEndUnixEpoch;
	}

	public void setEndUnixEpoch(long mEndUnixEpoch) {
		this.mEndUnixEpoch = mEndUnixEpoch;
	}

	public double getStartLatitude() {
		return mStartLatitude;
	}

	public void setStartLatitude(double mStartLatitude) {
		this.mStartLatitude = mStartLatitude;
	}

	public double getStartLongitude() {
		return mStartLongitude;
	}

	public void setStartLongitude(double mStartlongitude) {
		this.mStartLongitude = mStartlongitude;
	}

	public double getEndLatitude() {
		return mEndLatitude;
	}

	public void setEndLatitude(double mEndLatitude) {
		this.mEndLatitude = mEndLatitude;
	}

	public double getEndLongitude() {
		return mEndLongitude;
	}

	public void setEndLongitude(double mEndLongitude) {
		this.mEndLongitude = mEndLongitude;
	}

	public double getDistance() {
		return mDistance;
	}

	public void setDistance(double mDistance) {
		this.mDistance = mDistance;
	}



  
    
    
}
