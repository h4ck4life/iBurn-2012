package com.trailbehind.android.iburn_2012;

import java.util.Calendar;
import java.util.HashMap;

import com.trailbehind.android.iburn_2012.data.EventTable;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EventCursorAdapter extends SimpleCursorAdapter {

	public EventCursorAdapter(Context context, Cursor c){
		super(context, R.layout.camp_listview_item, c, new String[]{} , new int[]{}, 0);
	}

	
	@Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ViewCache view_cache = (ViewCache) view.getTag(R.id.list_item_cache);
        if (view_cache == null) {
        	view_cache = new ViewCache();
        	view_cache.title = (TextView) view.findViewById(R.id.list_item_title);
        	view_cache.sub = (TextView) view.findViewById(R.id.list_item_sub);
        	//view_cache.thumbnail = (ImageView) view.findViewById(R.id.list_item_image);
            
        	view_cache.title_col = cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME);
        	view_cache.sub_col = cursor.getColumnIndexOrThrow(EventTable.COLUMN_START_TIME);
        	view_cache._id_col = cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID);
        	if(cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ALL_DAY)) == 1 ){
        		view_cache.all_day = true;
        		view_cache.time_label = "All " + cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_START_TIME_PRINT));
        	}
        	else {
        		view_cache.all_day = false;
        		view_cache.time_label = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_START_TIME_PRINT));
        	}
        	//view_cache.thumbnail_col = cursor.getColumnIndexOrThrow(SQLiteWrapper.COLUMN_THUMBNAIL_PATH);
        	view_cache._id_col = cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID);
            //view.setTag(R.id.list_item_cache, view_cache);
        }
        //Log.d("bindView","yeah");
        view_cache.title.setText(cursor.getString(view_cache.title_col));
        view_cache.sub.setText(view_cache.time_label);
        //view_cache.thumbnail.setImageBitmap(BitmapFactory.decodeFile(cursor.getString(view_cache.thumbnail_col)));
        view.setTag(R.id.list_item_related_model, cursor.getInt(view_cache._id_col));
    }
	
	// Cache the views within a ListView row item 
    static class ViewCache {
        TextView title;
        //TextView body;
        TextView sub;
        
        boolean all_day;
        String time_label;
        
        int title_col; 
        int sub_col;
        int _id_col;
    }
}
