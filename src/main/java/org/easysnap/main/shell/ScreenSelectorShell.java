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

package org.easysnap.main.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.easysnap.main.shell.capturetool.AbstractCaptureTool;

public class ScreenSelectorShell {

    private final Display display;
    protected Shell shell;
    private int startX = -1;
    private int endX = -1;
    private int startY = -1;
    private int endY = -1;
    private boolean clicked = false;

    private AbstractCaptureTool panel;
    private long nextDraw = 0;

    public ScreenSelectorShell(Display display) {
        this.display = display;
        init();
    }

    private void initShellListeners() {
        initShellMouseListener();
        initShellCloseListener();
        initShellMouseMoveListener();
        initShellPaintListener();
    }

    private void initShellPaintListener() {
        shell.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (clicked) {
                    Rectangle rect = getRect();
                    e.gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
                    e.gc.setAlpha(64);
                    e.gc.fillRectangle(rect);
                    e.gc.setAlpha(128);
                    e.gc.drawRectangle(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
                    e.gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                    e.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
                    e.gc.drawText(rect.width + " x " + rect.height, rect.x - 1, rect.y - 17);
                }
            }
        });
    }

    private void initShellMouseMoveListener() {
        shell.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent mouseEvent) {
                if (nextDraw < System.nanoTime()) {
                    if (clicked) {
                        endX = mouseEvent.x;
                        endY = mouseEvent.y;
                        shell.redraw();
                    }
                    nextDraw = System.nanoTime() + (1000 * 1000 * 1000 / 60);
                }
            }
        });
    }

    private void initShellCloseListener() {
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                shell.dispose();
            }
        });
    }

    private void initShellMouseListener() {
        shell.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                startX = e.x;
                startY = e.y;
                clicked = true;
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                close();
                endX = e.x;
                endY = e.y;
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle rect = getRect();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        panel.open(rect);
                    }
                });
            }
        });
    }

    private void init() {
        initShell();
        initShellListeners();
    }

    private void initShell() {
        shell = new Shell(display.getActiveShell(), SWT.ON_TOP | SWT.DOUBLE_BUFFERED);
        shell.setLocation(0, 0);
        shell.setVisible(false);
        shell.setBounds(display.getBounds());
        shell.setAlpha(0x55);
        shell.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
        shell.setCursor(new Cursor(display, SWT.CURSOR_CROSS));
    }

    private Rectangle getRect() {
        int x, y, width, height;
        if (startX > endX) {
            x = endX;
            width = startX - endX;
        } else {
            x = startX;
            width = endX - startX;
        }
        if (startY > endY) {
            y = endY;
            height = startY - endY;
        } else {
            y = startY;
            height = endY - startY;
        }
        return new Rectangle(Math.max(0, x), Math.max(0, y), Math.max(1, width), Math.max(1, height));
    }

    private Region getRegion(boolean subRect) {
        Region region = new Region();
        region.add(shell.getBounds());
        if (subRect) {
            region.subtract(getRect());
        }
        return region;
    }

    public void setPanel(AbstractCaptureTool panel) {
        this.panel = panel;
    }

    public void open() {
        shell.setCapture(true);
        shell.forceActive();
        shell.forceFocus();
        shell.setBounds(display.getBounds());
        shell.setVisible(true);
    }

    public void close() {
        shell.setVisible(false);
        clicked = false;
    }
}