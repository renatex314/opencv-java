package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static class App {
        public String getResourceAbsolutePath(String fileName) {
            try {
                return Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).toURI()).toFile().getAbsolutePath();
            } catch (Exception err) {
                err.printStackTrace();

                return null;
            }
        }

        public Mat convertImageFileToMat(String fileName) {
            String absolutePath = this.getResourceAbsolutePath(fileName);

            Mat image = Imgcodecs.imread(absolutePath);

            if (image.empty()) {
                return null;
            }

            return image;
        }

        public static Mat createImageWithDetectionBoxes(Mat image, List<Detection> detections) {
            Mat result = new Mat();
            image.copyTo(result);

            for (Detection detection : detections) {
                String label = String.format("%s %.0f%%", detection.getClassName(), detection.getConfidence() * 100F);
                float[] detectionBox = detection.getDetectionBoxBounds();

                Point point1 = new Point(detectionBox[0], detectionBox[1]);
                Point point2 = new Point(detectionBox[2], detectionBox[3]);
                Scalar color = new Scalar(0, 0, 255);

                Imgproc.rectangle(
                        result,
                        point1,
                        point2,
                        color,
                        2
                );
                Imgproc.putText(
                        result,
                        label,
                        new Point(point1.x, point1.y - 4),
                        Core.FONT_HERSHEY_PLAIN,
                        2,
                        new Scalar(0, 255, 0),
                        2
                );
            }

            return result;
        }

        public void runModelTest() {
            String modelAbsolutePath = this.getResourceAbsolutePath("yolov8/yolov8n.onnx");
            String metadataAbsolutePath = this.getResourceAbsolutePath("yolov8/metadata.yaml");

            if (modelAbsolutePath == null || metadataAbsolutePath == null) {
                System.err.println("Model or metadata file not found !");

                return;
            }

//            Mat image = convertImageFileToMat("samples/cat.png");
//            Mat image = convertImageFileToMat("samples/dog.jpg");
            Mat image = convertImageFileToMat("samples/setup.jpg");
//            Mat image = convertImageFileToMat("samples/room.jpg");
//            Mat image = convertImageFileToMat("samples/train.jpg");

            List<Detection> detections = List.of();
            try (YOLOv8nInference model = new YOLOv8nInference(modelAbsolutePath, metadataAbsolutePath)) {

                if (image == null) {
                    throw new Exception("A imagem é vazia !");
                }

                long currentTime = System.currentTimeMillis();
                detections = model.infer(image);
                long elapsedTime = System.currentTimeMillis() - currentTime;

                for (Detection detection : detections) {
                    System.out.println(detection.toString());
                }

                System.out.printf("Got: %d detections !%n", detections.size());
                System.out.printf("Infer took: %.2f seconds. \n", elapsedTime / 1000F);
            } catch (Exception err) {
                err.printStackTrace();
            }

            if (Objects.nonNull(image)) {
                Mat imageWithBoxes = createImageWithDetectionBoxes(image, detections);

                HighGui.imshow("Teste", imageWithBoxes);
                HighGui.waitKey();
                HighGui.windows.values().forEach(imageWindow -> {
                    imageWindow.frame.dispose();
                });
                HighGui.windows.clear();

                image.release();
                imageWithBoxes.release();
            }
        }

        public void run() throws URISyntaxException, IOException {
            runModelTest();
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        OpenCV.loadLocally();

        new App().run();
    }
}
