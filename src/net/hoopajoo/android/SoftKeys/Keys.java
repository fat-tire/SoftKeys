/*
 *
 *  Copyright (c) 2010 Steve Slaven
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
*/
package net.hoopajoo.android.SoftKeys;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Keys extends Activity implements OnClickListener, OnLongClickListener {
    private String defaultLauncher;
    private boolean isPreTap = false;
    private final String LOG = "SoftKeys";
    private Handler mHandler = new Handler();
    private boolean isPaused = false;
    private final int PREFS_ACTIVITY = 9;
    private RecentAppsChunk recent = null;
    private boolean return_after_back = false;
    
    // these track the home action hacks for single/double/etc press actions
    private Runnable delayed_action ;
    private Runnable delayed_pretap_action ;
    private String homeaction;
    private int delayed_action_time;
    
    public static String ACTION_MENU = "net.hoopajoo.android.SoftKeys.KEY_MENU";
    public static String ACTION_HOME = "net.hoopajoo.android.SoftKeys.KEY_HOME";
    public static String ACTION_BACK = "net.hoopajoo.android.SoftKeys.KEY_BACK";
    public static String ACTION_SEARCH = "net.hoopajoo.android.SoftKeys.KEY_SEARCH";
    
    // simple typedef used to make the notifications a bit more generic
    private class NotificationButton {
        String mPrefKey;
        RemoteViews mView;
        int mIconId;
        Drawable mIcon;
        String mButtonText;
        String mAction;

        NotificationButton( String text, String pref, RemoteViews view, Drawable d, int icon, String act ) {
            mButtonText = text;
            mPrefKey = pref;
            mView = view;
            mIconId = icon;
            mIcon = d;
            mAction = act;
        }

        NotificationButton( String text, String pref, int icon, String act ) {
            this( text, pref, null, null, icon, act );
        }

    }

    // For use by the service and this activity
    public static void applyButtons( SharedPreferences settings, View v,
            OnClickListener onClick, OnLongClickListener onLongClick ) {
        applyButtons( settings, v, onClick, onLongClick, null, false );
    }
     
    public static void applyButtons( SharedPreferences settings, View v,
                OnClickListener onClick, OnLongClickListener onLongClick,
                OnTouchListener onTouch, Boolean service ) {
        // reorder the buttons, they will be in the order of the buttons[] array
        // default is the order from my captivate:
        //  menu, home, back, search
        int[] buttons = { R.id.menu, R.id.home, R.id.back,
                R.id.search, R.id.settings, R.id.exit };
        
        // now sort the buttons, we loop from 1 to 4, find the stuff with the same
        // index as our index we're using, and add them to the list.  This should pick
        // everything but since they will all have something in 1-4 and also handle
        // collisions in a predetermined way
        int button_index = 0;
        for( int i = 1; i < 5; i++ ) {
            // this could probably be optimized but it's late
            if( Integer.parseInt( settings.getString( "order_menu", "1" ) ) == i ) {
                buttons[ button_index++ ] = R.id.menu;
            }

            if( Integer.parseInt( settings.getString( "order_home", "1" ) ) == i ) {
                buttons[ button_index++ ] = R.id.home;
            }

            if( Integer.parseInt( settings.getString( "order_back", "1" ) ) == i ) {
                buttons[ button_index++ ] = R.id.back;
            }
            
            if( Integer.parseInt( settings.getString( "order_search", "1" ) ) == i ) {
                buttons[ button_index++ ] = R.id.search;
            }            
        }
        // now add choose and exit, always last
        buttons[ button_index++ ] = R.id.settings;
        buttons[ button_index++ ] = R.id.exit;
        
        ImageButton[] buttons_ordered = new ImageButton[ buttons.length ];
        
        button_index = 0;
        for( int i : buttons ) {
            ImageButton b = (ImageButton)v.findViewById( i );
            if( b != null ) {
                b.setOnClickListener( onClick );
                b.setOnLongClickListener( onLongClick );
                b.setOnTouchListener( onTouch );
                buttons_ordered[ button_index++ ] = b;
                
                if( ! service ) {
                    // hide some stuff
                    if( i == R.id.exit ) {
                        b.setVisibility(
                                settings.getBoolean( "exitbutton", true ) ?  View.VISIBLE : View.GONE );
                    }
                    
                    if( i == R.id.settings ) {
                        b.setVisibility(
                            settings.getBoolean( "choosebutton", true ) ?  View.VISIBLE : View.GONE );
                    }
                }
            }
        }

        ViewGroup l = (ViewGroup)v.findViewById( R.id.button_container );
        l.removeAllViews();
        for( ImageButton b : buttons_ordered ) {
            if( b != null ) {
                l.addView( b );
            }
        }
        

    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues( this, R.xml.prefs, true );
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );

        setContentView( R.layout.main );
        
        // warn if we don't notice some binaries we need
        for( String name : new String[] { "/system/bin/su", "/system/bin/input" } ) {
            File check = new File( name );
            try {
                if( ! check.exists() ) {
                    Toast.makeText( this, "Failed to find file: " + name + ", SoftKeys may not function", Toast.LENGTH_LONG ).show();                    
                }
            }catch( Exception e ) {
                Toast.makeText( this, "Failed to check for file: " + name, Toast.LENGTH_LONG ).show();
            }
                
        }
        
        // long click outside buttons == config
        View main = findViewById( R.id.main_view );
        main.setLongClickable( true );
        main.setOnLongClickListener( this );
        
        findViewById( R.id.main_view ).setClickable( true );

        if( settings.getBoolean( "blur_behind", false ) ) {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_BLUR_BEHIND );
        }else{
            getWindow().clearFlags( WindowManager.LayoutParams.FLAG_BLUR_BEHIND );
        }
        
        if( settings.getBoolean( "dim_behind", true ) ) {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
        }else{
            getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
        }

        // dynamically insert our button container and button views
        Generator.createButtonContainer( this, 0, 1, "main", (ViewGroup)findViewById( R.id.main_view ) );
        
        // this will reorder/hide buttons
        applyButtons( settings, findViewById( R.id.button_container ), this, this );

        //findViewById( R.id.main_view ).requestLayout();
        
        if( settings.getBoolean( "recent_apps", true ) ) {
            recent = new RecentAppsChunk( this );
        }else{
            findViewById( R.id.recent_apps ).setVisibility( View.GONE );
        }
        
        // Add notification buttons
        Globals app = (Globals)getApplication();
        if( ! app.didInitNotifications ) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            Context context = getApplicationContext();
    
            // note: notification theming is kind of weird because of the way the notification manager
            // handles icons, the icon in the bar itself when the status bar is closed HAS to come
            // from the package creating the notification.  We can however use any custom layouts
            // for the actual notification when the bar is open.  So if we are using custom notifications
            // I just make the icon empty in the status bar which looks odd, but if we don't it would
            // need to be an icon from this package and not from the theme which would mean the pull
            // down notification would look different from the icon in the status bar itself which would
            // be annoying.
            //
            // However is technically is possibly to theme these to an extent currently it's just
            // not very ideal.
            NotificationButton[] nb = new NotificationButton[ 5 ];
            Theme theme = new Theme( this, settings.getString( "theme", null ) );
            nb[ 0 ] = new NotificationButton( "SoftKeys", "nb_softkeys",
                    R.drawable.icon,
                    Intent.ACTION_MAIN );
            nb[ 1 ] = new NotificationButton( "Menu", "nb_menu",
                    theme.getRemoteViews( new String[] { "notification_menu" } ),
                    theme.getDrawable(  new String[] { "notification_menu" }  ),
                    R.drawable.button_menu,
                    ACTION_MENU );
            nb[ 2 ] = new NotificationButton( "Home", "nb_home", 
                    theme.getRemoteViews( new String[] { "notification_home" } ), 
                    theme.getDrawable(  new String[] { "notification_home" }  ),
                    R.drawable.button_home,
                    ACTION_HOME );
            nb[ 3 ] = new NotificationButton( "Back", "nb_back",
                    theme.getRemoteViews( new String[] { "notification_back" } ), 
                    theme.getDrawable(  new String[] { "notification_back" }  ),
                    R.drawable.button_back,
                    ACTION_BACK );
            nb[ 4 ] = new NotificationButton( "Search", "nb_search",
                    theme.getRemoteViews( new String[] { "notification_search" } ), 
                    theme.getDrawable(  new String[] { "notification_search" }  ),
                    R.drawable.button_search,
                    ACTION_SEARCH );
            
            for( NotificationButton b : nb ) {
                if( settings.getBoolean( b.mPrefKey, false ) ) {
                    Notification n = new Notification( b.mIconId, null, 0 );
                    PendingIntent i = PendingIntent.getActivity( this, 0, 
                            new Intent( b.mAction,
                                    null, this, Keys.class ), 0 );
                    
                    // if we got a drawable but no view then set up our own remote view
                    // and add in their drawable
                    if( b.mView == null && b.mIcon != null ) {
                        b.mView = new RemoteViews( getPackageName(), R.layout.notification_bar_shortcut );
                        // we run the drawable through resizeimage because that will rasterize it if it's
                        // not already a bitmapdrawable
                        b.mView.setImageViewBitmap( R.id.nb_image,
                                ((BitmapDrawable)Generator.resizeImage( b.mIcon, 48, 48 )).getBitmap()
                        );
                        b.mView.setTextViewText( R.id.nb_text, "Press SoftKeys " + b.mButtonText + " Button" );
                    }
                    
                    if( b.mView != null ) {
                        // discard icon, use the remote view instead
                        n.icon = -1; // this will make it draw a blank, this kind of sucks
                                     // but looking through notificationmanager and statusbarservice
                                     // you have to post some kind of icon, that id is based on the calling
                                     // package, and that icon is always added to the bar
                        n.contentView = b.mView;
                        n.contentIntent = i;
                    }else{
                        n.setLatestEventInfo( context, b.mButtonText, 
                                b.mAction == Intent.ACTION_MAIN ? "Start SoftKeys" :
                                    "Press SoftKeys " + b.mButtonText + " Button", i
                            );
                    }
                    
                    //Notification n = new Notification();
                    n.flags |= Notification.FLAG_NO_CLEAR;
                    
                    // we use the same icon id as the notification id since it should be unique,
                    // note the first parm here is a notification id we can use to reference/remove stuff
                    // we're not passing an icon here
                    mNotificationManager.notify( b.mIconId, n );
                }else{
                    mNotificationManager.cancel(  b.mIconId );
                }
            }
            // this way every time you click a notification soft key it doesn't readd them all making
            // them jump around as they are re-inserted
            app.didInitNotifications = true;
        }

        return_after_back = settings.getBoolean( "return_home_after_back", false );
        delayed_action = new Runnable() {
            public void run() {
                home_key_action( homeaction);                                        
            }
        };
        delayed_pretap_action = new Runnable() {
            public void run() {
                pretap_home_key_action( homeaction );
            }
        };
        delayed_action_time = Integer.parseInt( settings.getString( "homedoubletime", "250" ) );
        
        // Set default launcher to the first launcher we find so we don't freak out if it's not
        // set and there is no com.android.launcher
        Intent i = new Intent( Intent.ACTION_MAIN );
        i.addCategory( Intent.CATEGORY_HOME );
        PackageManager p = getPackageManager();
        List<ResolveInfo> packages = p.queryIntentActivities( i, 0 );
        
        defaultLauncher = null;
        for( Iterator<ResolveInfo> it = packages.iterator(); it.hasNext(); ) {
            ResolveInfo info = it.next();
            if( defaultLauncher == null ) {
                if( ! info.activityInfo.applicationInfo.packageName.equals( "net.hoopajoo.android.SoftKeys" ) ) {
                    defaultLauncher = info.activityInfo.applicationInfo.packageName;
                }
            }
        }
        if( defaultLauncher == null ) {
            // last ditch
            defaultLauncher = "com.android.launcher";
        }
 
        // what's new/getting started?
        int force_level = 2010122701;
        if( settings.getInt( "last_intro_level", 0 ) < force_level ) {
            Intent intent = new Intent( this, QuickDoc.class );
            intent.putExtra( "type", "getting_started" );
            startActivity( intent );
            SharedPreferences.Editor e = settings.edit();
            e.putInt( "last_intro_level", force_level );
            e.commit();
        }
        
        // simulate wake
        isPaused = true;
        onNewIntent( getIntent() );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle item selection
        switch( item.getItemId() ) {
            case R.id.menu_help:
                Intent intent = new Intent( this, QuickDoc.class );
                intent.putExtra( "type", "help" );
                startActivity( intent );
                return true;
                
            case R.id.menu_settings:
                generic_click( R.id.settings, false );
                return true;
                
            default:
                return super.onOptionsItemSelected( item );
        }
    }
    
    @Override
    public void onNewIntent( Intent i ) {
        Globals app = (Globals)getApplication();
        
        ///////// TODO: remove null junk
        
        // handle real actions
        if( i != null ) {
            String action = i.getAction();
            if( action != null ) {
                int clickbutton = 0;
                if( action.equals( ACTION_MENU  ) ) {
                    clickbutton = R.id.menu;
                }
                if( action.equals( ACTION_HOME  ) ) {
                    clickbutton = R.id.home;
                }
                if( action.equals( ACTION_BACK  ) ) {
                    clickbutton = R.id.back;
                }
                if( action.equals( ACTION_SEARCH  ) ) {
                    clickbutton = R.id.search;
                }
                if( clickbutton != 0 ) {
                    generic_click( clickbutton, false, false );
                    // don't draw the ui
                    this.finish();
                }
            }
        }
        
        if( isPaused ) {
            //d( "detected paused, resetting counter" );
            app.homeCounter = 0;
            isPaused = false;
            if( recent != null ) {
                recent.reloadButtons();
            }
        }

        if( i.hasCategory( Intent.CATEGORY_HOME ) ) {
            app.homeCounter++;
            if( app.homeCounter == 1 ) {
                // post our pretap
                setVisible( false );
                isPreTap = true;
                post_delayed_pretap_home( "pretap" );                
            }else{
                if( isPreTap ) {
                    // handle predoubletap
                    isPreTap = false;
                    clear_delayed_pretap_home();
                    pretap_home_key_action( "predoubletap" );                    
                }else{
                    // Just exit for tap outside of pretap
                    clear_delayed_home();
                    this.finish();
                }
            }
        }

                       
                /*
                // old home counter stuff
                //d( "homecounter: " + app.homeCounter );
                if( app.homeCounter != 0 ) {
                    // they whacked home again
                    
                    // if 2clicker waiting then do 2clicker action
                    if( app.homeCounter > 1 ) {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
                        clear_delayed_home();
                        home_key_action( settings.getString( "homebuttonmulti", "launcher" ) );                
                    }else{
                        // queue up an exit, if this timer doesn't finish before we come up again we'll run double-click instead
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
                        post_delayed_home( settings.getString(  "homebutton", "exit" ) );                        
                    }
                }
                */
            
        
    }

    // calling this will run the desired home action in the specified time unless canceled by
    // a newer action like another home tap
    private void post_delayed_home( String action ) {
        homeaction = action;
        clear_delayed_home();
        mHandler.postDelayed( delayed_action, delayed_action_time );
    }
    
    private void clear_delayed_home() {
        mHandler.removeCallbacks( delayed_action );
    }
    
    private void post_delayed_pretap_home( String action ) {
        homeaction = action;
        clear_delayed_pretap_home();
        mHandler.postDelayed( delayed_pretap_action, delayed_action_time );
    }
    
    private void clear_delayed_pretap_home() {
        mHandler.removeCallbacks( delayed_pretap_action );
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // mark not visible
        //d( "marking not visible" );
        isPaused = true;
    }
    
    public boolean onLongClick( View v ) {
        return( generic_click( v.getId(), true ) );
    }
    
    public void onClick( View v ) {
        generic_click( v.getId(), false );
    }
    
    
    private boolean generic_click( int id, boolean longClick ) {
        return generic_click( id, longClick, true );
    }
    
    private boolean generic_click( int id, boolean longClick, boolean backout ) {
        String keyid = "";
        switch( id ) {
            case R.id.back:
                // If backout=true we are in softkeys main ui so honor return to softkeys
                // by pressing home after this
                keyid = "4";
                break;
            
            case R.id.home:
                // do whatever is selected
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
                
                Intent i = new Intent( Intent.ACTION_MAIN );
                i.setPackage( settings.getString( longClick ? "launcher2" : "launcher" , defaultLauncher ) );
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( i );
                return true;
                
            case R.id.main_view:
            case R.id.settings:
                startActivityForResult( new Intent( this, Prefs.class ), PREFS_ACTIVITY );
                return true;
                
            case R.id.menu:
                keyid = "82";
                break;
                
            case R.id.search:
                keyid = "84";
                break;
            
            case R.id.exit:
                this.finish();
                return true;
          
            default:
                d( "Unkown click event: " + id );
                return true;
        }
        
        try {
            Globals.CommandShell cmd = ((Globals)getApplication()).getCommandShell();
            
            // run our key script
            String wd = getFilesDir().getAbsolutePath();

            // check if we have a dev script
            File script = new File( wd + "/pushkey.dev" );
            
            // check if we have a test script
            if( script.exists() ) {
                d( "Using dev key script" );
            }else{
                // write out our default script
                script = new File( wd + "/pushkey" );
                FileOutputStream out = new FileOutputStream( script );
                out.write( "for f in $* ; do input keyevent $f ; done\n".getBytes( "ASCII" ) );
                out.close();
            }
            
            // if longclick then add another back, e.g. for apps that do something odd like pause when you
            // open another app, so you can back out of that then send the intended key
            if( longClick ) {
                keyid = "4 " + keyid;
            }
     
            if( backout ) {
                // if we need to back out of softkeys before we send the other keys
                keyid = "4 " + keyid;
            }
            
            // source the file since datadata might be noexec
            cmd.system( "sh " + script.getAbsolutePath() + " " + keyid );
            
            // if we sent back, and didn't backout (so it was from main ui) and they
            // want to return, run am to get us back
            if( id == R.id.back && backout && return_after_back ) {
                cmd.system( "am start -a android.intent.action.MAIN -n net.hoopajoo.android.SoftKeys/.Keys" );
            }
        }catch( Exception e ) {
            Log.e( LOG, "Error: " + e.getMessage() );
            Toast.makeText( this, "Unable to execute as root", Toast.LENGTH_LONG ).show();
        }
        
        return true;
    }

    private void d( String log ) {
        Log.d( LOG, log );
    }
    
    private void pretap_home_key_action( String what ) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        String action = settings.getString( "prehomebutton", "launcher" );
        
        if( action.equals( "launcher" ) ) {
            if( what.equals( "pretap" ) ) {
                // do home key action
                home_key_action( "launcher" );
                this.finish();
            }else{
                // double tap, go to softkeys
                this.setVisible( true );
            }
        }else{
            if( what.equals( "pretap" ) ) {
                // go to softkeys
                this.setVisible( true );
            }else{
                // go home
                home_key_action( "launcher" );
                this.finish();
            }

        }    
    }
    
    // currently not used, used to be homekey and homekeymulti prefs
    private void home_key_action( String what ) {
        if( what.equals( "exit" ) ) {
            generic_click( R.id.exit, false );
        }else if( what.equals( "launcher" ) ) {
            // simulate home press
            generic_click( R.id.home, false );
        }else if( what.equals( "launcher2"  ) ) {
            generic_click( R.id.home, true );
        }else if( what.equals( "ignore" ) ) {
            // reset counter
            Globals app = (Globals)getApplication();
            app.homeCounter = 0;
        }/* else if( what.equals( "softkeys" ) ) {
            // does nothing, just cancels the jump-to-home action
            this.setVisible( true );
        }*/
    }
 
    @Override
    public boolean onKeyDown( int code, KeyEvent k ) {
        /* let menu be menu
        if( code == KeyEvent.KEYCODE_MENU ) {
            generic_click( R.id.settings, false );
            return true;
        }
        */
        return super.onKeyDown( code, k );
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.finish();
        // redo notification buttons
        ((Globals)getApplication()).didInitNotifications = false;
        ((Globals)getApplication()).restartService();
        
        startActivity( new Intent( this, Keys.class ) );
    }    
}