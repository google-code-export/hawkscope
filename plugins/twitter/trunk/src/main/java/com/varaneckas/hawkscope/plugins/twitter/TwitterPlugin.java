package com.varaneckas.hawkscope.plugins.twitter;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.command.Command;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;
import com.varaneckas.hawkscope.menu.ExecutableMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.menu.MenuFactory;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.Updater;

public class TwitterPlugin extends PluginAdapter {

    private TwitterSettingsTabItem settings;

    private final Image icon;

    protected static final String PROP_TWITTER_USER = "plugins.twitter.user";

    protected static final String PROP_TWITTER_PASS = "plugins.twitter.pass";

    private Twitter twitter;

    private static TwitterPlugin instance;

    public static TwitterPlugin getInstance() {
        if (instance == null) {
            instance = new TwitterPlugin();
        }
        return instance;
    }

    private String user;

    private String pass;

    private TwitterPlugin() {
        canHookBeforeQuickAccessList = true;
        icon = new Image(Display.getDefault(), getClass().getClassLoader()
                .getResourceAsStream("icons/twitter24.png"));
        refresh();
    }

    public void refresh() {
        Configuration cfg = ConfigurationFactory.getConfigurationFactory()
                .getConfiguration();
        user = cfg.getProperties().get(PROP_TWITTER_USER);
        pass = cfg.getProperties().get(PROP_TWITTER_PASS);
        twitter = new Twitter(user, pass);
        twitter.setSource("Hawkscope");
        new Thread(new Runnable() {
            public void run() {
                try {
                    log.info("Twitter ok: " + twitter.test());
                } catch (TwitterException e) {
                    log.warn("Twitter error", e);
                }
            }
        }).start();
    }

    @Override
    public void enhanceSettings(TabFolder folder,
            List<AbstractSettingsTabItem> tabItems) {
        settings = new TwitterSettingsTabItem(folder);
        tabItems.add(settings);
    }

    @Override
    public void beforeQuickAccess(MainMenu mainMenu) {
        final ExecutableMenuItem item = MenuFactory.newExecutableMenuItem();
        item.setText("Tweet!");
        item.setIcon(icon);
        item.setCommand(new Command() {
            public void execute() {
                InputDialog.open("Tweet:", 140, new Shell(), new Updater() {
                    public void setValue(String value) {
                        try {
                            twitter.update(value);
                        } catch (TwitterException e) {
                            throw new RuntimeException("Failed tweeting :(", e);
                        }
                    }
                });
            }
        });
        mainMenu.addMenuItem(new com.varaneckas.hawkscope.menu.MenuItem() {
            public void createMenuItem(Menu parent) {
                MenuItem m = new MenuItem(parent, SWT.CASCADE);
                m.setText("Twitter");
                m.setImage(icon);
                Menu menu = new Menu(parent);
                m.setMenu(menu);
                item.createMenuItem(menu);
                try {
                    MenuItem replies = new MenuItem(menu, SWT.CASCADE);
                    replies.setImage(icon);
                    replies.setText("Replies");
                    Menu repMenu = new Menu(parent);
                    replies.setMenu(repMenu);
                    listReplies(repMenu);
                    listFollowing(parent, menu);
                    listFollowers(parent, menu);
                } catch (final Exception e) {

                }
            }

            private void listFollowing(Menu parent, Menu menu)
                    throws TwitterException {
                MenuItem following = new MenuItem(menu, SWT.CASCADE);
                following.setImage(icon);
                following.setText("Following");
                Menu folMenu = new Menu(parent);
                following.setMenu(folMenu);
                for (final User followee : twitter.getFriends()) {
                    MenuItem mi = new MenuItem(folMenu, SWT.PUSH);
                    mi.setText(followee.getName());
                    mi.setImage(icon);
                    mi.addSelectionListener(new SelectionListener() {
                        public void widgetDefaultSelected(
                                SelectionEvent selectionevent) {
                            widgetSelected(selectionevent);
                        }

                        public void widgetSelected(
                                SelectionEvent selectionevent) {
                            Program.launch(twitter.getBaseURL()
                                    + followee.getId());
                        }
                    });
                }
            }
            
            private void listFollowers(Menu parent, Menu menu)
            throws TwitterException {
        MenuItem following = new MenuItem(menu, SWT.CASCADE);
        following.setImage(icon);
        following.setText("Followers");
        Menu folMenu = new Menu(parent);
        following.setMenu(folMenu);
        for (final User followee : twitter.getFollowers()) {
            MenuItem mi = new MenuItem(folMenu, SWT.PUSH);
            mi.setText(followee.getName());
            mi.setImage(icon);
            mi.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(
                        SelectionEvent selectionevent) {
                    widgetSelected(selectionevent);
                }

                public void widgetSelected(
                        SelectionEvent selectionevent) {
                    Program.launch(twitter.getBaseURL()
                            + followee.getId());
                }
            });
        }
    }            

            private void listReplies(Menu repMenu) throws TwitterException {
                for (final Status reply : twitter.getReplies()) {
                    MenuItem mi = new MenuItem(repMenu, SWT.PUSH);
                    mi.setText(reply.getUser().getName() + ": "
                            + reply.getText().replaceAll("\\n", ""));
                    mi.setImage(icon);
                    mi.addSelectionListener(new SelectionListener() {
                        public void widgetDefaultSelected(
                                SelectionEvent selectionevent) {
                            widgetSelected(selectionevent);
                        }

                        public void widgetSelected(
                                SelectionEvent selectionevent) {
                            Program.launch(twitter.getBaseURL()
                                    + +reply.getUser().getId());
                        }
                    });
                }
            }

            public void setEnabled(boolean enabled) {
            }

            public void setIcon(Image icon) {
            }

            public void setText(String text) {
            }
        });

        mainMenu.addSeparator();
    }

    public String getDescription() {
        return "Lets you tweet in Hawkscope";
    }

    public String getName() {
        return "Twitter";
    }

    public String getVersion() {
        return "1.0";
    }

}
