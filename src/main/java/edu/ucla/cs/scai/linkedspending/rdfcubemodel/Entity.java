/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.rdfcubemodel;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Entity implements Serializable {

    public String uri, label;

    public Entity(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public boolean setLabel(String label) {
        boolean res=this.label==null;
        if (!res && !this.label.equals(label)) {
            System.out.println("Label was "+this.label+" replaced with "+label);
        }
        this.label = label;
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.uri);
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
        final Entity other = (Entity) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        return true;
    }
    
    public String getKeyWords() {
        return label;
    }

}
