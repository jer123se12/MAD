package com.sp.chatmate;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;
public class JMDictParser extends DefaultHandler {
    private HashMap<String,dictEntry> wordList= new HashMap<>();
    private long counter=0;
    private List<dictEntry> words= null;
    private dictEntry entr = null;
    private StringBuilder data = null;
    private boolean hasReading=false;
    private boolean hasMeaning=false;
    private boolean hasName=false;
    private Handler handler;
    private TextView progress;

    public void setProgress(Handler handler, TextView progress) {
        this.handler = handler;
        this.progress = progress;
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                progress.setText("0 words processed");
            }
        });
    }
    public void publshProgress(long count){
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                progress.setText(String.valueOf(count)+" words processed");
            }
        });
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(qName){
            case"keb":
                dictEntry word=new dictEntry();
                word.setTerm(data.toString().strip());
                if(data.toString().equals("の")){
                    Log.i("correct", "inside");
                }
                words.add(word);
                break;
            case "reb":
                if(!hasReading) {
                    if (!hasName){
                        dictEntry wordd=new dictEntry();
                        wordd.setTerm(data.toString().strip());
                        if(data.toString().equals("の")){
                            Log.i("correct", "inside");
                        }
                        words.add(wordd);
                    }
                    for (int i = 0; i < words.size(); i++) {
                        words.get(i).setReading(data.toString());
                    }
                    hasReading = true;
                }
                break;
            case "gloss":
                if(!hasMeaning){
                    for (int i=0;i<words.size();i++){
                        words.get(i).setDefinition(data.toString());
                    }
                    hasMeaning=true;
                }
                break;
            case "entry":
                for (int i=0;i<words.size();i++){
                    wordList.put(words.get(i).term,words.get(i));
                }
                counter++;
                if (counter%100==0){
                    publshProgress(counter);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName){
            case "entry":
                words=new ArrayList<>();
                hasMeaning=false;
                hasReading=false;
                break;
            default:
                break;
        }
        data=new StringBuilder();
    }
    public HashMap<String,dictEntry> getHashmap(){
        return wordList;
    }

}
