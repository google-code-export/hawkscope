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
package com.varaneckas.hawkscope.plugins.gmail;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;

/**
 * Twitter Settings tab item
 * 
 * @author Tomas Varaneckas
 * @version $Id: TwitterSettingsTabItem.java 375 2009-02-28 07:48:02Z tomas.varaneckas $
 */
public class GmailCheckerSettingsTabItem extends AbstractSettingsTabItem {

	/**
	 * Twitter login section label
	 */
	private Label twitterLogin;
	
	/**
	 * Twitter user input
	 */
    private Text inputUser;
    
    /**
     * Twitter password input
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
    
    /**
     * "Display" label
     */
    private Label display;
    
    /**
     * Checkbox "Display friends tweets"
     */
    private Button checkDisplayFriendsTweets;
    
    /**
     * Checkbox "Display my tweets"
     */
    private Button checkDisplayMyTweets;
    
    /**
     * Button "Display replies"
     */
    private Button checkDisplayReplies;
    
    public GmailCheckerSettingsTabItem(final TabFolder folder) {
        super(folder, "&Gmail Checker");
        createLoginSection();
    }

    /**
     * Creates Twitter Login section
     */
    private void createLoginSection() {
		twitterLogin = addSectionLabel("Gmail Login");
        twitterLogin.setLayoutData(SharedStyle.relativeTo(null, null));
        
        user = addLabel("Username:");
        user.setLayoutData(ident(SharedStyle.relativeTo(twitterLogin, null)));
        
        pass = addLabel("Password:");
        pass.setLayoutData(ident(SharedStyle.relativeTo(user, null)));
        
        inputUser = addText(cfg.getProperties()
                .get(TwitterPlugin.PROP_TWITTER_USER), 255);
        FormData userLayout = SharedStyle.relativeTo(twitterLogin, 
                null, null, twitterLogin);
        userLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        userLayout.bottom = null;
        inputUser.setLayoutData(userLayout);
        
        inputPass = addText(cfg.getPasswordProperty
        		(TwitterPlugin.PROP_TWITTER_PASS), 255);
        inputPass.setEchoChar('*');
        FormData passLayout = SharedStyle.relativeTo(user, 
                null, null, twitterLogin);
        passLayout.bottom = null;
        passLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        inputPass.setLayoutData(passLayout);
	}

    @Override
    protected void saveConfiguration() {
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_USER, 
        		inputUser.getText());
        cfg.setPasswordProperty(TwitterPlugin.PROP_TWITTER_PASS, 
        		inputPass.getText());
        TwitterPlugin.getInstance().refresh();
    }

}
