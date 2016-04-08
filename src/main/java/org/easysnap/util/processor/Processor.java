/*
 * EasySnap - multiplatform desktop application, allows to capture screen as screen or video, edit it and share.
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

package org.easysnap.util.processor;

import org.eclipse.swt.graphics.Image;
import org.easysnap.main.shell.TrayIconShell;
import org.easysnap.util.clipboard.ClipboardManager;
import org.easysnap.util.config.Config;
import org.easysnap.util.ftp.FtpClient;
import org.easysnap.util.image.ImageHelper;
import org.easysnap.util.tooltip.ToolTipManager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Processor {
    private TrayIconShell shell;
    private Config config;
    private FtpClient client;
    private ToolTipManager toolTipManager;
    private ClipboardManager clipboardManager;

    public Processor(Config config, ClipboardManager clipboardManager, ToolTipManager toolTipManager, TrayIconShell shell) {
        initConfig(config);
        initFtpClient();
        initClipboardManager(clipboardManager);
        initToolTipManager(toolTipManager);
        initShell(shell);
    }

    private void initShell(TrayIconShell shell) {
        this.shell = shell;
    }

    private void initToolTipManager(ToolTipManager toolTipManager) {
        this.toolTipManager = toolTipManager;
    }

    private void initConfig(Config config) {
        this.config = config;
    }

    private void initClipboardManager(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    private void initFtpClient() {
        this.client = new FtpClient(this.config);
    }

    public boolean upload(Image image) {
        if (null == image) {
            return false;
        }
        String filename = generateFilename();
        File imageFile = ImageHelper.saveImageToFile(image, filename);
        String url = upload(imageFile, imageFile.getName());
        if (!imageFile.delete()) {
            System.err.println("Could not delete tmp file " + imageFile.getAbsolutePath());
        }
        if (null == url) {
            toolTipManager.show("Error!", "Could not process image.");
            return false;
        }
        toolTipManager.show("Image uploaded!", url);
        shell.addHistory("Uploaded image: " + imageFile.getName(), url, ImageHelper.resize(image, 50, 50));
        clipboardManager.setContent(url);
        return true;
    }

    public boolean save(Image image) {
        if (null == image) {
            return false;
        }
        String filename = config.getSavePath() + "/" + generateFilename();
        File imageFile = ImageHelper.saveImageToFile(image, filename);
        String filePath = imageFile.getAbsolutePath();
        toolTipManager.show("Image saved!", filePath);
        shell.addHistory("Uploaded image: " + imageFile.getName(), filePath, ImageHelper.resize(image, 50, 50));
        clipboardManager.setContent(filePath);
        return true;
    }

    public boolean clipboard(Image image) {
        if (null == image) {
            System.out.println("Image is empty, canceling");
            return false;
        }

        clipboardManager.setImage(image);
        toolTipManager.show("Image copied into clipboard!", "Now you can paste it everywhere you need :)");
        return true;
    }

    public boolean upload(String filename) {
        if ("".equals(filename) || null == filename) {
            return false;
        }
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("File not found: " + filename);
            return false;
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String remoteFilename = dateFormat.format(date) + "_" + file.getName();
        remoteFilename = remoteFilename.replaceAll("[^\\x00-\\x7F]", ""); //cut all non ascii
        String fileUrl = upload(file, remoteFilename);
        if (null == fileUrl) {
            toolTipManager.show("Error!", "Could not uploaded file.");
            return false;
        }
        toolTipManager.show("File uploaded!", fileUrl);
        shell.addHistory("Uploaded file: " + file.getName(), fileUrl);
        clipboardManager.setContent(fileUrl);
        return false;
    }

    public boolean save(String filename, boolean move) {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("File not found: " + filename);
            return false;
        }
        if (move) {
            String outputFilename = config.getSavePath() + "/" + file.getName();
            File dest = new File(outputFilename);
            if (!file.renameTo(dest)) {
                System.err.println("Could not rename file: " + filename + " into " + outputFilename);
                return false;
            }
            toolTipManager.show("File saved!", dest.getAbsolutePath());
            shell.addHistory("Saved file: " + dest.getName(), dest.getAbsolutePath());
            clipboardManager.setContent(dest.getAbsolutePath());
            return true;
        }
        toolTipManager.show("File saved!", file.getName());
        shell.addHistory("Saved file: " + file.getName(), file.getAbsolutePath());
        clipboardManager.setContent(file.getAbsolutePath());
        return true;
    }

    public boolean clipboard(String file) {
        return false;
    }

    private String upload(File file, String remoteFilename) {
        boolean status = client.upload(file, remoteFilename);
        if (status) {
            return client.getUploadPath(remoteFilename);
        }
        return null;
    }

    private String generateFilename() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return dateFormat.format(Calendar.getInstance().getTime()) + ".png";
    }
}
