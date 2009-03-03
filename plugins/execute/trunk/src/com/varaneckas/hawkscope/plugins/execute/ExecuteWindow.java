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
package com.varaneckas.hawkscope.plugins.execute;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.varaneckas.hawkscope.gui.AbstractWindow;
import com.varaneckas.hawkscope.gui.SharedStyle;

/**
 * Execute plugin window for running system commands
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class ExecuteWindow extends AbstractWindow {

    @Override
    public void open() {
        if (shell != null && !shell.isDisposed()) {
            shell.setVisible(true);
            shell.forceActive();
            shell.forceFocus();
            return;
        }
        createShell("Execute");

        final Label command = new Label(shell, SWT.NONE);
        command.setFont(SharedStyle.FONT_BOLD);
        command.setText("Command");
        command.setLayoutData(SharedStyle.relativeTo(null, null));

        final Text inputCommand = new Text(shell, SWT.BORDER);
        final FormData inputLayout = SharedStyle
                .relativeTo(command, null, null, null);
        inputLayout.bottom = null;
        inputCommand.setLayoutData(inputLayout);
        inputCommand.setToolTipText("Type a command and hit Enter to execute");

        final Label labelOutput = new Label(shell, SWT.NONE);
        labelOutput.setFont(SharedStyle.FONT_BOLD);
        labelOutput.setText("Output");
        labelOutput.setLayoutData(SharedStyle.relativeTo(inputCommand, null));

        createButtonClose();

        final Text output = new Text(shell, SWT.BORDER | SWT.MULTI
                | SWT.VERTICAL);
        final FormData outputLayout = SharedStyle.relativeTo(labelOutput, null,
                buttonClose, null);
        output.setEditable(false);
        outputLayout.width = 400;
        outputLayout.height = 100;
        output.setLayoutData(outputLayout);

        inputCommand.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent key) {
                if (key.keyCode == SWT.CR || key.keyCode == SWT.KEYPAD_CR) {
                    if (inputCommand.getText() == null
                            || inputCommand.getText().length() < 1) {
                        return;
                    }
                    Process p;
                    try {
                        output.setText("");
                        inputCommand.setToolTipText("");
                        String cmd = inputCommand.getText();
                        inputCommand.setText("");
                        p = Runtime.getRuntime().exec(cmd);
                        InputStream in = p.getInputStream();
                        int c;
                        while ((c = in.read()) != -1) {
                            output.setText(output.getText().concat(
                                    "" + (char) c));
                        }
                        int exit = p.waitFor();
                        output.setText(output.getText().concat("Exit: " + exit));
                    } catch (Exception e) {
                        output.append(e.getMessage());
                    }
                }
            }
        });

        packAndSetMinSize();
        shell.open();
        shell.forceActive();
        shell.forceFocus();
        shell.setTabList(new Control[] { inputCommand, buttonClose });
        inputCommand.setFocus();
    }

    /**
     * Main method for testing
     * 
     * @param args
     */
    public static void main(String[] args) {
        new ExecuteWindow().open();
        while (Display.getDefault().readAndDispatch()) {
            Display.getDefault().sleep();
        }
    }
}
