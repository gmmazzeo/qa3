/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.tokenizer.kbtagger;

import edu.ucla.cs.scai.qa3.tokenizer.Token;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TaggedChunk implements Comparable<TaggedChunk> {

    String text;
    int begin;
    int end;
    ArrayList<Token> tokens;
    ArrayList<KbTag> kbTags;

    public TaggedChunk() {
    }

    public TaggedChunk(String text, int begin, int end) {
        this.text = text;
        this.begin = begin;
        this.end = end;
        kbTags = new ArrayList<>();
    }

    public TaggedChunk(String text, int begin, int end, ArrayList<KbTag> kbTags) {
        this.text = text;
        this.begin = begin;
        this.end = end;
        this.kbTags = kbTags;
    }

    public int begin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int end() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<KbTag> getKbTags() {
        return kbTags;
    }

    public void setKbTags(ArrayList<KbTag> kbTags) {
        this.kbTags = kbTags;
    }

    public void assignTokens(Collection<Token> tokens) {
        this.tokens = new ArrayList<>();
        for (Token t : tokens) {
            if (begin <= t.beginPosition() && end >= t.endPosition()) {
                this.tokens.add(t);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(text).append("[").append(begin).append(":").append(end).append("]:");
        if (kbTags.isEmpty()) {
            sb.append("No tags");
        } else {
            for (KbTag kbt:kbTags) {
                sb.append(" ").append(kbt);
            }
        }
        return sb.toString();
    }

    @Override
    public int compareTo(TaggedChunk o) {
        return Integer.compare(begin, o.begin);
    }
}
