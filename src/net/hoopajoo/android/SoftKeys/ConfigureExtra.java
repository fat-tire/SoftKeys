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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class ConfigureExtra extends Activity implements OnClickListener {    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_extra);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );

        List<CustomKey> items = new ArrayList<CustomKey>();
        
        items.add( new CustomKey( 0, "NONE" ) );
        items.add( new CustomKey( -1, "SLEEP" ) );
        
        for( int cnt = 1; cnt < 100; cnt++ ) {
            String name = K.keyIdToName( cnt );
            if( name != null ) {
                items.add( new CustomKey( cnt, name ) );
            }
        }

        ArrayAdapter<CustomKey> adapter = new ArrayAdapter<CustomKey>( this, android.R.layout.simple_spinner_item, items );

        int i = 0;
        for( int id : new int[] { 
                    R.id.extra_custom1, R.id.extra_custom2,
                    R.id.extra_custom3, R.id.extra_custom4,
                    R.id.extra_custom5, R.id.extra_custom6                    
                } )  {
            i++;
            String pref_name = "service_extra_custom" + i + "_keyid";
            int keycode = settings.getInt( pref_name, 0 );

            Spinner s = (Spinner)findViewById( id );
            s.setAdapter( adapter );
            
            int idx = 0;
            for( CustomKey item : items ) {
                if( item.mId == keycode ) {
                    s.setSelection( idx );
                }
                idx++;
            }
        }
        
        findViewById( R.id.extra_modify_cancel ).setOnClickListener( this );
        findViewById( R.id.extra_modify_save ).setOnClickListener( this );
    }

    public void onClick( View v ) {
        if( v.getId() == R.id.extra_modify_save ) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
            SharedPreferences.Editor e = settings.edit();
            int i = 0;
            for( int id : new int[] { 
                        R.id.extra_custom1, R.id.extra_custom2,
                        R.id.extra_custom3, R.id.extra_custom4,
                        R.id.extra_custom5, R.id.extra_custom6                    
                    } )  {
                i++;
                String pref_name = "service_extra_custom" + i + "_keyid";
                Spinner s = (Spinner)findViewById( id );
                CustomKey k = (CustomKey)s.getSelectedItem();
                e.putInt( pref_name, k.mId );
            }
            e.commit();
            ((Globals)getApplication()).restartService();
        }
        this.finish();
    }
    
    private class CustomKey {
        public String mName;
        public int mId;
        
        CustomKey( int id, String name ) {
            mName = prettyPrint( name );
            mId = id;
        }
        
        public String toString() {
            return mName;
        }
    }
    
    public static String prettyPrint( String n ) {
        // replace _ with space, initcap
        String s = n.replace( "_", " " );

        boolean bound = true;
        StringBuilder r = new StringBuilder();
        for( int i = 0; i < s.length(); i++ ) {
            if( bound ) {
                // cap
                r.append( Character.toUpperCase( s.charAt( i ) ) );
                bound = false;
            }else{
                r.append( Character.toLowerCase( s.charAt( i ) ) );
            }

            // check for boundry
            if( Character.isSpace( s.charAt( i ) ) ) {
                bound = true;
            }
        }

        return r.toString();
    }    
}
