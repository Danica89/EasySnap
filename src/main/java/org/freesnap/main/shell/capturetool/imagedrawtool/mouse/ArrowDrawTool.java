package org.freesnap.main.shell.capturetool.imagedrawtool.mouse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;

public class ArrowDrawTool extends AbstractMouseDrawTool {

    public ArrowDrawTool(Image image, Color color, int size) {
        super(image, color, size);
    }

    @Override
    public synchronized Image onMove(MouseEvent event) {
        Image ret = super.onMove(event);
        Point end = new Point(event.x, event.y);
        drawArrow(end);
        return ret;
    }

    private void drawArrow(Point end) {
        //workaround
        graphicsContext.getGCData().state |= 1 << 14;
        graphicsContext.setLineAttributes(new LineAttributes(1, SWT.CAP_FLAT, SWT.JOIN_MITER));
        //end

        int lineWidth = graphicsContext.getLineWidth();
        graphicsContext.setLineWidth(1);

        int x1 = start.x;
        int y1 = start.y;
        int x2 = end.x;
        int y2 = end.y;

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.toDegrees(Math.atan2(dy, dx));

        int len = (int) Math.sqrt(dx * dx + dy * dy);
        int size = len / 6;

        Color color = graphicsContext.getBackground();

        graphicsContext.setBackground(shadowColor);
        graphicsContext.setForeground(shadowColor);

        Transform transform = new Transform(graphicsContext.getDevice());
        transform.translate(x1 + 1, y1 + 1);
        transform.rotate((float) angle);
        graphicsContext.setTransform(transform);

        graphicsContext.drawLine(0, 0, len, 0);
        graphicsContext.fillPolygon(new int[]{0, 0, len, size / 3, len, 0, len, -(size / 3)});
        graphicsContext.fillPolygon(new int[]{len - 1, 0, len - 1 - size / 2, size, len - 1 + size, 0, len - 1 - size / 2, -size});

        graphicsContext.setLineWidth(lineWidth);

        graphicsContext.setBackground(color);
        graphicsContext.setForeground(color);

        transform.dispose();
        transform = new Transform(graphicsContext.getDevice());
        transform.translate(x1, y1);
        transform.rotate((float) angle);
        graphicsContext.setTransform(transform);

        graphicsContext.drawLine(0, 0, len, 0);
        graphicsContext.fillPolygon(new int[]{0, 0, len, size / 3, len, 0, len, -(size / 3)});
        graphicsContext.fillPolygon(new int[]{len - 1, 0, len - 1 - size / 2, size, len - 1 + size, 0, len - 1 - size / 2, -size});

        graphicsContext.setLineWidth(lineWidth);
        transform.dispose();
    }
}
