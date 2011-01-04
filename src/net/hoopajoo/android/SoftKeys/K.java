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
import java.util.ArrayList;
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

// Key constants and resolver maps
public class K {
    public static String KEY_HOME = "home";
    public static String KEY_BACK = "back";
    public static String KEY_MENU = "menu";
    public static String KEY_SEARCH = "search";
    
    public static int KEYID_HOME = 0;
    public static int KEYID_BACK = 4;
    public static int KEYID_MENU = 82;
    public static int KEYID_SEARCH = 84;
    
    private static final Map<String,Integer> mKeymap;
    static {
        Map<String,Integer> t = new HashMap<String,Integer>();
        t.put( KEY_BACK, KEYID_BACK );
        t.put( KEY_MENU, KEYID_MENU );
        t.put( KEY_SEARCH, KEYID_SEARCH );
        mKeymap = Collections.unmodifiableMap( t );
    }
    
    public static int keyNameToId( String n ) {
        if( ! mKeymap.containsKey( n ) ) {
            return 0;
        }
        return mKeymap.get( n );
    }
}
