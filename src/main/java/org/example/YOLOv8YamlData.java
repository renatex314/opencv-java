package org.example;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class YOLOv8YamlData {
    private String description;
    private String author;
    private String date;
    private String version;
    private String license;
    private String docs;
    private int stride;
    private String task;
    private int batch;
    private ArrayList<Integer> imgsz;
    private List<String> names;

    public YOLOv8YamlData(String yamlAbsolutePath) {
        boolean loaded = this.loadYaml(yamlAbsolutePath);

        if (!loaded) {
            throw new RuntimeException("Could not load YAML file !");
        }
    }

    private boolean loadYaml(String yamlAbsolutePath) {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(options);
        LinkedHashMap<String, Object> metadata = null;

        File file = new File(yamlAbsolutePath);

        if (!file.exists()) {
            return false;
        }

        try (InputStream reader = new FileInputStream(file)) {
            metadata = yaml.load(reader);
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (metadata == null) {
            return false;
        }

        this.description = (String) metadata.get("description");
        this.author = (String) metadata.get("author");
        this.date = (String) metadata.get("date");
        this.version = (String) metadata.get("version");
        this.license = (String) metadata.get("license");
        this.docs = (String) metadata.get("docs");
        this.stride = (int) metadata.get("stride");
        this.task = (String) metadata.get("task");
        this.batch = (int) metadata.get("batch");
        this.imgsz = (ArrayList<Integer>) metadata.get("imgsz");
        this.names = new ArrayList<>();

        LinkedHashMap<Integer, String> names = (LinkedHashMap<Integer, String>) metadata.get("names");

        this.getNames().addAll(names.values());

        return true;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getVersion() {
        return version;
    }

    public String getLicense() {
        return license;
    }

    public String getDocs() {
        return docs;
    }

    public int getStride() {
        return stride;
    }

    public String getTask() {
        return task;
    }

    public int getBatch() {
        return batch;
    }

    public ArrayList<Integer> getImgsz() {
        return imgsz;
    }

    public List<String> getNames() {
        return names;
    }
}
