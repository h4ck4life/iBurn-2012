/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pro.dbro.iburn_2012;

import org.osmdroid.util.GeoPoint;

import pro.dbro.iburn_2012.ArtFragment.CursorLoaderListFragment;
import pro.dbro.iburn_2012.data.ArtTable;
import pro.dbro.iburn_2012.data.CampTable;
import pro.dbro.iburn_2012.data.EventTable;
import pro.dbro.iburn_2012.data.PlayaContentProvider;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.support.v4.widget.SimpleCursorAdapter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Demonstration of the use of a CursorLoader to load and display contacts
 * data in a fragment.
 */
@SuppressWarnings("all")
public class CampFragment extends FragmentActivity {
	public static Context c;
	
	public static PopupWindow pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this.getBaseContext();
        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment list = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }

    }
    
  


    public static class CursorLoaderListFragment extends PlayaListFragmentBase implements LoaderManager.LoaderCallbacks<Cursor> {

        // This is the Adapter being used to display the list's data.
        SimpleCursorAdapter mAdapter;
        
        LoaderManager lm;
        
        // TextView to display when no ListView items are present
        
        public void initLoader(){
        	getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void restartLoader(){
        	getLoaderManager().restartLoader(0, null, CursorLoaderListFragment.this);
    	 }

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Text to display before ListView is populated
            emptyText.setText("Loading Camps");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);
            /*
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(dbReadyReceiver,
            	      new IntentFilter("dbReady"));
            */
            lm = this.getLoaderManager();
            // Start out with a progress indicator.
            //setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            if(FragmentTabsPager.app.dbReady  && mAdapter == null){
            	mAdapter = new CampCursorAdapter(getActivity(), null);
            	setListAdapter(mAdapter);
            	getLoaderManager().initLoader(0, null, this);
            }
            // Else, this will be done on dbReady signal
            
            ListView lv = getListView();
            lv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            lv.setFastScrollEnabled(true);
        }
        
        private BroadcastReceiver dbReadyReceiver = new BroadcastReceiver() {
        	  @Override
        	  public void onReceive(Context context, Intent intent) {
        	    // 1 -- success, 0 -- error, -1 no data
        	    int status = intent.getIntExtra("status", -1);
        	    if(status == 1){
        	    	mAdapter = new CampCursorAdapter(getActivity(), null);
                    initLoader();
        	    	//getLoaderManager().initLoader(0, null, (android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) lm);
                    setListAdapter(mAdapter);
                    //setListShown(true); // may not be necessary
        	    	
        	    }
        	  }
        	};

        // These are the Camp rows that we will retrieve.
        static final String[] CAMP_PROJECTION = new String[] {
            CampTable.COLUMN_ID,
            CampTable.COLUMN_NAME,
            CampTable.COLUMN_LOCATION,
            CampTable.COLUMN_FAVORITE
        };

        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader, so we don't care about the ID.
            // First, pick the base URI to use depending on whether we are
            // currently filtering.
            
        	Uri baseUri;
            String ordering = null;
            if (mCurFilter != null) {
                baseUri = Uri.withAppendedPath(PlayaContentProvider.CAMP_SEARCH_URI, Uri.encode(mCurFilter));
            } else {
                baseUri = PlayaContentProvider.CAMP_URI;
                ordering = CampTable.COLUMN_NAME + " ASC";
            }
            
            String selection = null;
            String[] selectionArgs = null;
            
            if(limitListToFavorites){
            	selection = ArtTable.COLUMN_FAVORITE + " = ?";
            	selectionArgs = new String[]{"1"};
            }
			
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
        	/*
            String select = "((" + People.DISPLAY_NAME + " NOTNULL) AND ("
                    + People.DISPLAY_NAME + " != '' ))";
            
            return new CursorLoader(getActivity(), PlayaContentProvider.CAMP_URI,
                    CAMP_PROJECTION, select, null, CampTable.COLUMN_NAME + " ASC");
            */
            return new CursorLoader(getActivity(), baseUri,
                    CAMP_PROJECTION, selection, selectionArgs,
                    ordering);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);
            
            // If searching, show no camps match query
            if (data.getCount() == 0 && mCurFilter != "" && mCurFilter != null){
            	emptyText.setVisibility(View.VISIBLE);
            	emptyText.setText("These aren't the camps you're looking for...");
            }
            else if(data.getCount() == 0)
            	if(limitListToFavorites)
            		emptyText.setText("Select a camp, favorite it and see it here.");
            	else
            		emptyText.setText("No Camps Found");
            else
            	emptyText.setVisibility(View.GONE);
            // The list should now be shown.
            /*
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }*/
        }
        
        @Override
        public void onListItemClick (ListView l, View v, int position, long id){
        	
        	String camp_id = v.getTag(R.id.list_item_related_model).toString();
        	showCampPopup(listView, camp_id);
        	/*
        	Cursor result = getActivity().getContentResolver().query((PlayaContentProvider.CAMP_URI.buildUpon().appendPath(camp_id).build()), 
        			new String[] {CampTable.COLUMN_NAME, CampTable.COLUMN_DESCRIPTION, 
        						  CampTable.COLUMN_LATITUDE, CampTable.COLUMN_LONGITUDE, 
        						  CampTable.COLUMN_LOCATION, CampTable.COLUMN_CONTACT,
        						  CampTable.COLUMN_HOMETOWN, CampTable.COLUMN_FAVORITE},
        			null, 
        			null, 
        			null);
        	if(result.moveToFirst()){
        		View popup = super.getPopupView();
        		
	        	((TextView) popup.findViewById(R.id.popup_title)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_NAME)));

	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_CONTACT))){
	        		((TextView) popup.findViewById(R.id.popup_contact)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_CONTACT)));
	        		((TextView) popup.findViewById(R.id.popup_contact)).setVisibility(View.VISIBLE);
	        	}
	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_HOMETOWN))){
	        		((TextView) popup.findViewById(R.id.popup_hometown)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_HOMETOWN)));
	        		((TextView) popup.findViewById(R.id.popup_hometown)).setVisibility(View.VISIBLE);
	        	}
	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_DESCRIPTION))){
	        		((TextView) popup.findViewById(R.id.popup_description)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_DESCRIPTION)));
	        		((TextView) popup.findViewById(R.id.popup_description)).setVisibility(View.VISIBLE);
	        	}
	        	View favoriteBtn = popup.findViewById(R.id.favorite_button);
	        	int isFavorite = result.getInt(result.getColumnIndex(CampTable.COLUMN_FAVORITE));
	        	if(isFavorite == 1)
	        		((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_on);
	        	else
	        		((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_off);
	        	favoriteBtn.setTag(R.id.list_item_related_model, camp_id);
	        	favoriteBtn.setTag(R.id.favorite_button_state, isFavorite);
	        	
	        	favoriteBtn.setOnClickListener(new OnClickListener(){

	    			@Override
	    			public void onClick(View v) {
	    				String camp_id = v.getTag(R.id.list_item_related_model).toString();
	    				ContentValues values = new ContentValues();
	    				if((Integer)v.getTag(R.id.favorite_button_state) == 0){
	    					values.put(CampTable.COLUMN_FAVORITE, 1);
	    					v.setTag(R.id.favorite_button_state, 1);
	    					((ImageView)v).setImageResource(android.R.drawable.star_big_on);
	    				}
	    				else if((Integer)v.getTag(R.id.favorite_button_state) == 1){
	    					values.put(CampTable.COLUMN_FAVORITE, 0);
	    					v.setTag(R.id.favorite_button_state, 0);
	    					((ImageView)v).setImageResource(android.R.drawable.star_big_off);
	    				}
	    				int result = getActivity().getContentResolver().update(PlayaContentProvider.CAMP_URI.buildUpon().appendPath(camp_id).build(), 
	    						values, null, null);
	    				
	    			}
	    			 
	    		 });
	        	
	        	PopupWindow pw = new PopupWindow(popup,LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, true);
	        	pw.setBackgroundDrawable(new BitmapDrawable());
	        	pw.showAtLocation(listView, Gravity.CENTER, 0, 0);
        	}*/
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }
        
        
        public static void showCampPopup(View parent, String camp_id){
        	Log.d("CampQuery",camp_id);
        	Cursor result = FragmentTabsPager.app.getContentResolver().query((PlayaContentProvider.CAMP_URI.buildUpon().appendPath(camp_id).build()), 
        			new String[] {CampTable.COLUMN_NAME, CampTable.COLUMN_DESCRIPTION, 
        						  CampTable.COLUMN_LATITUDE, CampTable.COLUMN_LONGITUDE, 
        						  CampTable.COLUMN_LOCATION, CampTable.COLUMN_CONTACT,
        						  CampTable.COLUMN_HOMETOWN, CampTable.COLUMN_FAVORITE},
        			null, 
        			null, 
        			null);
        	if(result.moveToFirst()){
        		
        	
        		LayoutInflater layoutInflater = (LayoutInflater)FragmentTabsPager.app.getSystemService(FragmentTabsPager.app.LAYOUT_INFLATER_SERVICE); 
   			 	View popup = layoutInflater.inflate(R.layout.popup, null); 
        		
	        	((TextView) popup.findViewById(R.id.popup_title)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_NAME)));

	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_CONTACT))){
	        		((TextView) popup.findViewById(R.id.popup_contact)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_CONTACT)));
	        		((TextView) popup.findViewById(R.id.popup_contact)).setVisibility(View.VISIBLE);
	        	}
	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_HOMETOWN))){
	        		((TextView) popup.findViewById(R.id.popup_hometown)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_HOMETOWN)));
	        		((TextView) popup.findViewById(R.id.popup_hometown)).setVisibility(View.VISIBLE);
	        	}
	        	if(!result.isNull(result.getColumnIndex(CampTable.COLUMN_DESCRIPTION))){
	        		((TextView) popup.findViewById(R.id.popup_description)).setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_DESCRIPTION)));
	        		((TextView) popup.findViewById(R.id.popup_description)).setVisibility(View.VISIBLE);
	        	}
	        	if(FragmentTabsPager.app.embargoClear && !result.isNull(result.getColumnIndex(CampTable.COLUMN_LATITUDE))){
	        		TextView location = ((TextView) popup.findViewById(R.id.popup_location));
	        		location.setText(result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_LATITUDE)) + " , " + result.getString(result.getColumnIndexOrThrow(CampTable.COLUMN_LONGITUDE)));
	        		location.setVisibility(View.VISIBLE);
	        		//location.setFocusableInTouchMode(true);
	        		GeoPoint locationPoint = new GeoPoint(result.getDouble(result.getColumnIndexOrThrow(CampTable.COLUMN_LATITUDE)), 
	        										 result.getDouble(result.getColumnIndexOrThrow(CampTable.COLUMN_LONGITUDE)));
	        		location.setTag(R.id.view_location_link, locationPoint);
	        		location.setOnTouchListener(new OnTouchListener(){

						@Override
						public boolean onTouch(View v, MotionEvent me) {
							if(me.getAction() == MotionEvent.ACTION_DOWN){
								Log.d("Popup","Click " + v.getTag(R.id.view_location_link).toString());
								FragmentTabsPager.mViewPager.setCurrentItem(0);
								OpenStreetMapFragment.centerMap((GeoPoint)v.getTag(R.id.view_location_link));
								pw.dismiss();
								return true;
							}
							return false;
						}
	        			
	        		});
	        	}
        	
	        	View favoriteBtn = popup.findViewById(R.id.favorite_button);
	        	int isFavorite = result.getInt(result.getColumnIndex(CampTable.COLUMN_FAVORITE));
	        	if(isFavorite == 1)
	        		((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_on);
	        	else
	        		((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_off);
	        	favoriteBtn.setTag(R.id.list_item_related_model, camp_id);
	        	favoriteBtn.setTag(R.id.favorite_button_state, isFavorite);
	        	
	        	favoriteBtn.setOnClickListener(new OnClickListener(){

	    			@Override
	    			public void onClick(View v) {
	    				String camp_id = v.getTag(R.id.list_item_related_model).toString();
	    				ContentValues values = new ContentValues();
	    				if((Integer)v.getTag(R.id.favorite_button_state) == 0){
	    					values.put(CampTable.COLUMN_FAVORITE, 1);
	    					v.setTag(R.id.favorite_button_state, 1);
	    					((ImageView)v).setImageResource(android.R.drawable.star_big_on);
	    				}
	    				else if((Integer)v.getTag(R.id.favorite_button_state) == 1){
	    					values.put(CampTable.COLUMN_FAVORITE, 0);
	    					v.setTag(R.id.favorite_button_state, 0);
	    					((ImageView)v).setImageResource(android.R.drawable.star_big_off);
	    				}
	    				int result = FragmentTabsPager.app.getContentResolver().update(PlayaContentProvider.CAMP_URI.buildUpon().appendPath(camp_id).build(), 
	    						values, null, null);
	    				
	    			}
	    			 
	    		 });
	        	
	        	pw = new PopupWindow(popup,LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, true);
	        
	        	pw.setBackgroundDrawable(new BitmapDrawable());
	        	pw.showAtLocation(parent, Gravity.CENTER, 0, 0);
	        	result.close();
        	}// if moveToFirst
        }
    }

}
