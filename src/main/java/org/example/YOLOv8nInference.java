package org.example;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.SessionOptions;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class YOLOv8nInference implements AutoCloseable {
    private final YOLOv8YamlData metadata;
    private final OrtEnvironment env;
    private final OrtSession session;
    private final int objectClassesQuantity;
    private static final int INPUT_WIDTH = 640;
    private static final int INPUT_HEIGHT = 640;
    private static final float NMS_THRESHOLD = 0.3F;

    public YOLOv8nInference(String modelPath, String yamlMetadataPath, boolean useGPU) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();

        SessionOptions options = new SessionOptions();
        if (useGPU) {
            options.addCUDA();
        }

        this.session = env.createSession(modelPath, options);
        this.metadata = new YOLOv8YamlData(yamlMetadataPath);

        this.objectClassesQuantity = this.metadata.getNames().size();
    }

    public List<Detection> infer(Mat image) throws OrtException {
        float[][][][] inputTensor = this.preprocessInput(image);
        OnnxTensor input = OnnxTensor.createTensor(this.env, inputTensor);

        OrtSession.Result result = this.session.run(Collections.singletonMap("images", input));

        float[][][] results = (float[][][]) result.get(0).getValue();
        return this.postprocessOutput(results, image.width(), image.height());
    }

    private float[][][][] preprocessInput(Mat image) {
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(INPUT_WIDTH, INPUT_HEIGHT));
        resizedImage.convertTo(resizedImage, CvType.CV_32FC3, 1.0 / 255.0);

        int height = resizedImage.rows();
        int width = resizedImage.cols();
        int channels = resizedImage.channels();

        float[][][][] inputTensor = new float[1][channels][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] data = resizedImage.get(y, x);
                for (int c = 0; c < channels; c++) {
                    inputTensor[0][c][y][x] = (float) data[c];
                }
            }
        }

        resizedImage.release();

        return inputTensor;
    }

    private static float getIntersectionArea(float[] box1, float[] box2) {
        float x1 = Math.max(box1[0], box2[0]);
        float y1 = Math.max(box1[1], box2[1]);
        float x2 = Math.min(box1[2], box2[2]);
        float y2 = Math.min(box1[3], box2[3]);

        return (x2-x1)*(y2-y1);
    }

    private static float getUnionArea(float[] box1, float[] box2) {
        float box1Area = (box1[2] - box1[0])*(box1[3]-box1[1]);
        float box2Area = (box2[2] - box2[0])*(box2[3]-box2[1]);

        return box1Area + box2Area - getIntersectionArea(box1, box2);
    }

    private static float getIntersectionOverUnion(float[] box1, float[] box2) {
        return getIntersectionArea(box1, box2)/getUnionArea(box1, box2);
    }

    private String getClassNameFromClassId(int classId) {
        try {
            return this.metadata.getNames().get(classId);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Detection> postprocessOutput(float[][][] output, int originalWidth, int originalHeight) {
        float rescaleX = (float) originalWidth / INPUT_WIDTH;
        float rescaleY = (float) originalHeight / INPUT_HEIGHT;

        List<Detection> detectionsList = new ArrayList<>();

        float[][] detections = YOLOv8nInference.transpose(output[0]);

        // Extract the bounding boxes, confidences, and class IDs to Detection objects
        for (float[] data : detections) {
            float cx = data[0] * rescaleX;
            float cy = data[1] * rescaleY;
            float width = data[2] * rescaleX;
            float height = data[3] * rescaleY;

            int classId = -1;
            float maxConfidence = 0;

            for (int i = 0; i < this.objectClassesQuantity; i++) {
                if (maxConfidence < data[i + 4]) {
                    maxConfidence = data[i + 4];
                    classId = i;
                }
            }

            if (maxConfidence >= 0.01) {
                detectionsList.add(
                    new Detection(
                        cx, cy,
                        width, height,
                        maxConfidence,
                        classId, getClassNameFromClassId(classId)
                    )
                );
            }
        }

        // Apply The NMS to the list
        detectionsList.sort((o1, o2) -> Float.compare(o2.getConfidence(), o1.getConfidence()));
        List<Detection> detectionsOutput = new ArrayList<>();

        while (!detectionsList.isEmpty()) {
            Detection detection = detectionsList.get(0);

            detectionsOutput.add(detection);
            detectionsList.removeIf(
                d -> getIntersectionOverUnion(
                   detection.getDetectionBoxBounds(),
                    d.getDetectionBoxBounds()
                ) >= NMS_THRESHOLD
            );
        }

        return detectionsOutput;
    }

    public static float[][] transpose(float[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        float[][] transposed = new float[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public void close() throws OrtException {
        this.session.close();
        this.env.close();
    }
}
