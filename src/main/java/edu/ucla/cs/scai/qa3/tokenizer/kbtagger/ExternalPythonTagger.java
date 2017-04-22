/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.tokenizer.kbtagger;

import com.google.gson.Gson;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Attribute;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.DataSet;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Dimension;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Entity;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.KB;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Measure;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class ExternalPythonTagger implements KBTagger {

    String urlService;
    static HashMap<String, KBTaggingResult> cache = new HashMap<>();

    static synchronized KBTaggingResult getFromCache(String question) {
        return cache.get(question);
    }

    static synchronized void saveToCache(String question, KBTaggingResult tags) {
        cache.put(question, tags);
    }

    public ExternalPythonTagger(String url) {
        this.urlService = url;
    }

    public ExternalPythonTagger() {
        this.urlService = System.getProperty("taggerUrl", "http://swipe.unica.it/apps/qa3/?q=");
    }

    @Override
    public KBTaggingResult tag(String question) throws Exception {
        KBTaggingResult res = getFromCache(question);
        if (res != null) {
            return res;
        }
        URL url;
        InputStream is = null;
        BufferedReader br;

        try {
            url = new URL(urlService + URLEncoder.encode(question, "UTF-8"));
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            Gson gson = new Gson();
            String json = "";
            String line;
            while ((line = br.readLine()) != null) {
                json += line;
            }
            TaggerResult tr = gson.fromJson(json, TaggerResult.class);
            res = new KBTaggingResult();
            res.setDataset(KB.getDataSet("http://linkedspending.aksw.org/instance/"+tr.getDataset()));
            res.setTaggedChunks(new ArrayList<TaggedChunk>());
            String result = postProcessing(tr.getResult());
            int lastEnd = -1;
            for (String t : result.split("\n")) {
                String[] p = t.split("\t");
                //p[0] is the text
                p[0] = p[0].trim();
                int begin = question.indexOf(p[0], lastEnd + 1);
                if (begin >= 0) {
                    int end = begin + p[0].length() - 1;
                    lastEnd = end;
                    if (p.length == 1) { //no tags
                        res.getTaggedChunks().add(new TaggedChunk(p[0], begin, end));
                    } else if (p.length == 4) { //tags
                        p[1]=p[1].trim().substring(1);
                        p[1]=p[1].substring(0, p[1].length()-1);
                        p[2]=p[2].trim().substring(1);
                        p[2]=p[2].substring(0, p[2].length()-1);
                        p[3]=p[3].trim();
                        res.getTaggedChunks().add(new TaggedChunk(p[0], begin, end, kbTagsFromTriple(p[1], p[2], p[3], res.getDataset())));
                    } else {
                        System.out.println("Unexpected situation");
                    }
                }
            }
            saveToCache(question, res);
            return res;
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw mue;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                throw ioe;
            }
        }
    }

    private String postProcessing(String result) {
        StringBuilder sb = new StringBuilder();
        for (String t : result.split("\n")) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            StringTokenizer st = new StringTokenizer(t, "\t");
            String key = st.nextToken().trim();
            String k = key.toLowerCase();
            if (k.equals("years") || k.equals("year")) {
                sb.append(key).append("\thttp://linkedspending.aksw.org/ontology/refYear\thttp://www.w3.org/2000/01/rdf-schema#label\t\"year\"");
            } else if (k.equals("date") || k.equals("dates") || k.equals("day") || k.equals("days")) {
                sb.append(key).append("\thttp://linkedspending.aksw.org/ontology/refDate\thttp://www.w3.org/2000/01/rdf-schema#label\t\"date\"");
            } else if (!st.hasMoreTokens()) { //this means that the string is not annotated, thus we look into the string for year or date
                //try to find a missing year/date annotation - very rough implementaion
                if (k.contains("years")) {
                    int begin = k.indexOf("years");
                    int end = begin + 4;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refYear\thttp://www.w3.org/2000/01/rdf-schema#label\t\"year\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else if (k.contains("year")) {
                    int begin = k.indexOf("year");
                    int end = begin + 3;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refYear\thttp://www.w3.org/2000/01/rdf-schema#label\t\"year\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else if (k.contains("dates")) {
                    int begin = k.indexOf("dates");
                    int end = begin + 4;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refDate\thttp://www.w3.org/2000/01/rdf-schema#label\t\"date\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else if (k.contains("date")) {
                    int begin = k.indexOf("date");
                    int end = begin + 3;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refDate\thttp://www.w3.org/2000/01/rdf-schema#label\t\"date\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else if (k.contains("days")) {
                    int begin = k.indexOf("days");
                    int end = begin + 3;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refDate\thttp://www.w3.org/2000/01/rdf-schema#label\t\"date\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else if (k.contains("day")) {
                    int begin = k.indexOf("day");
                    int end = begin + 2;
                    if (begin > 0) {
                        sb.append(key.substring(0, begin)).append("\n");
                    }
                    sb.append(key.substring(begin, end + 1)).append("\thttp://linkedspending.aksw.org/ontology/refDate\thttp://www.w3.org/2000/01/rdf-schema#label\t\"date\"\n");
                    if (end < k.length() - 1) {
                        sb.append(key.substring(end + 1, key.length()));
                    }
                } else {
                    sb.append(key); //here, the are no other tokes in st
                }
            } else {
                sb.append(key);
                while (st.hasMoreTokens()) {
                    sb.append("\t").append(st.nextToken());
                }
            }
        }
        return sb.toString();
    }

    private ArrayList<KbTag> kbTagsFromTriple(String subject, String property, String value, DataSet dataset) {
        ArrayList<KbTag> kbTags = new ArrayList<>();
        Attribute ap = dataset.getAttribute(property);
        if (ap != null) { //triple of type <observation> <property> "literal": <http://linkedspending.aksw.org/instance/observation-town_of_cary_expenditures-a99179b2b547204b13509b402bc49889e55c354c>	<http://linkedspending.aksw.org/ontology/refYear>	"2011"^^<http://www.w3.org/2001/XMLSchema#gYear>
            kbTags.add(new KbTag(value, ap));
        } else {
            Dimension ds = dataset.getDimension(subject);
            if (ds != null) { //triple of type <dimension> <property> <value>
                if (property.equals("http://purl.org/dc/terms/identifier") || property.equals("http://www.w3.org/2000/01/rdf-schema#label") || property.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                    kbTags.add(new KbTag(ds));
                } else {
                    System.out.println("CHECK THIS CASE!");
                }
            } else {
                Attribute as = dataset.getAttribute(subject);
                if (as != null) { //triple of type <dimension property value>
                    if (property.equals("http://purl.org/dc/terms/identifier") || property.equals("http://www.w3.org/2000/01/rdf-schema#label") || property.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(as));
                    } else {
                        System.out.println("CHECK THIS CASE!");
                    }
                } else {
                    Measure ms = dataset.getMeasure(subject);
                    if (ms != null) { //triple of type <measure property value>
                        if (property.equals("http://purl.org/dc/terms/identifier") || property.equals("http://www.w3.org/2000/01/rdf-schema#label") || property.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                            kbTags.add(new KbTag(ms));
                        } else {
                            System.out.println("CHECK THIS CASE!");
                        }
                    } else {
                        Entity es = dataset.getEntity(subject);
                        if (es != null) { //triple of type <entity attribute literal>: <https://openspending.org/town_of_cary_expenditures/Class/6>	<http://www.w3.org/2000/01/rdf-schema#label>	"Public Works and Utilities"
                            if (property.equals("http://www.w3.org/2000/01/rdf-schema#label") || property.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //triple of type <entity rdfs:label "label">
                                HashSet<Dimension> dims = dataset.getDimensionsHavingValue(es);
                                if (dims.isEmpty()) {
                                    kbTags.add(new KbTag(es));
                                } else {
                                    if (dims.size() > 1) {
                                        System.out.println("Entity " + subject + " can be value of multiple dimensions");
                                    } else {
                                        kbTags.add(new KbTag(es, dims.iterator().next()));
                                    }
                                }
                            } else {
                                System.out.println("CHECK THIS CASE!");
                            }
                        } else {
                            if (subject.equals(dataset.getUri())) { //triple of type <entity attribute literal>: <http://linkedspending.aksw.org/instance/town_of_cary_revenues>	<http://purl.org/dc/terms/identifier>	"town_of_cary_revenues"
                                if (property.equals("http://purl.org/dc/terms/identifier") || property.equals("http://www.w3.org/2000/01/rdf-schema#label") || property.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                                    kbTags.add(new KbTag(dataset));
                                } else {
                                    System.out.println("CHECK THIS CASE!");
                                }
                            } else {
                                System.out.println("Unrecognized triple: " + subject+"\t"+property+"\t"+value);
                            }
                        }
                    }
                }
            }
        }
        return kbTags;
    }

    class TaggerResult {

        String dataset;
        String result;

        public String getDataset() {
            return dataset;
        }

        public void setDataset(String dataset) {
            this.dataset = dataset;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

    }

    public static void main(String[] args) throws Exception {
        KBTaggingResult res = new ExternalPythonTagger().tag("What was the average Uganda health budget over all districts in 2014?");
        System.out.println(res);
    }

}
