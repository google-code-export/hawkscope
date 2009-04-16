package com.varaneckas.hawkscope.plugins.delicious;

import java.util.Date;
import java.util.List;

import del.icio.us.Delicious;
import del.icio.us.beans.Post;

public class DeliciousClient {
    
    private static DeliciousClient instance = null;
    
    private DeliciousClient() {}
    
    private Delicious client;
    
    private List<Post> posts;
    
    private Date lastUpdated;
    
    public static DeliciousClient getInstance() {
        if (instance == null) {
            instance = new DeliciousClient();
        }
        return instance;
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
            return true;
        }
        return false;
    }
    
    public List<Post> getPosts() {
        update();
        return posts;
    }

    public void logout() {
        client = null;
    }

}
