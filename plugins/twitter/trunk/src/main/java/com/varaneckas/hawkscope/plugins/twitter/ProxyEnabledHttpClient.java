package com.varaneckas.hawkscope.plugins.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Map;

import sun.misc.BASE64Encoder;
import twitter4j.TwitterException;
import twitter4j.http.HttpClient;
import twitter4j.http.PostParameter;
import twitter4j.http.Response;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;

public class ProxyEnabledHttpClient extends HttpClient {

    private static final long serialVersionUID = -5189568653113523144L;
    
    private static final Configuration cfg = ConfigurationFactory
    .getConfigurationFactory().getConfiguration();

    private final int OK = 200;
    private final int NOT_MODIFIED = 304;
    private final int UNAUTHORIZED = 401;
    private final int FORBIDDEN = 403;

    private final boolean DEBUG = Boolean.getBoolean("twitter4j.debug");

    private final int INTERNAL_SERVER_ERROR = 500;
    private String userAgent =
        "twitter4j http://yusuke.homeip.net/twitter4j/ /1.1.5";
    private String basic;
    private int retryCount = 0;
    private int retryIntervalMillis = 10000;
    private String userId = null;
    private String password = null;

    public ProxyEnabledHttpClient(String userId, String password) {
        setUserId(userId);
        setPassword(password);
    }

    public ProxyEnabledHttpClient() {
        this.basic = null;
    }

    public void setUserId(String userId){
        this.userId = userId;
        encodeBasicAuthenticationString();
    }
    public void setPassword(String password){
        this.password = password;
        encodeBasicAuthenticationString();
    }

    public String getUserId() {
        return userId;
    }
    public String getPassword() {
        return password;
    }

    private void encodeBasicAuthenticationString(){
        if(null != userId && null != password){
        this.basic = "Basic " +
            new String(new BASE64Encoder().encode((userId + ":" + password).getBytes()));
        }
    }

    public void setRetryCount(int retryCount) {
        if (retryCount >= 0) {
            this.retryCount = retryCount;
        } else {
            throw new IllegalArgumentException("RetryCount cannot be negative.");
        }
    }

    public void setUserAgent(String ua) {
        this.userAgent = ua;
    }

    public void setRetryIntervalSecs(int retryIntervalSecs) {
        if (retryIntervalSecs >= 0) {
            this.retryIntervalMillis = retryIntervalSecs * 1000;
        } else {
            throw new IllegalArgumentException(
                "RetryInterval cannot be negative.");
        }
    }

    public Response post(String url, PostParameter[] PostParameters,
                         boolean authenticated) throws TwitterException {
        return httpRequest(url, PostParameters, authenticated);
    }

    public Response post(String url, boolean authenticated) throws TwitterException {
        return httpRequest(url, new PostParameter[0], authenticated);
    }

    public Response post(String url, PostParameter[] PostParameters) throws
        TwitterException {
        return httpRequest(url, PostParameters, false);
    }
    public Response post(String url) throws
        TwitterException {
        return httpRequest(url, new PostParameter[0], false);
    }

    public Response get(String url, boolean authenticated) throws
        TwitterException {
        return httpRequest(url, null, authenticated);
    }

    public Response get(String url) throws TwitterException {
        return httpRequest(url, null, false);
    }

