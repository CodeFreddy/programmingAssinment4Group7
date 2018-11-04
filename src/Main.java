import LM.BL;
import LM.UDS;
import LM.UJM;
import LM.UnigramLanguageModel;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.*;
import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.List;

//class to initial lucence index and search
public class Main {
    public static final int topSearch = 100;

    public static String INDEX_DIR = "C:\\CS853\\index";
    public static String OUTLINE = "C:\\CS853\\programAssignment3\\test200-train\\train.pages.cbor-outlines.cbor";
    public static String PARAGRAPH = "C:\\CS853\\programAssignment3\\test200-train\\train.pages.cbor-paragraphs.cbor";

    public static String OUTPUT_DIR = "output";

    private static List<Data.Page> getPageList(String path){
        List<Data.Page> pageList = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(path));

            for (Data.Page p : DeserializeData.iterableAnnotations(fileInputStream)){
                pageList.add(p);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return pageList;
    }

    private static List<Data.Paragraph> getParagraphList(String path){
        List<Data.Paragraph> paragraphList = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(path));

            for (Data.Paragraph p : DeserializeData.iterableParagraphs(fileInputStream)){
                paragraphList.add(p);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return paragraphList;
    }

    public static void main(String[] args) throws IOException {


        //get page list

        List<Data.Page> pageList = getPageList(OUTLINE);
        List<Data.Paragraph> paragraphList = getParagraphList(PARAGRAPH);

        Indexer indexer = new Indexer(INDEX_DIR);

        indexer.indexFile(paragraphList);

        //run unigram language model
        UnigramLanguageModel ul = new UnigramLanguageModel(pageList,topSearch,INDEX_DIR);
//        System.out.println(ul.getList().size());
//        for (String s : ul.getList()){
//            System.out.println(s);
//        }
        writeFile("UnigramLanguageModel.run",ul.getList());

        UJM ujm = new UJM(pageList,topSearch,INDEX_DIR);

        writeFile("UnigramLanguageModel-JM.run",ujm.getList());


        ArrayList<Data.Page> pgList = new ArrayList<>(pageList);
        UDS uds = new UDS(pgList);

        BL.RankDocWithBigram(pgList);

    }

    public static void writeFile(String name, List<String> content){
        String fullpath = "/C:\\CS853\\programmingAssignment4Group7\\output" + "\\" + name;
        System.out.println(fullpath);
        try (FileWriter runfile = new FileWriter(new File(fullpath))) {
            for (String line : content) {
                runfile.write(line + "\n");
            }

            runfile.close();
        } catch (IOException e) {
            System.out.println("Could not open " + fullpath);
        }
    }
}
