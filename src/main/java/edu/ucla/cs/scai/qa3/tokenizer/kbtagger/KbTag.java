/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.tokenizer.kbtagger;

import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Measure;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Attribute;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.DataSet;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Entity;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.Dimension;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KbTag {

    public final static String DATASET = "S", MEASURE = "M", DIMENSION = "D", ATTRIBUTE = "A", ENTITY = "E", LITERAL = "L", NONE="O";
    String type;
    String uri, label, literal;
    KbTag connectingKbTag; //needed for literals

    public KbTag(Entity e) {
        type=ENTITY;
        this.uri=e.getUri();
        this.label=e.getLabel();
    } 

    public KbTag(Dimension d) {
        type=DIMENSION;
        this.uri=d.getUri();
        this.label=d.getLabel();
    } 
    
    public KbTag(Attribute a) {
        type=ATTRIBUTE;
        this.uri=a.getUri();
        this.label=a.getLabel();
    }    
    
    public KbTag(Measure m) {
        type=MEASURE;
        this.uri=m.getUri();
        this.label=m.getLabel();
    } 
    
    public KbTag(DataSet d) {
        type=DATASET;
        this.uri=d.getUri();
        this.label=d.getLabel();
    } 
    
    public KbTag(String literal, Attribute a) {
        type=LITERAL;
        this.literal=literal;
        connectingKbTag=new KbTag(a);
    }
    
    public KbTag(Entity e, Dimension d) {
        type=ENTITY;
        this.uri=e.getUri();
        this.label=e.getLabel();
        connectingKbTag=new KbTag(d);
    }    

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public KbTag getConnectingKbTag() {
        return connectingKbTag;
    }

    public void setConnectingKbTag(KbTag connectingKbTag) {
        this.connectingKbTag = connectingKbTag;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "KbTag{" + "type=" + type + ", uri=" + uri + ", label=" + label + ", literal=" + literal + ", connectingKbTag=" + connectingKbTag + '}';
    }    

}
