package com.artifex.mupdfdemo;

import java.util.HashMap;
import java.util.List;



import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ChapterActivity extends ListActivity implements OnItemClickListener{


	ProductsPlistParsing myList;
	
	List<HashMap<String, String>> aList;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		myList=new ProductsPlistParsing(this);
		
		aList=myList.getProductsPlistValues();
		
	    String[] from = { "name","page" };
	    
	    // Ids of views in listview_layout
	    int[] to = { R.id.txt,R.id.cur};        
	    
	    // Instantiating an adapter to store each items
	    // R.layout.listview_layout defines the layout of each item
	    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_layout, from, to);
	    
	    // Getting a reference to listview of main.xml layout file
        ListView lv = getListView();
        
        //Own row layout
        lv.setOnItemClickListener(this);  	        
	    //ListView listView = ( ListView ) findViewById(R.id.listView1);
	    
	    // Setting the adapter to the listView
	    lv.setAdapter(adapter);     
	    
	    setResult(-1);
	
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id){
		// TODO Auto-generated method stub
		HashMap<String, String> hm = aList.get(position);
		int page = Integer.parseInt(hm.get("page"));
		setResult(page);
		finish();
		
	}

}
