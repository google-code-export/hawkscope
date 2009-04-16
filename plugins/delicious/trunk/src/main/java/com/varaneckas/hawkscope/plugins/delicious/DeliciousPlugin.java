package com.varaneckas.hawkscope.plugins.delicious;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.command.Command;
import com.varaneckas.hawkscope.menu.AbstractMenuItem;
import com.varaneckas.hawkscope.menu.ExecutableMenuItem;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.menu.MenuFactory;
import com.varaneckas.hawkscope.plugin.PluginAdapter;
import com.varaneckas.hawkscope.util.IconFactory;

public class DeliciousPlugin extends PluginAdapter {
    
    private DeliciousClient client;
    
    private AbstractMenuItem deliciousItem; 
    
    public DeliciousPlugin() {
        canHookBeforeQuickAccessList = true;
        refresh();
    }
    
    private void refresh() {
        final Configuration cfg = ConfigurationFactory.getConfigurationFactory()
            .getConfiguration();
        client = DeliciousClient.getInstance();
        client.login("", "");
        if (cfg.isHttpProxyInUse()) {
            client.getDelicous().setProxyConfiguration(cfg.getHttpProxyHost(), 
                    cfg.getHttpProxyPort());
            if (cfg.isHttpProxyAuthInUse()) {
                client.getDelicous().setProxyAuthenticationConfiguration(
                        cfg.getHttpProxyAuthUsername(), 
                        cfg.getHttpProxyAuthPassword());
            }
        }
        client.update();
    }

    @Override
    public void beforeQuickAccess(final MainMenu mainMenu) {
        deliciousItem = createDeliciousItem();
        deliciousItem.setText("Delicious");
        deliciousItem.setIcon(getDeliciousIcon());
        mainMenu.addMenuItem(deliciousItem);
        mainMenu.addSeparator();
        createPostBookmarkItem();
    }
    
    private void createPostBookmarkItem() {
        ExecutableMenuItem post = MenuFactory.newExecutableMenuItem();
        post.setText("Post new bookmark");
        post.setIcon(getDeliciousIcon());
        post.setCommand(new Command() {
            public void execute() {
            }
        });
        post.createMenuItem(deliciousItem.getSwtMenuItem().getMenu());
    }

    private AbstractMenuItem createDeliciousItem() {
        return new AbstractMenuItem() {
            public void createMenuItem(final Menu parent) {
                this.swtMenuItem = new MenuItem(parent, SWT.CASCADE);
                this.swtMenuItem.setEnabled(this.enabled);
                this.swtMenuItem.setText(this.text);
                this.swtMenuItem.setImage(this.icon);
                final Menu menu = new Menu(this.swtMenuItem);
                this.swtMenuItem.setMenu(menu);
            }
        };
    }

    private Image getDeliciousIcon() {
        return IconFactory.getInstance()
            .getPluginIcon("delicious24.png", getClass().getClassLoader());
    }

    public String getDescription() {
        return "Delicious.com bookmarks at your service";
    }

    public String getName() {
        return "Delicious";
    }

    public String getVersion() {
        return "1.0";
    }

}
