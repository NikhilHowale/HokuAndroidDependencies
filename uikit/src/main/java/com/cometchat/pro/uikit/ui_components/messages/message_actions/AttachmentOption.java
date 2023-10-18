package com.cometchat.pro.uikit.ui_components.messages.message_actions;

import com.cometchat.pro.uikit.AppConfig;
import com.cometchat.pro.uikit.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AttachmentOption implements Serializable {

    private int id;
    private String title;
    private int resourceImage;

    public static final byte DOCUMENT_ID = 101;
    public static final byte CAMERA_ID = 102;
    public static final byte GALLERY_ID = 103;
    public static final byte AUDIO_ID = 104;
    public static final byte LOCATION_ID = 105;
    public static final byte CONTACT_ID = 106;

    /**
     *  This method add default attachment to list
     * @return return default attachments
     */
    public static List<AttachmentOption> getDefaultList() {
        List<AttachmentOption> attachmentOptions = new ArrayList<>();
        attachmentOptions.add(new AttachmentOption(DOCUMENT_ID, "Document", R.drawable.document1));
        attachmentOptions.add(new AttachmentOption(CAMERA_ID, "Camera", R.drawable.camera));
        attachmentOptions.add(new AttachmentOption(GALLERY_ID, "Gallery", R.drawable.gallery));
        attachmentOptions.add(new AttachmentOption(AUDIO_ID, "Audio", R.drawable.audio));
        if(!AppConfig.DISABLE_LOCATION_MESSAGE){
            attachmentOptions.add(new AttachmentOption(LOCATION_ID, "Location", R.drawable.location1));
        }
        attachmentOptions.add(new AttachmentOption(CONTACT_ID, "Contact", R.drawable.document));

        return attachmentOptions;
    }

    public AttachmentOption(int id, String title, int resourceImage) {
        this.id = id;
        this.title = title;
        this.resourceImage = resourceImage;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getResourceImage() {
        return resourceImage;
    }
}
