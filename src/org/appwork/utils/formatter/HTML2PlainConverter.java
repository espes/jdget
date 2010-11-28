package org.appwork.utils.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HTML2PlainConverter {

    private static class HtmlParser extends HTMLEditorKit.ParserCallback {
        StringBuffer s = null;

        public HtmlParser() {

        }

        public String getText() {
            return s.toString();
        }

        @Override
        public void handleText(final char[] text, final int pos) {
            s.append(text);
            s.append("\n");
        }

        public void parse(final Reader in) throws IOException {
            s = new StringBuffer();
            final ParserDelegator delegator = new ParserDelegator();
            delegator.parse(in, this, Boolean.TRUE);
        }
    }

    /**
     * @param text
     */
    public static String convert(final String text) {
        final HtmlParser parser = new HtmlParser();
        try {
            parser.parse(new StringReader(text));
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parser.getText();
    }

}
