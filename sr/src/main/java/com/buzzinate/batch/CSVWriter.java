package com.buzzinate.batch;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class CSVWriter implements Closeable {

    public static final char FIELD_SEP_SEMICOLON = ';';
    public static final char FIELD_SEP_COMMA = ',';
    public static final char FIELD_SEP_TAB = '\t';
    public static final char FIELD_SEP_BAR = '|';
    
    public static final char QUOTE_SINGLE = '\'';
    public static final char QUOTE_DOUBLE = '"';
    
    public static final String EOL_CR = "\r";
    public static final String EOL_LF = "\n";
    public static final String EOL_CRLF = "\r\n";
    
    
    private final Writer writer;
    private final char fieldSep;
    private final char quoteChar;
    private final String lineSep;
    
    private final String fieldSepStr;
    private final String quoteCharStr;
    private final String doubleQuoteCharStr;
    private final String quoteCharPattern;
    
    public static void main(String[] args) throws IOException {
    	CSVWriter w = new CSVWriter(new OutputStreamWriter(System.out));
    	w.write(Arrays.asList("col 1", "col 2", "col 3"));
    	w.write(Arrays.asList("val \" 1", "val \" 2", "val 3"));
    }

    public CSVWriter(Writer writer) {
        this(writer, FIELD_SEP_COMMA, QUOTE_DOUBLE, EOL_CRLF);
    }
    
    public CSVWriter(Writer writer, char fieldSeparator) {
        this(writer, fieldSeparator, QUOTE_DOUBLE, EOL_CRLF);
    }
    
    public CSVWriter(Writer writer, char fieldSeparator, char quoteChar, String lineSeparator) {
        this.writer = writer;
        this.fieldSep = fieldSeparator;
        this.quoteChar = quoteChar;
        this.lineSep = lineSeparator;
        
        this.fieldSepStr = String.valueOf(fieldSep);
        this.quoteCharStr = String.valueOf(quoteChar);
        this.doubleQuoteCharStr = quoteChar + "" + quoteChar;
        this.quoteCharPattern = "\\" + quoteChar;
    }

    public char getFieldSeparator() {
        return fieldSep;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public String getLineSeparator() {
        return lineSep;
    }
    
    public void flush() throws IOException {
    	writer.flush();
    }

    /**
     * Close underlying writer.
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }
    
    /**
     * Write row of values as CSV.
     */
    public void write(List<String> list) throws IOException {
        boolean isFirst = true;
        for (String value : list) {
            if (!isFirst) {
                writer.write(fieldSepStr);
            }
            isFirst = false;
            
//            if (value != null) {
//                if (value.contains(fieldSepStr) 
//                        || value.contains(quoteCharStr)
//                        || value.contains(EOL_CR)
//                        || value.contains(EOL_LF)) {
                    writer.write(quoteCharStr);
                    writer.write(value.replaceAll(quoteCharPattern, doubleQuoteCharStr));
                    writer.write(quoteCharStr);
//                } else {
//                    writer.write(value);
//                }
//            }
        }
        writer.write(lineSep);
        writer.flush();
    }

}