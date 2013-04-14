package com.example.cpexample;

import java.util.Calendar;

import com.example.cpexample.SQLiteProvider.Contract;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Main2Edit extends Activity {
	
	private int STATE_INSERT = 0;
	private int STATE_EDIT = 1;
	
	private Uri mUri;
	private Cursor mCursor;
	private int mState;
	
	private EditText tv_title;
	private EditText tv_note;
	private TextView tv_date;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_edit2);
    	
    	final Intent intent = getIntent();
    	final String action = intent.getAction();
    	
    	tv_title = (EditText)findViewById(R.id.title);
    	tv_note = (EditText)findViewById(R.id.note);
    	tv_date = (TextView)findViewById(R.id.date);
    	
    	if (Intent.ACTION_INSERT.equals(action)) {
    		mState = STATE_INSERT;
    		
    		final Calendar calendar = Calendar.getInstance();

    		final int year = calendar.get(Calendar.YEAR);
    		final int month = calendar.get(Calendar.MONTH);
    		final int day = calendar.get(Calendar.DAY_OF_MONTH);
    		final String date = year + "-" + month + "-" + day;
//    		final String date = "2013-03-12";

    		ContentValues cv = new ContentValues();
            cv.put(Contract.Table2.columns.get(1), date);
            cv.put(Contract.Table2.columns.get(2), "");
            cv.put(Contract.Table2.columns.get(3), "");
    		mUri = getContentResolver().insert(intent.getData(), cv);
    	} else if (Intent.ACTION_EDIT.equals(action)) {
    		mState = STATE_EDIT;
    		mUri = intent.getData();
    	}
    	
    	mCursor = getContentResolver().query(mUri, null, null, null, null);
    	
    	Button btn = (Button)findViewById(R.id.button);
    	btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put(Contract.Table2.columns.get(2), tv_title.getText().toString());
				values.put(Contract.Table2.columns.get(3), tv_note.getText().toString());
		        getContentResolver().update(mUri, values, null, null);
		        
		        finish();
			}
    		
    	});
    }

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mState == STATE_EDIT) {
			if (mCursor != null) {
				mCursor.moveToFirst();
				tv_date.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Table2.columns.get(1))));
				tv_title.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Table2.columns.get(2))));
				tv_note.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Table2.columns.get(3))));
			}
		}
	}
}
