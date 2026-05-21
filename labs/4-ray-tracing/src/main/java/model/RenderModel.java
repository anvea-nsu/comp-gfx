package model;

public class RenderModel {
    private int backgroundR = 0;
    private int backgroundG = 0;
    private int backgroundB = 0;

    private double gamma = 1.6;

    private int depth = 1;

    public int getBackgroundR() {
        return backgroundR;
    }

    public int getBackgroundG() {
        return backgroundG;
    }

    public int getBackgroundB() {
        return backgroundB;
    }

    public double getGamma() {
        return gamma;
    }

    public int getDepth() {
        return depth;
    }


    public void setBackgroundR(int backgroundR) {
        this.backgroundR = backgroundR;
    }

    public void setBackgroundG(int backgroundG) {
        this.backgroundG = backgroundG;
    }

    public void setBackgroundB(int backgroundB) {
        this.backgroundB = backgroundB;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void clear() {
        backgroundR = 0;
        backgroundG = 0;
        backgroundB = 0;

        gamma = 1.6;
        depth = 1;
    }
}
