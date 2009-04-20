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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;
import com.varaneckas.hawkscope.menu.AbstractMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;
import com.varaneckas.hawkscope.util.OSUtils;
import com.varaneckas.hawkscope.util.OSUtils.OS;

import del.icio.us.beans.Post;
import del.icio.us.beans.Tag;

/**
 * Delicious.com Bookmarks Plugin
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class DeliciousPlugin extends PluginAdapter {
    
    /**
     * Gmail username property
     */
    public static final String PROP_USER = "plugins.delicious.user";
    
    /**
     * Gmail password property
     */
    public static final String PROP_PASS = "plugins.delicious.pass";
    
    /**
     * Delicious.com client
     */
    private DeliciousClient client;
    
    /**
     * Delicious Hawkscope menu item
     */
    private AbstractMenuItem deliciousItem; 
    
    /**
     * Delicious hawkscope settings tab item
     */
    private DeliciousSettingsTabItem settings;
    
    public DeliciousPlugin() {
        canHookBeforeQuickAccessList = true;
        refresh();
    }
    
    @Override
    public void enhanceSettings(TabFolder folder,
            List<AbstractSettingsTabItem> tabItems) {
        settings = new DeliciousSettingsTabItem(this, folder);
        tabItems.add(settings);
    }
    
    /**
     * Refreshes the plugin data
     */
    public void refresh() {
        final Configuration cfg = ConfigurationFactory.getConfigurationFactory()
            .getConfiguration();
        client = DeliciousClient.getInstance();
        client.login(cfg.getProperties().get(PROP_USER), 
                cfg.getPasswordProperty(PROP_PASS));
        if (client.getDelicious() != null) {
            if (cfg.isHttpProxyInUse()) {
                client.getDelicious().setProxyConfiguration(cfg.getHttpProxyHost(), 
                        cfg.getHttpProxyPort());
                if (cfg.isHttpProxyAuthInUse()) {
                    client.getDelicious().setProxyAuthenticationConfiguration(
                            cfg.getHttpProxyAuthUsername(), 
                            cfg.getHttpProxyAuthPassword());
                }
            }
            new Thread(new Runnable() {
                public void run() {
                    client.update();
                }
            }).start();
        }
    }

    @Override
    public void beforeQuickAccess(final MainMenu mainMenu) {
        deliciousItem = createDeliciousItem();
        deliciousItem.setText("Delicious");
        deliciousItem.setIcon(getDeliciousIcon());
        mainMenu.addMenuItem(deliciousItem);
        mainMenu.addSeparator();
        if (client.getDelicious() == null) {
            deliciousItem.getSwtMenuItem().setText("Delicious :( No User/Pass");
            deliciousItem.getSwtMenuItem().setEnabled(false);
            return;
        }
//        createPostBookmarkItem();
        new Thread(new Runnable() {
            public void run() {
                client.update();
                loadData();
            }
        }).start();
    }
    
    /**
     * Loads My Bookmarks and My Tags items (async)
     */
    protected void loadData() {
        createMyBookmarks();
        createMyTags();
    }

    /**
     * Creates My Tags item with lazy loaders
     */
    private void createMyTags() {
        deliciousItem.getSwtMenuItem().getDisplay().asyncExec(new Runnable() {
            public void run() {
                MenuItem myTags = new MenuItem(
                        deliciousItem.getSwtMenuItem().getMenu(), SWT.CASCADE);
                myTags.setImage(getDeliciousIcon());
                myTags.setText("My Tags");
                final Menu menuMy = new Menu(myTags);
                myTags.setMenu(menuMy);
                new Thread(new Runnable() {
                    public void run() {
                        final List<Tag> tags = client.getTags();
                        deliciousItem.getSwtMenuItem().getDisplay()
                                .asyncExec(new Runnable() {
                            public void run() {
                                loadTags(menuMy, tags);
                            }
                        });
                    }
                }).start();
            }
        });
    }
    
    /**
     * Loads tags into menu
     * 
     * @param targetMenu menu
     * @param tags list of {@link Tag}
     */
    private void loadTags(Menu targetMenu, List<Tag> tags) {
        for (final Tag t : tags) {
            try {
                log.debug("Adding tag: " + t);
                String msg = t.getTag() + " (" + t.getCount() + ")";
                final MenuItem mi = new MenuItem(targetMenu, SWT.CASCADE);
                mi.setText(msg);
                mi.setImage(getDeliciousIcon());
                final Menu tagPosts = new Menu(mi);
                mi.setMenu(tagPosts);
                if (client.hasPosts(t.getTag())) {
                    log.debug("Posts for tag already loaded: " + t.getTag());
                    loadPosts(tagPosts, client.getPosts(t.getTag()));
                    continue;
                } 
                final MenuItem loader = new MenuItem(tagPosts, SWT.PUSH);
                loader.setImage(getDeliciousIcon());
                loader.setText("Move over to load items");
                loader.addArmListener(new ArmListener() {
                    public void widgetArmed(ArmEvent event) {
                        if (!OSUtils.CURRENT_OS.equals(OS.UNIX)) {
                            loader.dispose();
                        } else {
                            loader.removeArmListener(this);
                            loader.setText("Loaded");
                            loader.setEnabled(false);
                        }
                        log.debug("Loading: " + t.getTag());
                        loadPosts(tagPosts, client.getPosts(t.getTag()));
                        log.debug("Loaded");
                    }
                });
            } catch (final Exception e) {
                log.error("Failed adding bookmark", e);
            }
        }
    }

    /**
     * Creates My Bookmarks item
     */
    private void createMyBookmarks() {
        deliciousItem.getSwtMenuItem().getDisplay().asyncExec(new Runnable() {
            public void run() {
                // My Tweets
                MenuItem myBookmarks = new MenuItem(
                        deliciousItem.getSwtMenuItem().getMenu(), SWT.CASCADE);
                myBookmarks.setImage(getDeliciousIcon());
                myBookmarks.setText("My Bookmarks");
                final Menu menuMy = new Menu(myBookmarks);
                myBookmarks.setMenu(menuMy);
                new Thread(new Runnable() {
                    public void run() {
                        final List<Post> posts = client.getPosts();
                        deliciousItem.getSwtMenuItem().getDisplay()
                                .asyncExec(new Runnable() {
                            public void run() {
                                loadPosts(menuMy, posts);
                            }
                        });
                    }
                }).start();
            }
        });
    }

