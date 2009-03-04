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
            input.setText("");
            input.setToolTipText("");                        
            if (async.getSelection()) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Runtime.getRuntime().exec(cmd);
                        } catch (final IOException e) {
                            MessageBox mb = new MessageBox(new Shell());
                            mb.setMessage("Error in asynchronous process: " 
                                    + e.getMessage());
                            log.warn("Error in async process", e);
                        }
                    }
                }).start();
            } else {
                shell.getDisplay().asyncExec(getSyncExecutor(cmd));
            }
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
                    output.setText("");
                    final Process p = Runtime.getRuntime().exec(cmd);
                    new Thread(new Runnable() {
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep(9000L);
                                    p.exitValue();
                                    return;
                                } catch (final Exception e) {
                                    log.debug(e.getMessage());
                                }
                                if ((System.currentTimeMillis() - start) 
                                        > 30000) {
                                    p.destroy();
                                    shell.getDisplay().syncExec(new Runnable() {
                                        public void run() {
                                            output.setText(
                                                    "Synchronous process timeout: " 
                                                    + cmd);
                                        }
                                    });
                                    return;
                                } 
                            }
                        }
                    }).start();
                    final InputStream in = p.getInputStream();
                    int c;
                    while ((c = in.read()) != -1) {
                        output.append("" + (char) c);
                    }
                    in.close();
                } catch (final Exception e) {
                    output.setText(e.getMessage());
                }
            }
        };
    }
}
