package org.freesnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class CropDrawTool extends AbstractMouseDrawTool {
    public CropDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);

        graphicsContext.getLineWidth();
        graphicsContext.setLineWidth(1);
        graphicsContext.setLineWidth(2);

        Color color = new Color(graphicsContext.getDevice(), 0, 0, 0);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);
        graphicsContext.setLineStyle(SWT.LINE_DOT);

        graphicsContext.drawRectangle(
                start.x + 1,
                start.y + 1,
                end.x - start.x,
                end.y - start.y
        );
        color.dispose();
        color = new Color(graphicsContext.getDevice(), 255, 32, 32);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);

        graphicsContext.drawRectangle(
                start.x,
                start.y,
                end.x - start.x,
                end.y - start.y
        );
        color.dispose();
        return ret;
    }

    @Override
    public synchronized Image onFinish(MouseEvent event) {
        Point end = new Point(event.x, event.y);
        Rectangle imageBounds = startImage.getBounds();
        Point start2 = new Point(Math.max(Math.min(start.x, end.x), 0), Math.max(Math.min(start.y, end.y), 0));
        Point end2 = new Point(Math.max(start.x, end.x), Math.max(start.y, end.y));
        Rectangle intersectedRectangle = imageBounds.intersection(new Rectangle(start2.x, start2.y, end2.x - start2.x, end2.y - start2.y));
        if (intersectedRectangle.width > 0 && intersectedRectangle.height > 0) {
            initGraphicsContext();
            image.dispose();
            graphicsContext.dispose();
            image = new Image(
                    Display.getCurrent(),
                    intersectedRectangle.width,
                    intersectedRectangle.height
            );
            graphicsContext = new GC(image);
            graphicsContext.drawImage(
                    startImage,
                    start2.x, start2.y,
                    intersectedRectangle.width, intersectedRectangle.height,
                    0, 0,
                    intersectedRectangle.width, intersectedRectangle.height
            );
        }
        return super.onFinish(event);
    }
}
