package com.varaneckas.hawkscope.plugins.delicious;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import del.icio.us.Delicious;
import del.icio.us.beans.Post;
import del.icio.us.beans.Tag;

public class DeliciousClient {
    
    private static DeliciousClient instance = null;
    
    private DeliciousClient() {}
    
    private Delicious client;
    
    private List<Post> posts;
    
    private List<Tag> tags;
    
    private Map<String, List<Post>> taggedPosts;
    
    private Date lastUpdated;
    
    public static DeliciousClient getInstance() {
        if (instance == null) {
            instance = new DeliciousClient();
        }
        return instance;
    }
    
    public Delicious getDelicous() {
        return client;
    }
    
    public void login(final String user, final String pass) {
        if (client != null) {
            logout();
        }
        client = new Delicious(user, pass);
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean update() {
        if (lastUpdated == null || client.getLastUpdate().after(lastUpdated)) {
            lastUpdated = client.getLastUpdate();
            posts = client.getAllPosts();
            tags = client.getTags();
            taggedPosts = new HashMap<String, List<Post>>();
            return true;
        }
        return false;
    }
    
    public List<Post> getPosts() {
        update();
        return posts;
    }
    
    @SuppressWarnings("unchecked")
    public List<Post> getPosts(final String tag) {
        update();
        if (taggedPosts.containsKey(tag)) {
            return taggedPosts.get(tag);
        }
        List<Post> tagPosts = client.getAllPosts(tag);
        taggedPosts.put(tag, tagPosts);
        return tagPosts;
    }
    
    public List<Tag> getTags() {
        update();
        return tags;
    }

    public void logout() {
        client = null;
        posts.clear();
        taggedPosts.clear();
        tags.clear();
    }

}
