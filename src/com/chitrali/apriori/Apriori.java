package com.chitrali.apriori;

/*
 * Implementation of Apriori Algorithm
 * Author: Chitrali Rai
 */
import java.sql.*;
import java.util.*;

public class Apriori {

	static Map<Set<Integer>, Integer> candidateSet = new HashMap<Set<Integer>, Integer>();
	static Map<Set<Integer>, Integer> frequentSet = new HashMap<Set<Integer>, Integer>();
	static Map<Set<Integer>, Integer> frequentSetTemp = new HashMap<Set<Integer>, Integer>();
	static Map<Set<Integer>, Set<Set<Integer>>> associationRules = new HashMap<Set<Integer>, Set<Set<Integer>>>();
	static Map<Integer, String> productName = new HashMap<Integer, String>();
	static Map<String, Float> confAR = new HashMap<String, Float>();
	static Connection con;
	static float minSupport;
	static float minConfidence;
	static int supportCount;
	static int size, itemSetNumber;
	static String companyName;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		System.out.println("Available Datasets:\nAmazon\nKmart\nWallmart\nSears\nShoprite\n");
		System.out.println("Please Enter the name of 1 dataset for Apriori Mining");
		companyName = in.nextLine();

		size = 0;
		itemSetNumber = 0;
		populateC1();
		productNameMap();

		System.out.println("Enter the Minimum Support Count value (In Integer):");
		supportCount = in.nextInt();
		minSupport = new Float(supportCount) / new Float(size);
		
		System.out.println("Enter the minimum confidence value (In Decimal):");
		minConfidence = in.nextFloat();
		
		System.out.println("\n*************************************************");
		System.out.println("***************User Entered Values***************");
		System.out.println("\nDataset:" + companyName + "\nMin support:" + minSupport + " \nMin confidence:" + minConfidence);

		System.out.println("\n*************************************************");
		System.out.println("**************************C1**********************");
		for (Set<Integer> key : candidateSet.keySet()) {
			System.out.println(key + ": =  " + candidateSet.get(key));
		}

		pruneBySupport();
		frequentItemSetGeneration();

		System.out.println("\n*************************************************");
		System.out.println("**************All Frequent Itemsets**************");
		for (Set<Integer> k : frequentSet.keySet())
			System.out.println(k + ": =  " + frequentSet.get(k));

		mineAssociationRules();
		getAssociationRules();

