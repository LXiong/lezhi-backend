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
 * Created on Jan 5, 2005 TODO To change the template for this generated file go
 * to Window - Preferences - Java - Code Style - Code Templates
 */
package org.knowceans.lda;

import org.knowceans.lda.corpus.Corpus;

import junit.framework.TestCase;

/**
 * unit tests for the lda application
 * 
 * @author heinrich
 */
public class Tests extends TestCase {

    public Tests(String name) {
        super(name);
    }

    public void testCorpusRead() throws Exception {
        // assertEquals();
        System.out.println("Corpus.read()");
        Corpus c = null; // TODO: build corpus
        System.out.println(c);
        assertEquals(c.getNumDocs(), 17);
        assertEquals(c.getNumTerms(), 16);
        // for(Document d : c.getDocs()) {
        // System.out.println(d);
        // }
    }

    public void testMersenneTwister() throws Exception {
        // values from lda-c randomMT with seed 4357U
        // MT from colt.
        // Cokus.cokusseed(4357);
        // for (int i = 0; i < 20; i++) {
        // // int r = mt.nextInt();
        // System.out.println(LdaEstimate.myrand());
        // // // System.out.println(r + " " + x[i]);
        // // assertEquals(r, x[i]);
        // }
    }

    public void testLnGamma() throws Exception {
        System.out.println("x lgamma(x) log_gamma(x) digamma(x))");
        // for (int i = -3; i < 5; i++) {
        // double x = pow(10, i);
        // System.out.println(x + " " + Utils.lgamma(x) + " "
        // + Utils.log_gamma(x) + " " + Utils.digamma(x));
        // }
    }

    public void testModelRead() throws Exception {
        System.out.println("LdaModel.read()");
        // LdaModel m = new LdaModel("berry");

    }

}
