package com.hokuapps.loadnativefileupload.delegate;

import com.hokuapps.loadnativefileupload.models.Error;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;

public interface OnUploadListener {
    public void onUploadFinish(ServiceRequest request, Error error);

}
