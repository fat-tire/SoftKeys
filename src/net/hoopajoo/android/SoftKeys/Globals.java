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

import java.io.OutputStream;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class Globals extends Application {
    private CommandShell cmd = null;
    private String android_id = null;
    
    public boolean restartKeys = false;
    public int homeCounter = 0;
    public boolean didInitNotifications = false;
    
    public CommandShell getCommandShell() throws Exception {
        if( cmd == null ) {
            if( android_id == null ) {
                // to run in the emulator
                // adb shell
                // # mkdir /data/tmp
                // # cat /system/bin/sh > /data/tmp/su
                // # chmod 6755 /data/tmp/su
                // # mount -oremount,suid /dev/block/mtdblock1 /data
                Log.d( "softkeys", "Detected emulator" );
                cmd = new CommandShell( "/data/tmp/su" );
            }else{
                cmd = new CommandShell( "su" );
            }
        }

        return( cmd );
    }

    public class CommandShell {
        Process p;
        OutputStream o;
        
        CommandShell( String shell ) throws Exception {
            Log.d( "softkeys.cmdshell", "Starting shell: '" + shell + "'" );
            p = Runtime.getRuntime().exec( shell );
            o = p.getOutputStream();
        }
        
        public void system( String cmd ) throws Exception {
            Log.d( "softkeys.cmdshell", "Running command: '" + cmd + "'" );
            o.write(  (cmd + "\n" ).getBytes( "ASCII" ) );
        }
        
        public void close() throws Exception {
            Log.d( "softkeys.cmdshell", "Destroying shell" );
            o.flush();
            o.close();
            p.destroy();
        }
    }
    
    @Override
    public void onCreate() {
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
}
