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
