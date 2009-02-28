/*
 * Copyright (c) 2008-2009 Tomas Varaneckas
 * http://www.varaneckas.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.InputDialog;
import com.varaneckas.hawkscope.gui.SharedStyle;
import com.varaneckas.hawkscope.util.Updater;

/**
 * Twitter input dialog 
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class TwitterDialog extends InputDialog {

	public TwitterDialog(final Updater updater) {
		open("", 140, new Shell(), updater);
		dialog.setText("Tweet!");
	}
	
	@Override
	protected void createTextInput(final int maxLength, int width) {
		text = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		FormData layout = SharedStyle.relativeTo(null, null, null, null);
		layout.width = 200;
		layout.top.offset += SharedStyle.TEXT_TOP_OFFSET_ADJUST;
		layout.height = 60;
		layout.bottom = null;
		text.setLayoutData(layout);
		FormData labelLayout = SharedStyle.relativeTo(text, null, cancel, null);
		labelLayout.left = null;
		label.setLayoutData(labelLayout);
		label.setFont(SharedStyle.FONT_BOLD);
		text.setTextLimit(maxLength);
		label.setText("Characters left: " + maxLength);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent ev) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						label.setText("Characters left: " + (maxLength 
								- text.getText().length()));
					}
				});
			}
		});
	}
}
