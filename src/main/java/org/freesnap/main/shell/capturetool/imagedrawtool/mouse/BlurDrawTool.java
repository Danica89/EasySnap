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

package org.freesnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;

import java.util.ArrayList;

/**
 * TODO: optymalizacja
 */
public class BlurDrawTool extends AbstractMouseDrawTool {
    public BlurDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        super.onMove(event);
        Point end = new Point(event.x, event.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        Rectangle rectangle = new Rectangle(Math.min(start.x, end.x), Math.min(start.y, end.y), width, height);
        Rectangle intersectRect = image.getBounds().intersection(rectangle);
        if (intersectRect.width > 0 && intersectRect.height > 0) {
            ImageData imageData = new ImageData(intersectRect.width, intersectRect.height, this.image.getImageData().depth, this.image.getImageData().palette);
            ImageData currentImageData = this.image.getImageData();
            int x = 0;
            for (int i = intersectRect.x; i < intersectRect.x + intersectRect.width; ++i) {
                int y = 0;
                for (int j = intersectRect.y; j < intersectRect.y + intersectRect.height; ++j) {
                    try {
                        imageData.setPixel(x, y++, currentImageData.getPixel(i, j));
                    } catch (IllegalArgumentException ex) {
                        //dupaaaaa
                    }
                }
                x++;
            }
            Image newImage = new Image(graphicsContext.getDevice(), this.blur(imageData, 10));
            graphicsContext.drawImage(newImage, intersectRect.x, intersectRect.y);
            newImage.dispose();
        }
        return image;
    }


