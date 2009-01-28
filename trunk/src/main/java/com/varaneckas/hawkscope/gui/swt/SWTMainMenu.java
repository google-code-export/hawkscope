package com.varaneckas.hawkscope.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.varaneckas.hawkscope.cfg.ConfigurationFactory;
import com.varaneckas.hawkscope.menu.MainMenu;
import com.varaneckas.hawkscope.menu.state.MenuClosedState;
import com.varaneckas.hawkscope.tray.TrayManagerFactory;

/**
 * {@link MainMenu} - SWT implementation
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class SWTMainMenu extends MainMenu {
	
    /**
     * Singleton instance
     */
    private static SWTMainMenu instance = null;

    /**
     * SWT Menu object
     */
    private final Menu menu;

    /**
     * Marks that menu is reloading
     */
    private boolean isReloading;
    
    /**
     * Unix timestamp since when menu was hidden. Used for enhancing the 
     * reloading performance.
     */
    private static long hiddenSince;
    
    /**
     * Initializing singleton constructor
     */
    private SWTMainMenu() {
        menu = new Menu(((SWTTrayManager) TrayManagerFactory.getTrayManager())
                .getShell(), SWT.POP_UP);
        menu.addListener(SWT.Hide, new Listener() {
            public void handleEvent(Event event) {
                new Thread(new Runnable() {
                    public void run() {
                        menu.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                try {
                                    Thread.sleep(10l);
                                    hiddenSince = System.currentTimeMillis();
                                } catch (InterruptedException e) {
                                	log.warn("Could not sleep", e);
                                }
                                if (!(state instanceof MenuClosedState)) {
                                    setState(MenuClosedState.getInstance());
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * Singleton instance getter
     * 
     * @return instance of SWTMainMenu
     */
    public static SWTMainMenu getInstance() {
        if (instance == null) {
            instance = new SWTMainMenu();
        }
        return instance;
    }

    @Override
    public void clearMenu() {
        for (MenuItem item : menu.getItems()) {
            if (!item.isDisposed()) {
                item.dispose();
            }
        }
    }

    @Override
    public void forceHide() {
        hiddenSince = System.currentTimeMillis();
        setState(MenuClosedState.getInstance());
        menu.setVisible(false);
    }

    @Override
    public void showMenu(final int x, final int y) {
        hiddenSince = 0L;
        menu.setLocation(x, y);
        menu.setVisible(true);
        menu.setDefaultItem(menu.getItems()[0]);
    }

    @Override
    public void addMenuItem(final com.varaneckas.hawkscope.menu.MenuItem item) {
        if (item instanceof SWTMenuItem) {
            ((SWTMenuItem) item).createMenuItem(menu);
        }
    }

    @Override
    public void addSeparator() {
        new MenuItem(menu, SWT.SEPARATOR);
    }
    
    public void setHiddenSince(final long timestamp) {
        hiddenSince = timestamp;
    }
    
    @Override
    public synchronized void reloadMenu(final boolean canWait) {
        if (!canWait && log.isDebugEnabled()) {
            log.debug("Forcing menu reload now.");
        }
        if (!isReloading || !canWait) {
            isReloading = true;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (canWait) {
                            Thread.sleep(ConfigurationFactory
                                    .getConfigurationFactory()
                                    .getConfiguration().getMenuReloadDelay());
                        }
                    } catch (InterruptedException e1) {
                    	log.warn("Could not sleep", e1);
                    }
                    doReload(canWait);
                }
            }).start();
        }
    }
    
    /**
     * Does the actual reload of Main Menu
     */
    private synchronized void doReload(final boolean canWait) {
        menu.getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {
                    if (canWait) {
                        if (hiddenSince == -1) {
                            log.debug("Skipping planned reload as it was forced a while ago.");
                            return;
                        }
                        if (hiddenSince == 0L) {
                            log.debug("Menu now open, reload skipped");
                            isReloading = false;
                            return;
                        } else if (System.currentTimeMillis() 
                                - hiddenSince < ConfigurationFactory
                                        .getConfigurationFactory()
                                        .getConfiguration().getMenuReloadDelay()) {
                            //menu is actively used, try reloading later
                            if (log.isDebugEnabled()) {
                                log.debug("Reloading later, menu is not sleeping " +
                                		"long enough: (" 
                                        + ((System.currentTimeMillis() - hiddenSince) 
                                                / 1000.0 ) + " seconds)");
                            }
                            isReloading = false;
                            reloadMenu(true);
                            return;
                        }
                    } else {
                        //way of telling other reloader threads to stay the f*** away :)
                        hiddenSince = -1; 
                    }
                    clearMenu();
                    loadMenu();
                    Runtime.getRuntime().gc();
                } catch (final Exception e) {
                    log.debug("Failed reloading menu", e);
                }
                isReloading = false;
            }
        });
    }
}
