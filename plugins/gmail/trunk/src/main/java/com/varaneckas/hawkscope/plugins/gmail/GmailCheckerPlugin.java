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

import java.net.Authenticator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;

import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailException;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.http.HttpGmailConnection;
import com.googlecode.gmail4j.rss.RssGmailClient;
import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;

/**
 * Plugin that checks Gmail account for new messages
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class GmailCheckerPlugin extends PluginAdapter {
    
    /**
     * Gmail username property
     */
    public static final String PROP_USER = "plugins.gmailchecker.user";
    
    /**
     * Gmail password property
     */
    public static final String PROP_PASS = "plugins.gmailchecker.pass";
    
    /**
     * Gmail Checker Settings Tab
     */
    private GmailCheckerSettingsTabItem settings;
    
    /**
     * Menu item
     */
    private GmailCheckerMenuItem menuItem;
    
    /**
     * Gmail error
     */
    private String gmailError = null;

    /**
     * Singleton instance
     */
    private static final GmailCheckerPlugin instance = new GmailCheckerPlugin();

    /**
     * Singleton constructor
     */
    private GmailCheckerPlugin() {
        canHookBeforeQuickAccessList = true;
        refresh();
    }
    
    /**
     * Refreshes plugin data
     */
    public void refresh() {
        try {
            gmailError = null;
            final Configuration cfg = ConfigurationFactory.getConfigurationFactory()
                .getConfiguration();
            gmail = new RssGmailClient();
            final HttpGmailConnection conn = new HttpGmailConnection();
            conn.setLoginCredentials(cfg.getProperties().get(PROP_USER), 
                    cfg.getPasswordProperty(PROP_PASS).toCharArray());
            if (cfg.isHttpProxyInUse()) {
                conn.setProxy(cfg.getHttpProxyHost(), cfg.getHttpProxyPort());
                if (cfg.isHttpProxyAuthInUse()) {
                    conn.setProxyCredentials(cfg.getHttpProxyAuthUsername(), 
                            cfg.getHttpProxyAuthPassword().toCharArray());
                }
            }
            gmail.setConnection(conn);
        } catch (final Exception e) {
            if (e instanceof GmailException) {
                gmailError = e.getMessage();
            }
            log.warn(e);
        }
    }

    /**
     * Singleton instance getter
     * 
     * @return
     */
    public static GmailCheckerPlugin getInstance() {
        return instance;
    }
    
    /**
     * Gmail4J client
     */
    private GmailClient gmail;
    
    public String getDescription() {
        return "Displays unread Gmail messages.";
    }

    public String getName() {
        return "Gmail Checker";
    }

    public String getVersion() {
        return "1.1";
    }
    
    @Override
    public void beforeQuickAccess(final MainMenu mainMenu) {
        menuItem = new GmailCheckerMenuItem();
        menuItem.setText("Gmail");
        menuItem.setIcon(getGmailIcon());
        mainMenu.addMenuItem(menuItem);
        mainMenu.addSeparator();
        if (gmailError != null) {
            menuItem.getSwtMenuItem().setText("Gmail :( " + gmailError);
            menuItem.getSwtMenuItem().setEnabled(false);
            return;
        }
        
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    //just in case
                    Authenticator.setDefault(null);
                    //for ROME property file reader:
                    Thread.currentThread().setContextClassLoader(getClass()
                            .getClassLoader());
                    final List<GmailMessage> messages = gmail.getUnreadMessages();
                    final Menu newMessages = new Menu(menuItem.getSwtMenuItem());
                    menuItem.getSwtMenuItem().setMenu(newMessages);
                    if (messages.size() > 0) {
                        menuItem.getSwtMenuItem().setText("&Gmail (" 
                                + messages.size() + ")");
                        for (GmailMessage message : messages) {
                            addUnreadMessage(newMessages, message);
                        }
                    }
                    else {
                        MenuItem noMessages = new MenuItem(newMessages, SWT.NONE);
                        noMessages.setImage(getGmailIcon());
                        noMessages.setText("No new messages");
                        noMessages.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event ev) {
                                Program.launch("https://mail.google.com");
                            }
                        });
                    }
                } catch (final Exception e) {
                    log.warn("Could not get gmail messages", e);
                }
            }
        });
        
    }
    
    /**
     * Adds unread message to menu
     * 
     * @param menuItem
     * @param message
     */
    private void addUnreadMessage(Menu menuItem, final GmailMessage message) {
        String msg = message.getFrom().toString().concat(": ")
            .concat(message.getSubject());
        final MenuItem mi = new MenuItem(menuItem, SWT.PUSH);
        if (msg.length() > 80) {
            msg = msg.substring(0, 79) 
                + "...";
        }
        mi.setText(msg);
        mi.setImage(getGmailIcon());
        mi.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(
                    SelectionEvent selectionevent) {
                widgetSelected(selectionevent);
            }
            public void widgetSelected(SelectionEvent selectionevent) {
                Program.launch(message.getLink());
            }
        });
        
    }

    /**
     * Gets gmail icon
     * 
     * @return
     */
    protected Image getGmailIcon() {
        return IconFactory.getInstance().getPluginIcon("gmail24.png",
                getClass().getClassLoader());
    }
    
    @Override
    public void enhanceSettings(final TabFolder folder,
            final List<AbstractSettingsTabItem> tabItems) {
        settings = new GmailCheckerSettingsTabItem(folder);
        tabItems.add(settings);
    }

}
