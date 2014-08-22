package com.artifex.mupdfdemo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
//import android.net.Uri;
import android.os.Bundle;
//import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;
class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity implements FilePicker.FilePickerSupport
{
	/* The core rendering instance */
	enum TopBarMode {Main, Search, Bookmark,PageGo};
	//enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText};

	private final int    OUTLINE_REQUEST=0;
	private final int    BOOKMARK_REQUEST=1;
	private MuPDFCore    core;
	//private String       mFileName;
	private MuPDFReaderView mDocView;
	private View         mButtonsView;
	private boolean      mButtonsVisible;
	//private TextView     mFilenameView;
	private ImageButton  mCoverButton;
	private ImageButton  mBackButton;
	private SeekBar      mPageSlider;
	private int          mPageSliderRes;
	private TextView     mPageNumberView;
	//private TextView     mInfoView;
	
	private ImageButton  mBookmarkGotoButton;
	private ImageButton  mBookmarkAddButton;
	private ImageButton  mBookmarkDeleteButton;
	
	private ImageButton  mPageButton;
	private ImageButton  mBookmarkButton;
	private ImageButton  mSearchButton;
	private ImageButton  mOutlineButton;
	private ViewAnimator mTopBarSwitcher;
	private TopBarMode   mTopBarMode = TopBarMode.Main;
	private ImageButton  mSearchBack;
	private ImageButton  mSearchFwd;
	private EditText     mSearchText;
	private SearchTask   mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private boolean mReflow = false;
	private AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
	private final static String xiaolong="jcWIEcOdlu8=MTIz";
	
	//==========for choose cover and back==============
	private ProductsPlistParsing myList;
	
	private List<HashMap<String, String>> aList;

	private int Cover, Back;
	
	
	//=============cover and back=============

