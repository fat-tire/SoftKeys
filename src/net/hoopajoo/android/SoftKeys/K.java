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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Key constants and resolver maps
// Auto-generated from keycodes.txt
public class K {

    public static String KEY_SOFT_RIGHT = "soft_right"; 
    public static String KEY_HOME = "home"; 
    public static String KEY_BACK = "back"; 
    public static String KEY_CALL = "call"; 
    public static String KEY_ENDCALL = "endcall"; 
    public static String KEY_0 = "0"; 
    public static String KEY_1 = "1"; 
    public static String KEY_2 = "2"; 
    public static String KEY_3 = "3"; 
    public static String KEY_4 = "4"; 
    public static String KEY_5 = "5"; 
    public static String KEY_6 = "6"; 
    public static String KEY_7 = "7"; 
    public static String KEY_8 = "8"; 
    public static String KEY_9 = "9"; 
    public static String KEY_STAR = "star"; 
    public static String KEY_POUND = "pound"; 
    public static String KEY_DPAD_UP = "dpad_up"; 
    public static String KEY_DPAD_DOWN = "dpad_down"; 
    public static String KEY_DPAD_LEFT = "dpad_left"; 
    public static String KEY_DPAD_RIGHT = "dpad_right"; 
    public static String KEY_DPAD_CENTER = "dpad_center"; 
    public static String KEY_VOLUME_UP = "volume_up"; 
    public static String KEY_VOLUME_DOWN = "volume_down"; 
    public static String KEY_POWER = "power"; 
    public static String KEY_CAMERA = "camera"; 
    public static String KEY_CLEAR = "clear"; 
    public static String KEY_A = "a"; 
    public static String KEY_B = "b"; 
    public static String KEY_C = "c"; 
    public static String KEY_D = "d"; 
    public static String KEY_E = "e"; 
    public static String KEY_F = "f"; 
    public static String KEY_G = "g"; 
    public static String KEY_H = "h"; 
    public static String KEY_I = "i"; 
    public static String KEY_J = "j"; 
    public static String KEY_K = "k"; 
    public static String KEY_L = "l"; 
    public static String KEY_M = "m"; 
    public static String KEY_N = "n"; 
    public static String KEY_O = "o"; 
    public static String KEY_P = "p"; 
    public static String KEY_Q = "q"; 
    public static String KEY_R = "r"; 
    public static String KEY_S = "s"; 
    public static String KEY_T = "t"; 
    public static String KEY_U = "u"; 
    public static String KEY_V = "v"; 
    public static String KEY_W = "w"; 
    public static String KEY_X = "x"; 
    public static String KEY_Y = "y"; 
    public static String KEY_Z = "z"; 
    public static String KEY_COMMA = "comma"; 
    public static String KEY_PERIOD = "period"; 
    public static String KEY_ALT_LEFT = "alt_left"; 
    public static String KEY_ALT_RIGHT = "alt_right"; 
    public static String KEY_SHIFT_LEFT = "shift_left"; 
    public static String KEY_SHIFT_RIGHT = "shift_right"; 
    public static String KEY_TAB = "tab"; 
    public static String KEY_SPACE = "space"; 
    public static String KEY_SYM = "sym"; 
    public static String KEY_EXPLORER = "explorer"; 
    public static String KEY_ENVELOPE = "envelope"; 
    public static String KEY_ENTER = "enter"; 
    public static String KEY_DEL = "del"; 
    public static String KEY_GRAVE = "grave"; 
    public static String KEY_MINUS = "minus"; 
    public static String KEY_EQUALS = "equals"; 
    public static String KEY_LEFT_BRACKET = "left_bracket"; 
    public static String KEY_RIGHT_BRACKET = "right_bracket"; 
    public static String KEY_BACKSLASH = "backslash"; 
    public static String KEY_SEMICOLON = "semicolon"; 
    public static String KEY_APOSTROPHE = "apostrophe"; 
    public static String KEY_SLASH = "slash"; 
    public static String KEY_AT = "at"; 
    public static String KEY_NUM = "num"; 
    public static String KEY_HEADSETHOOK = "headsethook"; 
    public static String KEY_FOCUS = "focus"; 
    public static String KEY_PLUS = "plus"; 
    public static String KEY_MENU = "menu"; 
    public static String KEY_NOTIFICATION = "notification"; 
    public static String KEY_SEARCH = "search"; 
    public static String KEY_TAG_LAST_KEYCODE = "tag_last_keycode"; 

