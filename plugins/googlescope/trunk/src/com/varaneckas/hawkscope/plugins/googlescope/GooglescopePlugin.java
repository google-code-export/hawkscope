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
package com.varaneckas.hawkscope.plugins.googlescope;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.swt.program.Program;

import com.varaneckas.hawkscope.command.Command;
import com.varaneckas.hawkscope.gui.InputDialog;
import com.varaneckas.hawkscope.menu.ExecutableMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.Updater;

/**
 * A Hawkscope plugin that lets you use google search
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class GooglescopePlugin extends PluginAdapter {

	public GooglescopePlugin() {
		canHookBeforeQuickAccessList = true;
	}
	
	@Override
	public void beforeQuickAccess(final MainMenu mainMenu) {
		ExecutableMenuItem google = new ExecutableMenuItem();
		google.setText("Google Search");
		google.setCommand(new Command() {
			public void execute() {
				InputDialog.open("Google for:", 512, 
						null, new Updater() {
							public void setValue(String q) {
								try {
									q = URLEncoder.encode(q, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								Program.launch("http://www.google.com/search?q=" + q);
							}
					});
			}
		});
		mainMenu.addMenuItem(google);
	}
	
	public String getDescription() {
		return "Allows running Google queries right from Hawkscope menu";
	}

	public String getName() {
		return "Googlescope";
	}

	public String getVersion() {
		return "1.0";
	}

}
