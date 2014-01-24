/*
 * (C) Copyright 2005, Gregor Heinrich (gregor :: arbylon : net) (This file is
 * part of the lda-j (org.knowceans.lda.*) experimental software package.)
 */
/*
 * lda-j is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 */
/*
 * lda-j is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
/*
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on Dec 3, 2004
 */
package org.knowceans.lda;

import static java.lang.Math.floor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.knowceans.lda.corpus.Corpus;
import org.knowceans.lda.corpus.Document;
import org.knowceans.util.Cokus;


/**
 * wrapper for an LDA model.
 * <p>
 * lda-c reference: Combines the struct lda_model in lda.h and the code in
 * lda-model.h
 * 
 * @author heinrich
 */
public class LdaModel {
	private static final int NUM_INIT = 1;

    private double alpha;
    private double[][] classWord;
    private double[] classTotal;

    private int numTopics;
    private int numTerms;
    private long version;
    
    static {
    	Cokus.cokusseed(4357);
    }
    
    static double myrand() {
        return (((long) Cokus.cokusrand()) & 0xffffffffl) / (double) 0x100000000l;
    }

    /**
     * create an empty lda model with parameters:
     * 
     * @param numTerms
     *            number of terms in dictionary
     * @param numTopics
     *            number of topics
     */
    public LdaModel(int numTerms, int numTopics) {
        this(numTerms, numTopics, 1);
    }
    
    public LdaModel(int numTerms, int numTopics, double alpha) {
        this.numTopics = numTopics;
        this.numTerms = numTerms;
        this.alpha = alpha;
        this.version = System.currentTimeMillis();

        initArrays(numTerms, numTopics);
    }
    
    public static LdaModel seeded(Corpus corpus, int nTopics, double alpha) {
    	LdaModel model = new LdaModel(corpus.getNumTerms(), nTopics);
        model.setAlpha(alpha);
        // foreach topic
        for (int k = 0; k < nTopics; k++) {
            // sample NUM_INIT documents and add their term counts to the
            // class-word table
            for (int i = 0; i < NUM_INIT; i++) {
                int d = (int) floor(myrand() * corpus.getNumDocs());
                System.out.println("initialized with document " + d);
                Document doc = corpus.getDoc(d);
                for (int n = 0; n < doc.getLength(); n++) {
                    model.addClassWord(k, doc.getWord(n), doc.getCount(n));
                }
            }
            // add to all terms in class-word table 1/nTerms; update class
            // total accordingly
            assert model.getNumTerms() > 0;
            for (int n = 0; n < model.getNumTerms(); n++) {
                model.addClassWord(k, n, 1.0 / model.getNumTerms());
                model.addClassTotal(k, model.getClassWord(k, n));
            }
        }
        return model;
    }
    
    public static LdaModel random(Corpus corpus, int nTopics, double alpha) {
    	 LdaModel model = new LdaModel(corpus.getNumTerms(), nTopics);
         model.setAlpha(alpha);
         // for each topic
         for (int k = 0; k < nTopics; k++) {
             // add to all terms in class-word table a random sample \in
             // (0,1) plus 1/nTerms;
             // update class total accordingly
             for (int n = 0; n < model.getNumTerms(); n++) {
                 model.addClassWord(k, n, 1.0 / model.getNumTerms() + myrand());
                 model.addClassTotal(k, model.getClassWord(k, n));
             }
         }
         return model;
    }

    /**
     * initialise data array in the model.
     * 
     * @param numTerms
     * @param numTopics
     */
    private void initArrays(int numTerms, int numTopics) {

        classTotal = new double[numTopics];
        classWord = new double[numTopics][numTerms];
        for (int i = 0; i < numTopics; i++) {
            this.classTotal[i] = 0;
            for (int j = 0; j < numTerms; j++) {
                this.classWord[i][j] = 0;
            }
        }
    }
    
    public double getAlpha() {
        return alpha;
    }

    public double[] getClassTotal() {
        return classTotal;
    }

    public double getClassTotal(int cls) {
        return classTotal[cls];
    }

    public void setClassTotal(int cls, double total) {
        classTotal[cls] = total;
    }

    public void addClassTotal(int cls, double total) {
        classTotal[cls] += total;
    }

    public double[][] getClassWord() {
        return classWord;
    }

    public double getClassWord(int cls, int word) {
        return classWord[cls][word];
    }

    public void setClassWord(int cls, int word, double value) {
        classWord[cls][word] = value;
    }

    public void addClassWord(int cls, int word, double value) {
        classWord[cls][word] += value;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public int getNumTopics() {
        return numTopics;
    }

    public void setAlpha(double d) {
        alpha = d;
    }

    public void setClassTotal(double[] ds) {
        classTotal = ds;
    }

    public void setClassWord(double[][] ds) {
        classWord = ds;
    }

    public void setNumTerms(int i) {
        numTerms = i;
    }

    public void setNumTopics(int i) {
        numTopics = i;
    }
    
    public long getVersion() {
    	return version;
    }
    
    /**
     * create an lda model from information read from the files below modelRoot,
     * i.e. {root}.beta and {root}.other.
     * 
     * @param modelRoot
     */
    public LdaModel(String modelRoot) {
        String filename;
        double alpha = 0;

        filename = modelRoot + ".other";
        System.out.println("loading " + filename + "\n");

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {

                if (line.startsWith("num_topics ")) {
                    numTopics = Integer.parseInt(line.substring(11).trim());
                } else if (line.startsWith("num_terms ")) {
                    numTerms = Integer.parseInt(line.substring(10).trim());
                } else if (line.startsWith("alpha ")) {
                    alpha = Double.parseDouble(line.substring(6).trim());
                } else if (line.startsWith("version ")) {
                	version = Long.parseLong(line.substring(8).trim());
                }
            }
            br.close();
            initArrays(numTerms, numTopics);
            this.alpha = alpha;
            filename = modelRoot + ".beta";
            System.out.println("loading " + filename);
            br = new BufferedReader(new FileReader(filename));
            line = br.readLine();
            String[] fields = line.trim().split(" ");
            for (int i = 0; i < numTopics; i++) {
            	this.classTotal[i] = Double.parseDouble(fields[i]);
            }
            for (int i = 0; i < numTopics; i++) {
                line = br.readLine();
                fields = line.trim().split(" ");
                for (int j = 0; j < numTerms; j++) {
                    this.classWord[i][j] = Double.parseDouble(fields[j]);
                }
            }
            br.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * save an lda model
     * 
     * @param modelRoot
     */
    public void save(String modelRoot) {
        String filename = modelRoot + ".beta";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < this.numTopics; i++) {
            	if (i > 0) bw.write(' ');
            	bw.write(Utils.formatDouble(this.classTotal[i]));
            }
            bw.newLine();
            
            for (int i = 0; i < this.numTopics; i++) {
                for (int j = 0; j < this.numTerms; j++) {
                    if (j > 0) bw.write(' ');
                    bw.write(Utils.formatDouble(this.classWord[i][j]));
                }
                bw.newLine();
            }
            bw.newLine();
            bw.close();
            filename = modelRoot + ".other";
            bw = new BufferedWriter(new FileWriter(filename));
            bw.write("num_topics " + numTopics + "\n");
            bw.write("num_terms " + numTerms + "\n");
            bw.write("alpha " + alpha + "\n");
            bw.write("version " + version + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Model {numTerms=" + numTerms + " numTopics=" + numTopics + " alpha=" + alpha + "}");
        return b.toString();
    }
}