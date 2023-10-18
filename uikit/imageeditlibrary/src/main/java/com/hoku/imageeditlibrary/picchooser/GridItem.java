package com.hoku.imageeditlibrary.picchooser;

class GridItem {
    final String name;
    final String path;
    final String imageTaken;
    final long imageSize;
    final boolean isVideo;

    public GridItem(final String n, final String p,final String imageTaken,final long imageSize) {
        name = n;
        path = p;
        this.imageTaken = imageTaken;
        this.imageSize = imageSize;
        this.isVideo = false;
    }

    public GridItem(final String n, final String p,final String imageTaken,final long imageSize,boolean isVideoItem) {
        name = n;
        path = p;
        this.imageTaken = imageTaken;
        this.imageSize = imageSize;
        this.isVideo = isVideoItem;
    }
}
