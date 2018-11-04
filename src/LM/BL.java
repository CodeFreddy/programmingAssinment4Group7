package LM;

import edu.unh.cs.treccar_v2.Data;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.*;

public class BL {
    private static String INDEX_DIR = "C:\\CS853\\index";
    private static String OUTLINE = "C:\\CS853\\programmingAssignment4Group7\\output";
    private  static String output = "results_bl.run";
    private static Integer numDocs = 100;
    private static QueryParser parser = null;
    private static  String teamName = "Group 7";
    private  static String methodName = "Bigram";

    private static IndexReader getInedexReader(String path) throws IOException {
        return DirectoryReader.open(FSDirectory.open((new File(path).toPath())));
    }

    public static SimilarityBase getFreqSimilarityBase() throws IOException {
        SimilarityBase freqSim = new SimilarityBase() {
            @Override
            protected float score(BasicStats stats, float freq, float docLen) {
                return freq;
            }

            @Override
            public String toString() {
                return null;
            }
        };
        return freqSim;
    }

    public static void RankDocWithBigram(ArrayList<Data.Page> queryList) {
        ArrayList<String> runFileStrList = new ArrayList<String>();
        if (queryList != null) {
            for (Data.Page p : queryList) {
                String queryStr = p.getPageId();
                System.out.println("Query String: " + queryStr);
                HashMap<String, Float> result_map = getRankedDocuments(queryStr);
                int i = 0;
                for (Map.Entry<String, Float> entry : result_map.entrySet()) {
                    String runFileString = queryStr + " Q0 " + entry.getKey() + " " + i + " " + entry.getValue() + " "
                            + teamName + " " + methodName;
                    runFileStrList.add(runFileString);
                    i++;
                }
            }
        }

        // Write run file function
        if (runFileStrList.size() > 0) {
            writeStrListToRunFile(runFileStrList, OUTLINE+"\\"+output);
        } else {
            System.out.println("No result for run file.");
        }

    }

    private static HashMap<String, Float> getRankedDocuments(String queryStr) {
        HashMap<String, Float> doc_score = new HashMap<String, Float>();

        try {
            IndexReader ir = getInedexReader(INDEX_DIR);
            IndexSearcher se = new IndexSearcher(ir);
            se.setSimilarity(getFreqSimilarityBase());
            parser = new QueryParser("parabody", new StandardAnalyzer());

            Query q = parser.parse(queryStr);
            TopDocs topDocs = se.search(q, numDocs);
            ScoreDoc[] hits = topDocs.scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document doc = se.doc(hits[i].doc);
                String docId = doc.get("paraid");
                String docBody = doc.get("parabody");
                ArrayList<Float> p_wt = new ArrayList<Float>();


                ArrayList<String> bigram_list = analyzeByBigram(docBody);
                ArrayList<String> unigram_list = analyzeByUnigram(docBody);
                ArrayList<String> query_list = analyzeByUnigram(queryStr);

                // Size of vocabulary
                int size_of_voc = getSizeOfVocabulary(unigram_list);
                int size_of_doc = unigram_list.size();

                String pre_term = "";
                for (String term : query_list) {
                    if (pre_term == "") {
                        int tf = countExactFreqInList(term, unigram_list);
                        float p = laplaceSmoothing(tf, size_of_doc, size_of_voc);
                        p_wt.add(p);
                    } else {
                        // Get total occurrences with given term.
                        String wildcard = pre_term + " ";
                        int tf_given_term = countStrFreqInList(wildcard, bigram_list);

                        // Get occurrences of term with given term.
                        String str = pre_term + " " + term;
                        int tf = countExactFreqInList(str, bigram_list);
                        float p = laplaceSmoothing(tf_given_term, tf, size_of_voc);
                        p_wt.add(p);
                    }
                    pre_term = term;
                }

                // Caculate score with log;
                System.out.println(p_wt);
                float score = getScoreByPListWithLog(p_wt);
                doc_score.put(docId, score);

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return sortByValue(doc_score);
    }

    private static float getScoreByPListWithLog(ArrayList<Float> p_list) {
        float score = 0;
        for (Float wt : p_list) {
            score = (float) ((float) score + Math.log(wt));
        }
        return score;

    }

    private static int countExactFreqInList(String term, ArrayList<String> list) {
        int occurrences = Collections.frequency(list, term);
        return occurrences;
    }


    private static float laplaceSmoothing(int tf_given_term, int tf, int size_of_v) {
        float p = (float) (tf_given_term + 1) / (float) (tf + size_of_v);
        return p;
    }

    private static int countStrFreqInList(String term, ArrayList<String> list) {
        int occurrences = 0;
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i);
            if (str.contains(term)) {
                occurrences++;
            }

        }
        return occurrences;
    }

    private static int getSizeOfVocabulary(ArrayList<String> unigramList) {
        ArrayList<String> list = new ArrayList<String>();
        Set<String> hs = new HashSet<>();

        hs.addAll(unigramList);
        list.addAll(hs);
        return list.size();
    }

    private static ArrayList<String> analyzeByBigram(String inputStr) throws IOException {
        ArrayList<String> strList = new ArrayList<String>();
        Analyzer analyzer = new BigramAnalyzer();
        TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            String token = charTermAttribute.toString();
            strList.add(token);

        }
        tokenizer.end();
        tokenizer.close();
        return strList;
    }

    private static ArrayList<String> analyzeByUnigram(String inputStr) throws IOException {

        ArrayList<String> strList = new ArrayList<String>();
        Analyzer analyzer = new UnigramAnalyzer();
        TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            String token = charTermAttribute.toString();
            strList.add(token);
        }
        tokenizer.end();
        tokenizer.close();
        return strList;
    }

    private static HashMap<String, Float> sortByValue(Map<String, Float> unsortMap) {

        List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {

            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<String, Float> sortedMap = new LinkedHashMap<String, Float>();
        for (Map.Entry<String, Float> entry : list)

        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void writeStrListToRunFile(ArrayList<String> strList, String path) {

        BufferedWriter bWriter = null;
        FileWriter fWriter = null;

        try {
            fWriter = new FileWriter(path);
            bWriter = new BufferedWriter(fWriter);

            for (String line : strList) {

                bWriter.write(line);
                bWriter.newLine();
            }

            System.out.println("Write all ranking result to run file: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bWriter != null) {
                    bWriter.close();
                }
                if (fWriter != null) {
                    fWriter.close();
                }
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }

    }

}
