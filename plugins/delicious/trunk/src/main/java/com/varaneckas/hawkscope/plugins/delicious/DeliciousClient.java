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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import del.icio.us.Delicious;
import del.icio.us.beans.Post;
import del.icio.us.beans.Tag;

/**
 * Delicious.com client. 
 * 
 * Uses Delicious client: http://delicious-java.sourceforge.net/
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class DeliciousClient {
    
    /**
     * Limit data to 50 items
     */
    private static final int COUNT = 20;
    
    /**
     * Singleton instance
     */
    private static DeliciousClient instance = null;
    
    /**
     * Private singleton instance constructor
     */
    private DeliciousClient() {}
    
    /**
     * Instance of delicious java client
     */
    private Delicious client;
    
    /**
     * Cached list of {@link Post}
     */
    private List<Post> posts;
    
    /**
     * Cached list of {@link Tag}
     */
    private List<Tag> tags;
    
    /**
     * Cached Lists of {@link Post} for {@link Tag}s
     */
    private Map<String, List<Post>> taggedPosts;
    
    /**
     * Last update date
     */
    private Date lastUpdated;
    
    /**
     * Singleton instance getter
     * 
     * @return instance
     */
    public static DeliciousClient getInstance() {
        if (instance == null) {
            instance = new DeliciousClient();
        }
        return instance;
    }
    
    /**
     * Gets the delicious java client
     * 
     * @return client or null if not configured
     */
    public Delicious getDelicious() {
        return client;
    }
    
    /**
     * Creates new instance of {@link Delicious} client
     * 
     * @param user Delicious.com account user
     * @param pass Delicious.com account password
     */
    public void login(final String user, final String pass) {
        if (user == null || pass == null || user.equals("") || pass.equals("")) {
            logout();
            return;
        }
        if (client != null) {
            logout();
        }
        client = new Delicious(user, pass);
    }
    
    /**
     * Updates the data
     * 
     * @return true if updated, false if cache was used
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean update() {
        if (client == null) {
            return false; 
        }
        if (lastUpdated == null || client.getLastUpdate().after(lastUpdated)) {
            lastUpdated = client.getLastUpdate();
            posts = client.getRecentPosts(null, COUNT);
            tags = client.getTags();
            taggedPosts = new HashMap<String, List<Post>>();
            return true;
        }
        return false;
    }
    
    /**
     * Gets all bookmarks
     * 
     * @return list of bookmarks
     */
    public List<Post> getPosts() {
        return posts;
    }
    
    /**
     * Tells if tere are cached bookmarks for {@link Tag}
     * 
     * @param tag Tag name
     * @return true if yes, false otherwise
     */
    public boolean hasPosts(final String tag) {
        return taggedPosts.containsKey(tag);
    }
    
    /**
     * Gets list of bookmarks for tag
     * 
     * @param tag Tag name
     * @return list of bookmarks
     */
    @SuppressWarnings("unchecked")
    public List<Post> getPosts(final String tag) {
        if (taggedPosts.containsKey(tag)) {
            return taggedPosts.get(tag);
        }
        List<Post> tagPosts = client.getRecentPosts(tag, COUNT);
        taggedPosts.put(tag, tagPosts);
        return tagPosts;
    }
    
    /**
     * Gets a list of tags
     * 
     * @return list of tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Logs out from Delicious.com account
     */
    public void logout() {
        client = null;
        clearCache();
    }

    /**
     * Clears delicious cache
     */
    public void clearCache() {
        lastUpdated = null;
        if (posts != null) {
            posts.clear();
        }
        if (taggedPosts != null) {
            taggedPosts.clear();
        }
        if (tags != null) {
            tags.clear();
        }
    }

}
