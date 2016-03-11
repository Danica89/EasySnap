package org.freesnap.main.listener;

import org.eclipse.swt.widgets.Display;
import org.freesnap.FreeSnap;
import org.freesnap.main.shell.ScreenSelectorShell;
import org.freesnap.main.shell.capturetool.ImageCaptureTool;
import org.freesnap.main.shell.capturetool.VideoCaptureTool;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalKeyListener implements NativeKeyListener {
    private static ScreenSelectorShell screenSelectorShell;

    public static void init() {
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

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN
                && ((e.getModifiers() & NativeKeyEvent.SHIFT_L_MASK) == NativeKeyEvent.SHIFT_L_MASK ||
                (e.getModifiers() & NativeKeyEvent.SHIFT_R_MASK) == NativeKeyEvent.SHIFT_R_MASK
        )) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    screenSelectorShell.setPanel(new VideoCaptureTool());
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
                        ImageCaptureTool imageCaptureTool = new ImageCaptureTool(FreeSnap.getConfig());
                        imageCaptureTool.open(Display.getDefault().getBounds());
                    }
                });
                return;
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    screenSelectorShell.setPanel(new ImageCaptureTool(FreeSnap.getConfig()));
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
