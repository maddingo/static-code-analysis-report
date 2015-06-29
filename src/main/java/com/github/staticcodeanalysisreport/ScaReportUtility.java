/**
 * Copyright (C) 2012-2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.github.staticcodeanalysisreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

public class ScaReportUtility {

    private static final String EMPTY = "";

	private static final Logger LOGGER = Logger.getLogger(ScaReportUtility.class);

	public static void main(final String[] args) {
	    ScaReportUtility scautil = new ScaReportUtility();
	    if (args.length != 4) {
	        scautil.usage(System.out);
	        return;
	    }
	    try {
	        String findbugs = args[0];
	        String checkstyle = args[1];
	        String pmd = args[2];
	        String result = args[3];
	        scautil.runReport(findbugs, checkstyle, pmd, result);
	    } catch (IOException e) {
	        LOGGER.error(null, e);
	    }
	}
	
	private void run(final String xslt, final File input, final File output, final String param,
			final String value) throws IOException {

	    if (LOGGER.isInfoEnabled()) {
	        LOGGER.info(input.getName() + "  > " + xslt + " " + param + " " + value + " >  " + output);
	    }

		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try (FileOutputStream outputStream = new FileOutputStream(output)) {

			// Process the Source into a Transformer Object
			final InputStream inputStream = ScaReportUtility.class.getResourceAsStream(xslt);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			final StreamSource source = new StreamSource(reader);
			final Transformer transformer = transformerFactory.newTransformer(source);

			// Add a parameter for the transformation
			if (!param.isEmpty()) {
				transformer.setParameter(param, value);
			}

			final StreamResult outputTarget = new StreamResult(outputStream);
			final StreamSource xmlSource = new StreamSource(input);

			// Transform the XML Source to a Result
			transformer.transform(xmlSource, outputTarget);

		} catch (final TransformerException | FileNotFoundException e) {
			LOGGER.error(null, e);
		}
	}

	public void usage(PrintStream out) {
	    out.println("Usage:\n\t java -jar scautil.jar findbugs.xml checkstyle.xml pmd.xml result.html");
    }

	/**
	 * 
	 * @param findbugsFilename
	 * @param checkstyleFilename
	 * @param pmdFilename
	 * @param resultFileName
	 * @throws IOException
	 */
    public void runReport(String findbugsFilename, String checkstyleFilename, String pmdFilename, String resultFileName) throws IOException {
		// Prepare userDirectory and tempDirectoryPrefix
		final String tempFilePrefix = "sca-" + Integer.toHexString((int) System.nanoTime());

		// 1. Create intermediate xml-file for Findbugs
		final File inputFileFindbugs = new File(findbugsFilename);
		final File findbugsTempFile = File.createTempFile(tempFilePrefix, "_PostFB.xml");
		findbugsTempFile.deleteOnExit();
	    run("/prepare_findbugs.xslt", inputFileFindbugs, findbugsTempFile, EMPTY, EMPTY);

		// 2. Create intermediate xml-file for Checkstyle
		final File inputFileCheckstyle = new File(checkstyleFilename);
		final File checkstyleTempFile = File.createTempFile(tempFilePrefix, "_PostCS.xml");
		checkstyleTempFile.deleteOnExit();
		run("/prepare_checkstyle.xslt", inputFileCheckstyle, checkstyleTempFile, EMPTY, EMPTY);

		// 3. Create intermediate xml-file for PMD
		final File inputFilePMD = new File(pmdFilename);
		final File pmdTempFile = File.createTempFile(tempFilePrefix, "_PostPM.xml");
		pmdTempFile.deleteOnExit();
		run("/prepare_pmd.xslt", inputFilePMD, pmdTempFile, EMPTY, EMPTY);

		// 4. Merge first two files and create firstMergeResult file
		final File firstMergeResult = File.createTempFile(tempFilePrefix, "_FirstMerge.xml");
		firstMergeResult.deleteOnExit();
		run("/merge.xslt", checkstyleTempFile, firstMergeResult, "with", findbugsTempFile.getAbsolutePath());

		// 5. Merge result file with third file and create secondMergeResult
		// file
		final File secondMergeResult = File.createTempFile(tempFilePrefix, "_SecondMerge.xml");
		secondMergeResult.deleteOnExit();
		run("/merge.xslt", firstMergeResult, secondMergeResult, "with", pmdTempFile.getAbsolutePath());

		// 6. Create html report out of secondMergeResult
		final File htmlOutputFileName = new File(resultFileName);
		run("/create_html.xslt", secondMergeResult, htmlOutputFileName, EMPTY, EMPTY);
	}
}
