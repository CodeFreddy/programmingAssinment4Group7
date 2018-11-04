import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Indexer {

    private String indexPath = "";

    public Indexer(String indexPath){
        this.indexPath = indexPath;
    }


    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter() throws IOException {
        if (indexWriter == null){
            Directory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());


            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            indexWriter= new IndexWriter(indexDir,config);
        }
        return indexWriter;
    }


    public void closeIndexWriter() throws IOException{
        if (indexWriter != null){
            indexWriter.close();
        }
    }

    public void indexFile(Data.Paragraph p) throws IOException {
        if(p != null){
            IndexWriter writer = getIndexWriter();
            Document d = new Document();
            d.add(new StringField("paraid",p.getParaId(), Field.Store.YES));
            d.add(new TextField("parabody",p.getTextOnly(),Field.Store.YES));

            FieldType indexType = new FieldType();
            indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            indexType.setStored(true);
            indexType.setStoreTermVectors(true);

            d.add(new Field("content",p.getTextOnly(),indexType));
            writer.addDocument(d);
        }
    }



    public void indexFile(List<Data.Paragraph> paragraphList) throws IOException{
        if (paragraphList.isEmpty()) return;
        IndexWriter writer = getIndexWriter();

        for (Data.Paragraph p: paragraphList){

            Document d = new Document();
            d.add(new StringField("paraid",p.getParaId(), Field.Store.YES));
            d.add(new TextField("parabody",p.getTextOnly(),Field.Store.YES));

            FieldType indexType = new FieldType();
            indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            indexType.setStored(true);
            indexType.setStoreTermVectors(true);

            d.add(new Field("content",p.getTextOnly(),indexType));
            writer.addDocument(d);
        }
        writer.close();

    }

    public void rebuildIndexes(List<Paragraph> list) throws IOException, CborException {
//        getIndexWriter();
//        if (!list.isEmpty()){
//            for (Paragraph p : list){
//                indexFile(p);
//            }
//            closeIndexWriter();
//        }
    }




}
