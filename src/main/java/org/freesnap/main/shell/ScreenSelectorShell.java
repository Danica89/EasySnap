package org.freesnap.main.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.freesnap.main.shell.capturetool.AbstractCaptureTool;

public class ScreenSelectorShell {

    protected Shell shell;
    private int startX = -1;
    private int endX = -1;
    private int startY = -1;
    private int endY = -1;
    private boolean clicked = false;

    private AbstractCaptureTool panel;
    private long nextDraw = 0;

    public ScreenSelectorShell() {
        init();
    }

    private void initShellListeners() {
        initShellMouseListener();
        initShellCloseListener();
        initShellMouseMoveListener();
        initShellPaintListener();
    }

    private void initShellPaintListener() {
        this.shell.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent paintEvent) {
                if (ScreenSelectorShell.this.clicked) {
                    ScreenSelectorShell.this.shell.setRegion(ScreenSelectorShell.this.getRegion(true));
                    Rectangle rect = ScreenSelectorShell.this.getRect();
                    paintEvent.gc.drawRectangle(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
                    Display display = Display.getCurrent();
                    paintEvent.gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                    paintEvent.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
                    paintEvent.gc.drawText(rect.width + " x " + rect.height, rect.x - 1, rect.y - 17);
                }
            }
        });
    }

    private void initShellMouseMoveListener() {
        this.shell.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent mouseEvent) {
                if (nextDraw < System.nanoTime()) {
                    if (ScreenSelectorShell.this.clicked) {
                        ScreenSelectorShell.this.endX = mouseEvent.x;
                        ScreenSelectorShell.this.endY = mouseEvent.y;
                        ScreenSelectorShell.this.shell.redraw();
                    }
                    nextDraw = System.nanoTime() + (1000 * 1000 * 1000 / 60);
                }
            }
        });
    }

    private void initShellCloseListener() {
        this.shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                shell.dispose();
            }
        });
    }

    private void initShellMouseListener() {
        this.shell.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseDown(MouseEvent mouseEvent) {
                if (mouseEvent.button != 1) {
                    return;
                }
                ScreenSelectorShell.this.startX = mouseEvent.x;
                ScreenSelectorShell.this.startY = mouseEvent.y;
                ScreenSelectorShell.this.clicked = true;
            }

            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                if (mouseEvent.button != 1) {
                    return;
                }
                ScreenSelectorShell.this.close();
                ScreenSelectorShell.this.endX = mouseEvent.x;
                ScreenSelectorShell.this.endY = mouseEvent.y;
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Rectangle rect = ScreenSelectorShell.this.getRect();
                        ScreenSelectorShell.this.panel.open(rect);
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
        this.shell = new Shell(Display.getCurrent(), SWT.ON_TOP | SWT.NO_TRIM | SWT.APPLICATION_MODAL);
        this.shell.setLocation(0, 0);
        this.shell.setVisible(false);
        this.shell.forceActive();
        this.shell.forceFocus();
        Display display = Display.getCurrent();
        this.shell.setBounds(display.getBounds());
        this.shell.setAlpha(0x55);
        this.shell.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
        this.shell.setCursor(new Cursor(display, SWT.CURSOR_CROSS));
    }

    private Rectangle getRect() {
        int x, y, width, height;
        if (this.startX > this.endX) {
            x = this.endX;
            width = this.startX - this.endX;
        } else {
            x = this.startX;
            width = this.endX - this.startX;
        }
        if (this.startY > this.endY) {
            y = this.endY;
            height = this.startY - this.endY;
        } else {
            y = this.startY;
            height = this.endY - this.startY;
        }
        return new Rectangle(Math.max(0, x), Math.max(0, y), Math.max(1, width), Math.max(1, height));
    }

    private Region getRegion(boolean subRect) {
        Region region = new Region();
        region.add(this.shell.getBounds());
        if (subRect) {
            region.subtract(this.getRect());
        }
        return region;
    }

    public void setPanel(AbstractCaptureTool panel) {
        this.panel = panel;
    }

    public void open() {
        this.shell.setBounds(Display.getCurrent().getBounds());
        this.shell.setVisible(true);
    }

    public void close() {
        this.shell.setVisible(false);
        this.clicked = false;
        this.shell.setRegion(ScreenSelectorShell.this.getRegion(false));
    }
}