public class Paragraph {


    private String paraID;
    private  String paraText;

    public Paragraph(){

    }

    public Paragraph(String paraID,String paraText){
        this.paraID = paraID;
        this.paraText = paraText;
    }

    public String getParaID(){
        return this.paraID;
    }

    public String getParaText() {
        return this.paraText;
    }

    public void setParaID(String paraID){
        this.paraID = paraID;
    }

    public void setParaText(String paraText){
        this.paraText = paraText;
    }
}
