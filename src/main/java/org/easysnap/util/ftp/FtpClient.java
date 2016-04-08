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

package org.easysnap.util.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.easysnap.util.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FtpClient {

    private Config config;

    public FtpClient(Config config) {
        this.config = config;
    }

    /**
     * TODO: dependency injection config
     */
    public boolean upload(File file, String fileName) {
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
            isUploaded = client.storeFile(getUploadPath(fileName), fis);

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

    public String getUploadPath(String fileName) {
        String ftpDirectory = config.getFtpDirectory();
        return (ftpDirectory.endsWith("/") ? ftpDirectory : ftpDirectory + "/") + fileName;
    }

    public boolean canConnect() {
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
