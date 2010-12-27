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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class Generator {
    public static int defaultIconSize( Context c ) {
        final Resources resources = c.getResources();
        return( (int) resources.getDimension( android.R.dimen.app_icon_size ) );
    }

    public static View createButtonContainer( Context c, int iconSize, float iconScale, String prefix, ViewGroup root ) {
        return createButtonContainer( c, iconSize, iconScale, prefix, root, null );
    }
    
    // this assembles a generic button_container that can be inserted into whatever layout
    public static View createButtonContainer( Context c, int iconSize, float iconScale, String prefix, ViewGroup root, int[] buttons ) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( c );
        Theme theme = new Theme( c, settings.getString( "theme", null ) );
        if( iconSize == 0 ) {
            // default icon size
            iconSize = defaultIconSize( c );
        }
        
        iconSize = (int)(iconSize * iconScale);
        
        // we start with some kind of viewgroup (linearlayout,etc)
        ViewGroup container = (ViewGroup)theme.inflateLayout( c,
                new String[] { prefix + "_button_container", "button_container" }
            , root, false );
        container.setId( R.id.button_container );
        Drawable d = theme.getDrawable( 
                new String[] { prefix + "_button_container_background", 
                        "button_container_background" }
            );
        
        if( d != null ) {
            // resize?
            if( theme.getBoolean( new String[] { prefix + "_resize_background", "resize_background" } ) ) {
                d = resizeImage( d, iconSize, iconSize );
            }
            
            applyTiling( d, theme.getString( new String[] {
                    prefix + "_tile_background", 
                    "tile_background" 
                } ) );

            container.setBackgroundDrawable( d );
        }
        
        // If they have specified a view id to use for insertion then switch to that now
        ViewGroup button_container = container;
        String button_container_name = theme.getString( new String[] {
                prefix + "_button_container_id",
                "button_container_id"
        } );
        if( button_container_name != null ) {
            button_container = (ViewGroup)container.findViewById( theme.getId(
                    new String[] { button_container_name } ) );
            
            // if it's null just go back to main container
            if( button_container == null ) {
                button_container = container;
            }
        }
            

        // now we add the buttons
        if( buttons == null ) {
            buttons = new int[] { R.id.menu, R.id.home, R.id.back,
                R.id.search, R.id.settings, R.id.exit };
        }

        for( int i : buttons ) {
            String name = "unknown";
            switch( i ) {
                case R.id.menu:
                    name = "menu";
                    break;
                    
                case R.id.home:
                    name = "home";
                    break;
                    
                case R.id.back:
                    name = "back";
                    break;
                    
                case R.id.search:
                    name = "search";
                    break;
                    
                case R.id.settings:
                    name = "settings";
                    break;
                    
                case R.id.exit:
                    name = "exit";
                    break;
                    
                case R.id.popper:
                    name = "popper";
                    break;
            }
            
            ImageButton b = (ImageButton)theme.inflateLayout( c,
                    new String[] { prefix + "_button_" + name, 
                            prefix + "_button", "button_" + name, "button" }
                , button_container, false );
            b.setId( i );
            
            // Add our images at the size we want
            d = b.getDrawable(); 
            
            if( d == null ) {
                d = theme.getDrawable( 
                    new String[] { prefix + "_button_" + name, 
                            prefix + "_button", "button_" + name, "button" }
                );
            }
            b.setImageDrawable( resizeImage( d, iconSize, iconSize ) );
            
            // add bg if not set and one is specified in the theme
            d = theme.getDrawable( 
                    new String[] { prefix + "_button_background_" + name, 
                            prefix + "_button_background", "button_background_" + name,
                            "button_background" }
                );
            
            if( d != null ) {
                if( theme.getBoolean( new String[] { 
                        prefix + "_resize_button_background_" + name, 
                        prefix + "_resize_button_background", 
                        "resize_button_background_" + name,
                        "resize_button_background" } ) ) {
                    d = resizeImage( d, iconSize, iconSize );
                }

                applyTiling( d, theme.getString( new String[] {
                    prefix + "_tile_button_background_" + name, 
                    prefix + "_tile_button_background", 
                    "tile_button_background_" + name,
                    "tile_button_background" 
                } ) );

                b.setBackgroundDrawable( d );
            }
            
            button_container.addView( b );
        }
        
        // add to root
        if( root != null ) {
            root.addView( container );
        }
        
        return( container );
    }
    
    // this will return a new drawable scaled to the new size, so you don't have to mutable the source
    public static Drawable resizeImage( Drawable d, int w, int h) {
        Bitmap b;
        if( d instanceof BitmapDrawable ) {
            // I found that the resources are already bitmapdrawables so we can do this,
            // I assume it it's not created from a bitmap like it's a shape or something
            // then this won't work?
            b = ((BitmapDrawable)d).getBitmap();
        }else{
            // this was the way more people said to do it, just render the drawable to a canvas
            // backed by your dest bitmap.  I assume if you're using a bitmapdrawable
            // then this is slower than just pulling in the drawable backed bitmap
            d.mutate();  // we change the setbounds() so lets not mess with the original
            b = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
            Canvas c = new Canvas( b );
            d.setBounds( 0, 0, w, h );
            d.draw( c );
        }
        
        int width = b.getWidth();
        int height = b.getHeight();

        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        Matrix matrix = new Matrix();
        matrix.postScale( scaleWidth, scaleHeight);

        BitmapDrawable ret = new BitmapDrawable( Bitmap.createBitmap(b, 0, 0, width, height, matrix, true) );
        // copy tile mode
        if( d instanceof BitmapDrawable ) {
            ret.setTileModeXY( ( (BitmapDrawable)d ).getTileModeX(), ( (BitmapDrawable)d ).getTileModeY() );
        }
        return ret;
    }
    
    
    /// NOTE: using any tile mode CLAMP seems to crash the emulator
    //        looking around online some people are saying it crashes their stuff
    //        too, don't know who's to blame but it seems like avoiding
    //        clamp is a good idea if you want compatibility
    // tiling is specified as mode,mode for x/y tiling, or just mode to
    // specify both x and y tiling
    private static void applyTiling( Drawable d, String tilemode ) {
        // check if they specify tiling, this will override tiling in an xml bitmap
        // it's mostly here in case they want just one dimension of tiling instead of both
        if( d instanceof BitmapDrawable ) {
            BitmapDrawable bm = (BitmapDrawable)d;
            // according to docs -1 is the disabled mode but doesn't exist as a constant
            // so I'm hoping that setting it null acheives the same effect
            Shader.TileMode[] tms = new Shader.TileMode[] { null, null };
            
            if( tilemode != null ) {
                String[] modes = new String[] { tilemode, tilemode };
                if( tilemode.contains( "," ) ) {
                    modes = tilemode.split( "," );
                }
                
                for( int i = 0; i < 2; i++ ) {
                    String check = modes[ i ];
                    if( check.equals( "clamp" ) ) {
                        tms[ i ] = TileMode.CLAMP;
                    }else if( check.equals( "repeat" ) ) {
                        tms[ i ] = TileMode.REPEAT;
                    }else if( check.equals( "mirror" ) ) {
                        tms[ i ] = TileMode.MIRROR;
                    }else{
                        // clear tiling, e.g. disabled
                        tms[ i ] = null;
                    }
                }
            }
            
            bm.setTileModeXY( tms[ 0 ], tms[ 1 ] );
        }
    }
}
    