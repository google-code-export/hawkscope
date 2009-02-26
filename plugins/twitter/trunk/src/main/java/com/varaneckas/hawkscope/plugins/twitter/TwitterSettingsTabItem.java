package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.gui.settings.AbstractSettingsTabItem;

public class TwitterSettingsTabItem extends AbstractSettingsTabItem {

    Text inputUser;
    
    Text inputPass;
    
    public TwitterSettingsTabItem(final TabFolder folder) {
        super(folder, "&Twitter");
        
        Label twitterLogin = addSectionLabel("Twitter Login");
        twitterLogin.setLayoutData(SharedStyle.relativeTo(null, null));
        
        Label user = addLabel("Username:");
        user.setLayoutData(ident(SharedStyle.relativeTo(twitterLogin, null)));
        
        Label pass = addLabel("Password:");
        pass.setLayoutData(ident(SharedStyle.relativeTo(user, null)));
        
        String userVal = cfg.getProperties()
            .get(TwitterPlugin.PROP_TWITTER_USER);
        if (userVal == null) userVal = "";
        inputUser = addText(userVal, 255);
        FormData userLayout = SharedStyle.relativeTo(twitterLogin, 
                null, null, twitterLogin);
        userLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        userLayout.bottom = null;
        inputUser.setLayoutData(userLayout);
        
        String passVal = cfg.getProperties()
            .get(TwitterPlugin.PROP_TWITTER_PASS);
        if (passVal == null) passVal = "";
        inputPass = addText(passVal, 255);
        inputPass.setEchoChar('*');
        FormData passLayout = SharedStyle.relativeTo(user, 
                null, null, twitterLogin);
        passLayout.bottom = null;
        passLayout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
        inputPass.setLayoutData(passLayout);
    }

    @Override
    protected void saveConfiguration() {
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_USER, inputUser.getText());
        cfg.getProperties().put(TwitterPlugin.PROP_TWITTER_PASS, inputPass.getText());
        TwitterPlugin.getInstance().refresh();
    }

}
