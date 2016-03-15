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

package org.freesnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class CropDrawTool extends AbstractMouseDrawTool {
    public CropDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);

        graphicsContext.getLineWidth();
        graphicsContext.setLineWidth(1);
        graphicsContext.setLineWidth(2);

        Color color = new Color(graphicsContext.getDevice(), 0, 0, 0);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.setLineStyle(SWT.LINE_DOT);

        graphicsContext.drawRectangle(
                start.x + 1,
                start.y + 1,
                end.x - start.x,
                end.y - start.y
        );
        color.dispose();
        color = new Color(graphicsContext.getDevice(), 255, 32, 32);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);

        graphicsContext.drawRectangle(
                start.x,
                start.y,
                end.x - start.x,
                end.y - start.y
        );
        color.dispose();
        return ret;
    }

    @Override
    public synchronized Image onFinish(MouseEvent event) {
        Point end = new Point(event.x, event.y);
        Rectangle imageBounds = startImage.getBounds();
        Point start2 = new Point(Math.max(Math.min(start.x, end.x), 0), Math.max(Math.min(start.y, end.y), 0));
        Point end2 = new Point(Math.max(start.x, end.x), Math.max(start.y, end.y));
        Rectangle intersectedRectangle = imageBounds.intersection(new Rectangle(start2.x, start2.y, end2.x - start2.x, end2.y - start2.y));
        if (intersectedRectangle.width > 0 && intersectedRectangle.height > 0) {
            initGraphicsContext();
            image.dispose();
            graphicsContext.dispose();
            image = new Image(
                    Display.getCurrent(),
                    intersectedRectangle.width,
                    intersectedRectangle.height
            );
            graphicsContext = new GC(image);
            graphicsContext.drawImage(
                    startImage,
                    start2.x, start2.y,
                    intersectedRectangle.width, intersectedRectangle.height,
                    0, 0,
                    intersectedRectangle.width, intersectedRectangle.height
            );
        }
        return super.onFinish(event);
    }
}
