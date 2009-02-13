package com.varaneckas.hawkscope.cfg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.varaneckas.hawkscope.util.OSUtils;
import com.varaneckas.hawkscope.util.OSUtils.OS;

/**
 * Basic Configuration Factory
 *
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class BasicConfigurationFactory extends ConfigurationFactory {

    @Override
    protected String loadConfigFilePath() {
        return new File(".").getAbsolutePath();
    }

    @Override
    protected Map<String, String> getDefaults() {
        final Map<String, String> data = new HashMap<String, String>();
        //hidden files are not displayed
        data.put(Configuration.HIDDEN_FILES_DISPLAYED, "0");
        //quick access list contains one entry - user home, 
        //read from system properties
        if (OSUtils.CURRENT_OS.equals(OS.MAC)) {
        	data.put(Configuration.QUICK_ACCESS_LIST, "${user.home};/Applications");
        } else {
        	data.put(Configuration.QUICK_ACCESS_LIST, "${user.home}");
        }
        //floppy drives are not displayed
        data.put(Configuration.FLOPPY_DRIVES_DISPLAYED, "0");
        //blacklist is empty
        data.put(Configuration.FILESYSTEM_BLACKLIST, "");
        //3 seconds menu reload delay
        data.put(Configuration.MENU_RELOAD_DELAY, "3000");
        //check for update by default
        data.put(Configuration.CHECK_FOR_UPDATES, "1");
        //no proxy by default
        data.put(Configuration.HTTP_PROXY_USE, "0");
        //no proxy host by default
        data.put(Configuration.HTTP_PROXY_HOST, "");
        //8080 default proxy port
        data.put(Configuration.HTTP_PROXY_PORT, "8080");
        //no proxy auth by default
        data.put(Configuration.HTTP_PROXY_AUTH_USE, "0");
        //no proxy auth username by default
        data.put(Configuration.HTTP_PROXY_AUTH_USERNAME, "");
        //no proxy auth password by default
        data.put(Configuration.HTTP_PROXY_AUTH_PASSWORD, "");
        //use OS icons - off by default
        data.put(Configuration.USE_OS_ICONS, "0");
        return data;
    }

}