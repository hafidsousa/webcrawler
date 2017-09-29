package com.hafidsousa.webcrawler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.sax.ToTextContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
public class Parser {

    @Test
    public void name() throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToTextContentHandler();

        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        //        ContentHandlerDecorator handler = new ContentHandlerDecorator() {
//            @Override
//            public void characters(char[] ch, int start, int length) {
//
//                String line = new String(ch);
//                text.add(line);
//            }
//        };

        try (InputStream stream = Parser.class.getResourceAsStream("/dodo_bill.pdf")) {
            parser.parse(stream, handler, metadata);

            String replace = StringUtils.replace(handler.toString(), "\n", " ");

            String[] split = StringUtils.splitByWholeSeparator(replace, ". ");

            for (String s : split) {
                System.out.println(s);
            }

        }


    }

    @Test
    public void namePdfBox() throws IOException, SAXException, TikaException {

        try (InputStream stream = Parser.class.getResourceAsStream("/agl_bill.pdf")) {
            PDDocument doc = PDDocument.load(stream);
            String text = new PDFTextStripper().getText(doc);

            System.out.println(text);

        }


    }
}