    public static int KEYID_SOFT_RIGHT = 2;
    public static int KEYID_HOME = 3;
    public static int KEYID_BACK = 4;
    public static int KEYID_CALL = 5;
    public static int KEYID_ENDCALL = 6;
    public static int KEYID_0 = 7;
    public static int KEYID_1 = 8;
    public static int KEYID_2 = 9;
    public static int KEYID_3 = 10;
    public static int KEYID_4 = 11;
    public static int KEYID_5 = 12;
    public static int KEYID_6 = 13;
    public static int KEYID_7 = 14;
    public static int KEYID_8 = 15;
    public static int KEYID_9 = 16;
    public static int KEYID_STAR = 17;
    public static int KEYID_POUND = 18;
    public static int KEYID_DPAD_UP = 19;
    public static int KEYID_DPAD_DOWN = 20;
    public static int KEYID_DPAD_LEFT = 21;
    public static int KEYID_DPAD_RIGHT = 22;
    public static int KEYID_DPAD_CENTER = 23;
    public static int KEYID_VOLUME_UP = 24;
    public static int KEYID_VOLUME_DOWN = 25;
    public static int KEYID_POWER = 26;
    public static int KEYID_CAMERA = 27;
    public static int KEYID_CLEAR = 28;
    public static int KEYID_A = 29;
    public static int KEYID_B = 30;
    public static int KEYID_C = 31;
    public static int KEYID_D = 32;
    public static int KEYID_E = 33;
    public static int KEYID_F = 34;
    public static int KEYID_G = 35;
    public static int KEYID_H = 36;
    public static int KEYID_I = 37;
    public static int KEYID_J = 38;
    public static int KEYID_K = 39;
    public static int KEYID_L = 40;
    public static int KEYID_M = 41;
    public static int KEYID_N = 42;
    public static int KEYID_O = 43;
    public static int KEYID_P = 44;
    public static int KEYID_Q = 45;
    public static int KEYID_R = 46;
    public static int KEYID_S = 47;
    public static int KEYID_T = 48;
    public static int KEYID_U = 49;
    public static int KEYID_V = 50;
    public static int KEYID_W = 51;
    public static int KEYID_X = 52;
    public static int KEYID_Y = 53;
    public static int KEYID_Z = 54;
    public static int KEYID_COMMA = 55;
    public static int KEYID_PERIOD = 56;
    public static int KEYID_ALT_LEFT = 57;
    public static int KEYID_ALT_RIGHT = 58;
    public static int KEYID_SHIFT_LEFT = 59;
    public static int KEYID_SHIFT_RIGHT = 60;
    public static int KEYID_TAB = 61;
    public static int KEYID_SPACE = 62;
    public static int KEYID_SYM = 63;
    public static int KEYID_EXPLORER = 64;
    public static int KEYID_ENVELOPE = 65;
    public static int KEYID_ENTER = 66;
    public static int KEYID_DEL = 67;
    public static int KEYID_GRAVE = 68;
    public static int KEYID_MINUS = 69;
    public static int KEYID_EQUALS = 70;
    public static int KEYID_LEFT_BRACKET = 71;
    public static int KEYID_RIGHT_BRACKET = 72;
    public static int KEYID_BACKSLASH = 73;
    public static int KEYID_SEMICOLON = 74;
    public static int KEYID_APOSTROPHE = 75;
    public static int KEYID_SLASH = 76;
    public static int KEYID_AT = 77;
    public static int KEYID_NUM = 78;
    public static int KEYID_HEADSETHOOK = 79;
    public static int KEYID_FOCUS = 80;
    public static int KEYID_PLUS = 81;
    public static int KEYID_MENU = 82;
    public static int KEYID_NOTIFICATION = 83;
    public static int KEYID_SEARCH = 84;
    public static int KEYID_TAG_LAST_KEYCODE = 85;

