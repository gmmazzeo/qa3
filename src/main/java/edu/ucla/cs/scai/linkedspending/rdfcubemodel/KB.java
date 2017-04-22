/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.rdfcubemodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KB {

    static HashMap<String, DataSet> datasets;

    static {
        String datasetPath = System.getProperty("datasetPath", "/home/massimo/Downloads/benchmarkdatasets");
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(datasetPath + "/datasets.data"));
            datasets = (HashMap<String, DataSet>) in.readObject();
        } catch (Exception e1) {
            try {
                e1.printStackTrace();
                loadDatasets(datasetPath);
                System.out.println("Writing files on disk");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(datasetPath + "/datasets.data"));
                out.writeObject(datasets);
                out.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static synchronized DataSet getDataSet(String name) {
        return datasets.get(name);
    }

    private static void loadDatasets(String directory) throws Exception {
        File dir = new File(directory);
        if (!dir.exists()) {
            System.out.println("Directory " + directory + " does not exist");
            System.exit(0);
        }

        File[] files = dir.listFiles();
        String regex = "(\\s|\\t)*<([^<>]*)>(\\s|\\t)*<([^<>]*)>(\\s|\\t)*(<|\")(.*)(>|\")";
        Pattern p = Pattern.compile(regex);
        //find the datasets defined by the files
        System.out.println("Loading datasets");
        datasets = new HashMap<>();
        HashMap<String, DataSet> dataSetFromFile = new HashMap<>();
        HashMap<String, String> fileFromDataSet = new HashMap<>();
        HashMap<String, Entity> entities = new HashMap<>();
        for (File f : files) {
            if (!f.getName().endsWith(".nt")) {
                System.out.println("Invalid file found: " + f.getName());
                continue;
            } else {
                System.out.println("Processing " + f.getName());
            }
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                            if (v.equals("http://purl.org/linked-data/cube#DataSet")) {
                                DataSet ds = new DataSet(s);
                                datasets.put(s, ds);
                                if (dataSetFromFile.containsKey(f.getName())) {
                                    System.out.println(f.getName() + " has more than one dataset");
                                    System.exit(0);
                                }
                                dataSetFromFile.put(f.getName(), ds);
                                fileFromDataSet.put(ds.getUri(), f.getName());
                            }
                        }
                    }
                }
            }
        }
        for (File f : files) {
            DataSet dataset = dataSetFromFile.get(f.getName());
            if (!f.getName().endsWith(".nt")) {
                System.out.println("Invalid file found: " + f.getName());
                continue;
            } else {
                System.out.println("Processing " + f.getName());
            }
            //In the first scan find the schema of the observations: dimensions, measures and attributes
            boolean refDate = false;
            boolean refYear = false;
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            if (s.equals(dataset.getUri())) {
                                if (!dataset.setLabel(v)) {
                                    System.out.println("Multiple dataset label for " + dataset.getUri());
                                }
                                System.out.println("Label: " + v);
                            }
                        } else if (a.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                            if (v.equals("http://purl.org/linked-data/cube#DimensionProperty")) {
                                System.out.println("Dimension: " + s);
                                dataset.addDimension(new Dimension(s));
                            } else if (v.equals("http://purl.org/linked-data/cube#MeasureProperty")) {
                                System.out.println("Measure: " + s);
                                dataset.addMeasure(new Measure(s));
                            } else if (v.equals("http://purl.org/linked-data/cube#AttributeProperty")) {
                                System.out.println("Attribute: " + s);
                                dataset.addAttribute(new Attribute(s));
                            }
                        } else if (a.equals("http://linkedspending.aksw.org/ontology/refDate") && !refDate) {
                            System.out.println("Attribute: " + a);
                            Attribute att = new Attribute(a);
                            att.setLabel("date");
                            dataset.addAttribute(att);
                            refDate = true;
                        } else if (a.equals("http://linkedspending.aksw.org/ontology/refYear") && !refYear) {
                            System.out.println("Attribute: " + a);
                            Attribute att = new Attribute(a);
                            att.setLabel("year");
                            dataset.addAttribute(att);
                            refYear = true;
                        }
                    }
                }
            }
            //In the second scan find the labels of dimensions and measures and the values of dimensions
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            Dimension dim = dataset.getDimension(s);
                            if (dim != null) {
                                if (!dim.setLabel(v)) {
                                    System.out.println("Multiple dimension label for " + dim.getUri());
                                }
                                System.out.println("Dimension Label: " + v);
                                continue;
                            }
                            Measure mea = dataset.getMeasure(s);
                            if (mea != null) {
                                if (!mea.setLabel(v)) {
                                    System.out.println("Multiple measure label for " + dataset.getUri());
                                }
                                System.out.println("Measure Label: " + v);
                                continue;
                            }
                            Attribute att = dataset.getAttribute(s);
                            if (att != null) {
                                if (!att.setLabel(v)) {
                                    System.out.println("Multiple attribute label for " + dataset.getUri());
                                }
                                System.out.println("Attribute Label: " + v);
                                continue;
                            }
                        } else {
                            Dimension dim = dataset.getDimension(a);
                            if (dim != null) {
                                if (v.startsWith("http")) { //it is an entity
                                    Entity e = entities.get(v);
                                    if (e == null) {
                                        e = new Entity(v);
                                        entities.put(v, e);
                                    }
                                    dataset.addEntity(e);
                                    dim.getEntityValues().add(e);
                                } else { //it is a literal
                                    System.out.println("Unexpected value " + v + " for " + a);
                                }
                                continue;
                            }
                            Attribute att = dataset.getAttribute(a);
                            if (att != null) {
                                if (v.startsWith("http")) { //it is an entity
                                    System.out.println("Unexpected value " + v + " for " + a);
                                } else { //it is a literal
                                    if (v.contains("^^")) {
                                        v = v.split("\\^\\^")[0];
                                        v = v.substring(0, v.length() - 1); //remove "
                                    }
                                    att.getLiteralValues().add(v);
                                }
                                continue;
                            }
                        }
                    }
                }
            }
            //In the third scan find the labels of entities
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            Entity ent = entities.get(s);
                            if (ent != null) {
                                ent.setLabel(v);
                                //System.out.println("Entity Label: " + v);
                                continue;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Datasets found: " + datasets.size());

        for (DataSet ds : datasets.values()) {
            ds.setDefaultMeasure();
            System.out.println("Dataset: <" + ds.getUri() + "> " + ds.getLabel());
            for (Measure m : ds.getMeasures()) {
                System.out.println("Measure: <" + m.getUri() + "> " + m.getLabel());
            }
            for (Dimension d : ds.getDimensions()) {
                System.out.println("Dimension: <" + d.getUri() + "> " + d.getLabel());
            }
            for (Attribute a : ds.getAttributes()) {
                System.out.println("Attribute: <" + a.getUri() + "> " + a.getLabel());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(KB.datasets.size());
    }
}
