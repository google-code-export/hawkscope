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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Input command listener which executes processes
 * 
 * @author Tomas Varaneckas
 * @version $Id$
 */
public class InputCommandKeyListener extends KeyAdapter {
    
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(InputCommandKeyListener.class);
    
    /**
     * List of last commands
     */
    private static final List<String> lastCommands = new ArrayList<String>();
    
    /**
     * Last Command index
     */
    private static int lastCommandIndex = 0;
    
    /**
     * Underlying shell
     */
    final Shell shell;
    
    /**
     * Command input 
     */
    final Text input;
    
    /**
     * Command result output
     */
    final Text output;
    
    /**
     * New thread checkbox
     */
    final Button async;
    
    /**
     * Constructor 
     * 
     * @param shell
     * @param input
     * @param output
     * @param async
     */
    public InputCommandKeyListener(final Shell shell, final Text input, 
            final Text output, final Button async) {
        this.shell = shell;
        this.input = input;
        this.output = output;
        this.async = async;
    }
    
    /**
     * Runs the command
     */
    public void keyPressed(final KeyEvent key) {
        if (key.keyCode == SWT.CR || key.keyCode == SWT.KEYPAD_CR) {
            final String cmd = input.getText();
            if (cmd == null || cmd.length() < 1) {
                return;
            }
            if (lastCommands.contains(cmd)) {
                lastCommands.remove(cmd);
            }
            if (lastCommands.size() > 20) {
                lastCommands.remove(0);
            }
            lastCommands.add(cmd);
            lastCommandIndex = lastCommands.size() - 1;
           
            input.setText("");
            input.setToolTipText("");                        
            if (async.getSelection()) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Runtime.getRuntime().exec(cmd);
                        } catch (final IOException e) {
                            final MessageBox mb = new MessageBox(new Shell());
                            mb.setMessage("Error in asynchronous process: " 
                                    + e.getMessage());
                            log.warn("Error in async process", e);
                        }
                    }
                }).start();
            } else {
                if (cmd.equalsIgnoreCase("clear")) {
                    output.setText("");
                } else {
                    shell.getDisplay().asyncExec(getSyncExecutor(cmd));
                }
            }
        } else if (key.keyCode == SWT.ARROW_DOWN) {
            key.doit = false;
            if (lastCommands.size() > 0) {
                adjustLastCommandIndex();
                input.setText(lastCommands.get(lastCommandIndex++));
                input.selectAll();
            }
        } else if (key.keyCode == SWT.ARROW_UP) {
            key.doit = false;
            if (lastCommands.size() > 0) {
                adjustLastCommandIndex();
                input.setText(lastCommands.get(lastCommandIndex--));
                input.selectAll();
            }
        }
    }

    /**
     * Adjusts last command index for looping through command array
     */
    private void adjustLastCommandIndex() {
        if (lastCommandIndex >= lastCommands.size()) {
            lastCommandIndex = 0;
        }
        if (lastCommandIndex < 0) {
            lastCommandIndex = lastCommands.size() -1;
        }
    }

    /**
     * Gets synchronous command executor
     * 
     * @param cmd
     * @return
     */
    private Runnable getSyncExecutor(final String cmd) {
        return new Runnable() {
            public void run() {
                try {
                    final long start = System.currentTimeMillis();
                    final Process p = Runtime.getRuntime().exec(cmd);
                    new Thread(new Runnable() {
                        public void run() {
                            while (true) {
                                try {
                                    
                                    p.exitValue();
                                    return;
                                } catch (final Exception e) {
                                    //logging would spam
                                }
                                if ((System.currentTimeMillis() - start) 
                                        > 30000) {
                                    p.destroy();
                                    shell.getDisplay().syncExec(new Runnable() {
                                        public void run() {
                                            output.append(
                                                    "Synchronous process timeout: " 
                                                    + cmd + '\n');
                                        }
                                    });
                                    return;
                                } 
                                try {
                                    Thread.sleep(50L);
                                } catch (final InterruptedException e) {
                                    log.warn("Interrupted while executing task:" 
                                            + cmd, e);
                                }
                            }
                        }
                    }).start();
                    final InputStream in = p.getInputStream();
                    int c;
                    while ((c = in.read()) != -1) {
                        output.append(String.valueOf((char) c));
                    }
                    in.close();
                } catch (final Exception e) {
                    output.append(e.getMessage()+ '\n');
                }
            }
        };
    }
}
