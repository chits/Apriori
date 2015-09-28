package com.chitrali.apriori;

import java.sql.*;
import java.util.*;

public class Apriori {
static List<Set<Integer>> dataSet= new ArrayList<Set<Integer>>();
static	Map<Set<Integer>,Integer> candidateSet = new HashMap<Set<Integer>,Integer>();
static	Map<Set<Integer>,Integer> frequentSet=new HashMap<Set<Integer>,Integer>();
static	Map<Set<Integer>,Integer> frequentSetTemp=new HashMap<Set<Integer>,Integer>();

static int minsup; 
static int mincon;

	public static void main(String[] args) {
		Scanner in=new Scanner(System.in);
		
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@prophet.njit.edu:1521:course","cr252","CKHMs7icB");
			Statement st = con.createStatement();
			String sql ="select * from tset1 ORDER BY tid";
			ResultSet res=st.executeQuery(sql);
			
			
			
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
			con.close();
			
			System.out.println("Enter the minimum support value: \n");
			minsup=in.nextInt();
			System.out.println("\nEnter the minimum confidence value: \n");
			mincon=in.nextInt();
			
			/* dataSet is the java Implementation of the DataBase*/
			System.out.println("The Transaction dataset:\n");
			for(Set<Integer> set:dataSet){
				for(Integer i:set){
					System.out.print(i+" ");
				}
				System.out.println("\n");
			}
			
			System.out.println("****C****");
			for(Set<Integer> key:candidateSet.keySet()){
				System.out.println(key + ": =  " + candidateSet.get(key));
							
			}
			
			prune();
			frequentItemSetGeneration();
		}
		catch(Exception e){System.out.println(e);}
			
		
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
			if(candidateSet.get(key)>=minsup){
				frequentSetTemp.put(key,candidateSet.get(key));
				frequentSet.put(key,candidateSet.get(key));
			}
		}
		System.out.println("****L****");
		for(Set<Integer> key : frequentSetTemp.keySet()){
			System.out.println(key + ": =  " + frequentSetTemp.get(key));
			
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
					candidateSet.put(s, numberofOccurence(s));			
				
			}
			System.out.println("****C****");
			for(Set<Integer> key:candidateSet.keySet())
				System.out.println(key + ": =  " + candidateSet.get(key));
			
			prune();
			if(frequentSetTemp.size()<=1)
				next=false;
			size++;
		}
		System.out.println("****LastFrequentItemSet****");
		for(Set<Integer> k:frequentSetTemp.keySet())
			System.out.println(k + ": =  " + frequentSetTemp.get(k));
	}
}


