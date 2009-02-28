package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.varaneckas.hawkscope.menu.AbstractMenuItem;

/**
 * Twitter menu item in Hawkscope menu
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class TwitterMenuItem extends AbstractMenuItem {

	/**
	 * Creates menu item
	 */
	public void createMenuItem(final Menu parent) {
		swtMenuItem = new MenuItem(parent, SWT.CASCADE);
		swtMenuItem.setEnabled(enabled);
		swtMenuItem.setText(text);
		swtMenuItem.setImage(icon);
		final Menu menu = new Menu(swtMenuItem);
		swtMenuItem.setMenu(menu);
	}

}
