package com.varaneckas.hawkscope.plugins.gmail;

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
import com.googlecode.gmail4j.rss.RssGmailClient;
import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;

public class GmailCheckerPlugin extends PluginAdapter {
    
    public static final String PROP_USER = "plugins.gmailchecker.user";
    
    public static final String PROP_PASS = "plugins.gmailchecker.pass";
    
    private GmailCheckerSettingsTabItem settings;
    
    private GmailCheckerMenuItem menuItem;
    
    private String gmailError = null;

    private static final GmailCheckerPlugin instance = new GmailCheckerPlugin();

    private GmailCheckerPlugin() {
        canHookBeforeQuickAccessList = true;
        refresh();
    }
    
    public void refresh() {
        try {
            Configuration cfg = ConfigurationFactory.getConfigurationFactory().getConfiguration();
            gmail = new RssGmailClient();
            gmail.setLoginCredentials(cfg.getProperties().get(PROP_USER), 
                    cfg.getPasswordProperty(PROP_PASS).toCharArray());
            gmail.init();
        } catch (final Exception e) {
            if (e instanceof GmailException) {
                gmailError = e.getMessage();
            }
            log.warn(e);
        }
    }

    public static GmailCheckerPlugin getInstance() {
        return instance;
    }
    
    private GmailClient gmail;
    
    public String getDescription() {
        return "Displays unread Gmail messages.";
    }

    public String getName() {
        return "Gmail Checker";
    }

    public String getVersion() {
        return "1.0";
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
    
    private void addUnreadMessage(Menu menuItem, final GmailMessage message) {
        log.debug("Adding unread message");
        String msg = message.getFrom().toString().concat(": ")
            .concat(message.getSubject());
        MenuItem mi = new MenuItem(menuItem, SWT.PUSH);
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
