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
import twitter4j.User;

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

public class TwitterPlugin extends PluginAdapter {

	private TwitterSettingsTabItem settings;

	protected static final String PROP_TWITTER_USER = "plugins.twitter.user";

	protected static final String PROP_TWITTER_PASS = "plugins.twitter.pass";
	
	protected static final String PROP_TWITTER_SHOW_MY = "plugins.twitter.show.my";

	protected static final String PROP_TWITTER_SHOW_RE = "plugins.twitter.show.replies";

	protected static final String PROP_TWITTER_SHOW_FRIENDS = "plugins.twitter.show.friends";
	
	protected static final String PROP_TWITTER_CACHE = "plugins.twitter.cache.lifetime";
	
	private static final int PAGE_SIZE = 20;
	
	private Twitter twitter;

	private String twitterError = null;

	private TwitterMenuItem twitterMenu;
	
	private long lastUpdate = System.currentTimeMillis();

	private static TwitterPlugin instance;

	public static TwitterPlugin getInstance() {
		if (instance == null) {
			instance = new TwitterPlugin();
		}
		return instance;
	}

	private String user;

	private String pass;

	private long cache;
	
	private boolean showMy;
	
	private boolean showReplies;
	
	private boolean showFriends;
	
	private Menu menuMy;
	
	private Menu menuReplies;
	
	private Menu menuFriends;
	
	private TwitterPlugin() {
		canHookBeforeQuickAccessList = true;
		refresh();
	}

	protected Image getTwitterIcon() {
		return IconFactory.getInstance().getPluginIcon("twitter24.png",
				getClass().getClassLoader());
	}
	
	private String getTwitterError() {
        if (twitterError.length() > 40) {
            return twitterError.substring(0, 39);
        }
        return twitterError;
	}

	public void refresh() {
		Configuration cfg = ConfigurationFactory.getConfigurationFactory()
				.getConfiguration();
		try {
			twitterError = null;
			user = cfg.getProperties().get(PROP_TWITTER_USER);
			pass = cfg.getPasswordProperty(PROP_TWITTER_PASS);
			cache = Long.valueOf(cfg.getProperties().get(PROP_TWITTER_CACHE));
			showMy = cfg.getProperties().get(PROP_TWITTER_SHOW_MY).equals("1");
			showReplies = cfg.getProperties().get(PROP_TWITTER_SHOW_RE).equals("1");		
			showFriends = cfg.getProperties().get(PROP_TWITTER_SHOW_FRIENDS).equals("1");
			createTwitter(cfg);
	 	} catch (final Exception e) {
	 		twitterError = "No configuration, please visit Settings -> Twitter";
	 		log.warn("Twitter is not configured");
	 	}
	}

	private boolean createTwitter(Configuration cfg) {
	    if (user == null || user.equals("")) {
	        twitterError = "No User/Pass. Please configure.";
	        return false;
	    }
		twitter = new Twitter(user, pass);
		twitter.setSource("Hawkscope");
		if (cfg.isHttpProxyInUse()) {
			twitter.setHttpProxy(cfg.getHttpProxyHost(), cfg.getHttpProxyPort());
			if (cfg.isHttpProxyAuthInUse()) {
				twitter.setHttpProxyAuth(cfg.getHttpProxyAuthUsername(), 
						cfg.getHttpProxyAuthPassword());
			}
		}
		try {
		    return twitter.test();
		} catch (Exception e) {
		    twitterError = e.getMessage();
		    return false;
		}
	}

	@Override
	public void enhanceSettings(TabFolder folder,
			List<AbstractSettingsTabItem> tabItems) {
		settings = new TwitterSettingsTabItem(folder);
		tabItems.add(settings);
	}

