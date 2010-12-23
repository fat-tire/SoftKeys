/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hoopajoo.android.SoftKeys;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class RecentAppsChunk {
    /// for recent apps
    //private static final boolean DBG_FORCE_EMPTY_LIST = false;
    private static final int NUM_BUTTONS = 6;
    private static final int MAX_RECENT_TASKS = NUM_BUTTONS * 2;    // allow for some discards
    final View[] mButtons = new View[NUM_BUTTONS];
    private int mIconSize;
    private Activity context;
    
    public RecentAppsChunk( Activity a ) {
        context = a;
        
        // recent apps buttons
        OnClickListener press = new OnClickListener() {
            public void onClick(View v) {
    
                for (View b : mButtons) {
                    if (b == v) {
                        // prepare a launch intent and send it
                        Intent intent = (Intent)b.getTag();
                        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        getContext().startActivity(intent);
                    }
                }
                //dismiss();
            }
        };

        int[] rbuttons = { R.id.recentbutton0, 
                R.id.recentbutton1,
                R.id.recentbutton2,
                R.id.recentbutton3,
                R.id.recentbutton4,
                R.id.recentbutton5
        };
        
        for( int i = 0; i < NUM_BUTTONS; i++ ) {          
            mButtons[ i ] = context.findViewById( rbuttons[ i ] );
            mButtons[ i ].setOnClickListener( press );
        }

        final Resources resources = context.getResources();
        mIconSize = (int) resources.getDimension(android.R.dimen.app_icon_size);
    }

    
    // basically from the recent apps dialog
    private Context getContext() {
        // this emulates some of the stuff that happened in the constructor, and also
        // the getcontext allowing reloadButtons to be included without modification`
        return( context );
    }
    
    public void reloadButtons() {

        final Context context = getContext();
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager)
                                        context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTasks =
                                        am.getRecentTasks(MAX_RECENT_TASKS, 0);

        ResolveInfo homeInfo = pm.resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                0);

        // Performance note:  Our android performance guide says to prefer Iterator when
        // using a List class, but because we know that getRecentTasks() always returns
        // an ArrayList<>, we'll use a simple index instead.
        int button = 0;
        int numTasks = recentTasks.size();
        for (int i = 0; i < numTasks && (button < NUM_BUTTONS); ++i) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);

            // for debug purposes only, disallow first result to create empty lists
            //if (DBG_FORCE_EMPTY_LIST && (i == 0)) continue;

            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }

            // Skip the current home activity.
            if (homeInfo != null) {
                if (homeInfo.activityInfo.packageName.equals(
                        intent.getComponent().getPackageName())
                        && homeInfo.activityInfo.name.equals(
                                intent.getComponent().getClassName())) {
                    continue;
                }
            }

            intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                final String title = activityInfo.loadLabel(pm).toString();
                final Drawable icon = activityInfo.loadIcon(pm);

                if (title != null && title.length() > 0 && icon != null) {
                    final View b = mButtons[button];
                    setButtonAppearance(b, title, icon);
                    b.setTag(intent);
                    b.setVisibility(View.VISIBLE);
                    b.setPressed(false);
                    b.clearFocus();
                    ++button;
                }
            }
        }

        // handle the case of "no icons to show"
        //mNoAppsText.setVisibility((button == 0) ? View.VISIBLE : View.GONE);

        // hide the rest
        for ( ; button < NUM_BUTTONS; ++button) {
            mButtons[button].setVisibility(View.GONE);
        }
    }

    /**
     * Adjust appearance of each icon-button
     */
    private void setButtonAppearance(View theButton, final String theTitle, final Drawable icon) {
        TextView tv = (TextView) theButton;
        tv.setText(theTitle);
        if (icon != null) {
            icon.setBounds(0, 0, mIconSize, mIconSize);
        }
        tv.setCompoundDrawables(null, icon, null, null);
    }
}     
