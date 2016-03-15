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

package org.freesnap.main.shell.capturetool.imagedrawtool.keyboard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class TextDrawTool extends AbstractKeyboardTool {

    private String str = "";
    private String lastStr = "";
    private Font font;

    public TextDrawTool(Image image, Color color, int size) {
        super(image, color, size);
        font = new Font(Display.getDefault(), "Tahoma", size * 2 + 8, SWT.BOLD);
    }

    @Override
    public void setSize(int size) {
        super.setSize(size);
        font.dispose();
        font = new Font(Display.getDefault(), "Tahoma", size * 2 + 8, SWT.BOLD);
    }

    public void clear() {
        this.str = "";
        this.lastStr += "";
    }


    @Override
    public void onUndo() {
        int index = str.length() - 1;
        if (index > 0) {
            str = str.substring(0, str.length() - 1);
            return;
        }
        str = "";
    }

    @Override
    public void onRedo() {
        int index = str.length() + 1;
        if (index < lastStr.length()) {
            str = lastStr.substring(0, index);
        }
    }

    @Override
    public Image onType(KeyEvent event) {
        if (event.keyCode != SWT.CR && !this.isPrintableChar(event.character)) {
            return image;
        }
        initGraphicsContext();
//        graphicsContext.setBackground(null);
        graphicsContext.setFont(font);
        graphicsContext.setLineWidth(2);
//        if (e.keyCode == SWT.BS && this.text.length() > 0) {
//            this.text = this.text.substring(0, this.text.length() - 1);
//            return;
//        }

        Color color = graphicsContext.getBackground();

//        Path path = new Path(Display.getCurrent());
//        path.addString(str, start.x, start.y, font);
//        graphicsContext.setForeground(shadowColor);
//        graphicsContext.setBackground(color);
//        graphicsContext.fillPath(path);
//        graphicsContext.drawPath(path);
//        path.dispose();

        str += event.character;
        lastStr += event.character;
        Point extent = extentText();
        graphicsContext.setBackground(shadowColor);
        graphicsContext.setForeground(shadowColor);
        String tmp = String.valueOf(event.character);
        graphicsContext.drawText(tmp, start.x + 1 + extent.x, start.y + 1 + extent.y, true);
        graphicsContext.drawText(tmp, (start.x - 1) + extent.x, start.y + 1 + extent.y, true);
        graphicsContext.drawText(tmp, (start.x - 1) + extent.x, (start.y - 1) + extent.y, true);
        graphicsContext.drawText(tmp, start.x + 1 + extent.x, (start.y - 1) + extent.y, true);
        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.drawText(tmp, start.x + extent.x, start.y + extent.y, true);

        return image;
    }

    private Point extentText() {
        Point extent = new Point(0, 0);
        String[] lines = str.split(String.valueOf(SWT.CR));
        String lastLine = lines[lines.length - 1];
        lines[lines.length - 1] = lastLine.substring(0, lastLine.length() - 1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Point lineExtent = graphicsContext.textExtent(line);
            extent.x = lineExtent.x;
            if (i > 0) {
                extent.y += lineExtent.y;
            }
        }
        return extent;
    }

    @Override
    protected void finalize() throws Throwable {
        font.dispose();
        super.finalize();
    }

    @Override
    public synchronized Image onStart(MouseEvent event) {
        Image ret = super.onStart(event);
        clear();
        return ret;
    }

    private boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public boolean canUndo() {
        return str.length() > 0;
    }

    @Override
    public void setStartImage(Image startImage) {
        super.setStartImage(startImage);
        initGraphicsContext();
    }
}
