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
package org.knowceans.lda.corpus;


/**
 * Represents a corpus of documents.
 * <p>
 * lda-c reference: struct corpus in lda.h and function in lda-data.c.
 * 
 * @author heinrich
 */
public class Corpus {

    private Document[] docs;

    private int numTerms;

    private int numDocs;

    public Corpus() {
    }
    
    public Corpus(Document[] docs, int nTerms) {
    	this.docs = docs;
    	this.numDocs = docs.length;
    	this.numTerms = nTerms;
    }

    public Document[] getDocs() {
        return docs;
    }

    public Document getDoc(int index) {
        return docs[index];
    }

    public void setDoc(int index, Document doc) {
        docs[index] = doc;
    }

    public int getNumDocs() {
        return numDocs;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public void setDocs(Document[] documents) {
        docs = documents;
    }

    public void setNumDocs(int i) {
        numDocs = i;
    }

    public void setNumTerms(int i) {
        numTerms = i;
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Corpus {numDocs=" + numDocs + " numTerms=" + numTerms + "}");
        return b.toString();
    }
}
