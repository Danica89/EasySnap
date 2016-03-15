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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class RectangleDrawTool extends AbstractMouseDrawTool {
    public RectangleDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);
        Color color = graphicsContext.getBackground();

        graphicsContext.setBackground(shadowColor);
        graphicsContext.setForeground(shadowColor);

        graphicsContext.drawRoundRectangle(
                start.x + 1,
                start.y + 1,
                (end.x - start.x) + 1,
                (end.y - start.y) + 1,
                5,
                5
        );

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);

        graphicsContext.drawRoundRectangle(
                start.x,
                start.y,
                end.x - start.x,
                end.y - start.y,
                5,
                5
        );
        return ret;
    }
}
