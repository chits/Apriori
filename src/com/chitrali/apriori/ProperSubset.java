package com.chitrali.apriori;
/*
 * Class to generate Proper Subsets of a Set
 */

import java.util.*;

public class ProperSubset {

 static Integer[] array;
 static BitSet startBitSet; 
 static BitSet endBitSet;  
 static Set<Set<Integer>> properSubset; 

public static Set<Set<Integer>> getProperSubset(int n, Set<Integer> itemSet) {
   Integer[] array = new Integer[itemSet.size()];
   ProperSubset.array = itemSet.toArray(array);
   properSubset = new HashSet<Set<Integer>>();
   startBitSet = new BitSet();
   endBitSet = new BitSet();

   for (int i=0; i<n; i++) {
	   startBitSet.set(i, true);
   }

   for (int i=array.length-1; i>=array.length-n; i--) {
	   endBitSet.set(i, true);
   }
  
   get(startBitSet);   
   //Using bit shifting to get all subsets based 0/1 or true/false
   while(!startBitSet.equals(endBitSet)) {
	    int zeroCount = 0; 
	    int oneCount = 0; 
	    int pos = 0;
	   
	    for (int i=0; i<array.length; i++) {
		     if (!startBitSet.get(i)) {
		    	 zeroCount++;
		     }
		     if (startBitSet.get(i) && !startBitSet.get(i+1)) {
		    	 pos = i;
		    	 oneCount = i - zeroCount;
		    	 startBitSet.set(i, false);
		    	 startBitSet.set(i+1, true);
		    	 break;
		     }
	    }
	
	    int counter = Math.min(zeroCount, oneCount);
	    int startIndex = 0;
	    int endIndex = 0;
	    if(pos>1 && counter>0) {
		     pos--;
		     endIndex = pos;
		     for (int i=0; i<counter; i++) {
			      startBitSet.set(startIndex, true);
			      startBitSet.set(endIndex, false);
			      startIndex = i+1;
			      pos--;
			      if(pos>0) {
			    	  endIndex = pos;
			      }
		     }
	    }
	    get(startBitSet);
   }  
   return properSubset;
}

/*
 * Populating proper set based on bitSet
 */
private static void get(BitSet bitSet) {
	   Set<Integer> set = new HashSet<Integer>();
	   for(int i=0; i<array.length; i++) {
		    if(bitSet.get(i)) {
		    	set.add(array[i]);
		    }
	   }
	   properSubset.add(set);
}

}