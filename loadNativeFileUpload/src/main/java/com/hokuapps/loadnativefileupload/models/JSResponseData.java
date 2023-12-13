package com.hokuapps.loadnativefileupload.models;


/**
 * Created by user on 30/12/16.
 */
public class JSResponseData {

    private String appID;
    private String languagePref;
    private String callbackFunction;
    private String responseData;
    private String srcImageName;
    private String OfflineID;
    private String instructionText;
    private boolean isSkipLibrary; // show or hide gallery on custom camera
    private boolean isSkipCamera;
    private boolean isSelectVideo;

    private boolean loadPhotoEditor;
    private boolean isDrawing;
    private boolean showCaption;
    private String caption;

    private boolean isProfileImage;
    private boolean isProfileUploadStart;

    private LocationMapModel locationMapModel;

    private String colorCode;

    private boolean isMapPlan;

    private String pageTitle;
    private boolean isWaitForResponse;
    private boolean isFileFormat;
    private boolean isCropped;
    private String fileMimeType;
    private String type;
    private String color;
    private int drawType;
    private int maxFileSize;

    private boolean isDefaultCamera = false;
    private boolean isDocumentsUpload = false;
    private boolean isBase64Data = false;
    private boolean isDocumentsOnly = false;
    private boolean isFrontCamera = false;
    private boolean isAudioRecording = false;
    private boolean isRectangle = false;

    private boolean isScanText = false;
    private boolean isScanDocument = false;

    private String imageURL;
    private String originalImagePath;
    private boolean usedForAnnotation;
    private String[] supportedFormat = new String[]{};
    private String extension;
    private String localImageName;

    private int imageType = 1;

    private int instructionNumberClockIn = 0;

    public String getLocalImageName() {
        return localImageName;
    }

    public void setLocalImageName(String localImageName) {
        this.localImageName = localImageName;
    }

    public JSResponseData() {
    }

    public boolean isUsedForAnnotation() {
        return usedForAnnotation;
    }

    public void setUsedForAnnotation(boolean usedForAnnotation) {
        this.usedForAnnotation = usedForAnnotation;
    }

    public String getOriginalImagePath() {
        return originalImagePath;
    }

    public void setOriginalImagePath(String originalImagePath) {
        this.originalImagePath = originalImagePath;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isScanDocument() {
        return isScanDocument;
    }

    public void setScanDocument(boolean scanDocument) {
        isScanDocument = scanDocument;
    }

    public boolean isScanText() {
        return this.isScanText;
    }

    public void setScanText(boolean scanText) {
        isScanText = scanText;
    }

    public String getLanguagePref() {
        return languagePref;
    }

    public void setLanguagePref(String languagePref) {
        this.languagePref = languagePref;
    }

    public String[] getSupportedFormat() {
        return supportedFormat;
    }

    public void setSupportedFormat(String[] supportedFormat) {
        this.supportedFormat = supportedFormat;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isAudioRecording() {
        return isAudioRecording;
    }

    public void setAudioRecording(boolean audioRecording) {
        isAudioRecording = audioRecording;
    }

    public boolean isBase64Data() {
        return isBase64Data;
    }

    public void setBase64Data(boolean base64Data) {
        isBase64Data = base64Data;
    }

    public boolean isCropped() {
        return isCropped;
    }

    public void setCropped(boolean isCropped) {
        this.isCropped = isCropped;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getCallbackFunction() {
        return callbackFunction;
    }

    public void setCallbackFunction(String callbackFunction) {
        this.callbackFunction = callbackFunction;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getSrcImageName() {
        return srcImageName;
    }

    public void setSrcImageName(String srcImageName) {
        this.srcImageName = srcImageName;
    }

    public String getOfflineID() {
        return OfflineID;
    }

    public void setOfflineID(String offlineID) {
        OfflineID = offlineID;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public boolean isSkipLibrary() {
        return isSkipLibrary;
    }

    public void setSkipLibrary(boolean skipLibrary) {
        isSkipLibrary = skipLibrary;
    }

    public boolean isSkipCamera() {
        return isSkipCamera;
    }

    public void setSkipCamera(boolean skipCamera) {
        isSkipCamera = skipCamera;
    }

    public boolean isSelectVideo() {
        return isSelectVideo;
    }

    public void setSelectVideo(boolean selectVideo) {
        isSelectVideo = selectVideo;
    }

    public boolean isLoadPhotoEditor() {
        return loadPhotoEditor;
    }

    public void setLoadPhotoEditor(boolean loadPhotoEditor) {
        this.loadPhotoEditor = loadPhotoEditor;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawing(boolean drawing) {
        isDrawing = drawing;
    }

    public boolean isProfileImage() {
        return isProfileImage;
    }

    public void setProfileImage(boolean profileImage) {
        isProfileImage = profileImage;
    }

    public boolean isProfileUploadStart() {
        return isProfileUploadStart;
    }

    public void setProfileUploadStart(boolean profileUploadStart) {
        isProfileUploadStart = profileUploadStart;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isMapPlan() {
        return isMapPlan;
    }

    public void setMapPlan(boolean mapPlan) {
        isMapPlan = mapPlan;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public boolean isShowCaption() {
        return showCaption;
    }

    public void setShowCaption(boolean showCaption) {
        this.showCaption = showCaption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isWaitForResponse() {
        return isWaitForResponse;
    }

    public void setWaitForResponse(boolean waitForResponse) {
        isWaitForResponse = waitForResponse;
    }

    public boolean isFileFormat() {
        return isFileFormat;
    }

    public void setFileFormat(boolean fileFormat) {
        isFileFormat = fileFormat;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDefaultCamera() {
        return isDefaultCamera;
    }

    public void setDefaultCamera(boolean defaultCamera) {
        isDefaultCamera = defaultCamera;
    }

    public boolean isDocumentsUpload() {
        return isDocumentsUpload;
    }

    public void setDocumentsUpload(boolean documentsUpload) {
        isDocumentsUpload = documentsUpload;
    }

    public boolean isDocumentsOnly() {
        return isDocumentsOnly;
    }

    public void setDocumentsOnly(boolean documentsOnly) {
        isDocumentsOnly = documentsOnly;
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    public boolean isRectangle() {
        return isRectangle;
    }

    public void setRectangle(boolean rectangle) {
        isRectangle = rectangle;
    }

    public int getDrawType() {
        return drawType;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public LocationMapModel getLocationMapModel() {
        return locationMapModel;
    }
    public void setLocationMapModel(LocationMapModel locationMapModel) {
        this.locationMapModel = locationMapModel;
    }

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public int getInstructionNumberClockIn() {
        return instructionNumberClockIn;
    }

    public void setInstructionNumberClockIn(int instructionNumberClockIn) {
        this.instructionNumberClockIn = instructionNumberClockIn;
    }
}
