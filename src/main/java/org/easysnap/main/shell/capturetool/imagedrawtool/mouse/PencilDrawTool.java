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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;

public class PencilDrawTool extends AbstractMouseDrawTool {
    private ArrayList<Integer> points;

    public PencilDrawTool(Image image, Color color, int size) {
        super(image, color, size);
        points = new ArrayList<Integer>();
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        points.add(event.x);
        points.add(event.y);

        Color color = graphicsContext.getBackground();

        graphicsContext.setBackground(shadowColor);
        graphicsContext.setForeground(shadowColor);
        int[] pointArr = new int[points.size()];
        for (int i = 0; i < pointArr.length; i++) {
            pointArr[i] = points.get(i);
        }
        graphicsContext.drawPolyline(pointArr);
        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.drawPolyline(pointArr);
        return ret;
    }

    @Override
    public synchronized Image onFinish(MouseEvent event) {
        points.clear();
        return super.onFinish(event);
    }

    @Override
    public synchronized Image onStart(MouseEvent event) {
        points.add(event.x);
        points.add(event.y);
        return super.onStart(event);
    }
}
