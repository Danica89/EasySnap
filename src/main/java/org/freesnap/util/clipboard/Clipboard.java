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

package org.freesnap.util.clipboard;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.freesnap.util.image.Helper;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Clipboard {

    public Clipboard() {
    }

    public void setContent(String content) {
        TextTransfer textTransfer = TextTransfer.getInstance();
        org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
        clipboard.setContents(new Object[]{content}, new Transfer[]{textTransfer});
        clipboard.dispose();
    }

    public void setImage(org.eclipse.swt.graphics.Image image) {
        ImageSelection imgSel = new ImageSelection(Helper.convertToAWT(image.getImageData()));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }

    private class ImageSelection implements Transferable {
        private Image image;

        ImageSelection(Image image) {
            this.image = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
