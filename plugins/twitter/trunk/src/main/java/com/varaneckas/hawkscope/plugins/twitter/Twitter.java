package com.varaneckas.hawkscope.plugins.twitter;

public class Twitter extends twitter4j.Twitter {
    
    private static final long serialVersionUID = 7459304598283222922L;

    public Twitter(String user, String pass) {
        super(user, pass);
        this.http = new ProxyEnabledHttpClient();
        this.http.setUserId(user);
        this.http.setPassword(pass);
    }
    
    public Twitter() {
        super();
        this.http = new ProxyEnabledHttpClient();
    }

}
