package org.example;

import java.nio.file.Paths;
import java.util.Objects;

public class Test {

    public static String getResourceAbsolutePath(String fileName) {
        try {
            return Paths.get(Objects.requireNonNull(Test.class.getClassLoader().getResource(fileName)).toURI()).toFile().getAbsolutePath();
        } catch (Exception err) {
            err.printStackTrace();

            return null;
        }
    }

    public static void main(String[] args) {
        String yamlPath = getResourceAbsolutePath("yolov8openimagev7/metadata.yaml");
        YOLOv8YamlData metadata = new YOLOv8YamlData(yamlPath);

        System.out.println(metadata);
    }
}
