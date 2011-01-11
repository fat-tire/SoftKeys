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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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

// this is just a stub to handle intent calls
public class SendInput extends Activity {
    public static String ACTION_CODE = "net.hoopajoo.android.SoftKeys.KEY_CODE";
    public static String ACTION_HOME = "net.hoopajoo.android.SoftKeys.KEY_HOME";
    public static String ACTION_MENU = "net.hoopajoo.android.SoftKeys.KEY_MENU";
    public static String ACTION_SEARCH = "net.hoopajoo.android.SoftKeys.KEY_SEARCH";
    public static String ACTION_BACK = "net.hoopajoo.android.SoftKeys.KEY_BACK";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent( getIntent() );
    }
    
    @Override
    public void onNewIntent( Intent i ) {
        Globals app = (Globals)getApplication();
        
        String action = i.getAction();
        int keyid = 0;
        if( action.equals(  ACTION_CODE ) ) {
            // by key name?
            Bundle e = i.getExtras();
            boolean longClick = e.getBoolean( "longclick", false );
            if( e.getString( "keyname" ) != null ) {
                String key = e.getString( "keyname" );
                //Log.d( "SoftKeys.SendInput", "resolving: " + key );
                if( key.equals( "home" ) ) {
                    ((Globals)getApplication()).doHomeAction( longClick );
                    this.finish();
                    return;
                }else{
                    // run through resolver
                    keyid = K.keyNameToId( key );
                }
            }else if( e.getInt( "keyid", 0 ) != 0 ) {
                keyid = e.getInt( "keyid", 0 );
            }
        }

        if( action.equals( ACTION_HOME ) ) {
            ((Globals)getApplication()).doHomeAction( false );
            this.finish();
            return;            
        }
        
        if( action.equals( ACTION_BACK ) ) {
            keyid = K.KEYID_BACK;
        }
        
        if( action.equals( ACTION_MENU ) ) {
            keyid = K.KEYID_MENU;
        }
        
        if( action.equals( ACTION_SEARCH ) ) {
            keyid = K.KEYID_SEARCH;
        }
                
        if( keyid != 0 ) {
            //Log.d( "SoftKeys.SendInput", "resolved key: " + keyid );
            ((Globals)getApplication()).sendKeys( new int[] { keyid } );
        }
        
        // todo: make me a broadcast receiver.. or maybe this should be removed all together?
        // any good reasons to allow other apps to call hw keys?
        this.finish();       

    }
}
