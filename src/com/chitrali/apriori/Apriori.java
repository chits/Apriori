package com.chitrali.apriori;

import java.sql.*;
import java.util.*;

public class Apriori {
static List<Set<Integer>> dataSet= new ArrayList<Set<Integer>>();
static	Map<Set<Integer>,Integer> candidateSet = new HashMap<Set<Integer>,Integer>();
static	Map<Set<Integer>,Integer> frequentSet=new HashMap<Set<Integer>,Integer>();
static	Map<Set<Integer>,Integer> frequentSetTemp=new HashMap<Set<Integer>,Integer>();
static Map<Set<Integer>,Set<Set<Integer>>> associationRules=new HashMap<Set<Integer>,Set<Set<Integer>>>();
static Map<Integer,String> productName=new HashMap<Integer,String>();
static Map<String,Float> confAR=new HashMap<String,Float>();

static float minsup; 
static float mincon;
static int suportCount;

	public static void main(String[] args) {
		Scanner in=new Scanner(System.in);
		
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@prophet.njit.edu:1521:course","cr252","CKHMs7icB");
			Statement st = con.createStatement();
			Statement st2=con.createStatement();
			String sql ="select * from tset1 ORDER BY tid";
			String sql2="select * from products order by pid";
			ResultSet res=st.executeQuery(sql);
			ResultSet res2=st2.executeQuery(sql2);
						
			/* read from database and map it to the memory implementation of database 
			 * while counting the support of each element*/
			
			while(res.next()){
				String[] str=res.getString(2).split(",");
				Set<Integer> row=new HashSet<Integer>();
				for(int i=0;i<str.length;i++){
					Integer j=Integer.parseInt(str[i]);
					Set<Integer> item= new HashSet<Integer>();
					item.add(j);
					row.add(j);
					Integer count=candidateSet.get(item);
					if(count==null)
						candidateSet.put(item, 1);
					else
						candidateSet.put(item,++count);
					
				}
				dataSet.add(row);
			}
			// id to product Map
			while(res2.next()){
				
				Integer id=res2.getInt(1);
				String name=res2.getString(2);
				productName.put(id, name);			
				
			}
			con.close();
			
			System.out.println("Enter the Minimum Support Count value (In Integer):");
			suportCount=in.nextInt();
			minsup=new Float(suportCount)/new Float(dataSet.size());
		    System.out.println("Enter the minimum confidence value (In Decimal):");
			mincon=in.nextFloat();
			System.out.println("\n*************************************************");
			System.out.println(" ***************User Entered Values***************");
			System.out.println("\nMin support:"+minsup+" \nMin confidence:"+mincon);
			
			/* dataSet is the java Implementation of the DataBase*/
			System.out.println("\n*************************************************\n");
			System.out.println(" *************The Transaction dataset*************\n");
			for(Set<Integer> set:dataSet){
				for(Integer i:set){
					System.out.print(i+" ");
				}
				System.out.println("\n");
			}
			
			System.out.println("\n*************************************************\n");
			System.out.println(" **************Id to product Mapping**************");
			for(Integer i: productName.keySet())
				System.out.println(i+" : "+productName.get(i));
			
			System.out.println("\n*************************************************\n");
			System.out.println("**************************C************************");
			for(Set<Integer> key:candidateSet.keySet()){
				System.out.println(key + ": =  " + candidateSet.get(key));
							
			}
		
			frequentSet.clear();
			prune();
			frequentItemSetGeneration();
					
			System.out.println("\n*************************************************\n");
			System.out.println("  **************All Frequent Itemsets**************");
			for(Set<Integer> k:frequentSet.keySet())
				System.out.println(k + ": =  " + frequentSet.get(k));
			
			mineAssociationRules();
			getAssociationRules();	
			
			System.out.println("\n*************************************************\n");
			System.out.println(" **************Rule Confidence**************");
			for(String i: confAR.keySet())
				System.out.println(i+" : "+confAR.get(i));
			
			
		}
		catch(Exception e){System.out.println(e);}
			in.close();
		
	}
	
	static int numberofOccurence(Set<Integer> set){
			int supportCount=0;
			boolean hasElement;
			for(Set<Integer> set1:dataSet){
				int count=0;
				for(Integer e:set){
					hasElement=false;
					for(Integer i: set1){
						if(e==i){
							hasElement=true;
							count++;
							break;
						}
					}
					if(!hasElement)
						break;
				}
				if(count==set.size())
					supportCount++;
			}
			
			return supportCount;
	}
	
	static void prune(){
		frequentSetTemp.clear();
		for(Set<Integer> key:candidateSet.keySet()){
			Float sup=new Float(candidateSet.get(key))/new Float (dataSet.size());
			if(sup>=minsup){
				frequentSetTemp.put(key,candidateSet.get(key));
				frequentSet.put(key,candidateSet.get(key));
			}
		}
		if(!frequentSetTemp.isEmpty()){
		System.out.println("\n*************************************************\n");
		System.out.println("  ************************L************************");
		for(Set<Integer> key : frequentSetTemp.keySet()){
			System.out.println(key + ": =  " + frequentSetTemp.get(key));
			
		}
		}
		
	}

	static void frequentItemSetGeneration(){
		boolean next=true;
		int element=0;
		int size=1;
		Set<Set<Integer>> candidate=new HashSet<>();
		while(next){
			candidate.clear();
			candidateSet.clear();
			for(Set<Integer> l1:frequentSetTemp.keySet()){
				Set<Integer>temp=l1;
				for(Set<Integer>  l2:frequentSetTemp.keySet()){
					for(Integer i:l2){
						try{
							element=i;
						}catch( Exception e){
							break;
						}
						temp.add(element);
						if(temp.size()!=size){
							Integer[] array=temp.toArray(new Integer[0]);
							Set<Integer> temp2 =new HashSet<>();
							for(Integer j:array)
								temp2.add(j);
							candidate.add(temp2);
							temp.remove(element);
						}
					}
					
				}
			}
			
			
			
			for(Set<Integer> s:candidate){
				if(pruneCandidate(s))
					candidateSet.put(s, numberofOccurence(s));			
				}
			
			System.out.println("\n*************************************************");
			System.out.println("  ***********************C*************************");
			for(Set<Integer> key:candidateSet.keySet())
				System.out.println(key + ": =  " + candidateSet.get(key));
			
			prune();
			if(frequentSetTemp.size()<=1)
				next=false;
			size++;
		}
	}
	
	static void mineAssociationRules(){
		for(Set<Integer> s:frequentSet.keySet()){
			if(s.size()>1){
				mine(s);
		  }
		}
	}
	static void mine(Set<Integer> itemset){
		// According to set symmetry only need to get half the proper subset
		int n=itemset.size()/2; 
		for(int i=1;i<=n;i++){
			Set<Set<Integer>> properSubset=ProperSubset.getProperSubset(i, itemset);
			for (Set<Integer> s:properSubset){
				Set<Integer> finalset=new HashSet<Integer>();
				finalset.addAll(itemset);
				finalset.removeAll(s);
				calculateConfidence(s,finalset);
			}
			
		}
	}
	
	static void calculateConfidence(Set<Integer>s1,Set<Integer> s2){
		int s1tos2Count=0;
		int s2tos1Count=0;
		int supportCount=0;
		for(Set<Integer> s:dataSet){
			Set<Integer>set1=new HashSet<Integer>();
			Set<Integer>set2=new HashSet<Integer>();
			set1.addAll(s1);
			set1.removeAll(s);
			if(set1.isEmpty())
				s1tos2Count++;
			set2.addAll(s2);
			set2.removeAll(s);
			if(set2.isEmpty())
				s2tos1Count++;
			if(set1.isEmpty() && set2.isEmpty())
				supportCount++;
			
		}
		
		Float s1tos2Confidence=new Float(supportCount)/new Float(s1tos2Count);
		if(s1tos2Confidence>=mincon){
			if(associationRules.get(s1)==null){
				Set<Set<Integer>> s2Set=new HashSet<Set<Integer>>();
				s2Set.add(s2);
				ConfOfAssociationRuleMap(associationRules.put(s1, s2Set),s1tos2Confidence);	
				
			}
			else
				associationRules.get(s1).add(s2);
			
		}
		
		Float s2tos1Confidence=new Float(supportCount)/new Float(s2tos1Count);
		if(s2tos1Confidence>=mincon){
			if(associationRules.get(s2)==null){
				Set<Set<Integer>> s2Set=new HashSet<Set<Integer>>();
				s2Set.add(s1);
				ConfOfAssociationRuleMap(associationRules.put(s2, s2Set),s2tos1Confidence);
			}
			else
				associationRules.get(s2).add(s1);
		}
	   
	
	}
	
	static void getAssociationRules(){
		System.out.println("\n*************************************************\n");
		System.out.println("****************Association Rules******************");
		 for(Set<Integer> key:associationRules.keySet()){
				for(Set<Integer> value:associationRules.get(key))
				System.out.println(valueToName(key)+"-->" + valueToName(value) + "\n");
			}
		
	}
	
	static String valueToName(Set<Integer> key){
		String name = "";
		for(Integer i:key){
			name += productName.get(i) + " " ;
		}
		return name;
	}
	
	static void ConfOfAssociationRuleMap(Set<Set<Integer>> a, Float confidence){
		
		String Rule="";
		Float conf;
		for(Set<Integer> key:associationRules.keySet()){
			for(Set<Integer> value:associationRules.get(key))
			 Rule+=valueToName(key)+"-->" + valueToName(value);
		}
		conf=confidence;
		confAR.put(Rule, conf);
	}
	
	static boolean pruneCandidate(Set<Integer> candidate){
		Set<Set<Integer>> properSubset=ProperSubset.getProperSubset(candidate.size()-1, candidate);
		for(Set<Integer> s1:properSubset){
			if(s1.size() == candidate.size()-1)
			{
				if(frequentSetTemp.get(s1)==null)
					return false;
			}
		}
		
		return true;
	}
	
}


