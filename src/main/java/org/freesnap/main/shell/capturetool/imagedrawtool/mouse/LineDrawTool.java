package org.freesnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class LineDrawTool extends AbstractMouseDrawTool {
    public LineDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);
        Color color = graphicsContext.getBackground();

        graphicsContext.setBackground(shadowColor);
        graphicsContext.setForeground(shadowColor);

        graphicsContext.drawLine(start.x + 1, start.y + 1, end.x + 1, end.y + 1);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.drawLine(start.x, start.y, end.x, end.y);
        return ret;
    }
}
