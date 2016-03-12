/*
 * FreeSnap - multiplatform desktop application to take screenshots.
 *
 *  Copyright (C) 2016 Kamil Karkus
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freesnap.main.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.freesnap.FreeSnap;

public class TrayIconShell {
    protected Shell shell;
    protected Image image;
    private Menu historyMenu = null;
    private MenuItem history;
    private MenuItem exit;
    private TrayItem trayItem;
    private Tray tray;
    private Menu menu;

    public TrayIconShell() {
        initShell();
        initImage();
        initTray();
    }

    private void initTray() {
        this.tray = Display.getCurrent().getSystemTray();
        if (this.tray == null) {
            System.out.println("The system tray is not available");
            return;
        }
        initTrayItem();
    }

    private void initTrayItem() {
        this.trayItem = new TrayItem(tray, SWT.NONE);
        this.trayItem.setImage(this.image);
        this.trayItem.setToolTipText("FreeSnap");
        initTrayItemListeners();
        initMenu();
    }

    private void initMenu() {
        this.menu = new Menu(this.shell, SWT.POP_UP);
        initMenuFileUploadEntry();
        initMenuHistoryEntry();
        new MenuItem(menu, SWT.SEPARATOR);
        initMenuSettingsEntry();
        initMenuAboutEntry();
        new MenuItem(menu, SWT.SEPARATOR);
        initMenuExitEntry();
    }

    private void initMenuExitEntry() {
        exit = new MenuItem(menu, SWT.PUSH);
        exit.setText("E&xit");

        exit.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                System.exit(0);
            }
        });
    }

    private void initMenuHistoryEntry() {
        history = new MenuItem(menu, SWT.CASCADE);
        history.setText("&History");
        history.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (historyMenu != null) {
                    return;
                }
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);

                messageBox.setText("Warning");
                messageBox.setMessage("There is no history right now, try again later ;)");
                messageBox.open();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });
    }

    private void initMenuAboutEntry() {
        exit = new MenuItem(menu, SWT.PUSH);
        exit.setText("&About");

        exit.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int style = SWT.ICON_INFORMATION;
                MessageBox messageBox = new MessageBox(shell, style);
                messageBox.setMessage("Author: Kamil Karkus");
                messageBox.open();
            }
        });
    }

    private void initMenuFileUploadEntry() {
        MenuItem fileUpload = new MenuItem(menu, SWT.PUSH);
        fileUpload.setText("&File upload");

        fileUpload.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = (new FileDialog(shell, SWT.OPEN));
                fileDialog.setText("Select file to upload");
                String file = fileDialog.open();
                FreeSnap.processFile(file);
            }
        });
    }

    private void initMenuSettingsEntry() {
        MenuItem settings = new MenuItem(menu, SWT.PUSH);
        settings.setText("&Settings");

        settings.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FreeSnap.getSettingsShell().show();
            }
        });
    }

    private void initTrayItemListeners() {
        this.trayItem.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
            }
        });
        this.trayItem.addListener(SWT.Hide, new Listener() {
            public void handleEvent(Event event) {
            }
        });

        this.trayItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            }
        });
        this.trayItem.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event event) {
            }
        });
        this.trayItem.addListener(SWT.MenuDetect, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });
    }

    private void initImage() {
        this.image = FreeSnap.getIconManager().getIconImage(32);
    }

    private void initShell() {
        this.shell = new Shell(Display.getCurrent());
        this.shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                shell.dispose();
            }
        });
    }

    public TrayItem getTrayItem() {
        return trayItem;
    }

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void addHistory(String name, final String url) {
        internalAddHistory(name, url);
    }

    public void addHistory(String name, final String url, Image image) {
        MenuItem newHistoryItem = internalAddHistory(name, url);
        newHistoryItem.setImage(image);
    }

    private MenuItem internalAddHistory(String name, final String url) {
        if (historyMenu == null) {
            historyMenu = new Menu(shell, SWT.DROP_DOWN);
            history.setMenu(historyMenu);
        }
        MenuItem newHistoryItem = new MenuItem(historyMenu, SWT.PUSH, 0);
        newHistoryItem.setText(name);
        newHistoryItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                TrayIconShell.this.showToolTip("Url copied!", url);
                FreeSnap.getClipboard().setContent(url);
            }
        });
        return newHistoryItem;
    }

    private void showToolTip(String title, String message) {
        ToolTip tooltip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
        tooltip.setText(title);
        tooltip.setMessage(message);
        trayItem.setToolTip(tooltip);

        tooltip.setVisible(true);
        tooltip.setAutoHide(true);
    }


}
