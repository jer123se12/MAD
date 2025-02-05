package com.sp.chatmate;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

public class JMDictParser extends DefaultHandler {
    private List<dictEntry> wordList= null;
    private dictEntry entr = null;
    private StringBuilder data = null;
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        Log.i("element", qName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Log.i("element", qName);
        data=new StringBuilder();
    }
}
