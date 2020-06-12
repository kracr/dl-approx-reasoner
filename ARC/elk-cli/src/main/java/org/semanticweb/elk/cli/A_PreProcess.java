package org.semanticweb.elk.cli;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class A_PreProcess implements Runnable
{
	static String bottom_class="owl:Nothing";
	
	ArrayList<String> alllines=new ArrayList<>();
	static ConcurrentLinkedQueue<String> card_comp = new ConcurrentLinkedQueue(); 
	static ConcurrentLinkedQueue<String> classification = new ConcurrentLinkedQueue();
	
	static A_Class ontology=new A_Class();
	
	static A_Class subclasses=new A_Class();	
	static A_Prop proprange=new A_Prop();
	static A_Prop propdomain=new A_Prop();
	static A_Class disjclasses=new A_Class();
	static A_Class equiclasses=new A_Class();
	static A_mincardnl mincard=new A_mincardnl();
	static A_Class subobjprop=new A_Class();
	
	String toprecss;
	public A_PreProcess(String m) 
	{
		toprecss=m;
	}
	public A_PreProcess() {
		
	}	
	void populate_data_struct(String args3, int chng) throws IOException 
	{
		for(int i=0;i<alllines.size();i++) 
		{	
			if(alllines.get(i).indexOf("SubClassOf")==0) 
			{
				String temp1=alllines.get(i).substring(11,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				if(tempoutp[0].indexOf("ObjectMinCardinality")==0) 
				{
					String tempprop="ObjectMinCardinality";
					String temp01=tempoutp[0].substring(tempprop.length()+1,tempoutp[0].length()-1);
					String[] tempoutp1=givesub_super2(temp01);
					mincard.add(tempoutp1[1],Integer.parseInt(tempoutp1[0]));					
					card_comp.add(tempoutp[0]);
				}
				if(tempoutp[1].indexOf("ObjectMinCardinality")==0) 
				{
					String tempprop="ObjectMinCardinality";
					String temp01=tempoutp[1].substring(tempprop.length()+1,tempoutp[1].length()-1);
//					System.out.println(temp01);
					String[] tempoutp1=givesub_super2(temp01);
					mincard.add(tempoutp1[1],Integer.parseInt(tempoutp1[0]));	
					card_comp.add(tempoutp[1]);
				}
				else {
					card_comp.add(alllines.get(i));						
				}
				subclasses.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[0],tempoutp[1]);				
				classification.add(alllines.get(i));			
			}
			else if(alllines.get(i).indexOf("SubObjectPropertyOf")==0) 
			{
				String tempprop="SubObjectPropertyOf";
				String temp1=alllines.get(i).substring(tempprop.length()+1,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				subobjprop.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[0],tempoutp[1]);
				classification.add(alllines.get(i));
				card_comp.add(alllines.get(i));
			}
			else if(alllines.get(i).indexOf("ObjectPropertyRange")==0) 
			{
				String tempprop="ObjectPropertyRange";
				String temp1=alllines.get(i).substring(tempprop.length()+1,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				proprange.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[0],tempoutp[1]);
				classification.add(alllines.get(i));
				card_comp.add(alllines.get(i));
			}
			else if(alllines.get(i).indexOf("ObjectPropertyDomain")==0) 
			{
				String tempprop="ObjectPropertyDomain";
				String temp1=alllines.get(i).substring(tempprop.length()+1,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				propdomain.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[0],tempoutp[1]);
				classification.add(alllines.get(i));
				card_comp.add(alllines.get(i));
			}
			else if(alllines.get(i).indexOf("DisjointClasses")==0) 
			{
				String tempprop="DisjointClasses";
				String temp1=alllines.get(i).substring(tempprop.length()+1,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				
				disjclasses.add(tempoutp[0],tempoutp[1]);
				disjclasses.add(tempoutp[1],tempoutp[0]);
				ontology.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[1],tempoutp[0]);
				
				classification.add(alllines.get(i));
				card_comp.add(alllines.get(i));
			}
			else if(alllines.get(i).indexOf("EquivalentClasses")==0) 
			{
				String tempprop="EquivalentClasses";
				String temp1=alllines.get(i).substring(tempprop.length()+1,alllines.get(i).length()-1);
				String[] tempoutp=givesub_super2(temp1);
				equiclasses.add(tempoutp[0],tempoutp[1]);
				equiclasses.add(tempoutp[1],tempoutp[0]);
				ontology.add(tempoutp[0],tempoutp[1]);
				ontology.add(tempoutp[1],tempoutp[0]);
				classification.add(alllines.get(i));
				card_comp.add(alllines.get(i));
			}
			else 
			{
				classification.add(alllines.get(i));				
			}
		}
		if(chng!=0) 
		{
			ArrayList<String> newlines=new ArrayList<>();
			try {
				
			      File myObJ = new File(args3);
			       String datA;
				   BufferedReader bR = new BufferedReader(new FileReader(myObJ));
				   while ((datA=bR.readLine()) != null) 
					{
						newlines.add(datA);
					} 
					bR.close();
			    } 
			catch (FileNotFoundException e) 
			{
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
			card_comp.clear();
			card_comp=new ConcurrentLinkedQueue<>(newlines);
		}

	}
	public void run() 
	{
			String toproce=toprecss;
			card_comp.add(toproce);
			if(toproce.indexOf("SubClassOf")==0) 
			{
				String expressiontype="SubClassOf";
				String expression=toproce.substring(expressiontype.length()+1,toproce.length()-1);				
				String[] axioms=givesub_super(expression);
				
				String class_A= ""+axioms[0];
				String class_B= ""+axioms[1];
				String not_class_A="ObjectComplementOf("+class_A+")";
				String not_class_B="ObjectComplementOf("+class_B+")";
				
				LinkedList<String> r2_list_supnotA=new LinkedList<>(checkoccursnegatively(not_class_A));
				LinkedList<String> r2_list_supnotB=new LinkedList<>(checkoccursnegatively(not_class_B));
			
				rule_one_first(class_A,class_B);
//				rule_one_second(class_A,class_B);																	/////////////////////////////////////////////R1				
				rule_two(r2_list_supnotA,r2_list_supnotB);															/////////////////////////////////////////////R2
				rule_three(class_A,class_B);																		/////////////////////////////////////////////R3
				rule_four_first(class_A,class_B);																	/////////////////////////////////////////////R4
				rule_five_second(class_A,class_B);																	/////////////////////////////////////////////R5
				rule_six_second(class_A,class_B);																	/////////////////////////////////////////////R6
				if(class_B.indexOf("ObjectSomeValuesFrom")==0) {
					rule_seven_second(class_A,class_B);																/////////////////////////////////////////////R7
					rule_eight_first(class_A,class_B);																/////////////////////////////////////////////R8
				}																			
			}
			if(toproce.indexOf("ObjectMinCardinality")==0) 
			{
				//assuming singular range and domain per property
				String expressiontype="ObjectMinCardinality";
				String expression=toproce.substring(expressiontype.length()+1,toproce.length()-1);				
				String[] axioms=givesub_super(expression);
				String prop_R=axioms[1];
				int sub_card_i=Integer.parseInt(axioms[0]);
				if(propdomain.sc.get(prop_R)!=null && proprange.sc.get(prop_R)!=null) 
				{
					String class_domain_X=propdomain.sc.get(prop_R);
					String class_range_A=proprange.sc.get(prop_R);

//					rule_five_first(class_domain_X,class_range_A);													/////////////////////////////////////////////R5							
//					
//					if(subclasses.sc.get(class_range_A)!=null) 
//					{
//						LinkedList<String> sup_classes_of_A=new LinkedList<>(subclasses.sc.get(class_range_A));
//						
//						for(int k=0;k<sup_classes_of_A.size();k++) 
//						{
//							if(sup_classes_of_A.get(k)!=null) 
//							{
//								String sup_class_B=sup_classes_of_A.get(k);
//
//								rule_four_second(class_domain_X,prop_R,sub_card_i,sup_class_B);							/////////////////////////////////////////////R4
//								
//
//								rule_six_first(class_domain_X,prop_R,axioms[0],sup_class_B);							/////////////////////////////////////////////R6						
//
//								rule_seven_first(sup_class_B,class_domain_X,prop_R);									/////////////////////////////////////////////R7	
//							}													
//						}					
//					}
//
//					rule_four_third(class_domain_X,prop_R,sub_card_i,class_range_A);								/////////////////////////////////////////////R4
//					
//					rule_eight_second(class_domain_X,prop_R,class_range_A);											/////////////////////////////////////////////R8

					rule_nine_first(class_domain_X,prop_R,sub_card_i,class_range_A);
//					rule_nine_second(class_domain_X,prop_R,sub_card_i,class_range_A);								/////////////////////////////////////////////R9
				}				
			}
			card_comp.remove(toproce);
			return;
	}
	public LinkedList<String> getrolecompos(String prop_R, String objsomval_prop_R2) {
		LinkedList<String> toret=new LinkedList<String>();
		LinkedList<String> allprops=new LinkedList<String>();
		for(Entry<String, LinkedList<String>> entry: subobjprop.sc.entrySet()) 
		{
	        allprops.add(entry.getKey());
	        for(String to_add_str:entry.getValue()) 
	        {
	        	allprops.add(to_add_str);       	
	        }
	    }
		
		for(String to_check:allprops) 
		{
			if(subobjprop.checksub(prop_R, to_check)) 
			{
				for(String to_check2:allprops) {
					if(subobjprop.checksub(objsomval_prop_R2, to_check2)) {
						String concatoadd="ObjectPropertyChain("+to_check+" "+to_check2+")";
						if(subobjprop.sc.get(concatoadd)!=null) 
						{
							for(String t_add:subobjprop.sc.get(concatoadd)) 
							{
								if(!toret.contains(t_add)) {
									toret.add(t_add);
								}
							}							
						}											
					}
				}				
			}
		}
		
		return toret;
	}	
	public int cntsub_super(String k) 
	{
		if(givesub_super(k)[0]==null) {
			return 1;
		}
		else {
			return 1+cntsub_super(givesub_super(k)[1]);
		}
	}
	
	public String[] givesub_super(String k) 
	{
		String[] list=new String[2];
		int brac_count=0;
		for(int j=0;j<k.length();j++) 
		{
			if(k.toCharArray()[j]==' ' && brac_count==0) {
				String tempformer= k.substring(0,j);
				String templatter= k.substring(j+1);
				list[0]=tempformer;
				list[1]=templatter;
				break;
			}
			else if(k.toCharArray()[j]=='(') {
				brac_count+=1;						
			}
			else if(k.toCharArray()[j]==')') {
				brac_count-=1;						
			}	
		}
		return list;		
	}
	static String[] givesub_super2(String k) 
	{
		String[] list=new String[2];
		int brac_count=0;
		for(int j=0;j<k.length();j++) 
		{
			if(k.toCharArray()[j]==' ' && brac_count==0) {
				String tempformer= k.substring(0,j);
				String templatter= k.substring(j+1);
				list[0]=tempformer;
				list[1]=templatter;
				break;
			}
			else if(k.toCharArray()[j]=='(') {
				brac_count+=1;						
			}
			else if(k.toCharArray()[j]==')') {
				brac_count-=1;						
			}	
		}
		return list;		
	}
	public void addifnotalready(String k) 
	{
//		System.out.println("Raj2");
		if(classification.contains(k)==false && toprecss.equals(k)==false) 
		{
//			System.out.println("Raj");
			classification.add(k);
			card_comp.add(k);
		}
	}
	public String getdomainfromprop(String k) 
	{
		String toret=null;
		if(propdomain.sc.get(k)!=null) {
			toret=propdomain.sc.get(k);
		}
		return toret;
	}
	public LinkedList<String> getpropfromrange(String k) 
	{
		LinkedList<String> toret=new LinkedList<String>();
		for(String i: proprange.sc.keySet()) 
		{
			if(proprange.sc.get(i)!=null) 
			{
									
				if(proprange.sc.get(i).equals(k)) 
				{
					toret.add(i);				
				}
			}
		}
		return toret;		
		 
	}
	public boolean checkifexistinset(String k,String l,int m) 
	{
		if(m==0) {
			return subclasses.sc.get(k)!=null && subclasses.sc.get(k).contains(l);
			
			
		}
		else if(m==1) 
		{
			return (equiclasses.sc.get(k)!=null && equiclasses.sc.get(k).contains(l))||
					(equiclasses.sc.get(l)!=null && equiclasses.sc.get(l).contains(k));
			
		}
		else if(m==2) {
			return proprange.sc.get(k)!=null && proprange.sc.get(k).equals(l);
		}		
		else if(m==3) {
			return propdomain.sc.get(k)!=null && propdomain.sc.get(k).equals(l);
		}
		else if(m==4) {
			return subobjprop.sc.get(k)!=null && subobjprop.sc.get(k).contains(l);
		}	
		else if(m==5) {
			return mincard.sc.get(k)!=null && mincard.sc.get(k)==Integer.parseInt(l);
		}	
		else {
			return (disjclasses.sc.get(k)!=null && disjclasses.sc.get(k).contains(l))
					|| (disjclasses.sc.get(l)!=null && disjclasses.sc.get(l).contains(k));
		}
	}
	public LinkedList<String> checkoccursnegatively(String k) 
	{
		LinkedList<String> toret=new LinkedList<String>();
		if(ontology.sc.get(k)!=null) 
		{
			toret= ontology.sc.get(k);
		}
		return toret;		
	}
	
	public void rule_one_first(String class_C,String class_D) 
	{
		boolean flogger=false;
		if(subclasses.sc.get(class_C)!=null) 
		{
			LinkedList<String> sup_classes_C=new LinkedList<>(subclasses.sc.get(class_C));			
			if(sup_classes_C.size()>0) 
			{								
				for(int k=0;k<sup_classes_C.size();k++) 
				{
					if(sup_classes_C.get(k)!=null && !sup_classes_C.get(k).equals(class_D)) 
					{
						String not_class_E="ObjectComplementOf("+sup_classes_C.get(k)+")";					
						LinkedList<String> sup_bottom_class=new LinkedList<>(checkoccursnegatively(bottom_class));
						
						if( (checkifexistinset(class_D,not_class_E,0)
								&& checkifexistinset(not_class_E,class_D, 0))
								|| checkifexistinset(not_class_E,class_D, 1)						
						){
							if(sup_bottom_class.size()>0)
							{
								for(String sub_bottom_class: sup_bottom_class ) 
								{
									if(sub_bottom_class!=null && !class_C.equals(sub_bottom_class)) {
										String conctoadd="SubClassOf("+class_C+" "+sub_bottom_class+")";									
										subclasses.add(class_C,sub_bottom_class);									
										addifnotalready(conctoadd);
										flogger=true;
										
									}
									
								}								
							}							
						}						
					}
				}
			}			
		}
//		if(flogger) {
//			System.out.println("New1_First");
//			System.out.println(conctoadd);			
//		}
		
		//System.out.p.println("//////////////////////////////////////////////////////");		
	}
	public void rule_one_second(String class_C,String class_E)
	{
		boolean flogger=false;
		if(subclasses.sc.get(class_C)!=null) 
		{
			LinkedList<String> sup_classes_C=new LinkedList<>(subclasses.sc.get(class_C));	
			String not_class_E="ObjectComplementOf("+class_E+")";
			if(sup_classes_C.size()>0) 
			{								
				for(int k=0;k<sup_classes_C.size();k++) 
				{
					if(sup_classes_C.get(k)!=null && !sup_classes_C.get(k).equals(class_E)) 
					{
						String class_D=sup_classes_C.get(k);
						LinkedList<String> sup_bottom_class=new LinkedList<>(checkoccursnegatively(bottom_class));					
						if( (checkifexistinset(class_D,not_class_E,0)
								&& checkifexistinset(not_class_E,class_D, 0))
								|| checkifexistinset(not_class_E,class_D, 1)						
						){
							if(sup_bottom_class.size()>0)
							{
								for(String sub_bottom_class: sup_bottom_class) 
								{
									if(sub_bottom_class!=null && !class_C.equals(sub_bottom_class)) {
										String conctoadd="SubClassOf("+class_C+" "+sub_bottom_class+")";									
										subclasses.add(class_C,sub_bottom_class);									
										addifnotalready(conctoadd);
										flogger=true;										
									}
									
									
								}								
							}							
						}						
					}
				}
			}			
		}
//		if(flogger) 
//		{
//			System.out.println("New1_Second");
//			System.out.println(conctoadd);
//			
//		}
		
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_two(LinkedList<String> not_classes_C,LinkedList<String> not_classes_D) 
	{
		boolean flogger=false;
		if(not_classes_C.size()>0
				&& not_classes_D.size()>0)
		{
			for(String not_class_D: not_classes_D) 
			{
				if(not_class_D!=null) 
				{
					for(String not_class_C: not_classes_C) 
					{
						if(not_class_C!=null && !not_class_C.equals(not_class_D)) {
							String conctoadd="SubClassOf("+not_class_D+" "+not_class_C+")";									
							subclasses.add(not_class_D,not_class_C);									
							addifnotalready(conctoadd);
							
							flogger=true;
							
						}
						
											
					}
					
				}
				
			}
		}
//		if(flogger) 
//		{
//			System.out.println("New2");
//			
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_three(String class_intersection,String owl_nothing)
	{
		boolean flogger=false;
		if(class_intersection.indexOf("ObjectIntersectionOf")==0 
				&& 
				owl_nothing.equals(bottom_class)						
		 ){					
			String intersection_expr=class_intersection.substring("ObjectIntersectionOf".length()+1,class_intersection.length()-1);
			String[] intersection_axioms=givesub_super(intersection_expr);
			String class_C=	intersection_axioms[0];
			String class_D=	intersection_axioms[1];

			String not_class_D="ObjectComplementOf("+class_D+")";					
			LinkedList<String> sup_not_classes_D=new LinkedList<>(checkoccursnegatively(not_class_D));
			if(sup_not_classes_D.size()>0) 
			{
				for(String sub_not_class_D:sup_not_classes_D) 
				{
					if(sub_not_class_D!=null && !class_C.equals(sub_not_class_D)) {
						String conctoadd="SubClassOf("+class_C+" "+sub_not_class_D+")";
						subclasses.add(class_C,sub_not_class_D);
						addifnotalready(conctoadd);
						
						flogger=true;						
					}
					
												
				}							
			}					
		}
//		if(flogger) 
//		{
//			System.out.println("New3");
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_four_first(String class_A,String class_B)
	{
		boolean flogger=false;
		LinkedList<String> prop_lst_R=new LinkedList<String>(getpropfromrange(class_A));
		LinkedList<String> prop_lst_S=new LinkedList<String>(getpropfromrange(class_B));
		
		for(String prop_R:prop_lst_R) {
			for(String prop_S:prop_lst_S) {
				if(subobjprop.checksub(prop_R, prop_S)) 
				{
					int sub_card_j;
					int sub_card_i;
					if(mincard.sc.get(prop_S)!=null && mincard.sc.get(prop_R)!=null) 
					{
						sub_card_i= mincard.sc.get(prop_R);
						sub_card_j= mincard.sc.get(prop_S);
						if(sub_card_i>=sub_card_j)
						{
							if(propdomain.sc.get(prop_R)!=null && propdomain.sc.get(prop_S)!=null) 
							{
								String class_X=propdomain.sc.get(prop_R);
								String class_Y=propdomain.sc.get(prop_S);
								if(class_X!=null && class_Y!=null && !class_X.equals(class_Y)) 
								{
									String conctoadd="SubClassOf("+class_X+" "+class_Y+")";
									subclasses.add(class_X,class_Y);
									addifnotalready(conctoadd);
									
									flogger=true;
								}
								
							}
						}
					}
				}
			}
		}
//		if(flogger) 
//		{
//			System.out.println("New4_Sub");
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");		
	}
	public void rule_four_second(String class_X,String prop_R,int sub_card_i,String class_B) 
	{
		boolean flogger=false;
		LinkedList<String> prop_lst_S=new LinkedList<>(getpropfromrange((class_B)));
		for(int l=0;l<prop_lst_S.size();l++) 
		{								
			String prop_S=prop_lst_S.get(l);
			if(mincard.sc.get(prop_S)!=null) 
			{
				String  class_Y=getdomainfromprop(prop_S);
				int sub_card_j= mincard.sc.get(prop_S);
				if(subobjprop.checksub(prop_R, prop_S) && sub_card_i<=sub_card_j) 
				{
					if(class_Y!=null && !class_X.equals(class_Y)) {
						String conctoadd="SubClassOf("+class_X+" "+class_Y+")";
						subclasses.add(class_X,class_Y);
						addifnotalready(conctoadd);
						
						flogger=true;
					}
					
														
				}									
			}								
		}
//		if(flogger) 
//		{
//			System.out.println("New4_Second");
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_four_third(String class_Y,String prop_S,int sub_card_j,String class_B)
	{		
		boolean flogger=false;
		LinkedList<String> frest=new LinkedList<String>(subclasses.sc.keySet());
		for(String class_A:frest)
		{
			if(subclasses.sc.get(class_A)!=null && subclasses.sc.get(class_A).contains(class_B)) 
			{					
				LinkedList<String> prop_lst_R=new LinkedList<>(getpropfromrange((class_A)));
				for(int l=0;l<prop_lst_R.size();l++) 
				{								
					String prop_R=prop_lst_R.get(l);
					if(mincard.sc.get(prop_R)!=null) 
					{
						String  class_X=getdomainfromprop(prop_R);
						int sub_card_i= mincard.sc.get(prop_R);
						if(subobjprop.checksub(prop_R,prop_S) && sub_card_i<=sub_card_j) 
						{
							if(class_X!=null && !class_X.equals(class_Y)) {
								String conctoadd="SubClassOf("+class_X+" "+class_Y+")";
								subclasses.add(class_X,class_Y);
								addifnotalready(conctoadd);
								
								flogger=true;
							}
							
																
						}									
					}
				}							
			}
		}				
//		if(flogger) 
//		{
//			System.out.println("New4_Third");
//			System.out.println(class_Y+" "+prop_S+" "+sub_card_j+" "+class_B);
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	
	public void rule_five_first(String class_E,String class_C) 
	{
		boolean flogger=false;
		if(checkifexistinset(class_C,bottom_class, 0)) 
		{
			if(!class_E.equals(bottom_class)) {
				String conctoadd="SubClassOf("+class_E+" "+bottom_class+")";
				subclasses.add(class_E,bottom_class);
				addifnotalready(conctoadd);
				flogger=true;			}
			
		}
		
//		if(flogger) 
//		{
//			System.out.println("New5_First");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_five_second(String class_C,String owl_nothing)
	{
		boolean flogger=false;
		if(owl_nothing.equals(bottom_class)) {
			LinkedList<String> prop_lst_R=new LinkedList<>(getpropfromrange((class_C)));
			for(String prop_R:prop_lst_R) {
				String class_E=getdomainfromprop(prop_R);
				if(class_E!=null && !class_E.equals(bottom_class)) {
					String conctoadd="SubClassOf("+class_E+" "+bottom_class+")";
					subclasses.add(class_E,bottom_class);
					addifnotalready(conctoadd);
					
					flogger=true;
				}
				
				
			}
		}
//		if(flogger) 
//		{
//			System.out.println("New5_Second");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	
	public void rule_six_first(String class_E,String prop_R,String sub_card_i,String class_D)
	{
		boolean flogger=false;
		LinkedList<String> prop_lst_S=new LinkedList<>(getpropfromrange((class_D)));
		for(String prop_S:prop_lst_S) 
		{
			if(subobjprop.checksub(prop_R, prop_S)
					&& checkifexistinset(prop_S,sub_card_i,5) && checkoccursnegatively("ObjectMinCardinality("+sub_card_i+" "+prop_S +")").size()>0);
			{
				if(prop_S!=null) {
					String conctoadd="ObjectPropertyDomain("+prop_S+" "+class_E+")";								
					propdomain.add(prop_S, class_E);
					addifnotalready(conctoadd);		
					
					flogger=true;
				}
				
			}
		}		
//		if(flogger) 
//		{
//			System.out.println("New6_First");
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_six_second(String class_C,String class_D)
	{
		
		boolean flogger=false;
		LinkedList<String> prop_lst_R=new LinkedList<>(getpropfromrange((class_C)));
		for(String prop_R : prop_lst_R) 
		{
			String class_E=getdomainfromprop(prop_R);
			if(mincard.sc.get(prop_R)!=null) 
			{
				String sub_card_i=""+mincard.sc.get(prop_R);
				
				LinkedList<String> prop_lst_S=new LinkedList<>(getpropfromrange((class_D)));
				for(String prop_S : prop_lst_S) 
				{
					if(subobjprop.checksub(prop_R, prop_S)
							&& checkifexistinset(prop_S,sub_card_i,5) && checkoccursnegatively("ObjectMinCardinality("+sub_card_i+" "+prop_S +")").size()>0);
					{
						if(prop_S!=null && class_E!=null) {
							String conctoadd="ObjectPropertyDomain("+prop_S+" "+class_E+")";								
							propdomain.add(prop_S, class_E);
							addifnotalready(conctoadd);
							
							flogger=true;
						}
						
															
					}
				}			
			}			
		}
//		if(flogger) 
//		{
//			System.out.println("New6_Second");
//			System.out.println(conctoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_seven_first(String objmincardnl,String class_E,String prop_R)
	{
		
		boolean flogger=false;
		if(objmincardnl.indexOf("ObjectSomeValuesFrom")==0) 
		{
			String objsomval_exprssntype="ObjectSomeValuesFrom";
			
			String objsomval_exprssn=objmincardnl.substring(objsomval_exprssntype.length()+1,objmincardnl.length()-1);
			
			String[] objsomval_axioms=givesub_super(objsomval_exprssn);
			
			String prop_R2= ""+objsomval_axioms[0];
			String class_D= ""+objsomval_axioms[1];
			
			LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
			for(String role_compos_S:role_compos_lst_S) 
			{
				if(role_compos_S!=null && class_D!=null && !class_E.equals(class_D)) {
					String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
					subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
					addifnotalready(concatoadd);
					
					flogger=true;
				}
				
				
			}							
		}
//		if(flogger) 
//		{
//			System.out.println("New7_First");
//			System.out.println(concatoadd);
//			
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_seven_second(String class_C, String objmincardnl)
	{
		boolean flogger=false;
		if(objmincardnl.indexOf("ObjectSomeValuesFrom")==0)
		{
			String objsomval_exprssntype="ObjectSomeValuesFrom";
			
			String objsomval_exprssn=objmincardnl.substring(objsomval_exprssntype.length()+1,objmincardnl.length()-1);
			
			String[] objsomval_axioms=givesub_super(objsomval_exprssn);
			
			String prop_R2= ""+objsomval_axioms[0];
			String class_D= ""+objsomval_axioms[1];
			
			LinkedList<String> prop_lst_R=new LinkedList<>(getpropfromrange((class_C)));
			for(String prop_R:prop_lst_R) 
			{
				String class_E=getdomainfromprop(prop_R);
				
				LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
				for(String role_compos_S:role_compos_lst_S) 
				{
					if(role_compos_S!=null && class_D!=null && class_E!=null && !class_E.equals(class_D)) {
						String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
						subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
						addifnotalready(concatoadd);
						
						flogger=true;
					}
					
				}	
				
			}
			
		}
//		if(flogger) 
//		{
//			System.out.println("New7_Second");
//			System.out.println(concatoadd);
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_eight_second(String class_C,String prop_R2,String class_D) 
	{
		boolean flogger=false;
		LinkedList<String> frest=new LinkedList<String>(subclasses.sc.keySet());
		for(String class_E:frest)
		{
			for(String objmincardnl: subclasses.sc.get(class_E)) 
			{
				if(objmincardnl!=null && objmincardnl.indexOf("ObjectSomeValuesFrom")==0) 
				{
					String objsomval_exprssntype="ObjectSomeValuesFrom";
					
					String objsomval_exprssn=objmincardnl.substring(objsomval_exprssntype.length()+1,objmincardnl.length()-1);
					
					String[] objsomval_axioms=givesub_super(objsomval_exprssn);
					
					String prop_R= ""+objsomval_axioms[0];
					String class_pot_C= ""+objsomval_axioms[1];
					if(class_pot_C.equals(class_C)) 
					{
						LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
						for(String role_compos_S:role_compos_lst_S) 
						{
							if(class_E!=null && role_compos_S!=null && !class_E.equals(class_D)) {
								String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
								subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
								addifnotalready(concatoadd);
								
								flogger=true;
							}
							
							
						}	
						
					}
				}
			}
		}		
//		if(flogger) 
//		{
//			System.out.println("New8_Second");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_eight_first(String class_E,String objmincardnl)
	{
		boolean flogger=false;
		String objsomval_exprssntype = "ObjectSomeValuesFrom";
		
		String objsomval_exprssn = objmincardnl.substring(objsomval_exprssntype.length()+1,objmincardnl.length()-1);
		
		String[] objsomval_axioms=givesub_super(objsomval_exprssn);

		String prop_R= ""+objsomval_axioms[0];
		String class_C= ""+objsomval_axioms[1];
		for(String prop_R2: propdomain.sc.keySet()) 
		{
			if(propdomain.sc.get(prop_R2)!=null && propdomain.sc.get(prop_R2).contains(class_C)) 
			{
				String class_D=proprange.sc.get(prop_R2);
				
				LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
				for(String role_compos_S:role_compos_lst_S) {
					if(role_compos_S!=null && class_D!=null && !class_E.equals(class_D)) {
						String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
						subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
						addifnotalready(concatoadd);
						
						flogger=true;
					}
					
					
				}	
			}
		}
//		if(flogger) 
//		{
//			System.out.println("New8_First");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
		
		
		
	}
	public void rule_nine_first(String class_E,String prop_R,int sub_card_i,String class_C) 
	{
		boolean flogger=false;
		for(String prop_R2: propdomain.sc.keySet()) 
		{
			if(!prop_R2.equals(prop_R) && propdomain.sc.get(prop_R2)!=null && mincard.sc.get(prop_R2)!=null && proprange.sc.get(prop_R2)!=null && propdomain.sc.get(prop_R2).contains(class_C)) 
			{
//				int sub_card_j=mincard.sc.get(prop_R2);
				String class_D = proprange.sc.get(prop_R2);
//				if(sub_card_i<=sub_card_j) 
//				{
					LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
					for(String role_compos_S:role_compos_lst_S) {
						if(role_compos_S!=null && class_D!=null && !class_E.equals(class_D)) {
							String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
							subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
							addifnotalready(concatoadd);
							
							flogger=true;							
						}						
					}					
//				}			
			}
		}	
//		if(flogger) 
//		{
//			System.out.println("New9_First");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public void rule_nine_second(String class_C,String prop_R2,int sub_card_j,String class_D)
	{
		boolean flogger=false;
		LinkedList<String> prop_lst_R=new LinkedList<>(getpropfromrange((class_C)));
		for(String prop_R:prop_lst_R) 
		{
			if(!prop_R.equals(prop_R2)) 
			{
				String class_E=getdomainfromprop(prop_R);
				if(mincard.sc.get(prop_R)!=null) 
				{
					int sub_card_i=mincard.sc.get(prop_R);
					if(sub_card_i<=sub_card_j) 
					{
						LinkedList<String> role_compos_lst_S=new LinkedList<>(getrolecompos(prop_R,prop_R2));
						for(String role_compos_S:role_compos_lst_S) {
							if(role_compos_S!=null && class_E!=null && !class_E.equals(class_D)) {
								String concatoadd="SubClassOf("+class_E+ " "+ "ObjectSomeValuesFrom("+role_compos_S+" "+class_D+"))";
								subclasses.add(class_E,"ObjectSomeValuesFrom("+role_compos_S+" "+class_D+")");
								addifnotalready(concatoadd);
								
								flogger=true;								
							}							
						}				
					}					
				}
							
			}	
		}	
//		if(flogger) 
//		{
//			System.out.println("New9_Second");
//		}
		//System.out.p.println("//////////////////////////////////////////////////////");
	}
	public long test_drive2(String args, String args2, String args3, int chng) throws IOException, InterruptedException, OWLOntologyCreationException 
	{
		
		System.out.println("New System Started"); 
		try {
	      File myObj = new File(args);
//	      Scanner myReader = new Scanner(myObj);
//	      while (myReader.hasNextLine()) {
//	        String data = myReader.nextLine();
//	        alllines.add(data);
//	      }
//	      myReader.close();
			String data;
			BufferedReader br = new BufferedReader(new FileReader(myObj));
			while ((data=br.readLine()) != null) 
			{
				alllines.add(data);
			} 
			br.close();
			
	    } 
		catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//Populating Data Structures(SubClassOf and Object Property)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		populate_data_struct(args3,chng);
		
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Iterating and Rule Application
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		ExecutorService	exec	=	Executors.newFixedThreadPool(Integer.parseInt(args2));	
		long startTime = System.currentTimeMillis();
		long duration=0;
		while(!card_comp.isEmpty()) 
		{
			String tempstr=card_comp.poll();
			Runnable task =new A_PreProcess(tempstr);
			exec.execute(task);			
		}
		exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        duration=(System.currentTimeMillis() - startTime);
        System.out.println("Execution time of CCReasoner: " + duration);
		while(classification.remove(")"));
		
		Set<String> set2 = new LinkedHashSet<>(classification);
		classification.clear();
		classification.addAll(set2);	
		
		classification.add(")");
		
		String finalee=String.join("\n", classification);
		BufferedWriter bw = new BufferedWriter(new FileWriter(args));
		bw.write(finalee);
		bw.close();
		

	
	    return duration;
	}
	public static void main(String args[]) throws OWLOntologyCreationException, IOException, InterruptedException 
	{
		A_PreProcess mncv=new A_PreProcess();
		long c1=mncv.test_drive2(args[0],args[1],args[2],0);
		
		
	}
}
