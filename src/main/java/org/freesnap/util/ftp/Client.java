package org.freesnap.util.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.freesnap.util.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Client {

    /**
     * TODO: dependency injection config
     */
    public static boolean upload(Config config, File file, String fileName) {
        FTPClient client = new FTPClient();
        FileInputStream fis = null;
        boolean isUploaded = false;

        try {
            client.connect(config.getFtpServer());

            int reply = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                System.err.println("FTP: server refused connection.");
                return false;
            }

            if (!client.login(config.getFtpUser(), config.getFtpPassword())) {
                client.logout();
                client.disconnect();
                System.err.println("FTP: Login failed.");
                return false;
            }

            client.enterRemotePassiveMode();
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);

            fis = new FileInputStream(file);
            isUploaded = client.storeFile(getUploadPath(config, fileName), fis);

            client.logout();

        } catch (IOException e) {
            System.err.println("FTP: IOException");
            System.err.println(e.getLocalizedMessage());
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isUploaded;
    }

    public static String getUploadPath(Config config, String fileName) {
        String ftpDirectory = config.getFtpDirectory();
        return (ftpDirectory.endsWith("/") ? ftpDirectory : ftpDirectory + "/") + fileName;
    }

    public static boolean canConnect(Config config) {
        try {
            FTPClient client = new FTPClient();
            client.connect(config.getFtpServer());

            int reply = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                System.err.println("FTP: server refused connection.");
                return false;
            }

            if (!client.login(config.getFtpUser(), config.getFtpPassword())) {
                client.logout();
                client.disconnect();
                System.err.println("FTP: Login failed.");
                return false;
            }

            client.enterRemotePassiveMode();
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);

            client.logout();
            return true;
        } catch (IOException e) {
            System.err.println("FTP: IOException");
            System.err.println(e.getLocalizedMessage());
        }
        return false;
    }
}
