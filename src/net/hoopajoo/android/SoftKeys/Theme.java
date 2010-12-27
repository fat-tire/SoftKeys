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
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

public class Theme {
    // the stack of resources we look through for stuff, first match
    // is the returned item (typically will be theme, app)
    List<IdPack> mResources = new ArrayList<IdPack>();
    
    Theme( Context c, String name ) {
        PackageManager pm = c.getPackageManager();
        try {
            IdPack i = new IdPack();
            i.name = name;
            i.R = pm.getResourcesForApplication( name );
            mResources.add( i );
        }catch( Exception e ) {
            // bad theme name
        }
        
        // add app as last resort
        IdPack i = new IdPack();
        i.name = c.getPackageName();
        i.R = c.getResources();
        mResources.add( i ); 
    }

    // Semi-stacked actions, look in theme, if not found, look in app
    // this way you can have a generic "button" but also more specific buttons,
    // like "service_back_button" if they need something more flashy allowing each
    // individual button to be different, or just define the base "button" and have
    // different icons
    public Drawable getDrawable( String[] name ) {
        IdPack i = getId( name, "drawable" );
        if( i != null ) {
            return i.R.getDrawable( i.id );
        }
        //Log.e( "SoftKeysTheme", "Unable to find drawable resource: " + name );
        return( null );
    }
    
    // these check theme config flags
    public boolean getBoolean( String[] name ) {
        return getBoolean( name, false );
    }
    
    public boolean getBoolean( String[] name, boolean def ) {
        String flag = getString( name );
        if( flag != null ) {
            // t/true is true, everything else is false
            if( flag.startsWith( "t" ) ) {
                return( true );
            }
            return( false );
        }
        
        return( def );
    }
    
    public String getString( String[] name ) {
        IdPack i = getId( name, "string" );
        if( i != null ) {
            return i.R.getString( i.id );
        }
        return( null );        
    }
    
    // For use mostly with the notification bar, allowing custom themes to include
    // new icons primarily, but since it's a layout they can do more than that
    public RemoteViews getRemoteViews( String[] name ) {
        IdPack i = getId( name, "layout" );
        if( i != null ) {
            Log.e( "SoftKeysTheme", "Found remoteview" );
            return new RemoteViews( i.name, i.id );
        }
        
        return null;
    }
    
    public View inflateLayout( Context c, String[] name, ViewGroup root, boolean add ) {
        // this makes a phony context to fool the layout inflater to use alternate resources
        // for resolution, e.g. android:background="@drawable/background.png"
        // without the phony context, we would instead end up using resources from the main
        // app instead of the resources referenced in the theme
        //
        // this is not documented but I can't find an officially supported way to inflate views
        // containing references from other packages
        IdPack i = getId( name, "layout" );
        if( i != null ) {
            FakeContext fake = new FakeContext( c, i.R );
            // you can't create this from the fake context, it still pulls a system service
            LayoutInflater inflater = LayoutInflater.from( c ).cloneInContext( fake );
            return inflater.inflate( i.R.getXml(  i.id ), root, add );
        }
        
        return( null );
    }
    
    /*
    public XmlResourceParser getLayout( String[] name ) {
        IdPack i = getId( name, "layout" );
        if( i != null ) {
            return i.R.getXml( i.id );
        }
        Log.e( "SoftKeysTheme", "Unable to find layout resource: " + name );
        return null;
    }
    */
    
    // this returns the first ID matched, for use by the generator to find the
    // button container view by id
    public int getId( String[] name ) {
        IdPack i = getId( name, "id" );
        if( i != null ) {
            return i.id;
        }
        return( 0 );
    }
    
    private IdPack getId( String[] name, String type ) {
        // return the most specific match, from theme first then from app
        for( IdPack check : mResources ) {
            for( String n : name ) {
                int id = check.R.getIdentifier( n, type, check.name );
                if( id != 0 ) {
                    IdPack i = new IdPack(); // return copy in case they need more than 1 id going (we don't right now)
                    i.id = id;
                    i.R = check.R;
                    i.name = check.name;
                    return( i );
                }
            }
        }
        
        return null;
    }
    
    private class IdPack {
        int id;
        Resources R;
        String name;
        
        IdPack() {
            
        }
    }
    
    private class FakeContext extends ContextWrapper {
        // from perusing layoutinflater.java it basically uses the context
        // for getResources() so we use this to fake it out
        private Resources mResources = null;
        private Resources.Theme mTheme = null;
        private int mThemeResource = 0;
        
        FakeContext( Context c, Resources r ) {
            super( c );
            mResources = r;
        }
        
        // this is based on ContextImpl
        // also override gettheme since it caches the old context resource
        @Override
        public void setTheme(int resid) {
            mThemeResource = resid;
        }

        @Override
        public Resources.Theme getTheme() {
            if (mTheme == null) {
                if( mThemeResource == 0 ) {
//                    mThemeResource = com.android.internal.R.style.Theme;
                    try {
                        mThemeResource = (Integer) Class.forName(
                            "com.android.internal.R$style").getField("Theme").get(null);
                    }catch( Exception e ) {
                        
                    }
                }
                
                mTheme = mResources.newTheme();
                if( mThemeResource != 0 ) {
                    mTheme.applyStyle(mThemeResource, true);
                }
            }
            return mTheme;
        }

        @Override
        public Resources getResources() {
            //Log.d( "fake", "returning fake resources" );
            return mResources;
        }
    }
}
    