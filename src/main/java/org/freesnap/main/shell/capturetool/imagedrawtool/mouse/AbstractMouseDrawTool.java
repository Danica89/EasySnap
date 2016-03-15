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
import org.freesnap.main.shell.capturetool.imagedrawtool.AbstractDrawTool;

abstract public class AbstractMouseDrawTool extends AbstractDrawTool {

    public AbstractMouseDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    public synchronized Image onMove(MouseEvent event) {
        initGraphicsContext();
        return image;
    }
}
