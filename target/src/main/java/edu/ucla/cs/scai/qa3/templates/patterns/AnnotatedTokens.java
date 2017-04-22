package edu.ucla.cs.scai.qa3.templates.patterns;

import edu.stanford.nlp.ling.CoreLabel;
import edu.ucla.cs.scai.qa3.tokenizer.Token;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AnnotatedTokens {

    ArrayList<CoreLabel> tokens = new ArrayList<>();
    HashMap<String, ArrayList<Token>> annotations = new HashMap<>();

    public ArrayList<CoreLabel> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<CoreLabel> tokens) {
        this.tokens = tokens;
    }

    public HashMap<String, ArrayList<Token>> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(HashMap<String, ArrayList<Token>> annotations) {
        this.annotations = annotations;
    }

}