    //for test purpose
    /*package*/ int retriedCount = 0;
    /*package*/String lastURL;
    private Response httpRequest(String url, PostParameter[] postParams,
                                 boolean authenticated) throws TwitterException {
        int retry = retryCount + 1;
        Response res = null;
        // update the status
        lastURL = url;
        for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            int responseCode = -1;
            try {
                HttpURLConnection con = null;
                InputStream is = null;
                OutputStream osw = null;
                try {
                    if (cfg.isHttpProxyInUse()) {
                        System.out.println("http proxy on");
                        if (cfg.isHttpProxyAuthInUse()) {
                            System.out.println("http proxy auth on");
                            Authenticator.setDefault(new Authenticator() {
                                @Override
                                protected PasswordAuthentication 
                                        getPasswordAuthentication() {
                                    if (getRequestorType().equals(RequestorType.PROXY)) {
                                        System.out.println("authing!");
                                        return new PasswordAuthentication(
                                                cfg.getHttpProxyAuthUsername(), 
                                                cfg.getHttpProxyAuthPassword()
                                                        .toCharArray());
                                    } else {
                                        System.out.println("non proxy");
                                        return null;
                                    }
                                }
                            });
                        }
                        final Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress
                                .createUnresolved(cfg.getHttpProxyHost()
                                        , cfg.getHttpProxyPort()));
                        System.out.println("opening proxied con");
                        con = (HttpURLConnection)new URL(url).openConnection(proxy);
                    } else {
                        con = (HttpURLConnection)new URL(url).openConnection();
                    }
                    con.setConnectTimeout(10000);
                    con.setReadTimeout(10000);
                    con.setDoInput(true);
                    setHeaders(con, authenticated);
                    if (null != postParams) {
                        log("POST ", url);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type",
                                               "application/x-www-form-urlencoded");
                        con.setDoOutput(true);
                        String postParam = encodeParameters(postParams);
                        log("Post Params: ", postParam);
                        byte[] bytes = postParam.getBytes("UTF-8");

                        con.setRequestProperty("Content-Length",
                                               Integer.toString(bytes.length));
                        osw = con.getOutputStream();
                        osw.write(bytes);
                        osw.flush();
                        osw.close();
                    } else {
                        log("GET "+url);
                        con.setRequestMethod("GET");
                    }
                    responseCode = con.getResponseCode();
                    log("Response code: ", String.valueOf(responseCode));
                    if (responseCode == UNAUTHORIZED || responseCode == FORBIDDEN) {
                        is = con.getErrorStream();
                    }else{
                        is = con.getInputStream(); // this will throw IOException in case response code is 4xx 5xx
                    }
                    res = new Response(con.getResponseCode(), is);
                    log("Response: ", res.toString());
                    if (responseCode == UNAUTHORIZED || responseCode == FORBIDDEN) {
                        throw new TwitterException(res.toString(), responseCode);
                    }

                    break;
                } finally {
                    try {
                        is.close();
                    } catch (Exception ignore) {}
                    try {
                        osw.close();
                    } catch (Exception ignore) {}
                    try {
                        con.disconnect();
                    } catch (Exception ignore) {}
                }
            } catch (IOException ioe) {
                if (responseCode == UNAUTHORIZED || responseCode == FORBIDDEN) {
                    //throw TwitterException without reply since this request won't success
                    if(DEBUG){
                        ioe.printStackTrace();
                    }
                    throw new TwitterException(ioe.getMessage(), responseCode);
                }
                if (retriedCount == retryCount) {
                    throw new TwitterException(ioe.getMessage(), responseCode);
                }
            }
            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ignore) {
                //nothing to do
            }
        }
        return res;
    }

    /**
     * sets HTTP headers
     * @param connection HttpURLConnection
     * @param authenticated boolean
     */
    private void setHeaders(HttpURLConnection connection, boolean authenticated) {
        if (authenticated) {
            if (basic == null) {
                throw new IllegalStateException(
                    "user ID/password combination not supplied");
            }
            connection.addRequestProperty("Authorization", this.basic);
        }
        for (String key : requestHeaders.keySet()) {
            connection.addRequestProperty(key, requestHeaders.get(key));
        }
    }

    private Map<String, String> requestHeaders = new HashMap<String, String> ();

    public void setRequestHeader(String name, String value) {
        requestHeaders.put(name, value);
    }

    @Override public int hashCode() {
        return this.retryCount + this.retryIntervalMillis + this.basic.hashCode()
            + requestHeaders.hashCode();
    }

    private void log(String message){
        if(DEBUG){
            System.out.println("[" + new java.util.Date() + "]" + message);
        }
    }
    private void log(String message,String message2){
        if(DEBUG){
            log(message+message2);
        }
    }

    
}
