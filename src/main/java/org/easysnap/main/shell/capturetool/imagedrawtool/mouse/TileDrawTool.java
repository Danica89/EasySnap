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

package org.easysnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;

import java.util.ArrayList;
import java.util.Random;

public class TileDrawTool extends AbstractMouseDrawTool {
    private int offset;

    public TileDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onStart(MouseEvent event) {
        offset = (offset < 1) ? 9 : --offset;
        return super.onStart(event);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        Rectangle rectangle = new Rectangle(Math.min(start.x, end.x), Math.min(start.y, end.y), width, height);
        Rectangle intersectRect = image.getBounds().intersection(rectangle);
        width = intersectRect.width;
        height = intersectRect.height;

        if (width > 0 && height > 0) {
            ImageData imageData = new ImageData(width, height, this.image.getImageData().depth, this.image.getImageData().palette);
            ImageData currentImageData = this.image.getImageData();
            int lineWidth = Math.max(2, graphicsContext.getLineWidth());
            int startPosX = intersectRect.x;
            int endPosX = intersectRect.x + intersectRect.width;
            int startPosY = intersectRect.y;
            int endPosY = intersectRect.y + intersectRect.height;
            PaletteData palette = currentImageData.palette;

            ArrayList<Rectangle> grid = new ArrayList<Rectangle>();

            for (int x = (startPosX - startPosX % lineWidth); x < endPosX + offset; x+= lineWidth) {
                for (int y = (startPosY - startPosY % lineWidth); y < endPosY + offset; y+=lineWidth) {
                    if (x % lineWidth != 0 || y % lineWidth != 0) {
                        continue;
                    }
                    Rectangle rect = new Rectangle(x - offset, y - offset, lineWidth, lineWidth);
                    rect = intersectRect.intersection(rect);
                    grid.add(rect);
                }
            }
            int x, y;
            RGB color;
            for (Rectangle rect : grid) {
                color = new RGB(0,0,0);
                for (int i = 0; i < rect.width; i++) {
                    x = rect.x + i;
                    for (int j = 0; j < rect.height; j++) {
                        y = rect.y + j;
                        int pixel = currentImageData.getPixel(x,y);
                        RGB pixelColor = palette.getRGB(pixel);
                        color.red += pixelColor.red;
                        color.green += pixelColor.green;
                        color.blue += pixelColor.blue;
                    }
                }
                double area = (double) rect.width * rect.height;
                color.red = (int) Math.min(255, ((double) color.red) / area);
                color.green = (int) Math.min(255, ((double) color.green) / area);
                color.blue = (int) Math.min(255, ((double) color.blue) / area);

                for (int i = 0; i < rect.width; i++) {
                    x = (rect.x - startPosX) + i;
                    for (int j = 0; j < rect.height; j++) {
                        y = (rect.y - startPosY) + j;
                        imageData.setPixel(x,y, palette.getPixel(color));
                    }
                }
            }

            Image newImage = new Image(this.graphicsContext.getDevice(), imageData);
            this.graphicsContext.drawImage(newImage, intersectRect.x, intersectRect.y);
            newImage.dispose();
        }
        return ret;
    }
}
