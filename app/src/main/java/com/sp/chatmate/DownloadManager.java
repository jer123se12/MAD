package com.sp.chatmate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kbj.androxlsxparser.mxlsxparser.StreamingReader;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class DownloadManager extends AppCompatActivity {
    private static String file_url="https://tsukubawebcorpus.jp/static/xlsx/NLT1.40_freq_list.xlsx";
    private static String dict_url="http://ftp.edrdg.org/pub/Nihongo/JMdict_b.gz";
    private static String fileName="download.xlsx";
    private static String dictName="compressedDictionary.gz";
    Handler handler;
    TextView status;
    private HashMap<String,Boolean>hashm;
    private HashMap<String, Integer>freqH;
    ProgressBar prog;
    vocabHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("path", getExternalFilesDir(null).toString());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download_manager);


        prog=findViewById(R.id.fileDownloadBar);
        ProgressBar prog2=findViewById(R.id.prog2);
        prog.setMax(100);
        ExecutorService executor= Executors.newSingleThreadExecutor();
        handler=new Handler(Looper.getMainLooper());
        status=findViewById(R.id.status);
        helper=new vocabHelper(this,"japanese");
        helper.deleteALl();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        executor.execute(DownloadFile(file_url,getExternalFilesDir(null).toString()+ "/"+fileName,prog));
        executor.execute(DownloadFile(dict_url,getExternalFilesDir(null).toString()+"/"+dictName,prog2));
    }


    public void next(String dest){
        if (dest.equals(getExternalFilesDir(null).toString()+ "/"+fileName)){

        }else{
            try{
            final OutputStream out = new FileOutputStream(new File(getExternalFilesDir(null).toString()+"/"+"output.xml"));
                final InputStream in   = new GZIPInputStream(new FileInputStream(new File(dest))) ;
                IOUtil.copyCompletely(in,out);
                out.close();
                in.close();
                ExecutorService executor= Executors.newSingleThreadExecutor();
                executor.execute(parseFile);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }
    Runnable finalthing=new Runnable() {
        @Override
        public void run() {

        }
    };
    Runnable parseXML=new Runnable() {
        @Override
        public void run() {
            try {
                File fXmlFile = new File(getExternalFilesDir(null).toString()+"/"+"output.xml");
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                JMDictParser filep= new JMDictParser();
                filep.setProgress(handler,findViewById(R.id.status));
                filep.setContext(DownloadManager.this);
                filep.setFreqList(freqH);
                saxParser.parse(fXmlFile,filep);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "done",  Toast.LENGTH_LONG).show();
                        startActivity(new Intent(DownloadManager.this, MainActivity.class));
                    }
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };
    Runnable parseFile=new Runnable() {
        @Override
        public void run() {
            try {


                InputStream is = new FileInputStream(new File(getExternalFilesDir(null).toString()
                        + "/"+fileName));
                Workbook workbook = StreamingReader.builder()
                        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
                        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                        .open(getBaseContext(), is);
                Sheet sheet = workbook.getSheetAt(1);
                int lastRow=sheet.getLastRowNum();
                int startRow=1;
                int numberOfErrors=0;
                int row=0;
                freqH=new HashMap<>();
                for (Row r : sheet) {
                    row++;
                    if (row==1){
                        continue;
                    }

                    int finalRow=row;
                    if (r.getCell(1)!=null && r.getCell(1).getStringCellValue()!="記号") {
                        if (r.getCell(0) != null && r.getCell(3)!=null) {
                            freqH.put(r.getCell(0).getStringCellValue().strip(),Integer.parseInt(r.getCell(3).getStringCellValue().replace(",","")));



                        }
                    }
                    if(row%133==0){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                status.setText(String.valueOf(finalRow)+" / "+String.valueOf(lastRow)+" words parsed");

                            }
                        });
                    }
                }

                Log.i("total", "cannot find"+String.valueOf(numberOfErrors)+"/"+String.valueOf(lastRow)+" "+String.valueOf((int)(((float)numberOfErrors/(float)lastRow)*100))+"%");
                Log.i("total", "words found "+String.valueOf(lastRow-numberOfErrors));
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        ExecutorService executor= Executors.newSingleThreadExecutor();
                        executor.execute(parseXML);
                    }
                });






            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    private Runnable DownloadFile(String fileurl, String dest){
        return DownloadFile(fileurl,dest,null);
    }
    private Runnable DownloadFile(String fileurl, String dest, ProgressBar progress) {
        if (progress!=null){
            progress.setMax(100);
        }
        Runnable downloadFile = new Runnable() {
            @Override
            public void run() {

                if (new File(dest).exists()){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setProgress(100);
                            next(dest);
                        }
                    });
                    return;
                }
                int count;
                try {
                    URL url = new URL(fileurl);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    // this will be useful so that you can show a tipical 0-100%
                    // progress bar
                    int lenghtOfFile = connection.getContentLength();


                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(dest);


                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        //publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                        int finalTotal = (int) (((total * 100) / lenghtOfFile));
                        if (progress!=null){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress(finalTotal);
                            }
                        });
                        }

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            next(dest);
                        }
                    });

                } catch (Exception e) {
                    Log.e("Error: ", e.toString());
                }
            }
        };
        return downloadFile;
    }
}
