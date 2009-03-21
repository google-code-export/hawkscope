package com.varaneckas.hawkscope.plugins.gmail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.rss.RssGmailClient;
import com.googlecode.gmail4j.util.LoginDialog;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;

public class GmailCheckerPlugin extends PluginAdapter {

    private static final GmailCheckerPlugin instance = new GmailCheckerPlugin();

    private GmailCheckerPlugin() {
        canHookBeforeQuickAccessList = true;
        gmail.setLoginCredentials(LoginDialog.getInstance().show("Gmail login"));
        gmail.init();
    }

    public static GmailCheckerPlugin getInstance() {
        return instance;
    }
    
    private final GmailClient gmail = new RssGmailClient();
    
    public String getDescription() {
        return "Displays unread Gmail messages";
    }

    public String getName() {
        return "Gmail Checker";
    }

    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public void beforeQuickAccess(final MainMenu mainMenu) {
        GmailCheckerMenuItem menuItem = new GmailCheckerMenuItem();
        menuItem.setText("Gmail");
        menuItem.setIcon(getGmailIcon());
        mainMenu.addMenuItem(menuItem);
        mainMenu.addSeparator();
        final Menu newMessages = new Menu(menuItem.getSwtMenuItem());
        menuItem.getSwtMenuItem().setMenu(newMessages);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                for (GmailMessage message : gmail.getUnreadMessages()) {
                    addUnreadMessage(newMessages, message);
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

}
