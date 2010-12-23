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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Prefs extends PreferenceActivity {    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        
        // setup launchers list
        Intent i = new Intent( Intent.ACTION_MAIN );
        i.addCategory( Intent.CATEGORY_HOME );

        fillListFromIntent( (ListPreference)findPreference( "launcher" ), i, null, null );
        fillListFromIntent( (ListPreference)findPreference( "launcher2" ), i, null, null );
        
        i = new Intent( "net.hoopajoo.android.SoftKeys.THEMES" );
        i.addCategory( Intent.CATEGORY_DEFAULT );
        fillListFromIntent( (ListPreference)findPreference( "theme" ), i, "Default", "" );
        
        String ver = "unknown";      
        try {
            PackageInfo info = getPackageManager().getPackageInfo( "net.hoopajoo.android.SoftKeys", PackageManager.GET_META_DATA );
            ver = info.versionName;
        }catch( Exception e ) {
        }

        Preference version = (Preference)findPreference( "pref_version" );
        version.setSummary( getString( R.string.pref_version_summary, ver ) );
    }
    
    private void fillListFromIntent( ListPreference l, Intent i, String firstItem, String firstValue ) {
        PackageManager p = getPackageManager();
        List<ResolveInfo> packages = p.queryIntentActivities( i, 0 );
        ArrayList<String> display = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        
        if( firstItem != null ) {
            display.add( firstItem );
            values.add( firstValue );
        }
        
        for( Iterator<ResolveInfo> it = packages.iterator(); it.hasNext(); ) {
            ResolveInfo info = it.next();
            values.add(  info.activityInfo.applicationInfo.packageName );
            display.add( info.activityInfo.loadLabel( p ).toString() );
        }
        
        l.setEntryValues( values.toArray( new CharSequence[ values.size() ] ) );
        l.setEntries( display.toArray( new CharSequence[ values.size() ] ) );
    }
}
