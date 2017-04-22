/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.rdfcubemodel;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DataSet implements Serializable {

    String uri, label;
    HashMap<String, Dimension> dimensions = new HashMap<>();
    HashMap<String, Measure> measures = new HashMap<>();
    HashMap<String, Attribute> attributes = new HashMap<>();
    HashMap<String, Entity> entities = new HashMap<>();
    Measure defaultMeasure;

    public DataSet(String uri) {
        this.uri = uri;
    }

    public void addDimension(Dimension d) {
        dimensions.put(d.getUri(), d);
    }

    public void setDefaultMeasure() {
        if (measures == null || measures.isEmpty()) {
            return;
        }
        if (measures.size() > 1) {
            for (Measure m : measures.values()) {
                if (m.label != null && m.label.toLowerCase().contains("amount")) {
                    defaultMeasure = m;
                    break;
                }
            }
        } else {
            defaultMeasure = measures.values().iterator().next();
        }
    }

    public void addMeasure(Measure m) {
        measures.put(m.getUri(), m);
    }

    public void addAttribute(Attribute a) {
        attributes.put(a.getUri(), a);
    }

    public void addEntity(Entity e) {
        entities.put(e.getUri(), e);
    }

    public boolean setLabel(String label) {
        boolean res = this.label == null;
        this.label = label;
        return res;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public Dimension getDimension(String uri) {
        return dimensions.get(uri);
    }

    public Measure getMeasure(String uri) {
        return measures.get(uri);
    }

    public Attribute getAttribute(String uri) {
        return attributes.get(uri);
    }

    public Entity getEntity(String uri) {
        return entities.get(uri);
    }

    public Collection<Attribute> getAttributes() {
        return attributes.values();
    }

    public Collection<Measure> getMeasures() {
        return measures.values();
    }

    public Collection<Dimension> getDimensions() {
        return dimensions.values();
    }

    public Collection<Entity> getEntities() {
        return entities.values();
    }

    public Measure getDefaultMeasure() {
        return defaultMeasure;
    }

    public HashSet<Dimension> getDimensionsHavingValue(Entity e) {
        HashSet<Dimension> res = new HashSet<>();
        for (Dimension d : dimensions.values()) {
            if (d.entityValues.contains(e)) {
                res.add(d);
            }
        }
        return res;
    }

    public HashSet<Attribute> getAttributesHavingValue(String v) {
        HashSet<Attribute> res = new HashSet<>();
        for (Attribute a : attributes.values()) {
            if (a.literalValues.contains(v)) {
                res.add(a);
            }
        }
        return res;
    }

    public String getKeyWords() {
        StringBuilder sb = new StringBuilder(label);
        for (Attribute a : attributes.values()) {
            sb.append("|").append(a.getKeyWords());
        }
        for (Dimension d : dimensions.values()) {
            sb.append("|").append(d.getKeyWords());
        }
        for (Measure m : measures.values()) {
            sb.append("|").append(m.getKeyWords());
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uri);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSet other = (DataSet) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        return true;
    }

}
