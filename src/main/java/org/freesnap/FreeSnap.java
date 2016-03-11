package org.freesnap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolTip;
import org.freesnap.main.listener.GlobalKeyListener;
import org.freesnap.main.shell.SettingsShell;
import org.freesnap.main.shell.TrayIconShell;
import org.freesnap.util.clipboard.Clipboard;
import org.freesnap.util.config.Config;
import org.freesnap.util.ftp.Client;
import org.freesnap.util.icon.IconManager;
import org.freesnap.util.image.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * TODO refactor this class
 */
public class FreeSnap {
    private static Config config;
    private static SettingsShell settingsShell;
    private static TrayIconShell trayIconShell;

    private static Clipboard clipboard;
    private static IconManager iconManager;

    public static SettingsShell getSettingsShell() {
        return settingsShell;
    }

    public static void main(String[] args) throws InterruptedException {
        initConfig();
        initClipboardManager();
        initIconManager();
        initTrayIcon();
        initGlobalKeyListener();
        initSettingsShell();

        run();
    }

    private static void initConfig() {
        config = new Config();
    }

    private static void initIconManager() {
        iconManager = new IconManager();
    }

    private static void initClipboardManager() {
        clipboard = new Clipboard();
    }

    private static void run() {
        Display display = Display.getCurrent();
        while (!display.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initSettingsShell() {
        settingsShell = new SettingsShell(config);
        if (config.isInitial()) {
            settingsShell.show();
        }
    }

    private static void initGlobalKeyListener() {
        GlobalKeyListener.init();
    }

    private static void initTrayIcon() {
        trayIconShell = new TrayIconShell();
    }

    private static String uploadFile(File file, String remoteFilename) {
        boolean status = Client.upload(config, file, remoteFilename);
        if (status) {
            return Client.getUploadPath(config, remoteFilename);
        }
        return null;
    }

    public static boolean testConnection() {
        return Client.canConnect(config);
    }

    public static void processImage(org.eclipse.swt.graphics.Image image, boolean upload) {
        if (image == null) {
            System.out.println("Image is empty, canceling");
            return;
        }

        File imageFile = saveImageToFile(image);
        String imageUrl;
        if (upload) {
            String hash = generateHashForFile(imageFile);
            String newFilename = hash.substring(0, 10) + ".png";
            File renamedFile = new File(newFilename);
            imageFile = imageFile.renameTo(renamedFile) ? renamedFile : imageFile;
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            imageUrl = uploadFile(imageFile, dateFormat.format(date) + "_" + imageFile.getName());
            if (imageUrl == null) {
                showToolTip("Error!", "Could not process image.");
                return;
            } else {
                imageFile.delete();
            }
            showToolTip("Image uploaded!", imageUrl);
            trayIconShell.addHistory("Uploaded image: " + imageFile.getName(), imageUrl, Helper.resize(image, 50, 50));
        } else {
            imageUrl = imageFile.getAbsolutePath();
            showToolTip("Image saved!", imageUrl);
            trayIconShell.addHistory("Saved image: " + imageFile.getName(), imageUrl, Helper.resize(image, 50, 50));
        }
        clipboard.setContent(imageUrl);
    }

    public static void processImageToClipboard(org.eclipse.swt.graphics.Image image) {
        if (image == null) {
            System.out.println("Image is empty, canceling");
            return;
        }

        clipboard.setImage(image);
        showToolTip("Image copied into clipboard!", "Now you can paste it everywhere you need :)");
    }

    public static void processVideo(String fileName, boolean upload) {
        File videoFile = new File(fileName);
        String videoUrl;
        if (upload) {
            videoUrl = uploadFile(videoFile, fileName);
            if (videoUrl == null) {
                showToolTip("Error!", "Could not process video.");
                return;
            } else {
                videoFile.delete();
            }
            showToolTip("Video uploaded!", videoUrl);
            trayIconShell.addHistory("Uploaded video: " + videoFile.getName(), videoUrl);
        } else {
            videoUrl = videoFile.getAbsolutePath();
            showToolTip("Image saved!", videoUrl);
            trayIconShell.addHistory("Saved video: " + videoFile.getName(), videoUrl);
        }
        clipboard.setContent(videoUrl);
    }

    public static void processFile(String fileName) {
        if ("".equals(fileName) || null == fileName) {
            return;
        }
        File file = new File(fileName);
        String fileUrl;
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String remoteFilename = dateFormat.format(date) + "_" + file.getName();
        remoteFilename = remoteFilename.replaceAll("[^\\x00-\\x7F]", ""); //replace all not ascii with empty
        fileUrl = uploadFile(file, remoteFilename);
        if (fileUrl == null) {
            showToolTip("Error!", "Could not uploaded file.");
            return;
        }
        showToolTip("File uploaded!", fileUrl);
        trayIconShell.addHistory("Uploaded file: " + file.getName(), fileUrl);
        clipboard.setContent(fileUrl);
    }

    private static void showToolTip(String title, String message) {
        ToolTip tooltip = new ToolTip(trayIconShell.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
        tooltip.setText(title);
        tooltip.setMessage(message);
        trayIconShell.getTrayItem().setToolTip(tooltip);

        tooltip.setVisible(true);
        tooltip.setAutoHide(true);
    }

    static File saveImageToFile(org.eclipse.swt.graphics.Image image) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String filename = dateFormat.format(Calendar.getInstance().getTime()) + ".png";
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[]{image.getImageData()};
        loader.save(filename, SWT.IMAGE_PNG);

        return new File(filename);
    }

    private static String generateHashForFile(File file) {
        FileInputStream fis = null;

        try {
            byte[] buf = new byte[1024];

            MessageDigest md = MessageDigest.getInstance("SHA");

            fis = new FileInputStream(file);
            int len;
            while ((len = fis.read(buf)) > 0) {
                md.update(buf, 0, len);
            }

            Formatter formatter = new Formatter();
            for (byte b : md.digest()) {
                formatter.format("%02x", b);
            }

            fis.close();

            return formatter.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public static Clipboard getClipboard() {
        return clipboard;
    }

    public static IconManager getIconManager() {
        return iconManager;
    }

    public static Config getConfig() {
        return config;
    }
}