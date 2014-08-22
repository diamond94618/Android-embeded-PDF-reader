package com.artifex.mupdfdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;


public class ProductsPlistParsing {   
Context context;

   // constructor for  to get the context object from where you are using this plist parsing
    public ProductsPlistParsing(Context ctx) {

        context = ctx;
    }

    public List<HashMap<String, String>> getProductsPlistValues() {

       // specifying the  your plist file.And Xml ResourceParser is an event type parser for more details Read android source
        XmlResourceParser parser = context.getResources()
                .getXml(R.xml.chapters);


          // flag points to find key and value tags .
        boolean keytag = false;
        boolean valuetag = false;
        String keyStaring = null;
        String stringvalue = null;


        HashMap<String, String> hashmap = new HashMap<String, String>();
        List<HashMap<String, String>> listResult = new ArrayList<HashMap<String, String>>();
        int event;
        try {
            event = parser.getEventType();

             // repeting the loop at the end of the doccument

            while (event != parser.END_DOCUMENT) {

               switch (event) {
                       //use switch case than the if ,else statements 
                case 0:
                        // start doccumnt nothing to do
                       // System.out.println("\n" + parser.START_DOCUMENT
                       // + "strat doccument");
                      // System.out.println(parser.getName());
                    break;
                case 1:
                    // end doccument
                    // System.out
                    // .println("\n" + parser.END_DOCUMENT + "end doccument");
                    // System.out.println(parser.getName());
                    break;
                case 2:

                    if (parser.getName().equals("key")) {
                        keytag = true;
                        valuetag = false;
                    }
                    if (parser.getName().equals("string")||parser.getName().equals("integer")) {
                        valuetag = true;
                    }

                    break;
                case 3:
                    if (parser.getName().equals("dict")) {
                        System.out.println("end tag");
                        listResult.add(hashmap);
                        System.out.println(listResult.size() + "size");
                        hashmap = null;
                        hashmap = new HashMap<String, String>();
                    }
                    break;
                case 4:
                    if (keytag) {
                        if (valuetag == false) {
                            // hashmap.put("value", parser.getText());
                            // System.out.println(parser.getText());
                            // starttag = false;
                            keyStaring = parser.getText();
                        }
                    }
                    if (valuetag && keytag) {
                        stringvalue = parser.getText();

                        hashmap.put(keyStaring, stringvalue);
                        // System.out.println(keyStaring);
                        // System.out.println(stringvalue);
                        valuetag = false;
                        keytag = false;
                        // System.out.println("this is hash map"
                        // + hashmap.get(keyStaring));
                        // Toast.makeText(getApplication(), keyStaring,
                        // Toast.LENGTH_SHORT).show();

                    }
                    break;
                default:
                    break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 //here you get the plistValues.
        return listResult;
    }
}