	private MuPDFCore openBuffer(byte buffer[], String magic)
	{
		System.out.println("Trying to open byte buffer");
		try
		{
			core = new MuPDFCore(this, buffer, magic);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		
		//=====================for choose Cover and Back================
		myList=new ProductsPlistParsing(this);
		
		
		//Log.e("AAAAAAAAAAAAAAAAAAAAAA", "SSSSSSSSSSSSSSSSSSSSSSS");
		aList=myList.getProductsPlistValues();
		
		//Log.e("aaaaaaaaaaaaaaaaaaaaa", String.valueOf(aList.size()));
		
		//Log.d("", "========================================================="+Integer.toString(Back));
		//===================choose cover and back ======================
		
		
		
		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore)getLastNonConfigurationInstance();

		}
		if (core == null) {
			
			//Intent intent = getIntent();
			byte buffer[] = null;
			//if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if (true) {
				//Uri uri = intent.getData();
				//System.out.println("URI to open is: " + uri);
				//if (uri.toString().startsWith("content://")) {
				if (true) {
					String reason = null;
					try {
				        
				        
						//InputStream is = getContentResolver().openInputStream(uri);
						AssetManager assetManager = getAssets();
						InputStream is = assetManager.open("test.pdf");
						
						int len = is.available();
						buffer = new byte[len];
						is.read(buffer, 0, len);
						is.close();
					}
					catch (java.lang.OutOfMemoryError e) {
						System.out.println("Out of memory during buffer reading");
						reason = e.toString();
					}
					catch (Exception e) {
						System.out.println("Exception reading from stream: " + e);

						// Handle view requests from the Transformer Prime's file manager
						// Hopefully other file managers will use this same scheme, if not
						// using explicit paths.
						// I'm hoping that this case below is no longer needed...but it's
						// hard to test as the file manager seems to have changed in 4.x.
						try {
							/*Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
							if (cursor.moveToFirst()) {
								String str = cursor.getString(0);
								if (str == null) {
									reason = "Couldn't parse data in intent";
								}
								else {
									uri = Uri.parse(str);
								}
							}*/
						}
						catch (Exception e2) {
							System.out.println("Exception in Transformer Prime file manager code: " + e2);
							reason = e2.toString();
						}
					}
					if (reason != null) {
						buffer = null;
						Resources res = getResources();
						AlertDialog alert = mAlertBuilder.create();
						setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
						alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										finish();
									}
								});
						alert.show();
						return;
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer, Intent.ACTION_VIEW);
				} else {
					//core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				SearchTaskResult.set(null);
			}
			
			//=========need password===================================================
			if (core != null && core.needsPassword()) {
				requestPassword(savedInstanceState);
				return;
			}
			
			if (core != null && core.countPages() == 0)
			{
				core = null;
			}
		}
		if (core == null)
		{
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			alert.show();
			return;
		}
		
		
		createUI(savedInstanceState);

	}

	public void requestPassword(final Bundle savedInstanceState) {
		
		
		if(core.authenticatePassword(Xiaolong.decrypt(xiaolong)))
		{
			createUI(savedInstanceState);
		}
		else
		{
			
			finish();
		}
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d / %d", i ,
						core.countPages()-1));
				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}

			@Override
			protected void onHit(Hit item) {
				switch (mTopBarMode) {
				// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages()-1,1);
		mPageSliderRes = ((10 + smax - 1)/smax) * 2;

		// Set the file-name text
		/***
		 * mFilenameView.setText(mFileName);
		 */

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress()+mPageSliderRes/2)/mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updatePageNumView((progress+mPageSliderRes/2)/mPageSliderRes);
			}
		});

		//Activate the Bookmark-prepare button
		mBookmarkButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				bookmarkModeOn();
			}
		});
		
		mBookmarkAddButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnCreateBookBtnClick();
			}
		});
		
		mBookmarkGotoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnSelectBookBtnClick();
			}
		});
		
		mBookmarkDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnDeleteBookBtnClick();
			}
		});
		
		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});

		mPageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnPageGoBtnClick();
			}
		});

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				setButtonEnabled(mSearchBack, haveText);
				setButtonEnabled(mSearchFwd, haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
		});

		//React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
					search(1);
				return false;
			}
		});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		/*
		if (core.hasOutline()) {
			mOutlineButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					OutlineItem outline[] = core.getOutline();
					if (outline != null) {
						OutlineActivityData.get().items = outline;
						Intent intent = new Intent(MuPDFActivity.this, OutlineActivity.class);
						startActivityForResult(intent, OUTLINE_REQUEST);
					}
				}
			});
		} else {
			mOutlineButton.setVisibility(View.GONE);
		}
		*/
		mOutlineButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MuPDFActivity.this, ChapterActivity.class);
				startActivityForResult(intent, OUTLINE_REQUEST);
			}
		});
		
		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("currentPage", 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if(savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();


		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		setContentView(layout);
		
		Cover=0;
		//aList.lastIndexOf(object)
		Back=core.countPages()-1;

	}
	
	//=============Related Bookmark========================================
	public void OnPageGoBtnClick(){
		AlertDialog.Builder alert = new AlertDialog.Builder(MuPDFActivity.this);

		alert.setTitle("Go Page");
		alert.setMessage("Please enter pagenumber");

		// Set an EditText view to get user input 
		final EditText input = new EditText(MuPDFActivity.this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText(Integer.toString(mDocView.getDisplayedViewIndex()));
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		    int pagenumber = Integer.parseInt(input.getText().toString());
		    // Do something with value!
		    
			//int page=mDocView.getDisplayedViewIndex()+1;
			//insertBookMark(bookmarkStr, page);
		    mDocView.setDisplayedViewIndex(pagenumber);
			updatePageNumView(pagenumber);
			//mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
			//mPageSlider.setProgress(index*mPageSliderRes);
		    
		    //u//pdatePageNumView(page);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();		
	}

	//=============Related Bookmark========================================
	public void OnCreateBookBtnClick(){
		AlertDialog.Builder alert = new AlertDialog.Builder(MuPDFActivity.this);

		alert.setTitle("Insert Bookmark");
		alert.setMessage("Please enter new bookmark text");

		// Set an EditText view to get user input 
		final EditText input = new EditText(MuPDFActivity.this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		    String bookmarkStr = input.getText().toString();
		    // Do something with value!
		    //mDocView.setDisplayedViewIndex(0);
		    
			int page=mDocView.getDisplayedViewIndex();
			insertBookMark(bookmarkStr, page);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();		
	}
	
	public void insertBookMark(String text, int page){
		
		MySQLiteHelper db = new MySQLiteHelper(this);
	    
	    /**
	     * CRUD Operations
	     * */
	    // Inserting Contacts
	    Log.d("Insert: ", "Inserting ..");
	    db.addBook(new Book(text, page));       
		
	}
	
	public void OnSelectBookBtnClick(){

		Intent intent = new Intent(MuPDFActivity.this, DataListActivity.class);
		startActivityForResult(intent, BOOKMARK_REQUEST);
		
	
	}
	
	public void OnDeleteBookBtnClick(){

		Intent intent = new Intent(MuPDFActivity.this, DataDelActivity.class);
		startActivity(intent);
		
	
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUTLINE_REQUEST:
			if (resultCode >= 0)
				mDocView.setDisplayedViewIndex(resultCode);
			break;
			
		case BOOKMARK_REQUEST:
			if (resultCode >= 0)
				mDocView.setDisplayedViewIndex(resultCode);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public Object onRetainNonConfigurationInstance()
	{
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//if (mFileName != null && mDocView != null) {
		if (mDocView != null) {	
			//outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("currentPage", mDocView.getDisplayedViewIndex());
			edit.commit();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search)
			outState.putBoolean("SearchMode", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchTask != null)
			mSearchTask.stop();

		//if (mFileName != null && mDocView != null) {
		if (mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("currentPage", mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}


	public void onDestroy()
	{
		if (mDocView != null) {
			mDocView.applyToChildren(new ReaderView.ViewMapper() {
				void applyToView(View view) {
					((MuPDFView)view).releaseBitmaps();
				}
			});
		}
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		//MuPDFActivity.super.onDestroy();
		super.onDestroy();

		
		
		
	}

	private void setButtonEnabled(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255):Color.argb(255, 128, 128, 128));
	}


	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
			mPageSlider.setProgress(index*mPageSliderRes);
			if (mTopBarMode == TopBarMode.Search) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mCoverButton.setVisibility(View.VISIBLE);
					mBackButton.setVisibility(View.VISIBLE);
					mPageSlider.setVisibility(View.VISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
				}
			});
			mCoverButton.startAnimation(anim);
			mPageSlider.startAnimation(anim);
			mBackButton.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mPageSlider.setVisibility(View.INVISIBLE);
					mCoverButton.setVisibility(View.INVISIBLE);
					mBackButton.setVisibility(View.INVISIBLE);
					
				}
			});
			mCoverButton.startAnimation(anim);
			mPageSlider.startAnimation(anim);
			mBackButton.startAnimation(anim);
		}
	}

	
	private void bookmarkModeOn(){
		if(mTopBarMode != TopBarMode.Bookmark){
			mTopBarMode=TopBarMode.Bookmark;
			
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}
	
	private void bookmarkModeOff() {
		if (mTopBarMode == TopBarMode.Bookmark) {
			mTopBarMode = TopBarMode.Main;

			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}


	
	
	
	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			//Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}
	//==========pagenumber text undate
	private void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d / %d", index, core.countPages()-1));
	}



	private void makeButtonsView() {
		
		mButtonsView = getLayoutInflater().inflate(R.layout.buttons,null);
		//mFilenameView = (TextView)mButtonsView.findViewById(R.id.docNameText);
		mPageButton=(ImageButton)mButtonsView.findViewById(R.id.PageButton);
		mBookmarkButton=(ImageButton)mButtonsView.findViewById(R.id.BookmarkButton);
		mSearchButton = (ImageButton)mButtonsView.findViewById(R.id.searchButton);
		mOutlineButton = (ImageButton)mButtonsView.findViewById(R.id.outlineButton);
		mTopBarSwitcher = (ViewAnimator)mButtonsView.findViewById(R.id.switcher);
		
		
		mBookmarkGotoButton=(ImageButton)mButtonsView.findViewById(R.id.gotoBookmark);
		mBookmarkAddButton=(ImageButton)mButtonsView.findViewById(R.id.addBookmark);
		mBookmarkDeleteButton=(ImageButton)mButtonsView.findViewById(R.id.deleteBookmark);
		
		
		mSearchBack = (ImageButton)mButtonsView.findViewById(R.id.searchBack);
		mSearchFwd = (ImageButton)mButtonsView.findViewById(R.id.searchForward);
		mSearchText = (EditText)mButtonsView.findViewById(R.id.searchText);
		
		mTopBarSwitcher.setVisibility(View.INVISIBLE);

		//=================bottom buttons
		mCoverButton=(ImageButton)mButtonsView.findViewById(R.id.coverButton);
		mPageSlider = (SeekBar)mButtonsView.findViewById(R.id.pageSlider);
		mBackButton=(ImageButton)mButtonsView.findViewById(R.id.backButton);
		mPageNumberView = (TextView)mButtonsView.findViewById(R.id.pageNumber);

		mCoverButton.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.INVISIBLE);
		mBackButton.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
	}


	//===========cancel search 
	public void OnCancelSearchButtonClick(View v) {
		searchModeOff();
	}

	//===========cancel bookmark 
	public void OnCancelBookmarkButtonClick(View v) {
		bookmarkModeOff();
		
	}

	
	//============goto Cover
	public void OnCoverButtonClick(View v){
		mDocView.setDisplayedViewIndex(Cover);
		updatePageNumView(Cover);
	}
	//===========goto back
	public void OnBackButtonClick(View v){
		mDocView.setDisplayedViewIndex(Back);
		updatePageNumView(Back);
		
	}

	
	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	
	private void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void performPickFor(FilePicker picker) {
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	    if (keyCode == KeyEvent.KEYCODE_BACK) {

			AlertDialog.Builder alert = new AlertDialog.Builder(MuPDFActivity.this);
	
			alert.setTitle("MyReader");
			alert.setMessage("Do you really want to exit?");
	
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				 }
			});
	
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
	
			alert.show();		
	    }
		
		return super.onKeyDown(keyCode, event);
	}
	
	
}
