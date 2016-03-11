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
