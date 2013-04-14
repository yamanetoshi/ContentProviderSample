package com.example.cpexample;

import com.example.cpexample.SQLiteProvider.Contract;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
    
   private SimpleCursorAdapter mAdapter;
   private ListView mListView;
   private ProgressDialog mProgressDialog;
   
   private final String TITLE = "title";
   private final String NOTE = "note";
   
   MultiChoiceModeListener mActionModeCalback = new MultiChoiceModeListener() {
       @Override
       public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
           return false;
       }

       @Override
       public boolean onCreateActionMode(ActionMode mode, Menu menu) {
           getMenuInflater().inflate(R.menu.list_action, menu);
           mode.setTitle("対象を選択");
           return true;
       }

       @Override
       public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
           switch (item.getItemId()) {
               case R.id.delete:
                       SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
                       SimpleCursorAdapter adapter = (SimpleCursorAdapter) mListView.getAdapter();
                       mProgressDialog.show();
                       
                       for (int i = 0 ; i < adapter.getCount(); i++) {
                    	   boolean checked = checkedItemPositions.get(i);
                           if (checked) {
                        	   long id = adapter.getItemId(i);
                        	   Uri delUri = ContentUris.withAppendedId(Contract.Table1.contentUri, id);
                        	   
                        	   getContentResolver().delete(
                                       delUri,
                                       null,
                                       null
                                       );
                           }

                       }

                       mProgressDialog.dismiss();
                       mode.finish();
                       return true;
                   default:
                       return false;
               }
           }
       
           @Override
           public void onDestroyActionMode(ActionMode mode) {
           }

           @Override
           public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                   boolean checked) {
           }
       };

    
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       
       mProgressDialog = ProgressDialog.show(this, null, "Loading...");
                
       // CursorAdapterをセット. フラグの部分はautoRequeryはしないようにセットするので注意.
       final String[] from = {"title"};
       final int[] to = {android.R.id.text1};
       mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);
       mListView = ((ListView)findViewById(R.id.listView));
       
       mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
       mListView.setMultiChoiceModeListener(mActionModeCalback);
       
       mListView.setAdapter(mAdapter);
       mListView.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;
			Cursor cursor = (Cursor)listView.getItemAtPosition(position);
			
			Intent i = new Intent(MainActivity.this, MainEdit.class);
            Uri uri = ContentUris.withAppendedId(Contract.Table1.contentUri, id);
            //Toast.makeText(getApplicationContext(), "Uri : " + uri ,Toast.LENGTH_SHORT).show();
            i.setData(uri);
            //startActivity(new Intent(Intent.ACTION_EDIT, uri));
            i.putExtra(TITLE, cursor.getString(cursor.getColumnIndex(Contract.Table1.columns.get(1))));
            i.putExtra(NOTE, cursor.getString(cursor.getColumnIndex(Contract.Table1.columns.get(2))));
            i.setAction(Intent.ACTION_EDIT);
            startActivity(i);
		}
       });
         
       // table1テーブルのデータを全件検索. 表示.
       getSupportLoaderManager().initLoader(0, null, this);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
           // Inflate the menu; this adds items to the action bar if it is present.
           getMenuInflater().inflate(R.menu.main, menu);
           return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	   case R.id.add:
           Intent i = new Intent(this, MainEdit.class);
           
           i.setData(Contract.Table1.contentUri);
           /*
           i.putExtra(TITLE, "");
           i.putExtra(NOTE, "");
           */
           i.setAction(Intent.ACTION_INSERT);
           startActivity(i);
           return true;
	   default:
           return super.onOptionsItemSelected(item);
	   }
   }

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
       Log.d(getClass().getSimpleName(), "onCreateLoader called.");
       return new CursorLoader(this, Contract.Table1.contentUri, null, null, null, null);
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
       Log.d(getClass().getSimpleName(), "onLoadFinished called.");
       mProgressDialog.dismiss();
        
       // CursorLoaderとCursorAdapterを使用する上での決まり文句.
       mAdapter.swapCursor(c);
   }

   @Override
   public void onLoaderReset(Loader<Cursor> cursor) {
       Log.d(getClass().getSimpleName(), "onLoaderReset called.");
        
       // CursorLoaderとCursorAdapterを使用する上での決まり文句.
       mAdapter.swapCursor(null);
   }
}
