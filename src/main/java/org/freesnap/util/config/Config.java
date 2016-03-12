/*
 * FreeSnap - multiplatform desktop application to take screenshots.
 *
 *  Copyright (C) 2016 Kamil Karkus
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freesnap.util.config;

import org.freesnap.FreeSnap;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Config {
    private boolean isInitial;
    private String ftpServer;
    private String ftpUser;
    private String ftpPassword;
    private String ftpDirectory;
    private String url;
    private int colorR;
    private int colorG;
    private int colorB;
    private int size;
    private Preferences prefs;

    public Config() {
        load();
    }

    private void load() {
        prefs = Preferences.userNodeForPackage(FreeSnap.class);
        setInitial(prefs.getBoolean("isInitial", true));
        setFtpServer(prefs.get("ftpServer", "localhost"));
        setFtpUser(prefs.get("ftpUser", "anonymous"));
        setFtpPassword(prefs.get("ftpPass", ""));
        setFtpDirectory(prefs.get("ftpDirectory", ""));
        setUrl(prefs.get("url", "http://localhost"));
        setColorR(prefs.getInt("colorR", 255));
        setColorG(prefs.getInt("colorG", 0));
        setColorB(prefs.getInt("colorB", 0));
        setSize(prefs.getInt("size", 8));

    }

    public boolean save() {
        prefs.putBoolean("isInitial", false);
        prefs.put("ftpServer", ftpServer);
        prefs.put("ftpUser", ftpUser);
        prefs.put("ftpPass", ftpPassword);
        prefs.put("ftpDirectory", ftpDirectory);
        prefs.put("url", url);
        prefs.putInt("colorR", colorR);
        prefs.putInt("colorG", colorG);
        prefs.putInt("colorB", colorB);
        prefs.putInt("size", size);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isInitial() {
        return isInitial;
    }

    private void setInitial(boolean initial) {
        isInitial = initial;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpDirectory() {
        return ftpDirectory;
    }

    public void setFtpDirectory(String ftpDirectory) {
        this.ftpDirectory = ftpDirectory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getColorR() {
        return colorR;
    }

    public void setColorR(int colorR) {
        this.colorR = colorR;
    }

    public int getColorG() {
        return colorG;
    }

    public void setColorG(int colorG) {
        this.colorG = colorG;
    }

    public int getColorB() {
        return colorB;
    }

    public void setColorB(int colorB) {
        this.colorB = colorB;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}