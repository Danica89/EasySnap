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

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;

/**
 * TODO: optymalizacja
 */
public class TileDrawTool extends AbstractMouseDrawTool {
    public TileDrawTool(Image image, Color color, int size) {
        super(image, color, size);
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
            int boxSizeX = (int) Math.max(Math.ceil((double) width / (double) lineWidth), 1) + 1;
            int boxSizeY = (int) Math.max(Math.ceil((double) height / (double) lineWidth), 1) + 1;
            RGB[][] colors = new RGB[boxSizeX][boxSizeY];
            int[][] size = new int[boxSizeX][boxSizeY];
            PaletteData palette = currentImageData.palette;
            int x;
            int y;
            int startPosX = intersectRect.x;
            int endPosX = intersectRect.x + intersectRect.width;
            int startPosY = intersectRect.y;
            int endPosY = intersectRect.y + intersectRect.height;

            //TODO: refactor
            for (int i = startPosX; i <= endPosX; ++i) {
                for (int j = startPosY; j <= endPosY; ++j) {
                    try {
                        if (i < 0 || j < 0) {
                            continue;
                        }
                        int pixel = currentImageData.getPixel(i, j);
                        RGB rgb = palette.getRGB(pixel);
                        x = i - startPosX;
                        y = j - startPosY;
                        int inBoxX = (int) Math.round(((double) x) / ((double) lineWidth));
                        int inBoxY = (int) Math.round(((double) y) / ((double) lineWidth));
                        if (inBoxX < 0 || inBoxX > boxSizeX || inBoxY < 0 || inBoxY > boxSizeY) {
                            continue;
                        }
                        if (colors[inBoxX][inBoxY] == null) {
                            size[inBoxX][inBoxY] = 1;
                            colors[inBoxX][inBoxY] = new RGB(rgb.red, rgb.green, rgb.blue);
                            continue;
                        }
                        colors[inBoxX][inBoxY].red += rgb.red;
                        colors[inBoxX][inBoxY].green += rgb.green;
                        colors[inBoxX][inBoxY].blue += rgb.blue;
                        size[inBoxX][inBoxY]++;

                    } catch (IllegalArgumentException ex) {
                        //dupaaaaa
                    }
                }
            }
            iterateOverOuterX(width, height, imageData, lineWidth, lineWidth, boxSizeX, boxSizeY, colors, size, palette);
            Image newImage = new Image(this.graphicsContext.getDevice(), imageData);
            this.graphicsContext.drawImage(newImage, intersectRect.x, intersectRect.y);
            newImage.dispose();
        }
        return ret;
    }

    private void iterateOverOuterX(int width, int height, ImageData imageData, int boxWidth, int boxHeight, int boxSizeX, int boxSizeY, RGB[][] colors, int[][] size, PaletteData palette) {
        int i = 0;
        while (true) {
            iterateOverOuterY(width, height, imageData, boxWidth, boxHeight, boxSizeY, colors[i], size[i], palette, i);

            if (++i >= boxSizeX) {
                return;
            }
        }
    }

    private void iterateOverOuterY(int width, int height, ImageData imageData, int boxWidth, int boxHeight, int boxSizeY, RGB[] color, int[] ints, PaletteData palette, int i) {
        int j = 0;
        while (true) {
            if (color[j] != null) {
                color[j].red = (int) Math.min(255, ((double) color[j].red) / ((double) ints[j]));
                color[j].green = (int) Math.min(255, ((double) color[j].green) / ((double) ints[j]));
                color[j].blue = (int) Math.min(255, ((double) color[j].blue) / ((double) ints[j]));

                iterateOverInnerX(width, height, imageData, boxWidth, boxHeight, palette.getPixel(color[j]), i, j);
            }
            if (++j >= boxSizeY) {
                return;
            }
        }
    }

    private void iterateOverInnerX(int width, int height, ImageData imageData, int boxWidth, int boxHeight, int pixel, int i, int j) {
        int i2 = 0;
        while (true) {
            iterateOverInnerY(width, height, imageData, boxWidth, boxHeight, pixel, i, j, i2);
            if (++i2 >= boxWidth) {
                return;
            }
        }
    }

    private void iterateOverInnerY(int width, int height, ImageData imageData, int boxWidth, int boxHeight, int pixel, int i, int j, int i2) {
        int j2 = 0;
        while (true) {
            calculatePixel(width, height, imageData, boxWidth, boxHeight, pixel, i, j, i2, j2);
            if (++j2 >= boxHeight) {
                return;
            }
        }
    }

    private void calculatePixel(int width, int height, ImageData imageData, int boxWidth, int boxHeight, int pixel, int i, int j, int i2, int j2) {
        int x;
        int y;
        x = i * boxWidth + i2;
        y = j * boxHeight + j2;
        if (x >= width || y >= height) {
            return;
        }
        if (x < 0 || y < 0) {
            return;
        }

        imageData.setPixel(x, y, pixel);
    }
}
