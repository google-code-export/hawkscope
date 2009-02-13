package com.varaneckas.hawkscope.util;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.varaneckas.hawkscope.cfg.ConfigurationFactory;

/**
 * {@link IconFactory} - SWT implmementation
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class IconFactory {

	private static final IconFactory instance = new IconFactory();
	
	private IconFactory() {}
	
	public static IconFactory getInstance() {
		return instance;
	}
	
    private final Map<String, Image> resourcePool = new HashMap<String, Image>();
    
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(IconFactory.class);
    
    /**
     * Default {@link Display}
     */
    private static final Display display = Display.getDefault();

    /**
     * Gets {@link Icon} for name
     * 
     * @param name icon name
     * @return icon
     */
    public Image getIcon(final String name) {
        try {
            if (resourcePool.containsKey(name)) {
                return resourcePool.get(name);
            } 
            final Image i = new Image(display, resources.get(name).openStream());
            resourcePool.put(name, i);
            return i;
        } catch (final Exception e) {
            log.error("Failed getting icon: " + name, e);
        }
        return null;
    }
    
    /**
     * Gets uncached icon
     * 
     * @param name Icon name with extension
     * @return icon
     */
    public Image getUncachedIcon(final String name) {
        if (resourcePool.containsKey(name)) {
            return resourcePool.get(name);
        } 
        final Image i = new Image(display, IconFactory.class.getClassLoader()
                .getResourceAsStream("icons/" + name));
        resourcePool.put(name, i);
        return i;
    }

    /**
     * Gets large file system icon for any file
     * 
     * @param file source
     * @return icon
     */
    public Image getFileSystemIcon(final File file) {
    	if (file.isDirectory() || !file.getName().contains(".")) {
    		return null;
    	}
        Image image = null;
        Program p = Program.findProgram(file.getName().replaceAll(".*\\.", "."));
        if (p != null) {
            if (resourcePool.containsKey(p.getName())) {
                return resourcePool.get(p.getName());
            }
            ImageData data = p.getImageData();
            if (data != null) {
                image = new Image(display, data);
                resourcePool.put(p.getName(), image);
            }
        }
        return image;
    }
    
    /**
     * Gets Hawkscope Tray Icon of best size
     * 
     * @return tray icon
     */
    public Image getTrayIcon() {
        String name = "HawkscopeTrayIcon";
        if (resourcePool.containsKey(name)) {
            resourcePool.get(name);
        }
        final Image trayIcon = new Image(display, IconFactory.class.getClassLoader()
                .getResourceAsStream(getBestTrayIcon()));
        resourcePool.put(name, trayIcon);
        return trayIcon;
    }
    
    public synchronized void cleanup() {
        for (final String im : resourcePool.keySet()) {
            try {
                log.debug("Releasing icon: " + im);
                resourcePool.get(im).dispose();
            } catch (final Exception e) {
                log.debug("Failed releasing icon", e);
            }
        }        
    }
    
    
    /**
     * Preloaded resources
     */
    protected static final Map<String, URL> resources = new HashMap<String, URL>();
    
    static {
        try {
            //initialize resources
            resources.put("drive",  IconFactory.class.getClassLoader().getResource("icons/hdd24.png"));
            resources.put("floppy",  IconFactory.class.getClassLoader().getResource("icons/fdd24.png"));
            resources.put("cdrom",  IconFactory.class.getClassLoader().getResource("icons/cdrom24.png"));
            resources.put("network",  IconFactory.class.getClassLoader().getResource("icons/network24.png"));
            resources.put("removable",  IconFactory.class.getClassLoader().getResource("icons/removable24.png"));
            resources.put("folder", IconFactory.class.getClassLoader().getResource("icons/folder24.png"));
            resources.put("folder.open", IconFactory.class.getClassLoader().getResource("icons/folder.open.24.png"));
            resources.put("file",   IconFactory.class.getClassLoader().getResource("icons/file24.png"));
            resources.put("executable",   IconFactory.class.getClassLoader().getResource("icons/executable24.png"));
            resources.put("exit",   IconFactory.class.getClassLoader().getResource("icons/exit24.png"));
            resources.put("hide",   IconFactory.class.getClassLoader().getResource("icons/down24.png"));
            resources.put("more",   IconFactory.class.getClassLoader().getResource("icons/more24.png"));
            resources.put("unknown", IconFactory.class.getClassLoader().getResource("icons/unknown24.png"));  
            resources.put("about",  IconFactory.class.getClassLoader().getResource("icons/about24.png"));  
            resources.put("open",  IconFactory.class.getClassLoader().getResource("icons/open24.png")); 
            resources.put("empty",  IconFactory.class.getClassLoader().getResource("icons/empty24.png")); 
            resources.put("update", IconFactory.class.getClassLoader().getResource("icons/update24.png"));
            resources.put("settings", IconFactory.class.getClassLoader().getResource("icons/settings24.png"));
        } catch (final Exception e) {
            log.warn("Cannot find icon", e);
        }
    }
    
    /**
     * Gets icon for {@link File}
     * 
     * @param targetFile any file
     * @return icon
     */    
    public Image getIcon(final File targetFile) {
        if (ConfigurationFactory.getConfigurationFactory().getConfiguration()
                .useOsIcons()) {
            Image icon = getFileSystemIcon(targetFile);
            if (icon != null) {
                return icon;
            }
        }
        if (OSUtils.isFileSystemRoot(targetFile)) {
            if (OSUtils.isFloppyDrive(targetFile)) {
                return getIcon("floppy");
            }
            if (OSUtils.isOpticalDrive(targetFile)) {
                return getIcon("cdrom");
            } 
            if (OSUtils.isNetworkDrive(targetFile)) {
                return getIcon("network");
            }
            if (OSUtils.isRemovableDrive(targetFile)) {
                return getIcon("removable");
            }
            return getIcon("drive");
        } else if (targetFile.isFile()) {
        	if (OSUtils.isExecutable(targetFile)) {
        	    return getIcon("executable");
        	}
            return getIcon("file");
        } else if (targetFile.isDirectory()) {
            //mac app
            if (OSUtils.CURRENT_OS.equals(OSUtils.OS.MAC) 
                    && targetFile.getName().endsWith(".app")) {
                return getIcon("executable");  
            } 
            return getIcon("folder");
        } else {
            return getIcon("unknown");
        }
    }
    
    /**
     * Gets best sized tray icon name for current setup
     * 
     * @return tray icon name
     */
    protected String getBestTrayIcon() {
        float height = OSUtils.getTrayIconSize();
        int[] sizes = new int[] { 64, 48, 32, 24, 16 };
        int best = 64;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] / height >= 1) {
                best = sizes[i];
            }
            else {
                break;
            }
        }
        final String res = "icons/hawkscope" + best + ".png";
        if (log.isDebugEnabled()) {
            log.debug("Chose best icon for " + (int) height 
                    + " pixel tray: " + res);
        }
        return res;
    }   
}