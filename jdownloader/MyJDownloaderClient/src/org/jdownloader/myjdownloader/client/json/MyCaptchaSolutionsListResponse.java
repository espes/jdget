package org.jdownloader.myjdownloader.client.json;

import java.util.List;

public class MyCaptchaSolutionsListResponse extends RequestIDOnly {
    
    private List<MyCaptchaSolution> list = null;
    
    public MyCaptchaSolutionsListResponse(/* Storable */) {
    }
    
    public List<MyCaptchaSolution> getList() {
        return this.list;
    }
    
    public void setList(final List<MyCaptchaSolution> list) {
        this.list = list;
    }
}
