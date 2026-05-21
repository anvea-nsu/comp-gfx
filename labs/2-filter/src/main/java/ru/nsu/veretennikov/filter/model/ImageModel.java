package ru.nsu.veretennikov.filter.model;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageModel {
    private BufferedImage original;
    private BufferedImage processed;
    private boolean showOriginal = false;
    private File currentFile;

    public BufferedImage getOriginal()  { return original; }
    public BufferedImage getProcessed() { return processed; }
    public File getCurrentFile()        { return currentFile; }
    public boolean isShowOriginal()     { return showOriginal; }

    public void setOriginal(BufferedImage img, File file) {
        this.original   = img;
        this.processed  = null;
        this.currentFile = file;
        this.showOriginal = false;
    }

    public void setProcessed(BufferedImage img) {
        this.processed   = img;
        this.showOriginal = false;
    }

    public void setShowOriginal(boolean show) {
        this.showOriginal = show;
    }

    public void toggleShowOriginal() {
        if (processed != null) showOriginal = !showOriginal;
    }

    public BufferedImage getDisplayImage() {
        if (original == null) return null;
        if (processed == null || showOriginal) return original;
        return processed;
    }

    public boolean hasImage()     { return original  != null; }
    public boolean hasProcessed() { return processed != null; }
}
