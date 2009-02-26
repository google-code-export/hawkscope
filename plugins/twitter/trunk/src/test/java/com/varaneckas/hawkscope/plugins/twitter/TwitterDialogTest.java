package com.varaneckas.hawkscope.plugins.twitter;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import com.varaneckas.hawkscope.util.Updater;

public class TwitterDialogTest {

	@Test
	public void testTwitterDialog() throws Exception {
		new TwitterDialog(new Updater() {
			public void setValue(String value) {
				System.out.println(value);
			}});
		
		Display d = Display.getDefault();
		while (d.readAndDispatch()) {
			if (!d.isDisposed()) {
				d.sleep();
			}
		}
		d.dispose();
	}
	
}
