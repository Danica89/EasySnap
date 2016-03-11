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
