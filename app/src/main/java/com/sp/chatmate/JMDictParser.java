package com.sp.chatmate;

import android.content.Context;
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
import java.util.zip.CheckedOutputStream;

import android.os.Handler;
public class JMDictParser extends DefaultHandler {
    private HashMap<String,Integer> freqList;
    private long counter=0;
    private List<dictEntry> words= null;
    private dictEntry entr = null;
    private StringBuilder data = null;
    private boolean hasReading=false;
    private boolean hasMeaning=false;
    private boolean hasName=false;
    private vocabHelper helper;
    private Context context;
    private boolean firstTime=true;
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

    public void setContext(Context context) {
        this.context = context;
    }

    public void setFreqList(HashMap<String, Integer> freqList) {
        this.freqList = freqList;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(qName){
            case"keb":
                hasName=true;
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
                    Integer freq=freqList.get(words.get(i).term);
                    if(freq!=null) {
                        helper.create(words.get(i).term, "Reading: " + words.get(i).reading + "\nMeaning: " + words.get(i).definition, freq);
                    }
                }
                counter++;
                if (counter%53==0){
                    publshProgress(counter);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (firstTime){
            firstTime=false;
            helper=new vocabHelper(progress.getContext(), "japanese");
        }
        switch (qName){
            case "entry":
                words=new ArrayList<>();
                hasMeaning=false;
                hasReading=false;
                hasName=false;
                break;
            default:
                break;
        }
        data=new StringBuilder();
    }

}
