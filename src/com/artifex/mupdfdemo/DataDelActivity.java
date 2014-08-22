package com.artifex.mupdfdemo;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import android.widget.ArrayAdapter;

public class DataDelActivity extends ListActivity implements OnItemClickListener{
	

	int pos;	
	private ArrayList<String> results = new ArrayList<String>();
	
	//private String tableName = MySQLiteHelper.tableName;
	
	private SQLiteDatabase newDB;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        openAndQueryDatabase();
        
        displayResultList();
        
        ListView lv = getListView();
        
        //Own row layout
        lv.setOnItemClickListener(this);  	        
    }
    
    
	private void displayResultList() {
		
		//TextView tView = new TextView(this);
        
		//tView.setText("Please select bookmark");
        
		//getListView().addHeaderView(tView);
        
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, results));
        
        getListView().setTextFilterEnabled(true);
		
	}
	
	
	private void openAndQueryDatabase() {
		try {
			MySQLiteHelper dbHelper = new MySQLiteHelper(this.getApplicationContext());
			newDB = dbHelper.getWritableDatabase();
			Cursor c = newDB.rawQuery("SELECT id, title, page FROM books", null);

	    	if (c != null ) {
	    		if  (c.moveToFirst()) {
	    			do {
	    				int id=c.getInt(c.getColumnIndex("id"));
	    				String title = c.getString(c.getColumnIndex("title"));
	    				int page = c.getInt(c.getColumnIndex("page"));
	    				results.add(title + "(" + page+"page)");
	    				
	    			}while (c.moveToNext());
	    		} 
	    	}			
		} catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {
        	if (newDB != null) 
        		//newDB.execSQL("DELETE FROM books");
        		newDB.close();
        }

	}
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id){
        //String val=(String)(lv.getItemAtPosition(position));
		//results.get(position)
		//int pos;
		pos=position;
		try{
			
//------------------------
				AlertDialog.Builder alert = new AlertDialog.Builder(DataDelActivity.this);

				alert.setTitle("Delete Bookmark");
				alert.setMessage("Do you really want to delete current Bookmark?");

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
	
						MySQLiteHelper dbHelper = new MySQLiteHelper(DataDelActivity.this.getApplicationContext());
						newDB = dbHelper.getWritableDatabase();
						Cursor c = newDB.rawQuery("SELECT id, title, page FROM books", null);
						if(c!=null){
							c.moveToPosition(pos);
							  // Do something with value!
							String rowId = c.getString(c.getColumnIndex("id")); 
							results.clear();
							newDB.delete("books", "id=?",  new String[]{rowId});
					        newDB.close();
					        
							openAndQueryDatabase();
					        
					        displayResultList();

						}

					 }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();		

				//-----------------

	            //Toast.makeText(getApplicationContext(), c.getString(c.getColumnIndex("page")),Toast.LENGTH_SHORT).show();
		}
		 catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {
        	if (newDB != null) 
        		//newDB.execSQL("DELETE FROM books");
        		newDB.close();
        }
		
	}
}