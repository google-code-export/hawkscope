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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
	
	private Twitter twitter;

	private String twitterError = null;

	private TwitterMenuItem twitterMenu;

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
		refresh();
	}

	protected Image getTwitterIcon() {
		return IconFactory.getInstance().getPluginIcon("twitter24.png",
				getClass().getClassLoader());
	}

	public void refresh() {
		Configuration cfg = ConfigurationFactory.getConfigurationFactory()
				.getConfiguration();
		user = cfg.getProperties().get(PROP_TWITTER_USER);
		pass = cfg.getPasswordProperty(PROP_TWITTER_PASS);
		twitter = new Twitter(user, pass);
		twitter.setSource("Hawkscope");
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					if (!twitter.test()) {
						twitterError = "Please check settings.";
					} else {
						log.info("Twitter ok: " + twitter.test());
					}
				} catch (TwitterException e) {
					log.warn("Twitter error", e);
				}
			}
		});
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
			twitterMenu.setText("Twitter :( " + twitterError);
			return;
		}
		createTweetItem();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					loadData();
				} catch (final TwitterException e) {
					twitterError = e.getMessage();
					log.warn("Twitter error", e);
				}
			}
		});
	}

	private void loadData() throws TwitterException {
		// My Tweets
		MenuItem timeline = new MenuItem(
				twitterMenu.getSwtMenuItem().getMenu(), SWT.CASCADE);
		timeline.setImage(getTwitterIcon());
		timeline.setText("My Tweets");
		Menu timeMenu = new Menu(twitterMenu.getSwtMenuItem().getMenu());
		timeline.setMenu(timeMenu);
		listMessages(timeMenu, twitter.getUserTimeline(twitter.getUserId(), 10));

		// Replies
		MenuItem replies = new MenuItem(twitterMenu.getSwtMenuItem().getMenu(),
				SWT.CASCADE);
		replies.setImage(getTwitterIcon());
		replies.setText("Replies");
		Menu repMenu = new Menu(replies);
		replies.setMenu(repMenu);
		listMessages(repMenu, twitter.getRepliesByPage(1));

		// Friends Tweets
		MenuItem friendTw = new MenuItem(
				twitterMenu.getSwtMenuItem().getMenu(), SWT.CASCADE);
		friendTw.setImage(getTwitterIcon());
		friendTw.setText("Friend Tweets");
		Menu frtwMenu = new Menu(friendTw);
		friendTw.setMenu(frtwMenu);
		listMessages(frtwMenu, twitter.getFriendsTimelineByPage(1));
	}

	private void addUserList(String name, List<User> users) {
		MenuItem following = new MenuItem(twitterMenu.getSwtMenuItem()
				.getMenu(), SWT.CASCADE);
		following.setImage(getTwitterIcon());
		following.setText(name);
		Menu folMenu = new Menu(twitterMenu.getSwtMenuItem().getMenu());
		following.setMenu(folMenu);
		listUsers(folMenu, users);

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

	private void listMessages(Menu repMenu, List<Status> messages) {
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

	private void listUsers(Menu folMenu, List<User> users) {
		for (final User followee : users) {
			try {
				MenuItem mi = new MenuItem(folMenu, SWT.CASCADE);
				mi.setText(followee.getName());
				mi.setImage(getTwitterIcon());
				Menu msgsMenu = new Menu(mi);
				mi.setMenu(msgsMenu);
				listMessages(msgsMenu, twitter.getUserTimeline(followee.getName(), 10));
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
