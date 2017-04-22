/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.tokenizer;

import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.KbTag;
import edu.stanford.nlp.ling.CoreLabel;
import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.TaggedChunk;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Token {

    public static final String SUM = "S", AVERAGE = "A", MAX = "M", MIN = "N", NONE = "O";
    CoreLabel token;
    ArrayList<KbTag> kbTags;
    String aggregateFunction;
    Token previous;
    Token next;

    public Token(CoreLabel coreLabel, ArrayList<TaggedChunk> taggedChunks) {
        this.token = coreLabel;
        kbTags = new ArrayList<>();
        for (TaggedChunk tc : taggedChunks) {
            if (token.beginPosition() >= tc.begin() && token.endPosition()-1 <= tc.end() && tc.getKbTags() != null) {
                kbTags.addAll(tc.getKbTags());
            }
        }
        if (kbTags.isEmpty()) { //try the aggregate funcions
            switch (token.lemma()) {
                case "sum":
                case "total":
                    aggregateFunction = SUM;
                    break;
                case "average":
                case "avg":
                    aggregateFunction = AVERAGE;
                    break;
                case "max":
                case "maximum":
                case "highest":
                case "largest":
                    aggregateFunction = MAX;
                    break;
                case "min":
                case "minimum":
                case "lowest":
                case "smallest":
                    aggregateFunction = MIN;
                    break;
                default:
                    aggregateFunction = NONE;
                    break;
            }
        } else {
            aggregateFunction = NONE;
        }
    }

    public String aggregateFunction() {
        return aggregateFunction;
    }

    public String tag() {
        return token.tag();
    }

    public String lemma() {
        return token.lemma();
    }

    public String word() {
        return token.word();
    }

    public String ner() {
        return token.ner();
    }

    public int beginPosition() {
        return token.beginPosition();
    }

    public int endPosition() {
        return token.endPosition()-1;
    }

    public Token previous() {
        return previous;
    }

    public Token next() {
        return next;
    }

    public void setPrevious(Token previous) {
        this.previous = previous;
    }

    public void setNext(Token next) {
        this.next = next;
    }

    public boolean hasKbTag(HashSet<String> kbTagTypes) {
        for (String t : kbTagTypes) {
            if (t.equals(KbTag.NONE)) {
                if (kbTags.isEmpty()) {
                    return true;
                }
            } else {
                for (KbTag t2 : kbTags) {
                    if (t2.getType().equals(t)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String kbTag() {
        String res = "";
        for (KbTag kbt : kbTags) {
            if (!kbt.getType().equals(NONE)) {
                res += kbt.getType();
            }
        }
        if (res.length() == 0) {
            res = "O";
        }
        return res;
    }

    public String aggregationTag() {
        return aggregateFunction;
    }

    public KbTag getKbTag(String kbTagType) {
        for (KbTag tag : kbTags) {
            if (tag.getType().equals(kbTagType)) {
                return tag;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return tag() + "," + lemma() + "," + word() + "," + ner() + "," + kbTag() + "," + aggregationTag();
    }

}
