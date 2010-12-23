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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SoftKeysService extends Service {
    private View mView;
    private View mBumpView;
    private boolean auto_hide;
    private boolean auto_hide_after_back;
    private boolean mDraggingView;
    private int mDraggingOrigX, mDraggingOrigY;
    private int mDraggingViewX, mDraggingViewY;
    private boolean mDidDrag;
    private int mNumDrags;
    private OrientationEventListener mOrientation;
    
    private final int mOffScreenMax = 20;
    
    private int mScreenWidth;
    private int mScreenHeight;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        OnClickListener c = new OnClickListener() {
            @Override
            public void onClick( View v ) {
                // send an intent to the main window
                Intent i = null;
                boolean hide = auto_hide;
                switch( v.getId() ) {
                    case R.id.home:
                        i = new Intent( Keys.ACTION_HOME );
                        break;
                        
                    case R.id.back:
                        i = new Intent( Keys.ACTION_BACK );
                        if( hide ) {
                            hide = auto_hide_after_back;
                        }
                        break;
                        
                    case R.id.menu:
                        i = new Intent( Keys.ACTION_MENU );
                        break;
                        
                    case R.id.search:
                        i = new Intent( Keys.ACTION_SEARCH );
                        break;
                        
                    case R.id.exit:
                        hide = true;
                        break;
                }
                
                if( i != null ) {
                    i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                    i.setClass( v.getContext(), Keys.class );
                    v.getContext().startActivity( i );
                }
                
                if( hide ) {
                    toggle_bar();
                }
            }
        };
        
        OnLongClickListener longpress = new OnLongClickListener() {
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
                        // save x/y
                        savePosition();

                        // do not click
                        return( true );
                    }
                } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                    mNumDrags++; // only really start dragging after a few drag events, so when
                                 // you're just tapping buttons it doesn't drag too by accident
                    
                    if( mNumDrags > 2 ) {
                        mDraggingView = true;
                        mDidDrag = true;

                        int currX = (int)me.getRawX();
                        int currY = (int)me.getRawY(); 
                        
                        // make our deltas work relative to movement, y
                        int dx = currX - mDraggingOrigX;
                        int dy = currY - mDraggingOrigY;
                        
                        //d( "dx: " + dx );
                        //d( "dy: " + dy );
                        

                        View root = view.getRootView();                        
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
                        
                        l.x = mDraggingViewX + dx;
                        l.y = mDraggingViewY + dy;
                        
                        // contraints
                        if( l.x < ( mOffScreenMax * -1 ) ) {
                            l.x = mOffScreenMax * -1;
                        }
                        
                        if( l.x + width > mScreenWidth + mOffScreenMax ) {
                            l.x = mScreenWidth + mOffScreenMax - width;
                        }
                        
                        if( l.y < ( mOffScreenMax * -1 ) ) {
                            l.y = mOffScreenMax * -1;
                        }
                        
                        if( l.y + height > mScreenHeight + mOffScreenMax ) {
                            l.y = mScreenHeight + mOffScreenMax - height;
                        }
                        
                        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
                        wm.updateViewLayout( root, l );
                        return( true );
                    }
                }
                return false;
            }
        };

        // get our root (don't go through theme handler, this comes from the main app always)
        LayoutInflater l = LayoutInflater.from( this );
        mView = l.inflate( R.layout.service, null );

        mOrientation = new OrientationEventListener( this, SensorManager.SENSOR_DELAY_NORMAL ) {
            @Override 
            public void onOrientationChanged( int orientation ) {
                initOrientation();
            }
        };
        mOrientation.enable();
        
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
        container.removeView( container.findViewById( R.id.settings ) ); // no settings in service
        
        // arrange buttons
        Keys.applyButtons( settings, mView, c, longpress, touch, true );
        mView.setVisibility( View.INVISIBLE );
        mView.setOnTouchListener( touch );
        mView.setOnLongClickListener( longpress );
        
        applyTransparency( mView, settings.getInt( "service_transparency", 0 ) );
        
        if( settings.getBoolean( "service_no_background", false ) ) {
            // make button container transparent
            ((LinearLayout)mView.findViewById( R.id.button_container )).setBackgroundResource( 0 );
            ((LinearLayout)mView.findViewById( R.id.button_container )).setPadding( 0, 0, 0, 0 );
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
                toggle_bar();
            }
        } );
        
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView( mBumpView, makeOverlayParams() );
        wm.addView( mView, makeOverlayParams() );

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

        /*
        // popup button
        //params.gravity = Gravity.RIGHT;
        WindowManager.LayoutParams params = (WindowManager.LayoutParams)mBumpView.getLayoutParams();
        params.x = settings.getInt( "service_bump_last_x", 0 );
        params.y = settings.getInt( "service_bump_last_y", 0 );
        if( params.x == 0 && params.y == 0 ) {
            params.gravity = Gravity.RIGHT;
        }else{
            params.gravity = Gravity.TOP | Gravity.LEFT;
        }
        wm.updateViewLayout(mBumpView, params);

        params = (WindowManager.LayoutParams)mView.getLayoutParams();
        params.x = settings.getInt( "service_last_x", 0 );
        params.y = settings.getInt( "service_last_y", 0 );
        if( params.x == 0 && params.y == 0 ) {
            params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        }else{
            params.gravity = Gravity.TOP | Gravity.LEFT;
        }
        
        wm.updateViewLayout(mView, params);
        */

        // popup button
        //params.gravity = Gravity.RIGHT;
        WindowManager.LayoutParams params = (WindowManager.LayoutParams)mBumpView.getLayoutParams();
        params.x = settings.getInt( "service_bump_last_x", 0 );
        params.y = settings.getInt( "service_bump_last_y", 0 );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        if( params.x == 0 && params.y == 0 ) {
            // float right by default
            params.x = mScreenWidth - mBumpView.getWidth();
            params.y = ( mScreenHeight / 2 ) - mBumpView.getHeight();
        }
        wm.updateViewLayout(mBumpView, params);

        params = (WindowManager.LayoutParams)mView.getLayoutParams();
        params.x = settings.getInt( "service_last_x", 0 );
        params.y = settings.getInt( "service_last_y", 0 );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        if( params.x == 0 && params.y == 0 ) {
            // bottom center
            params.x = ( mScreenWidth - mView.getWidth() ) / 2;
            params.y = ( mScreenHeight - mView.getHeight() ) - 30;
        }
        
        wm.updateViewLayout(mView, params);
        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove our views
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.removeView( mView );
        wm.removeView( mBumpView );
        
        mView = null;
        mBumpView = null;
        
        mOrientation.disable();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void d( String msg ) {
        Log.d( "SoftKeysService", msg );
    }
    
    public void toggle_bar() {
        if( mView.getVisibility() == View.INVISIBLE ) {
            mView.setVisibility( View.VISIBLE );
        }else{
            mView.setVisibility( View.INVISIBLE );            
        }
    }
    
    private void savePosition() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        
        SharedPreferences.Editor e = settings.edit();
        WindowManager.LayoutParams l = (WindowManager.LayoutParams)mView.getLayoutParams();
        e.putInt( "service_last_x", l.x );
        e.putInt( "service_last_y", l.y );
        
        l = (WindowManager.LayoutParams)mBumpView.getLayoutParams();
        e.putInt( "service_bump_last_x", l.x );
        e.putInt( "service_bump_last_y", l.y );
        
        e.putInt( "service_last_orientation",
                ((LinearLayout)mView.findViewById( R.id.button_container )).getOrientation() );
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
}
