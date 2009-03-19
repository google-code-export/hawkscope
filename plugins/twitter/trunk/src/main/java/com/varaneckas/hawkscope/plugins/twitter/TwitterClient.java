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
import java.net.URLConnection;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import twitter4j.RateLimitStatus;
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
	 * Five minute cache
	 */
	private static final int CACHE_SECONDS = 360; 

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
	
	/**
	 * Current user
	 */
	private User currentUser = null;
	
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
			if (canHit()) {
			    statusListCache.put(FRIENDS_TIMELINE, twitter4j.getFriendsTimeline());
			}
		}
		return statusListCache.get(FRIENDS_TIMELINE);
	}
	
	/**
	 * Tells if twitter client can hit the server
	 * 
	 * @return
	 */
	private synchronized boolean canHit() {
        int remainingHits;
        try {
            remainingHits = twitter4j.rateLimitStatus().getRemainingHits();
        } catch (TwitterException e) {
            log.warn("Failure while getting rate limit status", e);
            return false;
        }
        log.debug("Remaining hits: " + remainingHits);
        //leave 3 hits for reserve
        return (remainingHits > 3);
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
			if (canHit()) {
			    statusListCache.put(REPLIES, twitter4j.getReplies());
			}
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
			if (canHit()) {
    			statusListCache.put(USER_TIMELINE, 
					twitter4j.getUserTimeline(twitter4j.getUserId(), pageSize));
			}
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

	/**
	 * Gets an 24x24 image for User
	 * 
	 * @param user
	 * @return
	 */
	public Image getUserImage(final User user) {
	    if (user == null) {
	        return getTwitterIcon();
	    }
		if (!userImages.containsKey(user.getName())) {
			final Configuration cfg = ConfigurationFactory.getConfigurationFactory()
				.getConfiguration();
			Image i = null;
			try {
    			if (!cfg.isHttpProxyInUse()) {
    				i = new Image(Display.getCurrent(), user.getProfileImageURL().openStream());
    			} else {
    				Proxy p = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(
    						cfg.getHttpProxyHost(), cfg.getHttpProxyPort()));
    				URLConnection con = user.getProfileImageURL().openConnection(p);
    				con.setReadTimeout(3000);
    				i = new Image(Display.getCurrent(), con.getInputStream());
    			}
			    i = new Image(Display.getCurrent(), 
			            i.getImageData().scaledTo(24, 24));
    			userImages.put(user.getName(), i);
			} catch (final IOException e) {
				log.warn("Failed getting user icon: " + user.getName(), e);
				return getTwitterIcon();
			}
		} 
		return userImages.get(user.getName());
	}
	
	/**
	 * Gets twitter icon
	 * 
	 * @return
	 */
	public Image getTwitterIcon() {
	    return IconFactory.getInstance().getPluginIcon("twitter24.png"
                , getClass().getClassLoader());
	}
	
	/**
	 * Gets currently active user
	 * 
	 * @return
	 */
	public User getCurrentUser() {
        if (currentUser == null) {
            if (canHit()) {
                try {
                    currentUser = twitter4j.getAuthenticatedUser();
                } catch (TwitterException e) {
                    log.warn("Can't get authenticated user", e);
                    return null;
                }
            }
        }
        return currentUser;
    }
	
	@Override
	protected void finalize() throws Throwable {
	    for (final Image i : userImages.values()) {
	        i.dispose();
	    }
	}
	
	/**
	 * Gets the rate limit status
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public synchronized RateLimitStatus getRateLimitStatus() throws TwitterException {
	    return twitter4j.rateLimitStatus();
	}
}
