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

package org.freesnap.main.shell.capturetool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.freesnap.FreeSnap;
import org.freesnap.main.shell.capturetool.imagedrawtool.AbstractDrawTool;
import org.freesnap.main.shell.capturetool.imagedrawtool.keyboard.TextDrawTool;
import org.freesnap.main.shell.capturetool.imagedrawtool.mouse.*;
import org.freesnap.util.config.Config;
import org.freesnap.util.resource.ResourceManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class ImageCaptureTool extends AbstractCaptureTool {
    protected Shell shell;
    protected Image image;
    protected Color color;
    protected int size;
    private AbstractDrawTool drawTool;
    private long nextDraw = 0;
    private Rectangle rect;
    private ScrolledComposite imageScrolledComposite;
    private Canvas canvas;
    private HistoryManager historyManager;
    private ResourceManager resourceManager;
    private DrawToolManager drawToolManager;
    private Cursor cursor;
    private Config config;

    public ImageCaptureTool(Config config) {
        this.config = config;
        this.color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        this.size = 8;
        this.historyManager = new HistoryManager();
        this.resourceManager = new ResourceManager();
    }

    public void open(Rectangle rect) {
        this.rect = rect;
        this.preSetupShell();
        this.setupTopToolBar();
        this.setupCanvas();
        this.setupBottomToolBar();
        this.postSetupShell();
    }

    private void fitSize() {
        Point shellSize = this.shell.getSize();
        Rectangle clientArea = Display.getCurrent().getPrimaryMonitor().getBounds();
        Point diff = new Point(0, 0);
        if (shellSize.x > clientArea.width) {
            diff.x = shellSize.x - clientArea.width;
            shellSize.x = clientArea.width;
        }
        if (shellSize.y > clientArea.height) {
            diff.y = shellSize.y - clientArea.height;
            shellSize.y = clientArea.height;
        }
        this.shell.setSize(shellSize);
        this.shell.layout(true, true);
    }

    private void fitLocation() {
        Monitor primary = Display.getCurrent().getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = this.shell.getBounds();

        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;

        this.shell.setLocation(x, y);
    }

    private void fitSizeAndLocation() {
        this.shell.pack();
        this.fitSize();
        this.fitLocation();
    }

    private void postSetupShell() {
        this.fitSizeAndLocation();
        this.shell.open();
    }

    private void preSetupShell() {
        loadConfiguration();

        GC gc = new GC(Display.getCurrent());
        this.resourceManager.add(gc);
        this.image = new Image(Display.getCurrent(), this.rect.width, this.rect.height);
        gc.copyArea(this.image, rect.x, rect.y);
        gc.dispose();
        this.historyManager.add(image);
        setupShell();
        setupShellListeners();

        this.drawToolManager = new DrawToolManager(image, color, size);
    }

    private void setupShell() {
        this.shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
        GridLayout layout = new GridLayout();
        this.shell.setLayout(layout);
        this.shell.setMinimumSize(400, 200);
        this.shell.setText("FreeSnap");
        this.shell.setImages(FreeSnap.getIconManager().getIconImages());
    }

    private void setupShellListeners() {
        this.shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event e) {
                canvas.dispose();
                resourceManager.disposeAll();
                historyManager.disposeAll();
                drawToolManager.clear();
                if (null != cursor && !cursor.isDisposed()) {
                    cursor.dispose();
                }
            }
        });
    }

    private Image getColorImage() {
        Image image = new Image(Display.getCurrent(), 24, 24);
        resourceManager.add(image);
        GC gc = new GC(image);
        gc.setBackground(ImageCaptureTool.this.color);
        gc.fillRectangle(0, 0, 24, 24);
        gc.dispose();
        return image;
    }

    private void loadConfiguration() {
        this.color = new Color(Display.getCurrent(), config.getColorR(), config.getColorG(), config.getColorB());
        this.resourceManager.add(this.color);
        this.size = config.getSize();
    }

    private void setupBottomToolBar() {
        ToolBar bottomToolBar = new ToolBar(this.shell, SWT.WRAP | SWT.RIGHT);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
//        gridData.heightHint = 48;
        bottomToolBar.setLayoutData(gridData);

        final ToolItem color = new ToolItem(bottomToolBar, SWT.PUSH);
        color.setImage(this.getColorImage());
        color.setText("Color");
        color.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ColorDialog colorDialog = new ColorDialog(ImageCaptureTool.this.shell);
                colorDialog.setText("Choose a Color");
                RGB rgb = colorDialog.open();
                if (rgb != null) {
                    ImageCaptureTool.this.color = new Color(Display.getCurrent(), rgb);
                    ImageCaptureTool.this.drawToolManager.setColor(ImageCaptureTool.this.color);
                    resourceManager.add(ImageCaptureTool.this.color);
                    color.setImage(ImageCaptureTool.this.getColorImage());
                    config.setColorR(rgb.red);
                    config.setColorG(rgb.green);
                    config.setColorB(rgb.blue);
                    config.save();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        final ToolItem size = new ToolItem(bottomToolBar, SWT.DROP_DOWN);
        size.setText("Size " + this.size);
        SizeDropDownSelectionListener sizeSelectionListener = new SizeDropDownSelectionListener(size);
        for (int i = 1; i < 5; ++i) {
            sizeSelectionListener.add("" + i);
        }
        for (int i = 5; i <= 10; i += 2) {
            sizeSelectionListener.add("" + i);
        }
        for (int i = 10; i <= 20; i += 5) {
            sizeSelectionListener.add("" + i);
        }
        for (int i = 20; i <= 40; i += 10) {
            sizeSelectionListener.add("" + i);
        }
        size.addSelectionListener(sizeSelectionListener);

        ToolItem text = new ToolItem(bottomToolBar, SWT.RADIO);
        text.setImage(FreeSnap.getIconManager().getTextImage());
        text.setToolTipText("Text");
        text.setWidth(32);
        text.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.TEXT);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem arrow = new ToolItem(bottomToolBar, SWT.RADIO);
        arrow.setImage(FreeSnap.getIconManager().getArrowImage());
        arrow.setToolTipText("Arrow");
        arrow.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.ARROW);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem pencil = new ToolItem(bottomToolBar, SWT.RADIO);
        pencil.setImage(FreeSnap.getIconManager().getPencilImage());
        pencil.setToolTipText("Pencil");
        pencil.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.PENCIL);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem line = new ToolItem(bottomToolBar, SWT.RADIO);
        line.setImage(FreeSnap.getIconManager().getLineImage());
        line.setToolTipText("Line");
        line.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.LINE);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem rectangle = new ToolItem(bottomToolBar, SWT.RADIO);
        rectangle.setImage(FreeSnap.getIconManager().getRectangleImage());
        rectangle.setToolTipText("Rectangle");
        rectangle.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.RECTANGLE);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem ellipse = new ToolItem(bottomToolBar, SWT.RADIO);
        ellipse.setImage(FreeSnap.getIconManager().getEllipseImage());
        ellipse.setToolTipText("Ellipse");
        ellipse.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.ELLIPSE);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem blur = new ToolItem(bottomToolBar, SWT.RADIO);
        blur.setImage(FreeSnap.getIconManager().getBlurImage());
        blur.setToolTipText("Blur");
        blur.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.BLUR);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem tile = new ToolItem(bottomToolBar, SWT.RADIO);
        tile.setImage(FreeSnap.getIconManager().getTileImage());
        tile.setToolTipText("Tile Effect");
        tile.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.TILE);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

        ToolItem crop = new ToolItem(bottomToolBar, SWT.RADIO);
        crop.setImage(FreeSnap.getIconManager().getCropImage());
        crop.setToolTipText("Crop Image");
        crop.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                setDrawTool(DrawToolType.CROP);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });

    }

    private void setDrawTool(DrawToolType type) {
        drawTool = drawToolManager.get(type);
        drawTool.setStartImage(image);
        ((TextDrawTool) drawToolManager.get(DrawToolType.TEXT)).clear();
    }

    private void doRedo() {
        try {
            Image tmpImage = historyManager.next();
            if (tmpImage != image) {
                image = tmpImage;
                if (drawTool instanceof TextDrawTool) {
                    ((TextDrawTool) drawTool).onRedo();
                }
                redrawImage();
                if (drawTool != null) {
                    drawTool.setStartImage(this.image);
                }
            }
        } catch (IndexOutOfBoundsException ignored) {

        }
    }

    private void doUndo() {
        try {
            Image tmpImage = historyManager.prev();
            if (tmpImage != image) {
                image = tmpImage;
                if (drawTool instanceof TextDrawTool) {
                    ((TextDrawTool) drawTool).onUndo();
                }
                redrawImage();
                if (drawTool != null) {
                    drawTool.setStartImage(this.image);
                }
            }
        } catch (IndexOutOfBoundsException ignored) {

        }
    }

    private void setupTopToolBar() {
        Display.getCurrent().addFilter(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e) {
                if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'z')) {
                    ImageCaptureTool.this.doUndo();
                }

                if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'y')) {
                    ImageCaptureTool.this.doRedo();
                }
            }
        });
        ToolBar topToolBar = new ToolBar(this.shell, SWT.WRAP);
        topToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ToolItem undo = new ToolItem(topToolBar, SWT.PUSH);
        undo.setText("Undo");
        undo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ImageCaptureTool.this.doUndo();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });
        ToolItem redo = new ToolItem(topToolBar, SWT.PUSH);
        redo.setText("Redo");
        redo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                ImageCaptureTool.this.doRedo();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });
        new ToolItem(topToolBar, SWT.SEPARATOR);
        ToolItem save = new ToolItem(topToolBar, SWT.PUSH);
        save.setText("Save");
        save.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                FreeSnap.processImage(ImageCaptureTool.this.image, false);
                shell.dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });
        ToolItem upload = new ToolItem(topToolBar, SWT.PUSH);
        upload.setText("Upload");
        upload.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                FreeSnap.processImage(ImageCaptureTool.this.image, true);
                shell.dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });
        new ToolItem(topToolBar, SWT.SEPARATOR);
        ToolItem copyToClipboard = new ToolItem(topToolBar, SWT.PUSH);
        copyToClipboard.setText("Copy to clipboard");
        copyToClipboard.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                FreeSnap.processImageToClipboard(image);
                shell.dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            }
        });
    }

    private void setupCanvas() {
        imageScrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        imageScrolledComposite.setLayout(new GridLayout());
        imageScrolledComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        imageScrolledComposite.setExpandVertical(true);
        imageScrolledComposite.setExpandHorizontal(true);
        canvas = new Canvas(imageScrolledComposite, SWT.NONE);
        imageScrolledComposite.setMinSize(image.getBounds().width, image.getBounds().height);
        imageScrolledComposite.setContent(canvas);

        setupCanvasListeners();
    }

    private void redrawImage() {
        canvas.redraw();
        //resize component
        imageScrolledComposite.setMinSize(image.getBounds().width, image.getBounds().height);
        //update layout
        shell.layout(true, true);
    }

    private void setupCanvasListeners() {
        setupCanvasPaintListener();
        setupCanvasMouseListener();
        setupCanvasMouseMoveListener();
        setupCanvasKeyListener();
    }

    private void setupCanvasPaintListener() {
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                e.gc.drawImage(image, 0, 0);
            }
        });
    }

    private void setupCanvasKeyListener() {
        this.canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.keyCode == SWT.BS) {
                    if (drawTool instanceof TextDrawTool && ((TextDrawTool) ImageCaptureTool.this.drawTool).canUndo()) {
                        doUndo();
                        keyEvent.doit = true;
                        return;
                    }
                }
                if ((keyEvent.stateMask & SWT.CTRL) == SWT.CTRL || keyEvent.keyCode == SWT.CTRL) {
                    if (keyEvent.keyCode == 'z') {
                        return;
                    }

                    if (keyEvent.keyCode == 'y') {
                        return;
                    }
                    return;
                }

                if (ImageCaptureTool.this.drawTool instanceof TextDrawTool) {
                    try {
                        image = new Image(Display.getCurrent(), ((TextDrawTool) ImageCaptureTool.this.drawTool).onType(keyEvent), SWT.IMAGE_COPY);
                        historyManager.add(image);
                        drawTool.setStartImage(image);
                        redrawImage();
                    } catch (Exception ignored) {

                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
    }

    private void setupCanvasMouseMoveListener() {
        this.canvas.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent mouseEvent) {
                if (drawTool == null || nextDraw > System.nanoTime()) {
                    return;
                }
                if (drawTool instanceof AbstractMouseDrawTool && drawTool.isReady()) {
                    ImageCaptureTool.this.image = ((AbstractMouseDrawTool) drawTool).onMove(mouseEvent);
                    redrawImage();
                }

                nextDraw = System.nanoTime() + (1000 * 1000 * 1000 / 60);
            }
        });
    }

    private void setupCanvasMouseListener() {
        this.canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseDown(MouseEvent mouseEvent) {
                if (drawTool == null) {
                    return;
                }
//                if (drawTool instanceof TextDrawTool) {
//                    ((TextDrawTool) drawTool).clear();
////                    ImageCaptureTool.this.buffImage = ImageCaptureTool.this.image;
////                    ImageCaptureTool.this.image = new Image(ImageCaptureTool.this.display, ImageCaptureTool.this.buffImage, SWT.IMAGE_COPY);
////                    resources.add(image);
////                    createNewHistoryEntry(ImageCaptureTool.this.buffImage);
//                    draw(true);
//                    return;
//                }

                ImageCaptureTool.this.image = ImageCaptureTool.this.drawTool.onStart(mouseEvent);
                resourceManager.add(image);
                redrawImage();
                cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
                shell.setCursor(cursor);
            }

            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                Cursor oldCursor = cursor;
                cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);
                shell.setCursor(cursor);
                if (!oldCursor.isDisposed()) {
                    oldCursor.dispose();
                }
                if (drawTool == null) {
                    return;
                }
