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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class QuickDoc extends Activity {    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quickdoc);
        
        String url = "file:///android_asset/";
        
        Intent i = getIntent();
        if( i != null ) {
            Bundle ex = i.getExtras();
            String type = ex.getString( "type" );
            if( type.equals( "whats_new" ) ) {
                url += "whats_new.html";
            }else if( type.equals( "help" ) ) {
                url += "help.html"; 
            }else{
                url += "404.html";
            }
        }
        
        WebView mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled( false );
        mWebView.loadUrl( url  );

        /*
        String ver = "unknown";      
        try {
            PackageInfo info = getPackageManager().getPackageInfo( "net.hoopajoo.android.SoftKeys", PackageManager.GET_META_DATA );
            ver = info.versionName;
        }catch( Exception e ) {
        }

        Preference version = (Preference)findPreference( "pref_version" );
        version.setSummary( getString( R.string.pref_version_summary, ver ) );
        */
    }
 
    public void closeHelp( View v ) {
        this.finish();
    }
}
