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
package com.varaneckas.hawkscope.plugins.execute;

import com.varaneckas.hawkscope.command.Command;
import com.varaneckas.hawkscope.menu.ExecutableMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;

/**
 * A Hawkscope plugin that lets you use google search
 * 
 * @author Tomas Varaneckas
 * @version $Id: GooglescopePlugin.java 370 2009-02-26 20:56:44Z tomas.varaneckas $
 */
public class ExecutePlugin extends PluginAdapter {
    
    /**
     * Singleton Instance
     */
    private static ExecutePlugin instance;
    
    /**
     * Singleton Instance getter
     * 
     * @return
     */
    public static ExecutePlugin getInstance() {
        if (instance == null) {
            instance = new ExecutePlugin();
        }
        return instance;
    }

    /**
     * This plugin hooks before quick access list
     */
    private ExecutePlugin() {
        canHookBeforeQuickAccessList = true;
    }
    
    /**
     * Adds Execute Search item
     */
    public void beforeQuickAccess(final MainMenu mainMenu) {
        final ExecutableMenuItem execute = new ExecutableMenuItem();
        execute.setText("Execute");
        execute.setIcon(IconFactory.getInstance().getPluginIcon("execute24.png", 
                getClass().getClassLoader()));
        execute.setCommand(new Command() {
            public void execute() {
                new ExecuteWindow().open();
            }
        });
        mainMenu.addMenuItem(execute);
        mainMenu.addSeparator();
    }
    
    public String getDescription() {
        return "Executes commands.";
    }

    public String getName() {
        return "Execute";
    }

    public String getVersion() {
        return "1.1";
    }

}
