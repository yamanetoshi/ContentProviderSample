package com.example.cpexample;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestActivity extends Activity {
	private Cursor mCursor;
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		Button btn = (Button)findViewById(R.id.button_test);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				testQuery2();
			}
			
		});
	}
	
	private void testQuery2() {
		mUri = Uri.parse("content://com.example.test.cp/table2/date/2013-3-14");
		Log.d("TestActivity", mUri.getPathSegments().get(2));
		mCursor = getContentResolver().query(mUri, null, null, null, null);
		if(mCursor.moveToFirst()){
			do {
				long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
				String date = mCursor.getString(mCursor.getColumnIndex("date"));
				String title = mCursor.getString(mCursor.getColumnIndex("title2"));
				String note = mCursor.getString(mCursor.getColumnIndex("note2"));
				Log.d("TestActivity", "id : " + id + "date : " + date + 
						"title : " + title + "note : " + note);
			} while (mCursor.moveToNext());
		}
	}
	
	private void testQuery1() {
		mUri = Uri.parse("content://com.example.test.cp/table2/date");
		mCursor = getContentResolver().query(mUri, null, null, null, null);
		Log.d("TestActivity", mCursor.toString());
		if(mCursor.moveToFirst()){
			do {
				String bookmark = mCursor.getString(mCursor.getColumnIndex("date"));
				Log.d("TestActivity", " title : " + bookmark);
			} while (mCursor.moveToNext());
		}
	}

}
