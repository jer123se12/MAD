package com.sp.chatmate;

public class Card{
    public String lang;
    public String term;
    public String definition;
    public int id;
    public Card(String lang,String term,String definition,int id){
        this.lang=lang;
        this.term=term;
        this.definition=definition;
        this.id=id;
    }
    //updated
    public String getTerm() {
        return term;
    }

    // Getter for the definition updated
    public String getDefinition() {
        return definition;
    }

    // Getter for the ID updated
    public int getId() {
        return id;
    }
}
