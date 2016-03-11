package org.freesnap.util.resource;


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