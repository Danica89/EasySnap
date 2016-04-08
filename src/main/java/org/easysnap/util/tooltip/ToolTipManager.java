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

package org.easysnap.util.tooltip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolTip;
import org.easysnap.main.shell.TrayIconShell;

public class ToolTipManager {
    private TrayIconShell shell;

    public ToolTipManager(TrayIconShell shell) {
        this.shell = shell;
    }

    public void show(String title, String message) {
        ToolTip tooltip = new ToolTip(shell.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
        tooltip.setText(title);
        tooltip.setMessage(message);
        shell.getTrayItem().setToolTip(tooltip);

        tooltip.setVisible(true);
        tooltip.setAutoHide(true);
    }
}
