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

package org.easysnap.main.shell.capturetool.imagedrawtool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

abstract public class AbstractDrawTool {
    protected final Color shadowColor;
    protected GC graphicsContext;
    protected Image startImage;
    protected Image image;
    protected Color color;
    protected int size;
    protected boolean fill;
    protected Point start;
    private boolean ready = false;

    /**
     * TODO alpha and fill
     */
    public AbstractDrawTool(Image image, Color color, int size/*, int alpha, boolean fill*/) {
        this.startImage = image;
        this.color = color;
        this.size = size;
        this.shadowColor = new Color(Display.getCurrent(), 0, 0, 0);
//        this.alpha = alpha;
//        this.fill = fill;
    }

    protected void initGraphicsContext() {
        if (this.graphicsContext != null && !this.graphicsContext.isDisposed()) {
            this.graphicsContext.dispose();
        }
        if (this.image != null && !this.image.isDisposed()) {
            this.image.dispose();
        }
        this.image = new Image(
                Display.getCurrent(),
                startImage,
                SWT.IMAGE_COPY
        );
        this.graphicsContext = new GC(image);
        graphicsContext.setAdvanced(true);
        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.setAntialias(SWT.ON);
        graphicsContext.setTextAntialias(SWT.ON);
        graphicsContext.setLineWidth(size);
        graphicsContext.setInterpolation(SWT.HIGH);
        graphicsContext.setLineCap(SWT.CAP_ROUND);
        graphicsContext.setLineJoin(SWT.JOIN_ROUND);
    }


    @Override
    protected void finalize() throws Throwable {
        if (this.graphicsContext != null && !this.graphicsContext.isDisposed()) {
            this.graphicsContext.dispose();
        }
        if (this.shadowColor != null && !this.shadowColor.isDisposed()) {
            this.shadowColor.dispose();
        }
        super.finalize();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public void setStartImage(Image startImage) {
        this.startImage = startImage;
    }

    public synchronized Image onStart(MouseEvent event) {
        start = new Point(event.x, event.y);
        ready = true;
        return this.startImage;
    }

    public synchronized Image onFinish(MouseEvent event) {
        if (this.image != null) {
            this.startImage = this.image;
            this.image = null;
        }
        ready = false;
        return this.startImage;
    }

    public boolean isReady() {
        return ready;
    }
}
