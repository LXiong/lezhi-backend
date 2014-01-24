package com.buzzinate.keywords

import collection.JavaConverters._
import scala.collection.mutable.HashMap
import com.buzzinate.keywords.util.HashMapUtil
import com.buzzinate.nlp.segment.AtomSplit
import com.buzzinate.nlp.segment.Atom.AtomType
import com.buzzinate.nlp.dlg.AtomFreq
import scala.collection.mutable.ListBuffer
import com.buzzinate.nlp.ngram.NgramBuilder
import com.buzzinate.keywords.content.VDict
import org.arabidopsis.ahocorasick.WordFreqTree
import com.buzzinate.nlp.dlg.BestDlg
import com.buzzinate.nlp.util.DictUtil
import java.util.ArrayList
import org.ansj.splitWord.Segment
import com.buzzinate.nlp.util.DoublePriorityQueue

object MobileKeywordsExtractor {
  val maxLen = 6
  
  def extract(url: String, html: String): java.util.List[Keyword] = {
    LezhiKeywordsExtractor.extract(url, html).asJava
  }
  
  def extract(texts: java.util.List[String], maxWord: Int): java.util.List[Keyword] = {
    val textsnippets = texts.asScala
    val atomCnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    textsnippets foreach { snippet =>
      AtomSplit.split(snippet).asScala foreach { atom =>
        if (atom.atomType != AtomType.AT_PUNC) atomCnt.adjustOrPut(atom.token, 1, 1)
      }
    }
    val atomFreq = new AtomFreq(atomCnt.map { case (word, freq) => word -> new java.lang.Integer(freq)}.asJava)
    
    val snippets = new ListBuffer[java.util.List[String]]
    textsnippets foreach { snippet =>
      AtomSplit.splitSentences(snippet).asScala foreach { words =>
        val atoms = AtomSplit.split(words).asScala.map(atom => atom.token)
        snippets += atoms.asJava
      }
    }
    val ngramFreq = NgramBuilder.build(snippets.result.asJava, maxLen).asScala
   
    val ngram2dlg = new HashMap[String, Double]
    ngramFreq filter(x => Character.isLetter(x._1.charAt(0))) foreach { case (ngram, freq) =>
      val dlg = atomFreq.dlg(AtomSplit.split0(ngram), freq)
//      println(ngram + " => freq=" + freq + ", dlg=" + dlg)
      if (dlg > 0) {
        ngram2dlg += ngram -> dlg
      }
    }
    
    val vd = new VDict(ngram2dlg)
    
    val wordTree = new WordFreqTree
    textsnippets foreach { snippet =>
      AtomSplit.splitSnippets(snippet).asScala foreach { words =>
        val r = BestDlg.splitDlg(vd, words, maxLen).asScala filter { w =>
          val length = AtomSplit.atomLength(w)
          length > 1 && length < maxLen && Character.isLetter(w.charAt(0)) && !DictUtil.isStop(w) 
        } foreach { w =>
          wordTree.add(w)
        }
      }
    }
    wordTree.build
    
    val wordFreq = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    textsnippets foreach { snippet =>
      split(snippet, wordTree).asScala.foreach { word => wordFreq.adjustOrPut(word, 1, 1) }
    }
    
    val pq = new DoublePriorityQueue[Keyword](maxWord)
    wordFreq.toList foreach { case (word, freq) =>
      val score = freq * DictUtil.idf(word)
      pq.add(score, Keyword(word, 0, freq, 0))
    }
    
    pq.values
  }
  
  private def split(snippet: String, wordTree: WordFreqTree): java.util.List[String] = {
     val words = new ArrayList[String]
     words.addAll(wordTree.search(snippet))
     val wordset = words.asScala.toSet
     Segment.split(snippet).asScala foreach { term =>
       if (term.getName.length > 1 && !DictUtil.isUseless(term) && !DictUtil.isStop(term.getName)) {
         if (!wordset.contains(term.getName)) words.add(term.getName)
       }
     }
     words
  }
}