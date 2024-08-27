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
    private static final int INPUT_WIDTH = 640;
    private static final int INPUT_HEIGHT = 640;

    public YOLOv8nInference(String modelPath, String yamlMetadataPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();

        SessionOptions options = new SessionOptions();
        this.session = env.createSession(modelPath, options);
        this.metadata = new YOLOv8YamlData(yamlMetadataPath);
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

        return inputTensor;
    }

    // TODO: Apply Non-Maximum Suppression
    private List<Detection> postprocessOutput(float[][][] output, int originalWidth, int originalHeight) {
        float rescaleX = (float) 1;
        float rescaleY = (float) 1;

        HashMap<Integer, Detection> hashedDetections = new HashMap<>();

        float[][] detection = YOLOv8nInference.transpose(output[0]);

        // Extract the bounding boxes, confidences, and class IDs
        for (float[] data : detection) {
            float x0 = data[0] * rescaleX;
            float y0 = data[1] * rescaleY;
            float x1 = data[2] * rescaleX;
            float y1 = data[3] * rescaleY;

            int classId = -1;
            float maxConfidence = 0;

            for (int i = 0; i < 80; i++) {
                if (maxConfidence < data[i + 4]) {
                    maxConfidence = data[i + 4];
                    classId = i;
                }
            }

            Detection existingDetection = hashedDetections.get(classId);

            if (classId != -1 && (existingDetection == null || existingDetection.getConfidence() < maxConfidence)) {
                hashedDetections.put(classId, new Detection(x0, y0,x1, y1, maxConfidence, classId, this.metadata.getNames().get(classId)));
            }
        }

        List<Detection> detections = new ArrayList<>(hashedDetections.values());
        detections.sort((o1, o2) -> Float.compare(o2.getConfidence(), o1.getConfidence()));

        return detections;
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
