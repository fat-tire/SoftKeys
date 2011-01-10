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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class Globals extends Application {
    private RootContext mRootContext = null;
    private String android_id = null;
    private String LOG = "SoftKeys.Global";
    
    public boolean restartKeys = false;
    public int homeCounter = 0;
    public boolean didInitNotifications = false;
    public boolean firstRun = true;
    
    // interface to root context class
    public RootContext getRootContext() throws Exception {
        if( mRootContext == null ) {
            // set up env and run the context
            String wd = getFilesDir().getAbsolutePath();
            File jar = new File( wd + "/RemoteContext.jar" );
            if( ! jar.exists() ) {
                AssetManager m = getResources().getAssets();
                InputStream in = m.open( "input/RemoteContext.jar" );
                FileOutputStream out = new FileOutputStream( jar );
                int read;
                byte[] b = new byte[ 4 * 1024 ];
                while( ( read = in.read( b ) ) != -1 ) {
                    out.write( b, 0, read );
                }
                out.close();                
                in.close();
            }

            if( android_id == null ) {
                // to run in the emulator
                // adb shell
                // # mkdir /data/tmp
                // # cat /system/bin/sh > /data/tmp/su
                // # chmod 6755 /data/tmp/su
                // # mount -oremount,suid /dev/block/mtdblock1 /data
                Log.d( LOG, "Detected emulator" );
                mRootContext = new RootContext( "/data/tmp/su", wd );
            }else{
                mRootContext = new RootContext( "su", wd );
            }
            
        }

        return( mRootContext );
    }
    
    // this is a string of keydown/keyup events by key id
    public int sendKeys( List<Integer> a ) {
        return sendKeys( listToInt( a ) );
    }
    
    public int sendKeys( int[] keyids ) {
        try {
            Globals.RootContext cmd = getRootContext();
            for( int id : keyids ) {
                cmd.runCommand( "keycode " + id );
            }
        }catch( Exception e ) {
            Log.e( LOG, "Error: " + e.getMessage() );
            Toast.makeText( this, "Unable to execute as root", Toast.LENGTH_LONG ).show();
            return 1;
        }
        
        return 0;
    }
    
    public int sendKeyDown( int keyid ) {
        try {
            Globals.RootContext cmd = getRootContext();
            cmd.runCommand( "keycodedown " + keyid );
        }catch( Exception e ) {
            Log.e( LOG, "Error: " + e.getMessage() );
            Toast.makeText( this, "Unable to execute as root", Toast.LENGTH_LONG ).show();
            return 1;
        }
        
        return 0;
    }
    
    public int sendKeyUp( int keyid ) {
        try {
            Globals.RootContext cmd = getRootContext();
            cmd.runCommand( "keycodeup " + keyid );
        }catch( Exception e ) {
            Log.e( LOG, "Error: " + e.getMessage() );
            Toast.makeText( this, "Unable to execute as root", Toast.LENGTH_LONG ).show();
            return 1;
        }
        
        return 0;
    }
    
    public class RootContext {
        Process p;
        OutputStream o;
        
        RootContext( String shell, String workingDir ) throws Exception {
            //Log.d( "SoftKeys.RootContext", "Starting shell: '" + shell + "'" );
            p = Runtime.getRuntime().exec( shell );
            o = p.getOutputStream();
            
            // spawn our context
            system( "export CLASSPATH=" + workingDir + "/RemoteContext.jar" );
            system( "exec app_process " + workingDir + " net.hoopajoo.android.RemoteContext" );
            runCommand( "" );
        }
        
        private void system( String cmd ) throws Exception {
            //Log.d( "SoftKeys.RootContext", "Running command: '" + cmd + "'" );
            o.write(  (cmd + "\n" ).getBytes( "ASCII" ) );
        }
        
        // slightly renamed since we're not running system("cmd") anymore but
        // RootContext commands
        public void runCommand( String cmd ) throws Exception {
            system( cmd );
        }
        
        public void close() throws Exception {
            //Log.d( "SoftKeys.RootContext", "Destroying shell" );
            o.flush();
            o.close();
            p.destroy();
        }
    }
    
    @Override
    public void onCreate() {
        // warn if we don't notice some binaries we need
        for( String name : new String[] { "/system/bin/su" } ) {
            File check = new File( name );
            try {
                if( ! check.exists() ) {
                    Toast.makeText( this, "Failed to find file: " + name + ", SoftKeys may not function", Toast.LENGTH_LONG ).show();                    
                }
            }catch( Exception e ) {
                Toast.makeText( this, "Unable to check for file: " + name, Toast.LENGTH_LONG ).show();
            }
                
        }

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        restartService();
    }
    
    public void restartService() {
        // start the service
        this.stopService( new Intent( this, SoftKeysService.class ) );
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        if( settings.getBoolean(  "service", true ) ) {
            this.startService( new Intent( this, SoftKeysService.class ) );
        }
    }
    
    private int[] listToInt( List<Integer> a ) {
        int[] ret = new int[ a.size() ];
        for( int i = 0; i < a.size(); i++ ) {
            ret[ i ] = a.get( i ).intValue();
        }
        return( ret );
    }
    
    public void doHomeAction( boolean longClick ) {
        // special case
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        
        Intent ni = new Intent( Intent.ACTION_MAIN );
        String launcher = settings.getString( longClick ? "launcher2" : "launcher" , null );
        if( launcher == null ) {
            if( longClick ) {
                // default longpress home is softkeys
                launcher = "net.hoopajoo.android.SoftKeys";
            }else{
                launcher = getDefaultLauncher();
            }
        }
        ni.setPackage( launcher ); 
        ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( ni );
    }
    
    private String getDefaultLauncher() {
        // Set default launcher to the first launcher we find so we don't freak out if it's not
        // set and there is no com.android.launcher
        Intent i = new Intent( Intent.ACTION_MAIN );
        i.addCategory( Intent.CATEGORY_HOME );
        PackageManager p = getPackageManager();
        List<ResolveInfo> packages = p.queryIntentActivities( i, 0 );
        
        String defaultLauncher = null;
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
        return( defaultLauncher );
    }
}
