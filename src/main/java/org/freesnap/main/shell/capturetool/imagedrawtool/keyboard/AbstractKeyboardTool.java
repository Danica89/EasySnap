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

package org.freesnap.main.shell.capturetool.imagedrawtool.keyboard;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.freesnap.main.shell.capturetool.imagedrawtool.AbstractDrawTool;

abstract class AbstractKeyboardTool extends AbstractDrawTool {
    AbstractKeyboardTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    abstract public Image onType(KeyEvent event);

    abstract public void onUndo();

    abstract public void onRedo();
}
