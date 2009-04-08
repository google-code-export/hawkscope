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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.command.Command;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;
import com.varaneckas.hawkscope.menu.ExecutableMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.menu.MenuFactory;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;
import com.varaneckas.hawkscope.util.Updater;

/**
 * Twitter plugin for Hawkscope
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class TwitterPlugin extends PluginAdapter {

	/**
	 * Twitter Settings tab item
	 */
	private TwitterSettingsTabItem settings;

	/**
	 * Twitter user property name
	 */
	protected static final String PROP_TWITTER_USER = "plugins.twitter.user";

	/**
	 * Twitter password property name
	 */
	protected static final String PROP_TWITTER_PASS = "plugins.twitter.pass";
	
	/**
	 * Show my tweets property name
	 */
	protected static final String PROP_TWITTER_SHOW_MY = "plugins.twitter.show.my";

	/**
	 * Show replies property name 
	 */
	protected static final String PROP_TWITTER_SHOW_RE = "plugins.twitter.show.replies";

	/**
	 * Show friends tweets property name
	 */
	protected static final String PROP_TWITTER_SHOW_FRIENDS = "plugins.twitter.show.friends";
	
	/**
	 * Twitter cache lifetime property name
	 */
	protected static final String PROP_TWITTER_CACHE = "plugins.twitter.cache.lifetime";
	
	/**
	 * Page size for listing data
	 */
	private static final int PAGE_SIZE = 20;
	
	/**
	 * Twitter4j object
	 */
	private TwitterClient twitter;

	/**
	 * Twitter error
	 */
	private String twitterError = null;

	/**
	 * Twitter hawkscope menu item
	 */
	private TwitterMenuItem twitterMenu;
	
	/**
	 * Singleton instance of this plugin
	 */
	private static TwitterPlugin instance;

	/**
	 * Singleton instance getter
	 * 
	 * @return
	 */
	public static TwitterPlugin getInstance() {
		if (instance == null) {
			instance = new TwitterPlugin();
		}
		return instance;
	}

	/**
	 * Twitter user
	 */
	private String user;

	/**
	 * Twitter password
	 */
	private String pass;

	/**
	 * Should "My Tweets" item be shown?
	 */
	private boolean showMy;
	
	/**
	 * Should "Replies" item be shown?
	 */
	private boolean showReplies;
	
	/**
	 * Should "Friends Tweets" item be shown?
	 */
	private boolean showFriends;
	
	/**
	 * My Tweets menu
	 */
	private Menu menuMy;
	
	/**
	 * Replies menu
	 */
	private Menu menuReplies;
	
	/**
	 * Friends Tweets menu
	 */
	private Menu menuFriends;
	
	/**
	 * Private singleton constructor
	 */
	private TwitterPlugin() {
		canHookBeforeQuickAccessList = true;
		refresh();
	}

	/**
	 * Gets twitter icon
	 * 
	 * @return
	 */
	protected Image getTwitterIcon() {
		return IconFactory.getInstance().getPluginIcon("twitter24.png",
				getClass().getClassLoader());
	}
	
	/**
	 * Gets a trimmed twitter error message for displaying in menu item
	 * 
	 * @return
	 */
	private String getTwitterError() {
        if (twitterError.length() > 40) {
            return twitterError.substring(0, 39);
        }
        return twitterError;
	}

	/**
	 * Refreshes the plugin settings
	 */
	public void refresh() {
		Configuration cfg = ConfigurationFactory.getConfigurationFactory()
				.getConfiguration();
		try {
			twitterError = null;
			user = cfg.getProperties().get(PROP_TWITTER_USER);
			pass = cfg.getPasswordProperty(PROP_TWITTER_PASS);
			showMy = cfg.getProperties().get(PROP_TWITTER_SHOW_MY)
					.equals("1");
			showReplies = cfg.getProperties().get(PROP_TWITTER_SHOW_RE)
					.equals("1");		
			showFriends = cfg.getProperties().get(PROP_TWITTER_SHOW_FRIENDS)
					.equals("1");
			createTwitter(cfg);
	 	} catch (final Exception e) {
	 		twitterError = "No configuration, please visit Settings -> Twitter";
	 		log.warn("Twitter is not configured");
	 	}
	}

	/**
	 * Creates twitter4j object
	 * 
	 * @param cfg
	 * @return
	 */
	private boolean createTwitter(final Configuration cfg) {
	    if (user == null || user.equals("") || pass == null || pass.equals("")) {
	        twitterError = "No User/Pass. Please configure.";
	        return false;
	    }
		final Twitter twitter4j = new Twitter(user, pass);
		twitter4j.setHttpConnectionTimeout(30000);
		twitter4j.setSource("Hawkscope");
		if (cfg.isHttpProxyInUse()) {
			twitter4j.setHttpProxy(cfg.getHttpProxyHost(), cfg.getHttpProxyPort());
			if (cfg.isHttpProxyAuthInUse()) {
				twitter4j.setHttpProxyAuth(cfg.getHttpProxyAuthUsername(), 
						cfg.getHttpProxyAuthPassword());
			}
		}
		try {
			twitter = new TwitterClient(twitter4j);
		    return twitter.test();
		} catch (final Exception e) {
		    twitterError = "Please check configuration.";
		    return false;
		}
	}

	@Override
	public void enhanceSettings(final TabFolder folder,
			final List<AbstractSettingsTabItem> tabItems) {
		settings = new TwitterSettingsTabItem(folder);
		tabItems.add(settings);
	}

	@Override
	public void beforeQuickAccess(final MainMenu mainMenu) {
		twitterMenu = new TwitterMenuItem();
		twitterMenu.setText("Twitter");
		twitterMenu.setIcon(getTwitterIcon());
		mainMenu.addMenuItem(twitterMenu);

		if (twitterError != null) {
			twitterMenu.getSwtMenuItem().setText("Twitter :( " 
					+ getTwitterError());
			twitterMenu.getSwtMenuItem().setEnabled(false);
			twitterMenu.getSwtMenuItem().setMenu(null);
			mainMenu.addSeparator();
			return;
		}
		createTweetItem();

		new Thread(new Runnable() {
			public void run() {
				try {
					loadData();
				} catch (final TwitterException e) {
					twitterError = "Please check configuration.";
					log.warn("Twitter error: " + getTwitterError(), e);
				}
			}
		}).start();
		mainMenu.addSeparator();
	}

	/**
	 * Loads the twitter menu data
	 * 
	 * @throws TwitterException
	 */
	private void loadData() throws TwitterException {
		if (showMy) createMyTweets();
		if (showReplies) createReplies();
		if (showFriends) createFriendsTweets();
	}

	/**
	 * Creates friends tweets menu item
	 * 
	 * @throws TwitterException
	 */
	private void createFriendsTweets() throws TwitterException {
		// Friends Tweets
		twitterMenu.getSwtMenuItem().getDisplay().syncExec(new Runnable() {
			public void run() {
				MenuItem friendTw = new MenuItem(
						twitterMenu.getSwtMenuItem().getMenu(), SWT.CASCADE);
				friendTw.setImage(getTwitterIcon());
				friendTw.setText("Friend Tweets");
				menuFriends = new Menu(friendTw);
				friendTw.setMenu(menuFriends);
				new Thread(new Runnable() {
					public void run() {
						try {
							listMessages(menuFriends, twitter.getFriendsTimeline());
						} catch (final TwitterException e) {
							handleTwitterException(e);
						}
					}
				}).start();
			}
		});
	}

	/**
	 * Creates replies menu item
	 * 
	 * @throws TwitterException
	 */
	private void createReplies() throws TwitterException {
		twitterMenu.getSwtMenuItem().getDisplay().syncExec(new Runnable() {
			public void run() {
				// Replies
				MenuItem replies = new MenuItem(twitterMenu.getSwtMenuItem().getMenu(),
						SWT.CASCADE);
				replies.setImage(getTwitterIcon());
				replies.setText("Replies");
				menuReplies = new Menu(replies);
				replies.setMenu(menuReplies);
				new Thread(new Runnable() {
					public void run() {
						try {
							listMessages(menuReplies, twitter.getReplies());
						} catch (final TwitterException e) {
							handleTwitterException(e);
						}
					}
				}).start();
			}
		});
	}

	/**
	 * Creates my tweets menu items
	 * 
	 * @throws TwitterException
	 */
	private void createMyTweets() throws TwitterException {
		twitterMenu.getSwtMenuItem().getDisplay().syncExec(new Runnable() {
			public void run() {
				// My Tweets
				MenuItem timeline = new MenuItem(
						twitterMenu.getSwtMenuItem().getMenu(), SWT.CASCADE);
				timeline.setImage(getTwitterIcon());
				timeline.setText("My Tweets");
				menuMy = new Menu(twitterMenu.getSwtMenuItem().getMenu());
				timeline.setMenu(menuMy);
				new Thread(new Runnable() {
					public void run() {
						try {
							listMessages(menuMy, twitter.getUserTimeline(PAGE_SIZE));
						} catch (final TwitterException e) {
							handleTwitterException(e);
						}
					}
				}).start();
			}
		});
	}

	/**
	 * Creates tweet menu item
	 */
	private void createTweetItem() {
		ExecutableMenuItem tweet = MenuFactory.newExecutableMenuItem();
		tweet.setText("Tweet!");
		tweet.setIcon(twitter.getUserImage(twitter.getCurrentUser()));
		tweet.setCommand(new Command() {
			public void execute() {
				new TwitterDialog(new Updater() {
					public void setValue(final String value) {
						try {
							twitter.update(value);
						} catch (TwitterException e) {
							throw new RuntimeException("Failed tweeting :(", e);
						}
					}
				});
			}
		});
		tweet.createMenuItem(twitterMenu.getSwtMenuItem().getMenu());
	}

	/**
	 * Lists twitter messages in a menu
	 * 
	 * @param repMenu
	 * @param messages
	 */
	private void listMessages(final Menu repMenu, final List<Status> messages) {
		twitterMenu.getSwtMenuItem().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					for (final Status reply : messages) {
						String msg = reply.getUser().getName().concat(": ")
								.concat(reply.getText().replaceAll("\\n", " "));
						MenuItem mi = new MenuItem(repMenu, SWT.PUSH);
						if (msg.length() > 80) {
							msg = msg.substring(0, 79).concat("...");
						}
						mi.setText(msg);
						mi.setImage(twitter.getUserImage(reply.getUser()));
						mi.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(
									SelectionEvent selectionevent) {
								widgetSelected(selectionevent);
							}
							public void widgetSelected(SelectionEvent selectionevent) {
								Program.launch(twitter.getBaseURL()
										+ reply.getUser().getScreenName() 
										+ "/status/" 
										+ reply.getId());
							}
						});
					}
				} catch (final Exception e) {
					log.warn("Failed listing replies", e);
				}
			}
		});
	}

	public String getDescription() {
		return "Lets you tweet in Hawkscope.";
	}

	public String getName() {
		return "Twitter";
	}

	public String getVersion() {
		return "1.4";
	}

	/**
	 * Handles {@link TwitterException}.
	 * 
	 * @param e
	 */
	private void handleTwitterException(final TwitterException e) {
		if (e.getMessage().startsWith("Server")) {
			twitterError = "Server error";
		} else {
			twitterError = "Network error";
		}
		log.warn("Twitter error", e);
		createTwitter(ConfigurationFactory.getConfigurationFactory()
				.getConfiguration());
	}

}
