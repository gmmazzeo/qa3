package edu.ucla.cs.scai.qa3.tokenizer.kbtagger;

import edu.ucla.cs.scai.linkedspending.rdfcubemodel.DataSet;
import java.util.ArrayList;

public class KBTaggingResult {

    DataSet dataset;

    ArrayList<TaggedChunk> taggedChunks;

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public ArrayList<TaggedChunk> getTaggedChunks() {
        return taggedChunks;
    }

    public void setTaggedChunks(ArrayList<TaggedChunk> annotatedChunks) {
        this.taggedChunks = annotatedChunks;
    }
    
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("Dataset: ").append(dataset.getLabel()).append("\n");
        if (taggedChunks.isEmpty()) {
            sb.append("No annotations");
        } else {
            sb.append("Chunks: ");
            for (TaggedChunk tc:taggedChunks) {
                sb.append("\n\t").append(tc.toString());
            }
        }
        return sb.toString();
    }

}
