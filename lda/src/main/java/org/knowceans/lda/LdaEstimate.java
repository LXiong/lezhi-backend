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
 * Created on Jan 4, 2005
 */
package org.knowceans.lda;

import org.knowceans.lda.corpus.Corpus;
import org.knowceans.lda.corpus.Document;
import org.knowceans.util.Cokus;

/**
 * lda parameter estimation
 * <p>
 * lda-c reference: functions in lda-estimate.c.
 * 
 * @author heinrich
 */
public class LdaEstimate {

    /*
     * For remote debugging: -Xdebug -Xnoagent
     * -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=
     * <MyJdwpDebugPort>
     */

    static int NUM_INIT = 1;

    public static float EM_CONVERGED = 1e-5f;
    public static int EM_MAX_ITER = 100;
    public static int ESTIMATE_ALPHA = 1;

    public static double INITIAL_ALPHA = 0.1;
    public static double K = 200;

    static {
        Cokus.cokusseed(4357);
    }

    static double myrand() {
        return (((long) Cokus.cokusrand()) & 0xffffffffl) / (double) 0x100000000l;
    }

    /**
     * initializes class_word and class_total to reasonable beginnings.
     */
    public static LdaModel initialModel(String start, Corpus corpus, int numTopics, double alpha) {
        LdaModel model = null;

        if (start.equals("seeded")) {
            model = LdaModel.seeded(corpus, numTopics, alpha);
        } else if (start.equals("random")) {
            model = LdaModel.random(corpus, numTopics, alpha);
        }
        return model;
    }

    /**
     * iterate_document
     */
    public static double docEm(Document doc, double[] gamma, LdaModel model, LdaModel nextModel) {
        double likelihood;
        double[][] phi;

        phi = new double[doc.getLength()][model.getNumTopics()];

        likelihood = LdaInference.ldaInference(doc, model, gamma, phi);
        for (int n = 0; n < doc.getLength(); n++) {
            for (int k = 0; k < model.getNumTopics(); k++) {
                nextModel.addClassWord(k, doc.getWord(n), doc.getCount(n) * phi[n][k]);
                nextModel.addClassTotal(k, doc.getCount(n) * phi[n][k]);
            }
        }
        return likelihood;
    }

    /**
     * run_em
     */
    public static LdaModel runEm(String start, Corpus corpus) {
        double likelihood, likelihoodOld = Double.NEGATIVE_INFINITY, converged = 1;
        LdaModel model, nextModel;
        double[][] varGamma;
        varGamma = new double[corpus.getNumDocs()][(int) K];
        model = initialModel(start, corpus, (int) K, INITIAL_ALPHA);
        int i = 0;
        while (((converged > EM_CONVERGED) || (i <= 2)) && (i <= EM_MAX_ITER)) {
            i++;
            System.out.println("**** em iteration " + i + " ****");
            likelihood = 0;
            nextModel = new LdaModel(model.getNumTerms(), model.getNumTopics());
            nextModel.setAlpha(INITIAL_ALPHA);
            for (int d = 0; d < corpus.getNumDocs(); d++) {
                if ((d % 100) == 0) System.out.println("document " + d);
                likelihood += docEm(corpus.getDoc(d), varGamma[d], model, nextModel);
            }
            if (ESTIMATE_ALPHA == 1) LdaAlpha.maximizeAlpha(varGamma, nextModel, corpus.getNumDocs());
            
            model = nextModel;
            assert likelihoodOld != 0;
            converged = (likelihoodOld - likelihood) / likelihoodOld;
            likelihoodOld = likelihood;          
        }
        return model;
    }

    /**
     * inference only
     */
    public static InferResult infer(LdaModel model, Document doc) {
        double[] varGamma = new double[model.getNumTopics()];
        double[][] phi = new double[doc.getLength()][model.getNumTopics()];
        double likelihood = LdaInference.ldaInference(doc, model, varGamma, phi);
        return new InferResult(likelihood, varGamma, phi);
    }

//    /**
//     * main
//     */
//    public static void main(String[] args) {
//
//        int i = 0;
//        double y = 0;
//        double x, z, d;
//        Corpus corpus;
//
//        // command: lda est 0.10 16 settings.txt berry/berry.dat seeded
//        // berry.model
//        if (args[0].equals("est")) {
//            if (args.length < 7) {
//                System.out
//                    .println("usage\n: lda est <initial alpha> <k> <settings> <data> <random/seeded/*> <directory>");
//                System.out
//                    .println("example\n: lda est 10 100 settings.txt ..\\ap\\ap.dat seeded ..\\ap.model");
//                return;
//            }
//
//            INITIAL_ALPHA = Float.parseFloat(args[1]);
//            K = Integer.parseInt(args[2]);
//            
//            corpus = null; // TODO: build lda model
//            boolean a = new File(args[6]).mkdir();
//
//            System.out.println("LDA estimation. Settings:");
//            System.out.println("\tvar max iter " + LdaInference.VAR_MAX_ITER);
//            System.out.println("\tvar convergence " + LdaInference.VAR_CONVERGED);
//            System.out.println("\tem max iter " + EM_MAX_ITER);
//            System.out.println("\tem convergence " + EM_CONVERGED);
//            System.out.println("\testimate alpha " + ESTIMATE_ALPHA);
//
//            LdaModel model = runEm(args[5], args[6], corpus);
//            // model.getClassWord();
//
//        } else {
//            // command: lda inf settings.txt berry.model berry/berry.dat
//            // berry.inf
//            if (args.length < 5) {
//                System.out.println("usage\n: lda inf <settings> <model> <data> <name>\n");
//                System.out.println("example\n: lda inf settings.txt ..\\ap.model ..\\aptest ..\\aptest.inf\n");
//                return;
//            }
//
//            System.out.println("LDA inference. Settings:");
//            System.out.println("\tvar max iter " + LdaInference.VAR_MAX_ITER);
//            System.out.println("\tvar convergence " + LdaInference.VAR_CONVERGED);
//            System.out.println("\tem max iter " + EM_MAX_ITER);
//            System.out.println("\tem convergence " + EM_CONVERGED);
//            System.out.println("\testimate alpha " + ESTIMATE_ALPHA);
//
//            corpus = null; // TODO: build lda model
//
//            infer(null, args[4], corpus);
//
//        }
//    }
}
