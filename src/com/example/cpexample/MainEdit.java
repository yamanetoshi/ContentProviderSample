package com.example.cpexample;

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
import android.widget.TextView;

public class MainEdit extends Activity {
	
	private int STATE_INSERT = 0;
	private int STATE_EDIT = 1;
	
	private Uri mUri;
	private Cursor mCursor;
	private int mState;
	
	private TextView tv_title;
	private TextView tv_note;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_edit);
    	
    	final Intent intent = getIntent();
    	final String action = intent.getAction();
    	
    	tv_title = (TextView)findViewById(R.id.title);
    	tv_note = (TextView)findViewById(R.id.note);
    	
    	if (Intent.ACTION_INSERT.equals(action)) {
    		mState = STATE_INSERT;
    		ContentValues cv = new ContentValues();
            cv.put(Contract.Table1.columns.get(1), "");
            cv.put(Contract.Table1.columns.get(2), "");
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
				values.put(Contract.Table1.columns.get(1), tv_title.getText().toString());
				values.put(Contract.Table1.columns.get(2), tv_note.getText().toString());
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
				tv_title.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Table1.columns.get(1))));
				tv_note.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Table1.columns.get(2))));
			}
		}
	}
}
