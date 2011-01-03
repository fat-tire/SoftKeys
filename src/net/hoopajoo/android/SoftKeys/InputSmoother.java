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
import java.util.ListIterator;

import android.util.Log;

// keeps a list of points to drop outliers that make your drag jitter
public class InputSmoother {
    private List<PointCheck> mPoints = new ArrayList<PointCheck>();
    private int mHistLength;
    private PointCheck mCurrent;
    
    InputSmoother( int histLength ) {
        mHistLength = histLength;
    }
    
    public void addPoint( int x, int y ) {
        PointCheck p = new PointCheck();
        //Log.d( "Smoother", "adding points: " + x + ", " + y );
        p.x = x;
        p.y = y;
        mPoints.add( p );
        
        // if points too big pop out the last
        if( mPoints.size() > mHistLength ) {
            mPoints.remove( 0 );
        }
    }
    
    public void updateOutliers() {
        // flag the outliers
        for( PointCheck p : mPoints ) {
            p.outlier = false;
        }

        // currently this just checks to see if we delta'd farther than 10 pixels,
        // it would be nice to add something smarter like curve fitting someday
        // but it probably doesn't matter
        int lastx = mPoints.get( 0 ).x;
        int lasty = mPoints.get( 0 ).y;
        
        for( PointCheck p : mPoints ) {
            if( Math.abs( lastx - p.x ) > 10 ) {
                p.outlier = true;
            }
            if( Math.abs( lasty - p.y ) > 10 ) {
                p.outlier = true;
            }
            lastx = p.x;
            lasty = p.y;
        }
        
        // set current as the last non-outlier
        mCurrent = null;
        for( int i = mPoints.size() - 1; i >= 0; i-- ) {
            PointCheck p = mPoints.get( i ); 
                    
            if( mCurrent != null ) {
                if( p.outlier == false ) {
                    mCurrent = p;
                }
            }
        }
        
        // if still null just give it the last one even though it's an outlier
        if( mCurrent == null ) {
            mCurrent = mPoints.get( mPoints.size() - 1 );
        }
    }
    
    public int[] getCurrent() {
        int[] ret = new int[ 2 ];
        ret[ 0 ] = mCurrent.x;
        ret[ 1 ] = mCurrent.y;
        return( ret );
    }
    
    private class PointCheck {
        public int x;
        public int y;
        public boolean outlier;
    }
}
