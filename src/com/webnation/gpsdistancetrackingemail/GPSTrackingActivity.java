package com.webnation.gpsdistancetrackingemail;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

 
public class GPSTrackingActivity extends SherlockFragmentActivity implements DistanceFragment.OnCoordinatesAddedListener, 
                                                                             ReportsFragment.ReportStartDateListener
{
 
     
    long insertedID = 0;
	private Menu menu;
	//for shared preferences
	private static final String KEY_UNITS = "units";
	private static final String KEY_START_POSITION = "start";
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	String TAG = "GPSTrackingActivity";
	//set 0 for miles, 1 for kilometers
    int mMilesOrKilometers = 0;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
      //create a new ViewPager and set to the pager we have created in Ids.xml
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        setContentView(mViewPager);
        
        ActionBar actionBar = getSupportActionBar(); 
        actionBar.setDisplayShowTitleEnabled(false); 
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setNavigationMode ( ActionBar . NAVIGATION_MODE_TABS );
        //actionBar.setCustomView(R.layout.action_bar);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        
        //if user has previous settings, get them from shared prefs. 
        getSharedPrefs();
        
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(actionBar.newTab().setText(" Track").setIcon(R.drawable.browser_compass_icon),
                DistanceFragment.class, null);
        mTabsAdapter.addTab(actionBar.newTab().setText(" Trips").setIcon(R.drawable.folder_chart_icon),
                TripsFragment.class, null);
        mTabsAdapter.addTab(actionBar.newTab().setText(" Report").setIcon(R.drawable.mail_compose_icon),
                ReportsFragment.class, null);
        
        
    }    
    
    
    //updates the list view in the Trips Fragment
    //called from the DistanceFragment
    @Override
    public void onNewItemsAdded() {
    	//http://stackoverflow.com/questions/7723964/replace-fragment-inside-a-viewpager
    	//http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager/7393477#7393477
    	//to get current fragment being displayed use: "android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem();
    	try { 
    		TripsFragment fragment = (TripsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");
    	    if (fragment != null && fragment.getView() != null) {
    	            Log.d(TAG,"Class=" + fragment.getClass());
    	            if (fragment.getClass() == TripsFragment.class) {
    		              Log.d(TAG,"Found the Trips Fragment");
    		              fragment.ReloadListView();
    	             }
    		 }
    	    ReportsFragment ReportsFrag = (ReportsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":2");
    	    Log.d(TAG,"Reports Class=" + ReportsFrag.getClass());
    	    Log.d(TAG,"Reports get view=" + ReportsFrag.getView());
    	    if (ReportsFrag != null && ReportsFrag.getView() != null) {
    	            Log.d(TAG,"Class=" + ReportsFrag.getClass());
    	            //if (ReportsFrag.getClass() == ReportsFrag.class) {
    		              Log.d(TAG,"Found the Reports Fragment");
    		              ReportsFrag.GetDistanceTraveledForTime();
    	             //}
    		 }
    	    
    	}
    	//in case we change the getCurrentItem() value to anything other than 1
    	//would expect a ClassCastException
    	catch (Exception e) { 
    		Log.d(TAG,String.valueOf(e));
    	}
    	
    }
    
    /* set the units of measurement for all the fragments
     * //http://stackoverflow.com/questions/7723964/replace-fragment-inside-a-viewpager
    	//http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager/7393477#7393477
    	//to get current fragment being displayed use: "android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem();
     */
    public void ChangeUnitsOfMeasure() { 
    	try { 
    		DistanceFragment DistanceFrag = (DistanceFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
    	    if (DistanceFrag != null && DistanceFrag.getView() != null) {
    	            Log.d(TAG,"Class=" + DistanceFrag.getClass());
    	            //if (DistanceFrag.getClass() == DistanceFrag.class) {
    		              Log.d(TAG,"Found the Distance Fragment");
    		              DistanceFrag.ClearData();
    		              DistanceFrag.setMileOrKilometers(mMilesOrKilometers);
    	             //}
    		 }
    		TripsFragment TripsFrag = (TripsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");
    	    if (TripsFrag != null && TripsFrag.getView() != null) {
    	            Log.d(TAG,"Class=" + TripsFrag.getClass());
    	            if (TripsFrag.getClass() == TripsFragment.class) {
    		              Log.d(TAG,"Found the Trips Fragment");
    		              TripsFrag.setMileOrKilometers(mMilesOrKilometers);
    		              TripsFrag.ReloadListView();
    	             }
    		 }
    	    ReportsFragment ReportsFrag = (ReportsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":2");
    	    Log.d(TAG,"Reports Class=" + ReportsFrag.getClass());
    	    Log.d(TAG,"Reports get view=" + ReportsFrag.getView());
    	    if (ReportsFrag != null && ReportsFrag.getView() != null) {
    	            Log.d(TAG,"Class=" + ReportsFrag.getClass());
    	            //if (ReportsFrag.getClass() == ReportsFrag.class) {
    		              Log.d(TAG,"Found the Reports Fragment");
    		              ReportsFrag.setMileOrKilometers(mMilesOrKilometers);
    	             //}
    		 }
    	    
    	}
    	//in case we change the getCurrentItem() value to anything other than 1
    	//would expect a ClassCastException
    	catch (Exception e) { 
    		Log.d(TAG,String.valueOf(e));
    	}
    }
    
    public void getCurrentIdOfFragment() { 
    	int mCurrentItem = mViewPager.getCurrentItem();
    	Log.d(TAG,"Current View Page=" + String.valueOf(mCurrentItem));
    }
    
    /*
	 * as the name implies
	 * saves preferences for automatic and difficulty
	 */
	public void PopulateSharedPrefs() { 
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(GPSTrackingActivity.this);
		boolean Success = settings.edit().putInt(KEY_UNITS, mMilesOrKilometers).commit();
	    
		
	}
    
    /*
	 * as the name implies
	 * gets preferences for automatic and difficulty
	 */
	public void getSharedPrefs() { 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GPSTrackingActivity.this);
		   mMilesOrKilometers = prefs.getInt(KEY_UNITS,0);
	   
	      
	}
	
	    
    
    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    new MenuInflater(this).inflate(R.menu.main, menu);
	    this.menu = menu;
	    
	    return(super.onCreateOptionsMenu(menu));
	}
    
    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    { 
    	//check selected menu item
    	switch (item.getItemId()) { 
    	case R.id.miles: 
    		mMilesOrKilometers = 0;
    		ChangeUnitsOfMeasure();
    		return true;
    	case R.id.kilometers: 
    		mMilesOrKilometers = 1;
    		ChangeUnitsOfMeasure();
    		return true;
    	//quit program
	    case R.id.menu_quit:
		  finish();
		  return true;
	    default: 
 	      return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	@Override
    public void onRestart() {
      super.onRestart();
      getSharedPrefs();
      Log.d(getClass().getSimpleName(), "onRestart()");
    }

    @Override
    public void onStart() {
      super.onStart();
      getSharedPrefs();
      Log.d(getClass().getSimpleName(), "onStart()");
    }

	// create TabsAdapter to create tabs and behavior
    public class TabsAdapter extends FragmentPagerAdapter
     implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    
     private final Context mContext;
           private final ActionBar mActionBar;
           private final ViewPager mViewPager;
           private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    
           final class TabInfo {
               private final Class<?> clss;
               private final Bundle args;
    
               TabInfo(Class<?> _class, Bundle _args) {
                   clss = _class;
                   args = _args;
               }
           }
    
     public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
      super(activity.getSupportFragmentManager());
               mContext = activity;
               mActionBar = activity.getSupportActionBar();
               mViewPager = pager;
               mViewPager.setAdapter(this);
               mViewPager.setOnPageChangeListener(this);
           }
    
     public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
               TabInfo info = new TabInfo(clss, args);
               tab.setTag(info);
               tab.setTabListener(this);
               mTabs.add(info);
               mActionBar.addTab(tab);
               notifyDataSetChanged();
    
           }
     
     
     
     @Override
     public void onPageScrollStateChanged(int state) {
      // TODO Auto-generated method stub
    
     }
    
     @Override
     public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      // TODO Auto-generated method stub
    
     }
    
     @Override
     public void onPageSelected(int position) {
      // TODO Auto-generated method stub
      mActionBar.setSelectedNavigationItem(position);
     }
    
     @Override
     public void onTabReselected(Tab tab, FragmentTransaction ft) {
      // TODO Auto-generated method stub
    
     }
    
     @Override
     public void onTabSelected(Tab tab, FragmentTransaction ft) {
        Object tag = tab.getTag();
        for (int i=0; i<mTabs.size(); i++) {
            if (mTabs.get(i) == tag) {
                mViewPager.setCurrentItem(i);
            }
        }
     }
    
     @Override
     public void onTabUnselected(Tab tab, FragmentTransaction ft) {
      // TODO Auto-generated method stub
    
     }
         
     public Fragment getItem(int position) {
         TabInfo info = mTabs.get(position);
         //Fragment mFragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
         return (Fragment) Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }
     
     
     @Override
     public int getCount() {
      return mTabs.size();
     }
    
    }


    
 
}
