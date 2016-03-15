/*
 * FreeSnap - multiplatform desktop application, allows to make, edit and share screenshots.
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

package org.freesnap;

import org.eclipse.swt.widgets.Display;
import org.freesnap.main.listener.GlobalKeyListener;
import org.freesnap.main.shell.SettingsShell;
import org.freesnap.main.shell.TrayIconShell;
import org.freesnap.util.clipboard.ClipboardManager;
import org.freesnap.util.config.Config;
import org.freesnap.util.ftp.FtpClient;
import org.freesnap.util.icon.IconManager;
import org.freesnap.util.processor.Processor;
import org.freesnap.util.tooltip.ToolTipManager;

public class FreeSnap {
    private static Config config;
    private static SettingsShell settingsShell;
    private static TrayIconShell trayIconShell;

    private static ClipboardManager clipboardManager;
    private static IconManager iconManager;
    private static ToolTipManager toolTipManager;
    private static FtpClient client;
    private static Processor processor;

    public static SettingsShell getSettingsShell() {
        return settingsShell;
    }

    public static void main(String[] args) throws Exception {
        initConfig();
        initClipboardManager();
        initIconManager();
        initFtpClient();
        initTrayIcon();
        initToolTipManager();
        initProcessor();
        initGlobalKeyListener();
        initSettingsShell();

        run();
    }

    private static void initProcessor() {
        processor = new Processor(config, clipboardManager, toolTipManager, trayIconShell);
        trayIconShell.setProcessor(processor);
    }

    private static void initFtpClient() {
        client = new FtpClient(config);
    }

    private static void initToolTipManager() {
        toolTipManager = new ToolTipManager(trayIconShell);
    }

    private static void initConfig() {
        config = new Config();
    }

    private static void initIconManager() {
        iconManager = new IconManager();
    }

    private static void initClipboardManager() {
        clipboardManager = new ClipboardManager();
    }

    private static void run() {
        Display display = Display.getCurrent();
        while (!display.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initSettingsShell() {
        settingsShell = new SettingsShell(config, client, iconManager);
        if (config.isInitial()) {
            settingsShell.show();
        }
    }

    private static void initGlobalKeyListener() {
        GlobalKeyListener.init(config, iconManager, processor);
    }

    private static void initTrayIcon() throws Exception {
        trayIconShell = new TrayIconShell(clipboardManager, iconManager);
    }
}