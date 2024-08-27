package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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

        public void runModelTest() {
            String modelAbsolutePath = this.getResourceAbsolutePath("yolov8/yolov8n.onnx");
            String metadataAbsolutePath = this.getResourceAbsolutePath("yolov8/metadata.yaml");

            if (modelAbsolutePath == null || metadataAbsolutePath == null) {
                System.err.println("Model or metadata file not found !");

                return;
            }

            try (YOLOv8nInference model = new YOLOv8nInference(modelAbsolutePath, metadataAbsolutePath)) {
//                Mat image = convertImageFileToMat("samples/cat.png");
//                Mat image = convertImageFileToMat("samples/dog.jpg");
                Mat image = convertImageFileToMat("samples/setup.jpg");
//                Mat image = convertImageFileToMat("samples/train.jpg");

                if (image == null) {
                    throw new Exception("A imagem é vazia !");
                }

                List<Detection> results = model.infer(image);
                for (Detection detection : results) {
                    System.out.println(detection.toString());
                }

                System.out.printf("Got: %d detections !%n", results.size());
            } catch (Exception err) {
                err.printStackTrace();
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
