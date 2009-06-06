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
package com.varaneckas.hawkscope.plugins.delicious;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;

/**
 * Delicious Settings tab item
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class DeliciousSettingsTabItem extends AbstractSettingsTabItem {

    /**
     * Instance of the Delicious plugin
     */
    private DeliciousPlugin plugin;
    
	/**
	 * Gmail login section label
	 */
	private Label login;
	
	/**
	 * Gmail user input
	 */
    private Text inputUser;
    
    /**
     * Gmail password input
     */
    private Text inputPass;
    
    /**
     * User label
     */
    private Label user;
    
    /**
     * Password label
     */
    private Label pass;
    
    public DeliciousSettingsTabItem(final DeliciousPlugin plugin, final TabFolder folder) {
        super(folder, "&Delicious");
        this.plugin = plugin;
        createLoginSection();
    }

    /**
     * Creates Twitter Login section
     */
    private void createLoginSection() {
		login = addSectionLabel("Delicious.com Account");
        login.setLayoutData(SharedStyle.relativeTo(null, null));
        
        user = addLabel("Username:");
        user.setLayoutData(ident(SharedStyle.relativeTo(login, null)));
        
        pass = addLabel("Password:");
        pass.setLayoutData(ident(SharedStyle.relativeTo(user, null)));
        
        inputUser = addText(cfg.getProperties()
                .get(DeliciousPlugin.PROP_USER), 255);
        FormData userLayout = SharedStyle.relativeTo(login, 
                null, null, login);
        userLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        userLayout.bottom = null;
        inputUser.setLayoutData(userLayout);
        
        inputPass = addText(cfg.getPasswordProperty
        		(DeliciousPlugin.PROP_PASS), 255);
        inputPass.setEchoChar('*');
        FormData passLayout = SharedStyle.relativeTo(user, 
                null, null, login);
        passLayout.bottom = null;
        passLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        inputPass.setLayoutData(passLayout);
	}

    @Override
    protected void saveConfiguration() {
        cfg.getProperties().put(DeliciousPlugin.PROP_USER, 
        		inputUser.getText());
        cfg.setPasswordProperty(DeliciousPlugin.PROP_PASS, 
        		inputPass.getText());
        plugin.refresh();
    }

}
