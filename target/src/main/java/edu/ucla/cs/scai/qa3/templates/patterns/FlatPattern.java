/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.templates.patterns;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class FlatPattern {

    public static final String TEMPLATE = "template",
            ORDER_BY_SUM_MEASURE = "orderbysummeasure";

    String name, type;
    ArrayList<FlatPatternElement> elements = new ArrayList<>();
    String template;
    String expression;

    public FlatPattern(String name, String stringPattern) throws Exception {
        this.name = name;
        String patternString = "";

        String[] lines = stringPattern.split("\n");
        ArrayList<String> nonCommentLines = new ArrayList<>();
        for (String l : lines) {
            if (!l.startsWith("%")) {
                nonCommentLines.add(l);
            }
        }
        lines = new String[nonCommentLines.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = nonCommentLines.get(i);
        }
        type = lines[0];
        patternString = lines[1];

        this.expression = patternString;
        String[] tokens = patternString.split(" ");
        for (String token : tokens) {
            elements.add(new FlatPatternElement(token));
        }

        int i = 2;
        while (i < lines.length) {
            if (lines[i].isEmpty()) {
                i++;
                continue;
            }
            template = "";
            while (i < lines.length && !lines[i].isEmpty()) {
                template += lines[i] + "\n";
                i++;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
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
        final FlatPattern other = (FlatPattern) obj;
        return Objects.equals(this.name, other.name);
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public String toString() {
        return "FlatPattern{" + "name=" + name + ", elements=" + elements + '}';
    }

}