//                if (ImageCaptureTool.this.drawTool instanceof TextDrawTool) {
//                    return;
//                }
                //TODO: te linijki poniżej są powtarzalne więc refaktor
                ImageCaptureTool.this.image = ImageCaptureTool.this.drawTool.onFinish(mouseEvent);
                historyManager.add(ImageCaptureTool.this.image);
                redrawImage();
            }
        });
    }

    public void setSize(int size) {
        this.size = size;
        config.setSize(size);
        config.save();
    }

    private enum DrawToolType {
        TEXT, ARROW, PENCIL, LINE, RECTANGLE, ELLIPSE, BLUR, TILE, CROP
    }

    private class SizeDropDownSelectionListener extends SelectionAdapter {
        private ToolItem dropdown;
        private Menu menu;

        SizeDropDownSelectionListener(ToolItem dropdown) {
            this.dropdown = dropdown;
            menu = new Menu(dropdown.getParent().getShell());
        }

        void add(String item) {
            MenuItem menuItem = new MenuItem(menu, SWT.NONE);
            menuItem.setText(item);
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    MenuItem selected = (MenuItem) event.widget;
                    dropdown.setText("Size " + selected.getText());
                    ImageCaptureTool.this.setSize(Integer.parseInt(selected.getText()));
                    ImageCaptureTool.this.drawToolManager.setSize(Integer.parseInt(selected.getText()));
                }
            });
        }

        public void widgetSelected(SelectionEvent event) {
            if (event.detail == SWT.ARROW) {
                ToolItem item = (ToolItem) event.widget;
                Rectangle rect = item.getBounds();
                Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
                menu.setLocation(pt.x, pt.y + rect.height);
                menu.setVisible(true);
            }
        }
    }

    /**
     * TODO: new history manager should store draw tool and events instead of images
     */
    private class HistoryManager {
        ArrayList<Image> list;
        int iterator;

        HistoryManager() {
            this.list = new ArrayList<Image>();
            this.iterator = 0;
        }

        void add(Image image) {
            if (list.size() > 0 && (iterator + 1) != list.size()) {
                int size = list.size();
                for (int i = size - 1; i >= (iterator + 1); i--) {
                    remove(i);
                }
            }
            if (list.size() >= getLimit()) {
                remove(0);
            }
            list.add(image);
            iterator = list.size() - 1;
        }

        int getLimit() {
            return 25; //TODO: make it configurable
        }

        void disposeAll() {
            for (Image image : list) {
                if (!image.isDisposed()) {
                    image.dispose();
                }
            }
            list.clear();
        }

        Image prev() {
            if (iterator <= 0) {
                return list.get(0);
            }
            return list.get(--iterator);
        }

        Image next() {
            if ((iterator + 1) >= list.size()) {
                return list.get(list.size() - 1);
            }
            return list.get(++iterator);
        }

        void remove(int index) {
            if (!list.get(index).isDisposed()) {
                list.get(index).dispose();
            }
            list.remove(index);
        }
    }

    private class DrawToolManager {
        Map<DrawToolType, AbstractDrawTool> list;

        DrawToolManager(Image image, Color color, int size) {
            list = new EnumMap<DrawToolType, AbstractDrawTool>(DrawToolType.class);
            list.put(DrawToolType.TEXT, new TextDrawTool(image, color, size));
            list.put(DrawToolType.ARROW, new ArrowDrawTool(image, color, size));
            list.put(DrawToolType.PENCIL, new PencilDrawTool(image, color, size));
            list.put(DrawToolType.LINE, new LineDrawTool(image, color, size));
            list.put(DrawToolType.RECTANGLE, new RectangleDrawTool(image, color, size));
            list.put(DrawToolType.ELLIPSE, new EllipseDrawTool(image, color, size));
            list.put(DrawToolType.BLUR, new BlurDrawTool(image, color, size));
            list.put(DrawToolType.TILE, new TileDrawTool(image, color, size));
            list.put(DrawToolType.CROP, new CropDrawTool(image, color, size));
        }

        public void setSize(int size) {
            for (AbstractDrawTool drawTool : list.values()) {
                drawTool.setSize(size);
            }
        }

        public void setColor(Color color) {
            for (AbstractDrawTool drawTool : list.values()) {
                drawTool.setColor(color);
            }
        }

        AbstractDrawTool get(DrawToolType type) {
            return list.get(type);
        }

        void clear() {
            list.clear();
        }
    }
}
