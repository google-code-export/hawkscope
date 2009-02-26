package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.varaneckas.hawkscope.menu.AbstractMenuItem;

public class TwitterMenuItem extends AbstractMenuItem {

	public void createMenuItem(Menu parent) {
		swtMenuItem = new MenuItem(parent, SWT.CASCADE);
		swtMenuItem.setEnabled(enabled);
		swtMenuItem.setText(text);
		swtMenuItem.setImage(icon);
		Menu menu = new Menu(swtMenuItem);
		swtMenuItem.setMenu(menu);
	}

}
