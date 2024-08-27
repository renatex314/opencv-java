package org.example;

public class Detection {
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final float confidence;
    private final int classId;
    private final String className;

    public Detection(float x0, float y0, float x1, float y1, float confidence, int classId, String className) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.confidence = confidence;
        this.classId = classId;
        this.className = className;
    }

    public float getX0() {
        return x0;
    }

    public float getY0() {
        return y0;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
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
                "x0=" + x0 +
                ", y0=" + y0 +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", confidence=" + confidence +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                '}';
    }
}
