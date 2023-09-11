package com.hokuapps.loadnativefileupload.restrequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ServiceParser {
    public static String TAG = "ServiceParser";

    /**
     *
     * @param result
     * @return
     */
    public static Element getDocumentElement(String result) {
        try {
            // This is not the correct resolution this should be fixed at server end.
            if (result != null) result = result.replaceAll("<>", "&lt;&gt;");
            InputStream streamToParse = stringToInputStream(result);
            if (streamToParse == null) return null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(streamToParse);
            Element root = dom.getDocumentElement();
            return root;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param text
     * @return
     */
    private static InputStream stringToInputStream(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException unsupportedException) {
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

}
