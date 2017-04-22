/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3.templates.patterns;

import edu.ucla.cs.scai.qa3.tokenizer.Token;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class FlatPatternMatcher {

    private static final HashMap<String, HashMap<String, FlatPattern>> patterns = new HashMap<>();

    private static ArrayList<String> getResources(final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(File.pathSeparator);
        for (final String element : classPathElements) {
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    private static ArrayList<String> getResources(final String element, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(getResourcesFromDirectory(file, pattern));
        }
        return retval;
    }

    private static ArrayList<String> getResourcesFromDirectory(final File directory, final Pattern pattern) {
        System.out.println("Loading patterns in " + directory.getAbsolutePath());
        final ArrayList<String> retval = new ArrayList<>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else {
                try {
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        retval.add(fileName);
                    }
                } catch (final IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }

    public FlatPatternMatcher() {
        this(getResources(Pattern.compile(".*\\.fprn")));
    }

    public FlatPatternMatcher(final String directoryName) {
        this(new File(directoryName));
    }

    public FlatPatternMatcher(final File directory) {
        this(getResourcesFromDirectory(directory, Pattern.compile(".*\\.fprn")));
    }

    public FlatPatternMatcher(Collection<String> list) {
        for (final String fileName : list) {
            System.out.println(fileName);
            try {
                BufferedReader in = new BufferedReader(new FileReader(fileName));
                String l = in.readLine();
                String s = "";
                while (l != null) {
                    if (s.length() > 0) {
                        s += "\n";
                    }
                    s += l;
                    l = in.readLine();
                }
                String[] fn = fileName.split(File.separator);
                fn = fn[fn.length - 1].split("\\.");
                FlatPattern p = new FlatPattern(fn[0], s);
                HashMap<String, FlatPattern> typePatterns = patterns.get(p.type);
                if (typePatterns == null) {
                    typePatterns = new HashMap<>();
                    patterns.put(p.type, typePatterns);
                }
                typePatterns.put(p.name, p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean recursiveMatch(ArrayList<Token> tokens, ArrayList<FlatPatternElement> patternElements, int indexTokens, int indexElements, AnnotatedTokens result) {
        if (indexTokens == 10) {
            System.out.print("");
        }
        if (indexElements == patternElements.size()) { //no more pattern elements to be matched
            return indexTokens == tokens.size();
        }
        if (indexTokens == tokens.size()) {
            return false;
        }

        FlatPatternElement patternElement = patternElements.get(indexElements);
        if (patternElement.multiplicity == FlatPatternElement.SIMPLE) {
            if (patternElement.match(tokens.get(indexTokens))) {
                if (patternElement.label != null) {
                    ArrayList<Token> ann = new ArrayList<>();
                    ann.add(tokens.get(indexTokens));
                    result.annotations.put(patternElement.label, ann);
                }
                return recursiveMatch(tokens, patternElements, indexTokens + 1, indexElements + 1, result);
            }
        } else { //find the longest sequence of tokens matching the pattern element
            int lastAcceptedTokenIndex = indexTokens - 1;
            ArrayList<Token> ann = new ArrayList<>();
            while (lastAcceptedTokenIndex < tokens.size() - 1 && patternElement.match(tokens.get(lastAcceptedTokenIndex + 1))) {
                lastAcceptedTokenIndex++;
                ann.add(tokens.get(lastAcceptedTokenIndex));
            }
            for (int i = lastAcceptedTokenIndex; i >= (patternElement.multiplicity == FlatPatternElement.PLUS ? indexTokens : indexTokens - 1); i--) {
                if (recursiveMatch(tokens, patternElements, i + 1, indexElements + 1, result)) {
                    if (patternElement.label != null) {
                        if (result.annotations.containsKey(patternElement.label)) {
                            ann.addAll(result.annotations.get(patternElement.label));
                        }
                        result.annotations.put(patternElement.label, ann);
                    }
                    return true;
                }
                if (!ann.isEmpty()) {
                    ann.remove(ann.size() - 1);
                }
            }
        }
        return false;
    }

    public HashMap<FlatPattern, AnnotatedTokens> match(ArrayList<Token> tokens, String type) throws Exception {
        if (!patterns.containsKey(type)) {
            return null;
        }
        HashMap<FlatPattern, AnnotatedTokens> res = new HashMap<>();
        for (FlatPattern pattern : patterns.get(type).values()) {
            AnnotatedTokens result = new AnnotatedTokens();
            if (recursiveMatch(tokens, pattern.elements, 0, 0, result)) {
                res.put(pattern, result);
            }
        }
        return res;
    }

    public FlatPattern getPatternByName(String name, String type) {
        return patterns.get(type).get(name);
    }

}
