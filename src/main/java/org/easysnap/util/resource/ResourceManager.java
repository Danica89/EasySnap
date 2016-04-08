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

package org.easysnap.util.resource;


import org.eclipse.swt.graphics.Resource;

import java.util.ArrayList;

public class ResourceManager {

    private ArrayList<Resource> list;

    public ResourceManager() {
        this.list = new ArrayList<Resource>();
    }

    public void add(Resource resource) {
        list.add(resource);
    }

    public void disposeAll() {
        for (Resource resource : list) {
            if (!resource.isDisposed()) {
                resource.dispose();
            }
        }
        list.clear();
    }
}