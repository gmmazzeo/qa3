package edu.ucla.cs.scai.qa3.tokenizer.kbtagger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface KBTagger {
    
    public KBTaggingResult tag(String question) throws Exception;
    
}