//    private void createPostBookmarkItem() {
//        ExecutableMenuItem post = MenuFactory.newExecutableMenuItem();
//        post.setText("Post new bookmark");
//        post.setIcon(getDeliciousIcon());
//        post.setCommand(new Command() {
//            public void execute() {
//            }
//        });
//        post.createMenuItem(deliciousItem.getSwtMenuItem().getMenu());
//    }

    /**
     * Creates Hawkscope Delicious menu item
     */
    private AbstractMenuItem createDeliciousItem() {
        return new AbstractMenuItem() {
            public void createMenuItem(final Menu parent) {
                this.swtMenuItem = new MenuItem(parent, SWT.CASCADE);
                this.swtMenuItem.setEnabled(this.enabled);
                this.swtMenuItem.setText(this.text);
                this.swtMenuItem.setImage(this.icon);
                final Menu menu = new Menu(this.swtMenuItem);
                this.swtMenuItem.setMenu(menu);
            }
        };
    }

    /**
     * Gets Delicious icon
     * 
     * @return icon
     */
    private Image getDeliciousIcon() {
        return IconFactory.getInstance()
            .getPluginIcon("delicious24.png", getClass().getClassLoader());
    }

    public String getDescription() {
        return "Delicious.com bookmarks at your service";
    }

    public String getName() {
        return "Delicious";
    }

    public String getVersion() {
        return "1.1";
    }

    /**
     * Loads a list of posts into menu
     * 
     * @param targetMenu target menu
     * @param posts list of posts to be loaded
     */
    private void loadPosts(final Menu targetMenu, final List<Post> posts) {
        for (final Post p : posts) {
            try {
                log.debug("Adding bookmark: " + p);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String msg = df.format(p.getTimeAsDate()).concat(": ")
                    .concat(p.getDescription());
                MenuItem mi = new MenuItem(targetMenu, SWT.PUSH);
                if (msg.length() > 80) {
                    msg = msg.substring(0, 79).concat("...");
                }
                mi.setText(msg);
                mi.setImage(getDeliciousIcon());
                mi.addSelectionListener(new SelectionListener() {
                    public void widgetDefaultSelected(
                            SelectionEvent selectionevent) {
                        widgetSelected(selectionevent);
                    }
                    public void widgetSelected(SelectionEvent selectionevent) {
                        Program.launch(p.getHref());
                    }
                });
            } catch (final Exception e) {
                log.error("Failed adding bookmark", e);
            }
        }
    }

}
