package org.freesnap.main.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.freesnap.FreeSnap;
import org.freesnap.util.config.Config;

public class SettingsShell {
    private Config config;
    private Shell shell;

    private Text serverField;
    private Text usernameField;
    private Text passwordField;
    private Text directoryField;
    private Text urlField;

    public SettingsShell(Config config) {
        this.config = config;
        this.prepareShell();
        this.prepareForm();
        this.loadConfiguration();
    }

    public void show() {
        this.shell.pack();
        this.shell.open();
    }

    private void prepareShell() {
        this.shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
        this.shell.setText("FreeSnap Settings");
        this.shell.setImages(FreeSnap.getIconManager().getIconImages());
        this.shell.setMinimumSize(300, 100);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
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

        Button testButton = new Button(this.shell, SWT.PUSH | SWT.RIGHT);
        testButton.setText("Test connection");

        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 2;
        testButton.setLayoutData(data);
        testButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                saveConfiguration();
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText("Status");
                messageBox.setMessage(FreeSnap.testConnection() ? "Connected!" : "Not connected!");
                messageBox.open();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 2;

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
    }

    private void saveConfiguration() {
        config.setFtpServer(serverField.getText());
        config.setFtpUser(usernameField.getText());
        config.setFtpPassword(passwordField.getText());
        config.setFtpDirectory(directoryField.getText());
        config.setUrl(urlField.getText());
        config.save();
    }
}
