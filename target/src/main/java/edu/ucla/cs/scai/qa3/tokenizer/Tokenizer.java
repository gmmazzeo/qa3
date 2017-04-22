/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.tokenizer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.DataSet;
import edu.ucla.cs.scai.linkedspending.rdfcubemodel.KB;
import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.ExternalPythonTagger;
import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.KBTagger;
import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.KBTaggingResult;
import edu.ucla.cs.scai.qa3.tokenizer.kbtagger.TaggedChunk;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Tokenizer {

    StanfordCoreNLP pipelineTokens;
    KBTagger kbTagger;

    public Tokenizer(KBTagger kbTagger) {
        Properties propsTokens = new Properties();
        propsTokens.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref");
        pipelineTokens = new StanfordCoreNLP(propsTokens);
        this.kbTagger = kbTagger;
    }

    public ArrayList<Token> tokenize(String text) throws Exception {
        Annotation qaTokens = new Annotation(text);
        pipelineTokens.annotate(qaTokens);
        List<CoreMap> qssTokens = qaTokens.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentenceTokens = qssTokens.get(0);
        ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) sentenceTokens.get(CoreAnnotations.TokensAnnotation.class);
        ArrayList<Token> res = new ArrayList<>();
        KBTaggingResult kbTags = kbTagger.tag(text);
        for (CoreLabel t : tokens) {
            res.add(new Token(t, kbTags.getTaggedChunks()));
        }
        for (TaggedChunk tc : kbTags.getTaggedChunks()) {
            tc.assignTokens(res);
        }
        for (int i = 0; i < res.size() - 1; i++) {
            res.get(i).setNext(res.get(i + 1));
        }
        for (int i = 1; i < res.size(); i++) {
            res.get(i).setPrevious(res.get(i - 1));
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new ExternalPythonTagger());
        ArrayList<Token> res = tokenizer.tokenize("What was the average Uganda health budget over all districts in 2014?");
        for (Token t : res) {
            System.out.print(t + "\t");
        }
    }

}
