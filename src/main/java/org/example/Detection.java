package org.example;

public class Detection {
    private final float cx;
    private final float cy;
    private final float width;
    private final float height;
    private final float confidence;
    private final int classId;
    private final String className;

    public Detection(float cx, float cy, float width, float height, float confidence, int classId, String className) {
        this.cx = cx;
        this.cy = cy;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
        this.classId = classId;
        this.className = className;
    }

    public float getCx() {
        return cx;
    }

    public float getCy() {
        return cy;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float[] getDetectionBoxBounds() {
        return new float[] {
                this.getCx() - this.getWidth() / 2F,
                this.getCy() - this.getHeight() / 2F,
                this.getCx() + this.getWidth() / 2F,
                this.getCy() + this.getHeight() / 2F
        };
    }

    public float getConfidence() {
        return confidence;
    }

    public int getClassId() {
        return classId;
    }

    public String getClassName() {
        if (className == null) {
            return String.valueOf(classId);
        }

        return className;
    }

    @Override
    public String toString() {
        return "Detection{" +
                "cx=" + cx +
                ", cy=" + cy +
                ", width=" + width +
                ", height=" + height +
                ", confidence=" + confidence +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                '}';
    }
}
