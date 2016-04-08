/*
 * EasySnap - multiplatform desktop application, allows to capture screen as screen or video, edit it and share.
 *
 * Copyright (C) 2016 Kamil Karkus
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

package org.easysnap.main.listener;

import org.eclipse.swt.widgets.Display;
import org.easysnap.main.shell.ScreenSelectorShell;
import org.easysnap.main.shell.capturetool.ImageCaptureTool;
import org.easysnap.main.shell.capturetool.VideoCaptureTool;
import org.easysnap.util.config.Config;
import org.easysnap.util.icon.IconManager;
import org.easysnap.util.processor.Processor;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalKeyListener implements NativeKeyListener {
    private static ScreenSelectorShell screenSelectorShell;
    private static Config config;
    private static IconManager iconManager;
    private static Processor processor;

    public static void init(Config config, IconManager iconManager, Processor processor) {
        setConfig(config);
        setIconManager(iconManager);
        setProcessor(processor);
        setupLogs();
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                screenSelectorShell = new ScreenSelectorShell();
            }
        });

        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    }

    private static void setupLogs() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            handler.setLevel(Level.OFF);
        }
    }

    private static void setConfig(Config config) {
        GlobalKeyListener.config = config;
    }

    private static void setProcessor(Processor processor) {
        GlobalKeyListener.processor = processor;
    }

    private static void setIconManager(IconManager iconManager) {
        GlobalKeyListener.iconManager = iconManager;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN
                && ((e.getModifiers() & NativeKeyEvent.SHIFT_L_MASK) == NativeKeyEvent.SHIFT_L_MASK ||
                (e.getModifiers() & NativeKeyEvent.SHIFT_R_MASK) == NativeKeyEvent.SHIFT_R_MASK
        )) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    screenSelectorShell.setPanel(new VideoCaptureTool(processor));
                    screenSelectorShell.open();
                }
            });
        } else if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
            if ((e.getModifiers() & NativeKeyEvent.CTRL_L_MASK) == NativeKeyEvent.CTRL_L_MASK ||
                    (e.getModifiers() & NativeKeyEvent.CTRL_R_MASK) == NativeKeyEvent.CTRL_R_MASK
                    ) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        ImageCaptureTool imageCaptureTool = new ImageCaptureTool(config, iconManager, processor);
                        imageCaptureTool.open(Display.getDefault().getBounds());
                    }
                });
                return;
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    screenSelectorShell.setPanel(new ImageCaptureTool(config, iconManager, processor));
                    screenSelectorShell.open();
                }
            });
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    screenSelectorShell.close();
                }
            });
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}
