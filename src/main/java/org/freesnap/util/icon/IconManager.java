package org.freesnap.util.icon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.freesnap.util.resource.ResourceManager;

public class IconManager {
    private ResourceManager resourceManager;

    public IconManager() {
        this.resourceManager = new ResourceManager();
    }

    public Image[] getIconImages() {
        Image[] images = new Image[7];
        images[0] = createImage("/icon/16.png");
        images[1] = createImage("/icon/24.png");
        images[2] = createImage("/icon/32.png");
        images[3] = createImage("/icon/48.png");
        images[4] = createImage("/icon/64.png");
        images[5] = createImage("/icon/96.png");
        images[6] = createImage("/icon/128.png");
        return images;
    }

    public Image getIconImage(int size) {
        String name = "/icon/" + size + ".png";
        return createImage(name);
    }

    private Image createImage(String name) {
        Image image = new Image(Display.getCurrent(), IconManager.class.getResourceAsStream(name));
        resourceManager.add(image);
        return image;
    }

    public Image getArrowImage() {
        return createImage("/arrow/24.png");
    }

    public Image getPencilImage() {
        return createImage("/pencil/24.png");
    }

    public Image getBlurImage() {
        return createImage("/blur/24.png");
    }

    public Image getTileImage() {
        return createImage("/tile/24.png");
    }

    public Image getCropImage() {
        return createImage("/crop/24.png");
    }

    public Image getLineImage() {
        return createImage("/line/24.png");
    }

    public Image getTextImage() {
        return createImage("/text/24.png");
    }

    public Image getRectangleImage() {
        return createImage("/rectangle/24.png");
    }

    public Image getEllipseImage() {
        return createImage("/ellipse/24.png");
    }

    @Override
    protected void finalize() throws Throwable {
        resourceManager.disposeAll();
        super.finalize();
    }
}