		System.out.println("\n*******************************************");
		System.out.println("**************Rule Confidence**************");
		for (String i : confAR.keySet())
			System.out.println(i + " : " + confAR.get(i));
		in.close();
	}

	/*
	 * Scanning the database to count the support of each item set
	 */
	static int numberofOccurence(Set<Integer> set) {
		int supportCount = 0;
		try {
			ResultSet res = getDatabaseEntries();
			while (res.next()) {
				String[] str = res.getString(2).split(",");
				Set<Integer> row = new HashSet<Integer>();
				for (int i = 0; i < str.length; i++) {
					Integer j = Integer.parseInt(str[i]);
					row.add(j);
				}
				if(row.containsAll(set))
					supportCount++;
			}
			con.close();
		} catch (Exception exp) {
			System.out.println(exp);
		}
		return supportCount;
	}

	/*
	 * Pruning the Candidate Set to generate Frequent Item set based on Support
	 * value entered by user
	 */
	static void pruneBySupport() {
		itemSetNumber++;
		frequentSetTemp.clear();
		for (Set<Integer> key : candidateSet.keySet()) {
			Float sup = new Float(candidateSet.get(key)) / new Float(size);
			if (sup >= minSupport) {
				frequentSetTemp.put(key, candidateSet.get(key));
				frequentSet.put(key, candidateSet.get(key));
			}
		}
		if (!frequentSetTemp.isEmpty()) {
			System.out.println("\n*************************************************");
			System.out.println("************************L" + itemSetNumber + "************************");
			for (Set<Integer> key : frequentSetTemp.keySet()) {
				System.out.println(key + ": =  " + frequentSetTemp.get(key));

			}
		} else {
			System.out.println("\n********************************");
			System.out.println("******No More Frequents Sets******");
		}

	}
	
	/*
	 * Generate k+1 Candidate set from k Frequent Item Set
	 */
	static void frequentItemSetGeneration() {
		boolean next = true;
		int element = 0;
		int size = 1;
		Set<Set<Integer>> candidate = new HashSet<>();
		while (next) {
			candidate.clear();
			candidateSet.clear();
			for (Set<Integer> l1 : frequentSetTemp.keySet()) {
				Set<Integer> temp = l1;
				for (Set<Integer> l2 : frequentSetTemp.keySet()) {
					for (Integer i : l2) {
						try {
							element = i;
						} catch (Exception e) {
							break;
						}
						temp.add(element);
						if (temp.size() != size) {
							Integer[] array = temp.toArray(new Integer[0]);
							Set<Integer> temp2 = new HashSet<>();
							for (Integer j : array)
								temp2.add(j);
							candidate.add(temp2);
							temp.remove(element);
						}
					}
				}
			}

			for (Set<Integer> s : candidate) {
				if (pruneCandidate(s))
					candidateSet.put(s, numberofOccurence(s));
			}
			if (!candidateSet.isEmpty()) {
				System.out.println("\n*************************************************");
				System.out.println("***********************C*************************");
				for (Set<Integer> key : candidateSet.keySet())
					System.out.println(key + ": =  " + candidateSet.get(key));
				
			} else {
				System.out.println("\n*************************************************");
				System.out.println("************No more Candidates*******************");
			}
			pruneBySupport();
			if (frequentSetTemp.size() <= 1)
				next = false;
			size++;
		}
	}

	/*
	 * Mining the Association Rules from Frequent Item Sets
	 */
	static void mineAssociationRules() {
		for (Set<Integer> s : frequentSet.keySet()) {
			if (s.size() > 1) {
				mine(s);
			}
		}
	}
	
	/*
	 * Generating subset of entries in frequent item set
	 */
	static void mine(Set<Integer> itemSet) {
		// According to set symmetry only need to get half the proper subset
		int n = itemSet.size() / 2;
		for (int i = 1; i <= n; i++) {
			Set<Set<Integer>> properSubset = ProperSubset.getProperSubset(i, itemSet);
			for (Set<Integer> s : properSubset) {
				Set<Integer> finalset = new HashSet<Integer>();
				finalset.addAll(itemSet);
				finalset.removeAll(s);
				calculateConfidence(s, finalset);
			}
		}
	}

	/*
	 * Calculating Confidence of each rule and placing it in Rule Map if it
	 * satisfies the Confidence entered by user
	 */
	static void calculateConfidence(Set<Integer> s1, Set<Integer> s2) {
		int s1tos2Count = 0;
		int s2tos1Count = 0;
		int supportCount = 0;
		try {
			ResultSet res = getDatabaseEntries();
			while (res.next()) {
				String[] str = res.getString(2).split(",");
				Set<Integer> row = new HashSet<Integer>();
				for (int i = 0; i < str.length; i++) {
					Integer j = Integer.parseInt(str[i]);
					row.add(j);
				}
				Set<Integer> set1 = new HashSet<Integer>();
				Set<Integer> set2 = new HashSet<Integer>();
				set1.addAll(s1);
				set1.removeAll(row);
				if (set1.isEmpty())
					s1tos2Count++;
				set2.addAll(s2);
				set2.removeAll(row);
				if (set2.isEmpty())
					s2tos1Count++;
				if (set1.isEmpty() && set2.isEmpty())
					supportCount++;
			}
			con.close();
		} catch (Exception ex) {
			System.out.println("Unable to generate rules" + ex.toString());
		}

		Float s1tos2Confidence = new Float(supportCount) / new Float(s1tos2Count);
		if (s1tos2Confidence >= minConfidence) {
			if (associationRules.get(s1) == null) {
				Set<Set<Integer>> s2Set = new HashSet<Set<Integer>>();
				s2Set.add(s2);
				confOfAssociationRuleMap(associationRules.put(s1, s2Set), s1tos2Confidence);

			} else
				associationRules.get(s1).add(s2);
		}

		Float s2tos1Confidence = new Float(supportCount) / new Float(s2tos1Count);
		if (s2tos1Confidence >= minConfidence) {
			if (associationRules.get(s2) == null) {
				Set<Set<Integer>> s2Set = new HashSet<Set<Integer>>();
				s2Set.add(s1);
				confOfAssociationRuleMap(associationRules.put(s2, s2Set), s2tos1Confidence);
			} else
				associationRules.get(s2).add(s1);
		}

	}

	/*
	 * Printing Association Rules
	 */
	static void getAssociationRules() {
		if (!associationRules.isEmpty()) {
			System.out.println("\n***************************************************");
			System.out.println("****************Association Rules******************");
			for (Set<Integer> key : associationRules.keySet()) {
				for (Set<Integer> value : associationRules.get(key))
					System.out.println(valueToName(key) + "-->" + valueToName(value) + "\n");
			}
		} else {
			System.out.println("\n**********************************************");
			System.out.println("*********No Association Rules Generated*******");
		}

	}

	/*
	 * Getting the name of the Item in Data set
	 */
	static String valueToName(Set<Integer> key) {
		String name = "";
		for (Integer i : key) {
			name += productName.get(i) + " ";
		}
		return name;
	}
	
	/*
	 * Mapping calculated confidence with the association rules
	 */
	static void confOfAssociationRuleMap(Set<Set<Integer>> a, Float confidence) {

		String Rule = "";
		Float conf;
		for (Set<Integer> key : associationRules.keySet()) {
			for (Set<Integer> value : associationRules.get(key))
				Rule += valueToName(key) + "-->" + valueToName(value);
		}
		conf = confidence;
		confAR.put(Rule, conf);
	}

	/*
	 * Pruning the Supersets of infrequent item sets
	 */
	static boolean pruneCandidate(Set<Integer> candidate) {
		Set<Set<Integer>> properSubset = ProperSubset.getProperSubset(candidate.size() - 1, candidate);
		for (Set<Integer> s1 : properSubset) {
			if (s1.size() == candidate.size() - 1) {
				if (frequentSetTemp.get(s1) == null)
					return false;
			}
		}
		return true;
	}
	
	/*
	 * Fetching entries from transaction data set
	 */
	static ResultSet getDatabaseEntries() {
		ResultSet res = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@prophet.njit.edu:1521:course", "cr252", "CKHMs7icB");
			Statement st = con.createStatement();
			String sql = "select * from " + companyName + " ORDER BY tid";
			res = st.executeQuery(sql);

		} catch (Exception e) {
			System.out.println(e);
		}
		return res;
	}
	
	/*
	 * Fetching item names from data set based on id
	 */
	static ResultSet getNamesById() {
		ResultSet res = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@prophet.njit.edu:1521:course", "cr252", "CKHMs7icB");
			Statement st = con.createStatement();
			String sql = "select * from " + companyName + "_products order by pid";
			res = st.executeQuery(sql);

		} catch (Exception e) {
			System.out.println(e);
		}
		return res;
	}
	
	/*
	 * Computing C1
	 */
	static void populateC1()
	{
		try
		{
			ResultSet res = getDatabaseEntries();
			while (res.next()) {
				String[] str = res.getString(2).split(",");
				for (int i = 0; i < str.length; i++) {
					Integer j = Integer.parseInt(str[i]);
					Set<Integer> item = new HashSet<Integer>();
					item.add(j);
					Integer count = candidateSet.get(item);
					if (count == null)
						candidateSet.put(item, 1);
					else
						candidateSet.put(item, ++count);
				}
				size++;
			}
			con.close();
		} 
		catch (Exception ex) 
		{
			System.out.println("Unable to populate C1" + ex.toString());
		}
	}
	
	/*
	 * Mapping product name to ids locally
	 */
	static void productNameMap()
	{
		try
		{
			ResultSet res = getNamesById();
			while (res.next()) {

				Integer id = res.getInt(1);
				String name = res.getString(2);
				productName.put(id, name);
			}
			con.close();
		}
		catch (Exception ex) 
		{
			System.out.println("Unable to get product name for ids" + ex.toString());
		}
	}

}
