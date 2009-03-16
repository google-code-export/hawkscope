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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
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

    /**
     * Label "Command"
     */
    private Label command;
    
    /**
     * Command input line
     */
    private Text inputCommand;
    
    /**
     * Checkbox "Run in a separate thread"
     */
    private Button async;
    
    /**
     * Label "Output"
     */
    private Label labelOutput;
    
    /**
     * Output text area
     */
    private Text output;
    
    @Override
    public void open() {
        if (shell != null && !shell.isDisposed()) {
            shell.setVisible(true);
            shell.forceActive();
            shell.forceFocus();
            return;
        }
        createShell("Execute");
        createCommandLabel();
        createCommandInput();
        createAsyncCheckbox();
        createOutputLabel();
        createButtonClose();
        createOutputText();
        enableAsyncCheckbox();        
        
        inputCommand.addKeyListener(new InputCommandKeyListener(
                shell, inputCommand, output, async));

        packAndSetMinSize();
        shell.open();
        shell.forceActive();
        shell.forceFocus();
        shell.setTabList(new Control[] { inputCommand, async, buttonClose });
        inputCommand.setFocus();
    }

    /**
     * Enables Run in separate thread checkbox to react to clicks
     */
    private void enableAsyncCheckbox() {
        async.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent ev) {
                labelOutput.setVisible(!async.getSelection());
                output.setVisible(!async.getSelection());
                if (async.getSelection()) {
                    async.setLayoutData(SharedStyle.relativeTo(inputCommand, 
                            null, buttonClose, null));
                } else {
                    async.setLayoutData(SharedStyle.relativeTo(inputCommand, null));
                }
                shell.setMinimumSize(shell.getSize().x, 10);
                shell.pack();
                shell.setMinimumSize(400, shell.getSize().y);
            }
        });
    }

    /**
     * Creates command output text area
     */
    private void createOutputText() {
        output = new Text(shell, SWT.BORDER | SWT.MULTI 
                | SWT.VERTICAL | SWT.HORIZONTAL);
        final FormData outputLayout = SharedStyle.relativeTo(labelOutput, null,
                buttonClose, null);
        output.setEditable(false);
        output.setFont(SharedStyle.FONT_FIXED);
        outputLayout.width = 400;
        outputLayout.height = 100;
        output.setLayoutData(outputLayout);
    }

    /**
     * Creates label "Output"
     */
    private void createOutputLabel() {
        labelOutput = new Label(shell, SWT.NONE);
        labelOutput.setFont(SharedStyle.FONT_BOLD);
        labelOutput.setText("Output");
        labelOutput.setLayoutData(SharedStyle.relativeTo(async, null));
    }

    /**
     * Creates "Run in a separate thread" checkbox
     */
    private void createAsyncCheckbox() {
        async = new Button(shell, SWT.CHECK);
        async.setText("Run in a separate thread");
        async.setToolTipText("If this is checked, command will run in a " +
        		"new thread. Suitable for long running processes or for " +
        		"starting applications. If unchecked, command will be " +
        		"terminated if execution will take longer than 30 seconds.");
        async.setLayoutData(SharedStyle.relativeTo(inputCommand, null));
    }

    /**
     * Creates executable command input
     */
    private void createCommandInput() {
        inputCommand = new Text(shell, SWT.BORDER);
        final FormData inputLayout = SharedStyle
                .relativeTo(command, null, null, null);
        inputLayout.bottom = null;
        inputCommand.setLayoutData(inputLayout);
        inputCommand.setToolTipText("Type a command and hit Enter to execute");
    }

    /**
     * Creates label "Command"
     */
    private void createCommandLabel() {
        command = new Label(shell, SWT.NONE);
        command.setFont(SharedStyle.FONT_BOLD);
        command.setText("Command");
        command.setLayoutData(SharedStyle.relativeTo(null, null));
    }

    /**
     * Main method for testing
     * 
     * @param args
     */
    public static void main(final String[] args) {
        new ExecuteWindow().open();
        while (Display.getDefault().readAndDispatch()) {
            Display.getDefault().sleep();
        }
    }
}
