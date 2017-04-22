/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.templates.patterns;

import edu.ucla.cs.scai.qa3.tokenizer.Token;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class FlatPatternElement {

    public static final int SIMPLE = 1, WILDCARD = 0, PLUS = 2;

    int multiplicity;

    HashSet<String> tags = new HashSet<>();
    HashSet<String> lemmas = new HashSet<>();
    HashSet<String> words = new HashSet<>();
    HashSet<String> notTags = new HashSet<>();
    HashSet<String> notLemmas = new HashSet<>();
    HashSet<String> notWords = new HashSet<>();
    HashSet<String> ners = new HashSet<>();
    HashSet<String> notNers = new HashSet<>();
    HashSet<String> kbTags = new HashSet<>();
    HashSet<String> notKbTags = new HashSet<>();
    HashSet<String> aggregateFunctions = new HashSet<>();
    HashSet<String> notAggregateFunctions = new HashSet<>();
    String label;
    HashMap<String, String> labels = new HashMap<>();
    boolean not;

    FlatPatternElement(String token) throws Exception {
        String originalToken = token;
        if (token == null || token.trim().length() == 0) {
            throw new Exception("Received an empty token");
        }
        token = token.trim();
        if (token.startsWith("{")) {
            if (token.endsWith("*")) {
                multiplicity = WILDCARD;
            } else if (token.endsWith("+")) {
                multiplicity = PLUS;
            } else {
                throw new Exception("Unrecognized token: " + originalToken);
            }
            if (token.length() < 4 || token.charAt(token.length() - 2) != '}') {
                throw new Exception("Unrecognized token: " + originalToken);
            }
            token = token.substring(1, token.length() - 2);
        } else {
            multiplicity = SIMPLE;
        }

        if (token.charAt(0) == '^') {
            not = true;
            if (token.length() < 4 || token.charAt(1) != '(' || token.charAt(token.length() - 1) != ')') {
                throw new Exception("Unrecognized token: " + originalToken);
            }
            token = token.substring(2, token.length() - 1);
        }
        String[] s = token.split("#");
        String[] elements = s[0].split(",");
        for (String v : elements[0].split("\\|")) {
            if (v.startsWith("!")) {
                notTags.add(v.replace("!", ""));
            } else {
                tags.add(v);
            }
        }
        if (elements.length > 1) {
            //System.out.print("/" + elements[1]);
            for (String l : elements[1].split("\\|")) {
                if (l.startsWith("!")) {
                    notLemmas.add(l.replace("!", "").toLowerCase());
                } else {
                    lemmas.add(l.toLowerCase());
                }
            }
            if (elements.length > 2) {
                for (String l : elements[2].split("\\|")) {
                    if (l.startsWith("!")) {
                        notWords.add(l.replace("!", "").toLowerCase());
                    } else {
                        words.add(l.toLowerCase());
                    }
                }
                if (elements.length > 3) {
                    for (String l : elements[3].split("\\|")) {
                        if (l.startsWith("!")) {
                            notNers.add(l.replace("!", ""));
                        } else {
                            ners.add(l);
                        }
                    }
                    if (elements.length > 4) {
                        for (String l : elements[4].split("\\|")) {
                            if (l.startsWith("!")) {
                                notKbTags.add(l.replace("!", ""));
                            } else {
                                kbTags.add(l);
                            }
                        }
                        if (elements.length > 5) {
                            for (String l : elements[5].split("\\|")) {
                                if (l.startsWith("!")) {
                                    notAggregateFunctions.add(l.replace("!", ""));
                                } else {
                                    aggregateFunctions.add(l);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (s.length > 1) {
            label = s[1];
            //System.out.print("#" + label);
        }
    }

    public boolean match(Token token) {

        //check for the word-level negation (!)
        if (notTags.contains(token.tag()) || notLemmas.contains(token.lemma()) || notNers.contains(token.ner()) || token.hasKbTag(notKbTags) || notAggregateFunctions.contains(token.aggregateFunction())) {
            return false;
        }
        boolean ok
                = (tags.isEmpty() || tags.contains("_") || tags.contains(token.tag()))
                && (lemmas.isEmpty() || lemmas.contains("_") || lemmas.contains(token.lemma()))
                && (words.isEmpty() || words.contains("_") || words.contains(token.lemma()))
                && (ners.isEmpty() || ners.contains("_") || ners.contains(token.ner()))
                && (kbTags.isEmpty() || kbTags.contains("_") || token.hasKbTag(kbTags))
                && (aggregateFunctions.isEmpty() || aggregateFunctions.contains("_") || aggregateFunctions.contains(token.aggregateFunction()));

        return ok == !not;

    }

    public void print(int l) {
        for (int i = 0; i < l; i++) {
            System.out.print("\t");
        }
        if (not) {
            System.out.print("^");
        }
        print(tags);
        if (notTags.size() > 0) {
            System.out.print("!(");
            print(notTags);
            System.out.print(")");
        }
        if (lemmas.size() > 0 || notLemmas.size() > 0) {
            System.out.print("/");
        }
        if (lemmas.size() > 0) {
            print(lemmas);
        }
        if (notTags.size() > 0) {
            System.out.print("!(");
            print(notTags);
            System.out.print(")");
        }
        if (ners.size() > 0) {
            System.out.print("/");
            print(ners);
        }
        if (label != null) {
            System.out.print("#" + label);
        }
    }

    private void print(HashSet<String> a) {
        String delimiter = "";
        for (String s : a) {
            System.out.print(delimiter + s);
            delimiter = "|";
        }
    }

    public boolean sameAs(FlatPatternElement n) {
        return sameValues(n) && sameLemmas(n) && sameNotValues(n) && sameNotLemmas(n) && sameNers(n) && not == n.not;
    }

    public boolean sameValues(FlatPatternElement n) {
        HashSet<String> vs = new HashSet<>(n.tags);
        vs.removeAll(tags);
        return vs.isEmpty();
    }

    public boolean sameLemmas(FlatPatternElement n) {
        HashSet<String> ls = new HashSet<>(n.lemmas);
        ls.removeAll(lemmas);
        return ls.isEmpty();
    }

    public boolean sameNotValues(FlatPatternElement n) {
        HashSet<String> vs = new HashSet<>(n.notTags);
        vs.removeAll(notTags);
        return vs.isEmpty();
    }

    public boolean sameNotLemmas(FlatPatternElement n) {
        HashSet<String> ls = new HashSet<>(n.notLemmas);
        ls.removeAll(notLemmas);
        return ls.isEmpty();
    }

    public boolean sameNers(FlatPatternElement n) {
        HashSet<String> ns = new HashSet<>(n.ners);
        ns.removeAll(ners);
        return ns.isEmpty();
    }

    public void fillLabels(String name) {
        if (label != null) {
            labels.put(name, label);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (multiplicity != SIMPLE) {
            sb.append("{");
        }
        if (not) {
            sb.append("^(");
        }
        sb.append("TODO");
        if (not) {
            sb.append(")");
        }
        if (multiplicity != SIMPLE) {
            sb.append("}");
            if (multiplicity == WILDCARD) {
                sb.append("*");
            } else if (multiplicity == PLUS) {
                sb.append("*");
            }
        }
        return sb.toString();
    }
}