    private ImageData blur(final ImageData originalImageData, int radius) {
        /*
         * This method will vertically blur all the pixels in a row at once.
		 * This blurring is performed incrementally to each row.
		 *
		 * In order to vertically blur any given pixel, maximally (radius * 2 +
		 * 1) pixels must be examined. Since each of these pixels exists in the
		 * same column, they span across a series of consecutive rows. These
		 * rows are horizontally blurred before being cached and used as input
		 * for the vertical blur. Blurring a pixel horizontally and then
		 * vertically is equivalent to blurring the pixel with both its
		 * horizontal and vertical neighbours at once.
		 *
		 * Pixels are blurred under the notion of a 'summing scope'. A certain
		 * scope of pixels in a column are summed then averaged to determine a
		 * target pixel's resulting RGB value. When the next lower target pixel
		 * is being calculated, the topmost pixel is removed from the summing
		 * scope (by subtracting its RGB) and a new pixel is added to the bottom
		 * of the scope (by adding its RGB). In this sense, the summing scope is
		 * moving downward.
		 */
        if (radius < 1) {
            return originalImageData;
        }
        // prepare new image data with 24-bit direct palette to hold blurred
        // copy of image
        final ImageData newImageData = new ImageData(originalImageData.width, originalImageData.height, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
        if (radius >= newImageData.height || radius >= newImageData.width) {
            radius = Math.min(newImageData.height, newImageData.width) - 1;
        }
        // initialize cache
        final ArrayList<RGB[]> rowCache = new ArrayList<RGB[]>();
        final int cacheSize = radius * 2 + 1 > newImageData.height ? newImageData.height : radius * 2 + 1; // number
        // of
        // rows
        // of
        // imageData
        // we
        // cache
        int cacheStartIndex = 0; // which row of imageData the cache begins with
        for (int row = 0; row < cacheSize; row++) {
            // row data is horizontally blurred before caching
            rowCache.add(rowCache.size(), blurRow(originalImageData, row, radius));
        }
        // sum red, green, and blue values separately for averaging
        final RGB[] rowRGBSums = new RGB[newImageData.width];
        final int[] rowRGBAverages = new int[newImageData.width];
        int topSumBoundary = 0; // current top row of summed values scope
        int targetRow = 0; // row with RGB averages to be determined
        int bottomSumBoundary = 0; // current bottom row of summed values scope
        int numRows = 0; // number of rows included in current summing scope
        for (int i = 0; i < newImageData.width; i++) {
            rowRGBSums[i] = new RGB(0, 0, 0);
        }
        while (targetRow < newImageData.height) {
            if (bottomSumBoundary < newImageData.height) {
                do {
                    // sum pixel RGB values for each column in our radius scope
                    for (int col = 0; col < newImageData.width; col++) {
                        rowRGBSums[col].red += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].red;
                        rowRGBSums[col].green += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].green;
                        rowRGBSums[col].blue += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].blue;
                    }
                    numRows++;
                    bottomSumBoundary++; // move bottom scope boundary lower
                    if (bottomSumBoundary < newImageData.height && bottomSumBoundary - cacheStartIndex > radius * 2) {
                        // grow cache
                        rowCache.add(rowCache.size(), blurRow(originalImageData, bottomSumBoundary, radius));
                    }
                } while (bottomSumBoundary <= radius); // to initialize
                // rowRGBSums at start
            }
            if (targetRow - topSumBoundary > radius) {
                // subtract values of top row from sums as scope of summed
                // values moves down
                for (int col = 0; col < newImageData.width; col++) {
                    rowRGBSums[col].red -= rowCache.get(topSumBoundary - cacheStartIndex)[col].red;
                    rowRGBSums[col].green -= rowCache.get(topSumBoundary - cacheStartIndex)[col].green;
                    rowRGBSums[col].blue -= rowCache.get(topSumBoundary - cacheStartIndex)[col].blue;
                }
                numRows--;
                topSumBoundary++; // move top scope boundary lower
                rowCache.remove(0); // remove top row which is out of summing
                // scope
                cacheStartIndex++;
            }
            // calculate each column's RGB-averaged pixel
            for (int col = 0; col < newImageData.width; col++) {
                rowRGBAverages[col] = newImageData.palette.getPixel(new RGB(rowRGBSums[col].red / numRows, rowRGBSums[col].green / numRows, rowRGBSums[col].blue / numRows));
            }
            // replace original pixels
            newImageData.setPixels(0, targetRow, newImageData.width, rowRGBAverages, 0);
            targetRow++;
        }
        return newImageData;
    }

    /**
     * Average blurs a given row of image data. Returns the blurred row as a
     * matrix of separated RGB values.
     */
    private RGB[] blurRow(final ImageData originalImageData, final int row, final int radius) {
        final RGB[] rowRGBAverages = new RGB[originalImageData.width]; // resulting
        // rgb
        // averages
        final int[] lineData = new int[originalImageData.width];
        originalImageData.getPixels(0, row, originalImageData.width, lineData, 0);
        int r = 0, g = 0, b = 0; // sum red, green, and blue values separately
        // for averaging
        int leftSumBoundary = 0; // beginning index of summed values scope
        int targetColumn = 0; // column of RGB average to be determined
        int rightSumBoundary = 0; // ending index of summed values scope
        int numCols = 0; // number of columns included in current summing scope
        RGB rgb;
        while (targetColumn < lineData.length) {
            if (rightSumBoundary < lineData.length) {
                // sum RGB values for each pixel in our radius scope
                do {
                    rgb = originalImageData.palette.getRGB(lineData[rightSumBoundary]);
                    r += rgb.red;
                    g += rgb.green;
                    b += rgb.blue;
                    numCols++;
                    rightSumBoundary++;
                } while (rightSumBoundary <= radius); // to initialize summing
                // scope at start
            }
            // subtract sum of left pixel as summing scope moves right
            if (targetColumn - leftSumBoundary > radius) {
                rgb = originalImageData.palette.getRGB(lineData[leftSumBoundary]);
                r -= rgb.red;
                g -= rgb.green;
                b -= rgb.blue;
                numCols--;
                leftSumBoundary++;
            }
            // calculate RGB averages
            rowRGBAverages[targetColumn] = new RGB(r / numCols, g / numCols, b / numCols);
            targetColumn++;
        }
        return rowRGBAverages;
    }
}
