/*
 * Copyright (c) 2008-2009 Tomas Varaneckas
 * http://www.varaneckas.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.varaneckas.hawkscope.hotkey;

import com.varaneckas.hawkscope.util.OSUtils;

/**
 * Global hotkey listener for invoking hawkscope menu
 *
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class GlobalHotkeyListener {
    
    /**
     * Global Hotkey Listener instance
     */
    private static GlobalHotkeyListener instance = null;
    
    /**
     * Loads the required instance if possible
     * 
     * @return instance
     */
    public static synchronized GlobalHotkeyListener getInstance() {
        if (instance == null) {
            instance = chooseImpl();
        }
        return instance;
    }
    
    /**
     * Chooses {@link GlobalHotkeyListener} implementation according to OS
     * 
     * @return
     */
    private static GlobalHotkeyListener chooseImpl() {
        switch (OSUtils.CURRENT_OS) {
        case UNIX:
            return new X11KeyListener();
        case WIN:
            return new WinKeyListener();
        default:
            return null;
        }
    }

}