	@Override
	public void beforeQuickAccess(MainMenu mainMenu) {

		twitterMenu = new TwitterMenuItem();
		twitterMenu.setText("Twitter");
		twitterMenu.setIcon(getTwitterIcon());
		mainMenu.addMenuItem(twitterMenu);

		if (twitterError != null) {
			twitterMenu.getSwtMenuItem().setText("Twitter :( " + getTwitterError());
			return;
		}
		createTweetItem();

		new Thread(new Runnable() {
			public void run() {
				try {
					loadData();
				} catch (final TwitterException e) {
					twitterError = e.getMessage();
					log.warn("Twitter error: " + getTwitterError(), e);
				}
			}
		}).start();
		mainMenu.addSeparator();
	}

	private void loadData() throws TwitterException {
		createMyTweets();
		createReplies();
		createFriendsTweets();
	}

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
							listMessages(menuFriends, twitter.getFriendsTimelineByPage(1));
						} catch (final TwitterException e) {
							twitterError = e.getMessage();
						}
					}
				}).start();
			}
		});
	}

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
							listMessages(menuReplies, twitter.getRepliesByPage(1));
						} catch (final TwitterException e) {
							twitterError = e.getMessage();
						}
					}
				}).start();
			}
		});
	}

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
							listMessages(menuMy, twitter.getUserTimeline(twitter.getUserId(), PAGE_SIZE));
						} catch (final TwitterException e) {
							twitterError = e.getMessage();
						}
					}
				}).start();
			}
		});
	}

	private void addUserList(final String name, final List<User> users) {
		twitterMenu.getSwtMenuItem().getDisplay().syncExec(new Runnable() {
			public void run() {
				MenuItem following = new MenuItem(twitterMenu.getSwtMenuItem()
						.getMenu(), SWT.CASCADE);
				following.setImage(getTwitterIcon());
				following.setText(name);
				Menu folMenu = new Menu(twitterMenu.getSwtMenuItem().getMenu());
				following.setMenu(folMenu);
				listUsers(folMenu, users);
			}
		});
	}

	private void createTweetItem() {
		ExecutableMenuItem tweet = MenuFactory.newExecutableMenuItem();
		tweet.setText("Tweet!");
		tweet.setIcon(IconFactory.getInstance().getPluginIcon("twitter24.png",
				getClass().getClassLoader()));
		tweet.setCommand(new Command() {
			public void execute() {
				new TwitterDialog(new Updater() {
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
		tweet.createMenuItem(twitterMenu.getSwtMenuItem().getMenu());
	}

	private void listMessages(final Menu repMenu, final List<Status> messages) {
		twitterMenu.getSwtMenuItem().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					for (final Status reply : messages) {
						String msg = reply.getUser().getName() + ": "
						+ reply.getText().replaceAll("\\n", "");
						MenuItem mi = new MenuItem(repMenu, SWT.PUSH);
						mi.setText(msg);
						mi.setImage(getTwitterIcon());
						mi.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(
									SelectionEvent selectionevent) {
								widgetSelected(selectionevent);
							}
							public void widgetSelected(SelectionEvent selectionevent) {
								Program.launch(twitter.getBaseURL()
										+ reply.getUser().getName());
							}
						});
					}
				} catch (final Exception e) {
					log.warn("Failed listing replies", e);
				}
			}
		});
	}

	private void listUsers(Menu folMenu, List<User> users) {
		for (final User followee : users) {
			try {
				MenuItem mi = new MenuItem(folMenu, SWT.CASCADE);
				mi.setText(followee.getName());
				mi.setImage(getTwitterIcon());
				Menu msgsMenu = new Menu(mi);
				mi.setMenu(msgsMenu);
				listMessages(msgsMenu, twitter.getUserTimeline(followee.getName(), 
						PAGE_SIZE));
				mi.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(
							SelectionEvent selectionevent) {
						widgetSelected(selectionevent);
					}

					public void widgetSelected(SelectionEvent selectionevent) {
						Program.launch(twitter.getBaseURL()
								+ followee.getName());
					}
				});
			} catch (final Exception e) {
				log.warn("Failed listing user", e);
			}
		}
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
