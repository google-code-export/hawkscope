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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;

/**
 * Twitter Settings tab item
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class TwitterSettingsTabItem extends AbstractSettingsTabItem {

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
     * Input for twitter data update frequency seconds
     */
    private Text inputUpdateFreq;
    
    /**
     * "Update frequency" section label
     */
    private Label frequency;
    
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
    
    public TwitterSettingsTabItem(final TabFolder folder) {
        super(folder, "&Twitter");
        createLoginSection();
        createDisplaySection();
        createUpdateFrequencySection();
    }

    /**
     * Creates Update Frequency section
     */
	private void createUpdateFrequencySection() {
        frequency = addSectionLabel("Update frequency");
        frequency.setLayoutData(ident(SharedStyle.relativeTo(
        		checkDisplayFriendsTweets, null)));
        inputUpdateFreq = addText(cfg.getProperties()
                .get(TwitterPlugin.PROP_TWITTER_CACHE), 5);
        inputUpdateFreq.setLayoutData(ident(SharedStyle.relativeTo(frequency, 
        		null)));
        final String freq = cfg.getProperties()
        		.get(TwitterPlugin.PROP_TWITTER_CACHE) == null ? 
        				"60" : cfg.getProperties()
                			.get(TwitterPlugin.PROP_TWITTER_CACHE) ;
        inputUpdateFreq.setText(freq);
        inputUpdateFreq.addListener(SWT.FocusOut, new Listener() {
            public void handleEvent(final Event event) {
                try {
                    double d = Double.valueOf(inputUpdateFreq.getText());
                    if (d <= 0) {
                    	inputUpdateFreq.setText("0.1");
                    }
                    if (d > 9999) {
                    	inputUpdateFreq.setText("9999");
                    }
                } catch (final Exception e) {
                	inputUpdateFreq.setText(freq);
                }
            }
        });
    }

	/**
	 * Creates Display section
	 */
    private void createDisplaySection() {
	    display = addSectionLabel("Display");
	    display.setLayoutData(SharedStyle.relativeTo(pass, null));
	    
	    final String showMy = cfg.getProperties().get(
	    		TwitterPlugin.PROP_TWITTER_SHOW_MY);
	    checkDisplayMyTweets = addCheckbox("Display my tweets");
	    checkDisplayMyTweets.setLayoutData(ident(SharedStyle.relativeTo(
	    		display, null)));
	    checkDisplayMyTweets.setSelection(showMy == null ? 
	    		true : showMy.equals("1"));

	    final String showRe = cfg.getProperties().get(
	    		TwitterPlugin.PROP_TWITTER_SHOW_RE);
	    checkDisplayReplies = addCheckbox("Display replies");
	    checkDisplayReplies.setLayoutData(ident(SharedStyle.relativeTo(
	    		checkDisplayMyTweets, null)));
	    checkDisplayReplies.setSelection(showRe == null ? 
	    		true : showRe.equals("1"));
	    
	    final String showF = cfg.getProperties().get(
	    		TwitterPlugin.PROP_TWITTER_SHOW_FRIENDS);
	    checkDisplayFriendsTweets = addCheckbox("Display friends tweets");
	    checkDisplayFriendsTweets.setLayoutData(ident(SharedStyle.relativeTo(
	    		checkDisplayReplies, null)));
	    checkDisplayFriendsTweets.setSelection(showF == null ? 
	    		true : showF.equals("1"));
    }

    /**
     * Creates Twitter Login section
     */
    private void createLoginSection() {
		twitterLogin = addSectionLabel("Twitter Login");
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
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_CACHE, "" + 
                Math.round(Double.valueOf(inputUpdateFreq.getText()) * 1000));
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_SHOW_MY, 
        		checkDisplayMyTweets.getSelection() ? "1" : "0");
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_SHOW_RE, 
        		checkDisplayReplies.getSelection() ? "1" : "0");
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_SHOW_FRIENDS, 
        		checkDisplayFriendsTweets.getSelection() ? "1" : "0");
        TwitterPlugin.getInstance().refresh();
    }

}