    private static final Map<String,Integer> mKeymap;
    static {
        Map<String,Integer> t = new HashMap<String,Integer>();

        t.put( KEY_SOFT_RIGHT, KEYID_SOFT_RIGHT );
        t.put( KEY_HOME, KEYID_HOME );
        t.put( KEY_BACK, KEYID_BACK );
        t.put( KEY_CALL, KEYID_CALL );
        t.put( KEY_ENDCALL, KEYID_ENDCALL );
        t.put( KEY_0, KEYID_0 );
        t.put( KEY_1, KEYID_1 );
        t.put( KEY_2, KEYID_2 );
        t.put( KEY_3, KEYID_3 );
        t.put( KEY_4, KEYID_4 );
        t.put( KEY_5, KEYID_5 );
        t.put( KEY_6, KEYID_6 );
        t.put( KEY_7, KEYID_7 );
        t.put( KEY_8, KEYID_8 );
        t.put( KEY_9, KEYID_9 );
        t.put( KEY_STAR, KEYID_STAR );
        t.put( KEY_POUND, KEYID_POUND );
        t.put( KEY_DPAD_UP, KEYID_DPAD_UP );
        t.put( KEY_DPAD_DOWN, KEYID_DPAD_DOWN );
        t.put( KEY_DPAD_LEFT, KEYID_DPAD_LEFT );
        t.put( KEY_DPAD_RIGHT, KEYID_DPAD_RIGHT );
        t.put( KEY_DPAD_CENTER, KEYID_DPAD_CENTER );
        t.put( KEY_VOLUME_UP, KEYID_VOLUME_UP );
        t.put( KEY_VOLUME_DOWN, KEYID_VOLUME_DOWN );
        t.put( KEY_POWER, KEYID_POWER );
        t.put( KEY_CAMERA, KEYID_CAMERA );
        t.put( KEY_CLEAR, KEYID_CLEAR );
        t.put( KEY_A, KEYID_A );
        t.put( KEY_B, KEYID_B );
        t.put( KEY_C, KEYID_C );
        t.put( KEY_D, KEYID_D );
        t.put( KEY_E, KEYID_E );
        t.put( KEY_F, KEYID_F );
        t.put( KEY_G, KEYID_G );
        t.put( KEY_H, KEYID_H );
        t.put( KEY_I, KEYID_I );
        t.put( KEY_J, KEYID_J );
        t.put( KEY_K, KEYID_K );
        t.put( KEY_L, KEYID_L );
        t.put( KEY_M, KEYID_M );
        t.put( KEY_N, KEYID_N );
        t.put( KEY_O, KEYID_O );
        t.put( KEY_P, KEYID_P );
        t.put( KEY_Q, KEYID_Q );
        t.put( KEY_R, KEYID_R );
        t.put( KEY_S, KEYID_S );
        t.put( KEY_T, KEYID_T );
        t.put( KEY_U, KEYID_U );
        t.put( KEY_V, KEYID_V );
        t.put( KEY_W, KEYID_W );
        t.put( KEY_X, KEYID_X );
        t.put( KEY_Y, KEYID_Y );
        t.put( KEY_Z, KEYID_Z );
        t.put( KEY_COMMA, KEYID_COMMA );
        t.put( KEY_PERIOD, KEYID_PERIOD );
        t.put( KEY_ALT_LEFT, KEYID_ALT_LEFT );
        t.put( KEY_ALT_RIGHT, KEYID_ALT_RIGHT );
        t.put( KEY_SHIFT_LEFT, KEYID_SHIFT_LEFT );
        t.put( KEY_SHIFT_RIGHT, KEYID_SHIFT_RIGHT );
        t.put( KEY_TAB, KEYID_TAB );
        t.put( KEY_SPACE, KEYID_SPACE );
        t.put( KEY_SYM, KEYID_SYM );
        t.put( KEY_EXPLORER, KEYID_EXPLORER );
        t.put( KEY_ENVELOPE, KEYID_ENVELOPE );
        t.put( KEY_ENTER, KEYID_ENTER );
        t.put( KEY_DEL, KEYID_DEL );
        t.put( KEY_GRAVE, KEYID_GRAVE );
        t.put( KEY_MINUS, KEYID_MINUS );
        t.put( KEY_EQUALS, KEYID_EQUALS );
        t.put( KEY_LEFT_BRACKET, KEYID_LEFT_BRACKET );
        t.put( KEY_RIGHT_BRACKET, KEYID_RIGHT_BRACKET );
        t.put( KEY_BACKSLASH, KEYID_BACKSLASH );
        t.put( KEY_SEMICOLON, KEYID_SEMICOLON );
        t.put( KEY_APOSTROPHE, KEYID_APOSTROPHE );
        t.put( KEY_SLASH, KEYID_SLASH );
        t.put( KEY_AT, KEYID_AT );
        t.put( KEY_NUM, KEYID_NUM );
        t.put( KEY_HEADSETHOOK, KEYID_HEADSETHOOK );
        t.put( KEY_FOCUS, KEYID_FOCUS );
        t.put( KEY_PLUS, KEYID_PLUS );
        t.put( KEY_MENU, KEYID_MENU );
        t.put( KEY_NOTIFICATION, KEYID_NOTIFICATION );
        t.put( KEY_SEARCH, KEYID_SEARCH );
        t.put( KEY_TAG_LAST_KEYCODE, KEYID_TAG_LAST_KEYCODE );

        mKeymap = Collections.unmodifiableMap( t );
    }

    public static int keyNameToId( String n ) {
        if( ! mKeymap.containsKey( n ) ) {
            return 0;
        }
        return mKeymap.get( n );
    }
}
