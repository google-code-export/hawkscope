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
package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;

public class TwitterSettingsTabItem extends AbstractSettingsTabItem {

    Text inputUser;
    
    Text inputPass;
    
    Label pass;
    
    Button checkDisplayFriendsTweets;
    
    public TwitterSettingsTabItem(final TabFolder folder) {
        super(folder, "&Twitter");
        
        createLoginSection();
        
        createDisplaySection();
        
        createUpdateFrequencySection();
    }

	private void createUpdateFrequencySection() {
        Label cache = addSectionLabel("Update frequency");
        cache.setLayoutData(ident(SharedStyle.relativeTo(checkDisplayFriendsTweets, null)));
        
        Text inputUpdateFreq = addText(cfg.getProperties()
                .get(TwitterPlugin.PROP_TWITTER_CACHE), 5);
        
        
    }

    private void createDisplaySection() {
	    Label display = addSectionLabel("Display");
	    display.setLayoutData(SharedStyle.relativeTo(pass, null));
	    
	    Button checkDisplayMyTweets = addCheckbox("Display my tweets");
	    checkDisplayMyTweets.setLayoutData(ident(SharedStyle.relativeTo(display, null)));
	    
	    Button checkDisplayReplies = addCheckbox("Display replies");
	    checkDisplayReplies.setLayoutData(ident(SharedStyle.relativeTo(checkDisplayMyTweets, null)));
	    
	    checkDisplayFriendsTweets = addCheckbox("Display friends tweets");
	    checkDisplayFriendsTweets.setLayoutData(ident(SharedStyle.relativeTo(checkDisplayReplies, null)));
	    
    }

    private void createLoginSection() {
		Label twitterLogin = addSectionLabel("Twitter Login");
        twitterLogin.setLayoutData(SharedStyle.relativeTo(null, null));
        
        Label user = addLabel("Username:");
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
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_USER, inputUser.getText());
        cfg.setPasswordProperty(TwitterPlugin.PROP_TWITTER_PASS, inputPass.getText());
        TwitterPlugin.getInstance().refresh();
    }

}
