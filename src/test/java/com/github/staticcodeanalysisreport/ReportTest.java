package com.github.staticcodeanalysisreport;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ReportTest {

    /**
     * The output file should be created but empty.
     */
    @Test
    public void createReportNoInputNoOutput() throws Exception {
        ScaReportUtility scautil = new ScaReportUtility();
        
        File outFile = new File("target", "aa4.out");
        outFile.deleteOnExit();
        scautil.runReport("aa1", "aa2", "aa3", outFile.getAbsolutePath());
        
        assertThat("Output File should exist", outFile.exists(), is(equalTo(true)));
        assertThat("Output File should be empty", fileSize(outFile), is(equalTo(0L)));
    }
    
    /**
     * @see http://stackoverflow.com/questions/116574/java-get-file-size-efficiently
     */
    private static long fileSize(File file) throws IOException {
        long readTotal = 0L;
        try (InputStream stream = new FileInputStream(file);) {
            byte[] buf = new byte[0xff];
            int readChunk;
            while((readChunk = stream.read(buf)) >= 0) {
                readTotal += readChunk;
            }
            return readTotal;
        }
    }
    
    private String toString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new FileReader(file);) {
            char[] buf = new char[0xff];
            int readChunk;
            while((readChunk = reader.read(buf)) >= 0) {
                sb.append(buf, 0, readChunk);
            }
            return sb.toString();
        }
    }
    
    @Test
    public void createFullReport() throws Exception {
        ScaReportUtility scautil = new ScaReportUtility();
        
        File outFile = new File("target", "scareport.html");
        outFile.deleteOnExit();
        scautil.runReport("src/test/resources/findbugs.xml", "src/test/resources/checkstyle.xml", "src/test/resources/pmd.xml", outFile.getAbsolutePath());
        
        assertThat("Output File should exist", outFile.exists(), is(equalTo(true)));
        
        String content = toString(outFile);
        
        assertThat(content, endsWith("</html>"));
    }
}
