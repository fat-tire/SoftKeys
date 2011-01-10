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

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class SoftKeysService extends Service {
    private InputSmoother mInputSmoother;
    private View mView;
    private View mExtraView;
    private View mBumpView;
    private boolean auto_hide;
    private boolean auto_hide_after_back;
    private boolean mDraggingView;
    private View mDraggingViewObj;
    private int mDraggingOrigX, mDraggingOrigY;
    private int mDraggingViewX, mDraggingViewY;
    private boolean mDidDrag;
    private boolean mExtraEnabled = false;
    private int mNumDrags;
    private OrientationEventListener mOrientationListener;
    private Runnable mUpdateDrag;
    
    private int mNumRows = 0;
    private Map<Integer,Integer> mCustomKeys = new HashMap<Integer,Integer>();
    
    private final int mOffScreenMax = 20;
    
    private int mScreenWidth;
    private int mScreenHeight;
    private String mOrientation;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        OnClickListener c = new OnClickListener() {
            @Override
            public void onClick( View v ) {
                genericClick( v, false );
            }
        };
        
        OnLongClickListener lc = new OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                return genericClick( v, true );
            }
        };
        
        /* TODO: fix this
        OnTouchListener click = new OnTouchListener() {
            @Override
            public boolean onTouch( View view, MotionEvent me ) {
                return genericClick( view, false, me );
            }
        };
        */
        
        OnLongClickListener longpress_rotate = new OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                if( mDraggingView || mDidDrag ) {
                    return false;
                }
                
                // rotate
                LinearLayout l = (LinearLayout)mView.findViewById( R.id.button_container );
                if( l.getOrientation() == LinearLayout.HORIZONTAL ) {
                    l.setOrientation( LinearLayout.VERTICAL );
                }else{
                    l.setOrientation( LinearLayout.HORIZONTAL );
                }
                
                savePosition();
                return true;
            }
        };
        
        OnTouchListener touch = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                if (me.getAction() == MotionEvent.ACTION_DOWN) {
                    mInputSmoother = new InputSmoother( 5 );
                    
                    mDraggingOrigX = (int)me.getRawX();
                    mDraggingOrigY = (int)me.getRawY();
                    
                    View root = view.getRootView();                        
                    WindowManager.LayoutParams l = (WindowManager.LayoutParams)root.getLayoutParams();
                    mDraggingViewX = l.x;
                    mDraggingViewY = l.y;
                    
                    // If we're anchored use orig x/y as the main loc
                    if( l.gravity != (Gravity.TOP | Gravity.LEFT) ) {
                        int[] loc = new int[ 2 ];
                        root.getLocationOnScreen( loc );
                        mDraggingViewX = loc[ 0 ];
                        mDraggingViewY = loc[ 1 ];
                    }
                            
                    mDraggingView = false;
                    mDidDrag = false;
                    mNumDrags = 0;
                }
                if (me.getAction() == MotionEvent.ACTION_UP) {
                    mDraggingView = false;
                    
                    if( mDidDrag ) {
                        // always final update
                        mUpdateDrag.run();
                        
                        // save x/y
                        savePosition();

                        // do not click
                        return( true );
                    }
                } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                    mNumDrags++; // only really start dragging after a few drag events, so when
                                 // you're just tapping buttons it doesn't drag too by accident
                    
                    if( mNumDrags > 2 ) {
                        mDraggingViewObj = view;
                        mDraggingView = true;
                        mDidDrag = true;

                        // note: input smoother has no effect on the nook, something else is
                        // causing the jitters, I'm guessing we need to lock something when
                        // we update the params.  I've done a lot of testing and even when the
                        // service window jumps to the wierd position the finalxy updates in the
                        // log are not in a weird spot.  After looking through a lot of aosp code
                        // I can't find any place we can lock and force the update in a synchonized way
                        // 
                        // the error seems to happen when you roll your finger, so it feels like
                        // it has something to do with the amount of surface area you have on the
                        // display, and changing that causes stuff to somehow "reset" the layoutparams
                        mInputSmoother.addPoint( (int)me.getRawX(), (int)me.getRawY() );
                        mInputSmoother.updateOutliers();
                        
                        view.post( mUpdateDrag );
                        return( false );
                    }
                }
                return false;
            }
        };

        // run this at move and end touch to make sure we anchor in the right spot
        mUpdateDrag = new Runnable() {
            @Override
            public void run() {
                int[] pts = mInputSmoother.getCurrent();
                int currX = pts[ 0 ];
                int currY = pts[ 1 ];                     
                
                // make our deltas work relative to movement, y
                int dx = currX - mDraggingOrigX;
                int dy = currY - mDraggingOrigY;
                
                //d( "dx: " + dx );
                //d( "dy: " + dy );
                
                View root = mDraggingViewObj.getRootView(); 
                WindowManager.LayoutParams l = (WindowManager.LayoutParams)root.getLayoutParams();
                //d( "x: " + l.x );
                //d( "y: " + l.y );
                //d( "grav: " + l.gravity );
                int width = root.getWidth();
                int height = root.getHeight();
                
                //l.gravity = Gravity.NO_GRAVITY;
                //l.gravity = Gravity.TOP | Gravity.LEFT;
                //l.x += dx;
                //l.y += dy;
                
                int finalx = mDraggingViewX + dx;
                int finaly = mDraggingViewY + dy;
                
                // contraints
                if( finalx < ( mOffScreenMax * -1 ) ) {
                    finalx = mOffScreenMax * -1;
                }
                
                if( finalx + width > mScreenWidth + mOffScreenMax ) {
                    finalx = mScreenWidth + mOffScreenMax - width;
                }
                
                if( finaly < ( mOffScreenMax * -1 ) ) {
                    finaly = mOffScreenMax * -1;
                }
                
                if( finaly + height > mScreenHeight + mOffScreenMax ) {
                    finaly = mScreenHeight + mOffScreenMax - height;
                }
                
                //d( "Final xy: " + finalx + "," + finaly );
                
                l.x = finalx;
                l.y = finaly;
                
                WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
                wm.updateViewLayout( root, l );
            }
        };
        
        // get our root (don't go through theme handler, this comes from the main app always)
        LayoutInflater l = LayoutInflater.from( this );
        
        // The main buttons
        mView = l.inflate( R.layout.service, null );
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );

        // set back/auto hide stuff
        auto_hide = settings.getBoolean( "service_close_after", true );
        auto_hide_after_back = settings.getBoolean( "service_close_after_back", false );
        
        // get button sizes
        String size = settings.getString( "service_size", "medium" );
        float buttonMult = 1;
        if( size.equals( "huge" ) ) {
            buttonMult = 2;
        }else if( size.equals( "large" ) ) {
            buttonMult = 1.5f;
        }else if( size.equals( "medium" ) ) {
            // regular size for the system
            buttonMult = 1;
        }else if( size.equals( "small" ) ) {
            buttonMult = 0.75f;
        }else if( size.equals( "tiny" ) ) {
            buttonMult = 0.5f;
        }

        // insert the container
        ViewGroup container = (ViewGroup)Generator.createButtonContainer( this, 0, buttonMult, "service", (ViewGroup)mView.findViewById( R.id.main_view ) );
        // container may not be button_container for a custom xml view
        ((LinearLayout)mView.findViewById( R.id.button_container )).removeView( container.findViewById( R.id.settings ) ); // no settings in service
        
        // arrange buttons
        Keys.applyButtons( settings, mView, c, lc, null, true );
        mView.setOnTouchListener( touch );
        mView.setOnLongClickListener( longpress_rotate );
        
        // only drag by the exit button now
        mView.findViewById( R.id.exit ).setOnTouchListener( touch );
        mView.findViewById( R.id.exit ).setOnLongClickListener( longpress_rotate );

        /* For when long click motionevent is fixed
        // home button uses old generic click
        mView.findViewById( R.id.home ).setOnTouchListener( null );
        mView.findViewById( R.id.home ).setOnClickListener( c );
        mView.findViewById( R.id.home ).setOnLongClickListener( lc );
        */
        
        applyTransparency( mView, settings.getInt( "service_transparency", 0 ) );
        
        if( settings.getBoolean( "service_no_background", false ) ) {
            // make button container transparent
            recursivelyBlank( container );
        }
        
        // Put together the popper
        mBumpView = l.inflate( R.layout.service_popper, null );
        mBumpView.setOnTouchListener( touch );
        
        // insert the button
        Generator.createButtonContainer( this, 0, buttonMult, "service_popper",
                (ViewGroup)mBumpView.findViewById( R.id.main_view ),
                new int[] { R.id.popper } );

        ImageButton b = (ImageButton)mBumpView.findViewById( R.id.popper );
        b.setOnTouchListener( touch );

        // apply alpha
        applyTransparency( mBumpView, settings.getInt( "service_popper_transparency", 0 ) );
        
        b.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) {
                toggleBar();
            }
        } );

        b.setOnLongClickListener( new OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                mExtraEnabled = ! mExtraEnabled;
                matchExtraView();
                return true;
            }
        } );
        
        // extra view (dpad, customizable buttons)
        mExtraView = l.inflate( R.layout.service_extra, null );
        Generator.applyContainerExtras( mExtraView.findViewById( R.id.button_container ), "service_extra",
            Generator.currentTheme( this ),
            Generator.scaledIconSize( this, 0, buttonMult ) );

        mExtraView.setOnTouchListener( touch );
        
        OnLongClickListener configButtons = new OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                // run the button configure dialog, since we are a service and not an
                // application window context, we can not use alert dialogs or
                // spinners or anything like that which is kind of annoying
                toggleBar();
                Intent i = new Intent( v.getContext(), ConfigureExtra.class );
                i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                v.getContext().startActivity( i );
                return (true );
            }
        };

        for( int id : new int[] { R.id.extra_center, R.id.extra_up,R.id.extra_down,
                    R.id.extra_left, R.id.extra_right, R.id.extra_more, R.id.extra_less,
                    R.id.extra_custom1, R.id.extra_custom2,
                    R.id.extra_custom3, R.id.extra_custom4,
                    R.id.extra_custom5, R.id.extra_custom6                    
                } ) {
            View v = mExtraView.findViewById( id );
            v.setOnClickListener( c );
            //v.setOnTouchListener( click );
            
            String name = null;
            switch( id ) {
                case R.id.extra_center:
                    name = "dpad_center";
                    break;
                case R.id.extra_up:
                    name = "dpad_up";
                    break;
                case R.id.extra_down:
                    name = "dpad_down";
                    break;
                case R.id.extra_left:
                    name = "dpad_left";
                    break;
                case R.id.extra_right:
                    name = "dpad_right";
                    break;
                    
                case R.id.extra_more:
                    name = "extra_more";
                    break;
                    
                case R.id.extra_less:
                    name = "extra_less";
                    break;
            }
            
            if( name != null ) {
                Generator.applyButtonExtras( (ImageButton)v, "service_extra", name,
                    Generator.currentTheme( this ),
                    Generator.scaledIconSize( this, 0, buttonMult ) );
            }
            
            // long press on more/less to configure customs
            switch( id ) {
                case R.id.extra_more:
                case R.id.extra_less:
                    //v.setOnTouchListener( null );
                    //v.setOnClickListener( c );
                    v.setOnLongClickListener( configButtons );
                    break;
            }
        }

        // update the button configs, they are simply mapped by id in to a hashmap
        int i = 0;
        for( int id : new int[] { 
                    R.id.extra_custom1, R.id.extra_custom2,
                    R.id.extra_custom3, R.id.extra_custom4,
                    R.id.extra_custom5, R.id.extra_custom6                    
                } )  {
            i++;
            String pref_name = "service_extra_custom" + i + "_keyid";
            int keycode = settings.getInt( pref_name, 0 );
            String keyname = K.keyIdToName( keycode );
            if( keycode > 0 ) {
                if( keyname == null ) {
                    keyname = "NONE";
                }else{
                    keyname = ConfigureExtra.prettyPrint( keyname );
                }
            }else{
                if( keycode == 0 ) {
                    keyname = "NONE";
                }
                if( keycode == -1 ) {
                    keyname = "SLEEP";
                }
            }
            
            ((Button)mExtraView.findViewById( id )).setText( keyname );
            mCustomKeys.put( id, keycode );
        }

        applyTransparency( mExtraView, settings.getInt( "service_extra_transparency", 0 ) );

        mNumRows = settings.getInt( "service_extra_num_custom", 0 );
        mExtraEnabled = settings.getBoolean( "service_extra_enabled", false );
        updateExtraRows();
                
        // hide stuff
        toggleBar();
        
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView( mBumpView, makeOverlayParams() );
        wm.addView( mView, makeOverlayParams() );
        wm.addView( mExtraView, makeOverlayParams() );
        
        mOrientationListener = new OrientationEventListener( this, SensorManager.SENSOR_DELAY_NORMAL ) {
            @Override 
            public void onOrientationChanged( int orientation ) {
                initOrientation();
            }
        };
        mOrientationListener.enable();
        
        initOrientation();
    }

    private WindowManager.LayoutParams makeOverlayParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                // in adjustWindowParams system overlay windows are stripped of focus/touch events
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }
    
    public void initOrientation() {
        // init x/y of buttons and save screen width/heigth
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        
        // save screen width/height
        Display display = wm.getDefaultDisplay(); 
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();

        mOrientation = "portrait";
        if( mScreenWidth > mScreenHeight ) {
            mOrientation = "landscape";
        }
        
        // popup button
        WindowManager.LayoutParams params = (WindowManager.LayoutParams)mBumpView.getLayoutParams();
        // float right by default
        params.x = settings.getInt( "service_bump_last_x_" + mOrientation, mScreenWidth - mBumpView.getWidth() );
        params.y = settings.getInt( "service_bump_last_y_" + mOrientation, ( mScreenHeight / 2 ) - mBumpView.getHeight() );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.updateViewLayout(mBumpView, params);

        params = (WindowManager.LayoutParams)mView.getLayoutParams();
        // bottom center default
        params.x = settings.getInt( "service_last_x_" + mOrientation, ( mScreenWidth - mView.getWidth() ) / 2 );
        params.y = settings.getInt( "service_last_y_" + mOrientation, ( mScreenHeight - mView.getHeight() ) - 30 );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.updateViewLayout( mView, params );

        params = (WindowManager.LayoutParams)mExtraView.getLayoutParams();
        params.x = settings.getInt( "service_extra_last_x_" + mOrientation, 0 );
        params.y = settings.getInt( "service_extra_last_y_" + mOrientation, 0 );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.updateViewLayout( mExtraView, params );
    }
    
    private boolean genericClick( View v, boolean longClick ) {
        return genericClick( v, longClick, null );
    }
    
    // note: from testing keyup/keydown don't work the way I expect, we need
    // to also insert keyrepeat, so long-press volume, etc doesn't work the
    // way you'd think.  We should fix that someday, but for now you can just
    // mash the key a bunch of times
    private boolean genericClick( View v, boolean longClick, MotionEvent me ) {
        if( me != null ) {
            Toast.makeText( this, "Warning: MotionEvent is broken", Toast.LENGTH_LONG );
        }
        
        // send an intent to the main window
        int keyid = 0;
        Globals app = (Globals)getApplication();
        boolean hide = auto_hide;
        switch( v.getId() ) {
            case R.id.home:
                app.doHomeAction( longClick );
                break;
                
            case R.id.back:
                keyid = K.KEYID_BACK;
                if( hide ) {
                    hide = auto_hide_after_back;
                }
                break;
                
            case R.id.menu:
                keyid = K.KEYID_MENU;
                break;
                
            case R.id.search:
                if( longClick ) {
                    app.doLongSearchAction();
                }else{
                    keyid = K.KEYID_SEARCH;
                }
                break;
                
            case R.id.exit:
                hide = true;
                break;
            
            case R.id.extra_center:
                keyid = K.KEYID_DPAD_CENTER;
                hide = false;
                break;
                
            case R.id.extra_up:
                keyid = K.KEYID_DPAD_UP;
                hide = false;
                break;
                
            case R.id.extra_down:
                keyid = K.KEYID_DPAD_DOWN;
                hide = false;
                break;
                
            case R.id.extra_left:
                keyid = K.KEYID_DPAD_LEFT;
                hide = false;
                break;
                
            case R.id.extra_right:
                keyid = K.KEYID_DPAD_RIGHT;
                hide = false;
                break;

            case R.id.extra_more:
                mNumRows++;
                if( mNumRows > 6 ) {
                    mNumRows = 6;
                }
                updateExtraRows();
                hide = false;
                break;
                
            case R.id.extra_less:
                mNumRows--;
                if( mNumRows < 0 ) {
                    mNumRows = 0;
                }
                updateExtraRows();
                hide = false;
                break;
                
            case R.id.extra_custom1:
            case R.id.extra_custom2:
            case R.id.extra_custom3:
            case R.id.extra_custom4:
            case R.id.extra_custom5:
            case R.id.extra_custom6:
                keyid = mCustomKeys.get( v.getId() );
                hide = false;
                break;
                
            default:
                return true;
        }
        
        if( keyid != 0 ) {
            if( keyid == -1 ) {
                // sleep
                try {
                    Globals.RootContext cmd = ((Globals)getApplication()).getRootContext();
                    cmd.runCommand( "sleep" );
                }catch( Exception e ) {
                    // we don't really care if this fails, they should have gotten a shell
                    // error from the sendkeys
                }
            }else{
                if( me != null ) {
                    // do down/up
                    if( me.getAction() == KeyEvent.ACTION_DOWN ) {
                        app.sendKeyDown( keyid );
                    }else if( me.getAction() == KeyEvent.ACTION_UP ) {
                        app.sendKeyUp( keyid );
                    }
                }else{
                    app.sendKeys( new int[] { keyid } );
                }
            }
        }

        if( me != null ) {
            // hide always false unless keyup
            if( me.getAction() != KeyEvent.ACTION_UP ) {
                hide = false;
            }
        }
        
        if( hide ) {
            toggleBar();
        }
        
        return true;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove our views
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.removeView( mView );
        wm.removeView( mBumpView );
        wm.removeView( mExtraView );
        
        mView = null;
        mBumpView = null;
        mExtraView = null;
        
        mOrientationListener.disable();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void d( String msg ) {
        Log.d( "SoftKeysService", msg );
    }
    
    public void toggleBar() {
        if( mView.getVisibility() == View.INVISIBLE ) {
            mView.setVisibility( View.VISIBLE );
        }else{
            mView.setVisibility( View.INVISIBLE );            
        }
        matchExtraView();
    }
    
    public void matchExtraView() {
        if( mExtraEnabled ) {
            mExtraView.setVisibility( mView.getVisibility() );
        }else{
            mExtraView.setVisibility( View.INVISIBLE );
        }
    }
    
    private void updateExtraRows() {
        int i = 0;
        int[] ids = {
                R.id.extra_custom1,
                R.id.extra_custom2,
                R.id.extra_custom3,
                R.id.extra_custom4,
                R.id.extra_custom5,
                R.id.extra_custom6
            };
        
        for( int id : ids ) {
            i++;

            if( mNumRows < i ) {
                mExtraView.findViewById( id ).setVisibility( View.GONE );
            }else{
                mExtraView.findViewById( id ).setVisibility( View.VISIBLE );                
            }            
        }
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        SharedPreferences.Editor e = settings.edit();
        e.putInt( "service_extra_num_custom", mNumRows );
        e.commit();
    }
    
    private void savePosition() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        
        SharedPreferences.Editor e = settings.edit();
        WindowManager.LayoutParams l = (WindowManager.LayoutParams)mView.getLayoutParams();
        e.putInt( "service_last_x_" + mOrientation, l.x );
        e.putInt( "service_last_y_" + mOrientation, l.y );
        
        l = (WindowManager.LayoutParams)mBumpView.getLayoutParams();
        e.putInt( "service_bump_last_x_" + mOrientation, l.x );
        e.putInt( "service_bump_last_y_" + mOrientation, l.y );
        
        l = (WindowManager.LayoutParams)mExtraView.getLayoutParams();
        e.putInt( "service_extra_last_x_" + mOrientation, l.x );
        e.putInt( "service_extra_last_y_" + mOrientation, l.y );
        
        e.putBoolean( "service_extra_enabled", mExtraEnabled );
        
        e.commit();
    }
    
    private void applyTransparency( View v, int amount ) {
        // apply transparency, is there a better way?
        float transparency = (float)amount;
        float finalAlpha = ( 100f - transparency ) / 100f;
        
        Animation alpha = new AlphaAnimation( finalAlpha, finalAlpha );
        alpha.setDuration( 0 );
        alpha.setFillAfter( true );
        
        // need to create an animation controller since its empty by default and the animation doesn't work
        ((ViewGroup)v).setLayoutAnimation( new LayoutAnimationController( alpha, 0 ) );
    }
    
    private void recursivelyBlank( View v ) {
        // we walk this view and children and keep removing backgrounds and padding until we hit
        if( v instanceof ImageButton ) {
            return;
        }
        
        v.setBackgroundColor( 0 );
        v.setPadding( 0, 0, 0, 0 );
        
        if( v instanceof ViewGroup ) {
            ViewGroup g = (ViewGroup)v;
            for( int i = 0; i < g.getChildCount(); i++ ) {
                recursivelyBlank( g.getChildAt( i ) );
            }
        }
    }
}
