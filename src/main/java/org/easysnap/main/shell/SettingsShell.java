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

package org.easysnap.main.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.easysnap.util.config.Config;
import org.easysnap.util.ftp.FtpClient;
import org.easysnap.util.icon.IconManager;

public class SettingsShell {
    private final Display display;
    private Config config;
    private FtpClient client;
    private IconManager iconManager;
    private Shell shell;

    private Text serverField;
    private Text usernameField;
    private Text passwordField;
    private Text directoryField;
    private Text urlField;
    private Text savePathField;

    public SettingsShell(Config config, FtpClient client, IconManager iconManager, Display display) {
        this.config = config;
        this.client = client;
        this.iconManager = iconManager;
        this.display = display;
        this.prepareShell();
        this.prepareForm();
        this.loadConfiguration();
    }

    public void show() {
        this.shell.pack();
        this.shell.open();
    }

    private void prepareShell() {
        this.shell = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE));
        this.shell.setText("EasySnap Settings");
        this.shell.setImages(iconManager.getIconImages());
        this.shell.setMinimumSize(300, 100);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginLeft = 5;
        layout.marginRight = 5;
        this.shell.setLayout(layout);
        this.shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                shell.setVisible(false);
                event.doit = false;
            }
        });
    }

    private void prepareForm() {
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 2;

        Label serverLabel = new Label(this.shell, SWT.RIGHT);
        serverLabel.setText("Server: ");
        this.serverField = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        this.serverField.setLayoutData(data);

        Label usernameLabel = new Label(this.shell, SWT.RIGHT);
        usernameLabel.setText("Username: ");
        this.usernameField = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        this.usernameField.setLayoutData(data);

        Label passwordLabel = new Label(this.shell, SWT.RIGHT);
        passwordLabel.setText("Password: ");
        this.passwordField = new Text(this.shell, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        this.passwordField.setLayoutData(data);

        Label directoryLabel = new Label(this.shell, SWT.RIGHT);
        directoryLabel.setText("Directory: ");
        this.directoryField = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        this.directoryField.setLayoutData(data);

        Label urlLabel = new Label(this.shell, SWT.RIGHT);
        urlLabel.setText("URL: ");
        this.urlField = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        this.urlField.setLayoutData(data);

        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 1;
        Label savePathLabel = new Label(this.shell, SWT.RIGHT);
        savePathLabel.setText("Save path: ");
        this.savePathField = new Text(this.shell, SWT.SINGLE | SWT.BORDER);
        this.savePathField.setLayoutData(data);

        Button selectSavePathButton = new Button(this.shell, SWT.PUSH | SWT.RIGHT);
        selectSavePathButton.setText("...");
        selectSavePathButton.setToolTipText("select");
        selectSavePathButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                String directory = dialog.open();
                config.setSavePath(directory);
                loadConfiguration();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        Button testButton = new Button(this.shell, SWT.PUSH | SWT.RIGHT);
        testButton.setText("Test connection");

        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 3;
        testButton.setLayoutData(data);
        testButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                saveConfiguration();
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText("Status");
                messageBox.setMessage(client.canConnect() ? "Connected!" : "Not connected!");
                messageBox.open();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;

        Button okButton = new Button(this.shell, SWT.PUSH | SWT.CENTER);
        okButton.setText("Save");
        okButton.setLayoutData(data);
        okButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                saveConfiguration();
                shell.setVisible(false);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button cancelButton = new Button(this.shell, SWT.PUSH | SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(data);
        cancelButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                shell.setVisible(false);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void loadConfiguration() {
        serverField.setText(config.getFtpServer());
        usernameField.setText(config.getFtpUser());
        passwordField.setText(config.getFtpPassword());
        directoryField.setText(config.getFtpDirectory());
        urlField.setText(config.getUrl());
        savePathField.setText(config.getSavePath());
    }

    private void saveConfiguration() {
        config.setFtpServer(serverField.getText());
        config.setFtpUser(usernameField.getText());
        config.setFtpPassword(passwordField.getText());
        config.setFtpDirectory(directoryField.getText());
        config.setUrl(urlField.getText());
        config.setSavePath(config.getSavePath());
        config.save();
    }
}
