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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.util.IconFactory;

/**
 * Twitter4j based Twitter client with simple request cache to prevent throttling
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class TwitterClient {
	
	/**
	 * Logger
	 */
	private static final Log log = LogFactory.getLog(TwitterClient.class);
	
	/**
	 * Two seconds cache. Ends up with 60 / 2 * 3 = 90 max requests per hour.
	 */
	private static final int CACHE_SECONDS = 120; 

	/**
	 * Twitter4J instance
	 */
	private final Twitter twitter4j;
	
	/**
	 * Status list cache
	 */
	private final Map<Short, List<Status>> statusListCache;
	
	/**
	 * Request times
	 */
	private final Map<Short, Long> requestTimes;
	
	/**
	 * Friends timeline flag
	 */
	private static final short FRIENDS_TIMELINE = 0;
	
	/**
	 * User timeline flag
	 */ 
	private static final short USER_TIMELINE = 1;
	
	/**
	 * Replies timeline flag
	 */
	private static final short REPLIES = 2;
	
	private final Map<String, Image> userImages = new HashMap<String, Image>();
	
	/**
	 * Constructor that wraps up Twitter4J client
	 * @param twitter4j
	 */
	public TwitterClient(final Twitter twitter4j) {
		statusListCache = new HashMap<Short, List<Status>>();
		requestTimes = new HashMap<Short, Long>();
		this.twitter4j = twitter4j;
	}
	
	/**
	 * Tests the client
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public boolean test() throws TwitterException {
		return twitter4j.test();
		
	}
	
	/**
	 * Tells if cache is expired for certain status type
	 * 
	 * @param type
	 * @return
	 */
	private boolean cacheExpired(final short type) {
		if (!requestTimes.containsKey(type)) {
			if (log.isDebugEnabled()) {
				log.debug("Cache not found for type: " + type);
			}
			return true;
		}
		return System.currentTimeMillis() - requestTimes.get(type) > 
				CACHE_SECONDS * 1000;
	}
	
	/**
	 * Updates last call time for certain status type
	 * 
	 * @param type
	 */
	private void updateLastCall(final short type) {
		if (log.isDebugEnabled()) {
			log.debug("Updating last call for type: " + type);
		}
		requestTimes.put(type, System.currentTimeMillis());
	}
	
	/**
	 * Gets friends timeline (last 20 entries)
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public List<Status> getFriendsTimeline() throws TwitterException {
		if (cacheExpired(FRIENDS_TIMELINE)) {
			updateLastCall(FRIENDS_TIMELINE);
			statusListCache.put(FRIENDS_TIMELINE, twitter4j.getFriendsTimeline());
		}
		return statusListCache.get(FRIENDS_TIMELINE);
	}
	
	/**
	 * Gets replies (last 20 entries)
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public List<Status> getReplies() throws TwitterException {
		if (cacheExpired(REPLIES)) {
			updateLastCall(REPLIES);
			statusListCache.put(REPLIES, twitter4j.getReplies());
		}
		return statusListCache.get(REPLIES);
	}
	
	/**
	 * Gets user timeline
	 * 
	 * @param pageSize
	 * @return
	 * @throws TwitterException
	 */
	public List<Status> getUserTimeline(final int pageSize) throws TwitterException {
		if (cacheExpired(USER_TIMELINE)) {
			updateLastCall(USER_TIMELINE);
			statusListCache.put(USER_TIMELINE, 
					twitter4j.getUserTimeline(twitter4j.getUserId(), pageSize));
		}
		return statusListCache.get(USER_TIMELINE);
	}
	
	/**
	 * Updates your twitter status
	 * 
	 * @param message
	 * @return
	 * @throws TwitterException
	 */
	public Status update(final String message) throws TwitterException {
		return twitter4j.update(message);
	}
	
	/**
	 * Gets base twitter service URL
	 * 
	 * @return
	 */
	public String getBaseURL() {
		return twitter4j.getBaseURL();
	}

	public Image getUserImage(final User user) {
		if (!userImages.containsKey(user.getName())) {
			Configuration cfg = ConfigurationFactory.getConfigurationFactory()
				.getConfiguration();
			Image i;
			try {
			if (!cfg.isHttpProxyInUse()) {
				i = new Image(Display.getCurrent(), user.getProfileImageURL().openStream());
			} else {
				Proxy p = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(
						cfg.getHttpProxyHost(), cfg.getHttpProxyPort()));
//				i = new Image(Display.getCurrent(), user.get)
				//FIXME todo continue
			}
			} catch (final IOException e) {
				log.warn("Failed getting user icon: " + user.getName(), e);
				i = IconFactory.getInstance().getPluginIcon("twitter"
						, getClass().getClassLoader());
			}
		} 
		return null;
	}
}
