package org.semanticweb.elk.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;



public class AfterC {
	static void prc(File in, File out) throws IOException 
	{
		ArrayList<String> alllines=new ArrayList<>();
		ArrayList<String> newlines=new ArrayList<>();
		try 
		{
//			Scanner myReader = new Scanner(out);
//			while (myReader.hasNextLine()) 
//			{
//				String data = myReader.nextLine();
//
//				if(data.indexOf("Declaration(")!=0 && data.indexOf("AnnotationAssertion(")!=0) 
//				{
//					alllines.add(data);				
//				}			
//			}
//		    myReader.close();
			String data;
			BufferedReader br = new BufferedReader(new FileReader(out));
			while ((data=br.readLine()) != null) {
				if(data.indexOf("Declaration(")!=0 && data.indexOf("AnnotationAssertion(")!=0) 
				{
					alllines.add(data);				
				}	
			} 
			br.close();
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		alllines.remove(0);
		alllines.remove(alllines.size()-1);
		
		ArrayList<String> alllines2=new ArrayList<>();
		try 
		{

//			Scanner myReader = new Scanner(in);
//			while (myReader.hasNextLine()) 
//			{
//				
//				String data = myReader.nextLine();
//				if(data.indexOf("AnnotationAssertion(")!=0) 
//				{
//					alllines2.add(data);
//				}
//				
//			}
//		    myReader.close();
			String data;
			BufferedReader br = new BufferedReader(new FileReader(in));
			while ((data=br.readLine()) != null) 
			{
				if(data.indexOf("AnnotationAssertion(")!=0) 
				{
					alllines2.add(data);
				}	
			} 
			br.close();
		}		
		catch (FileNotFoundException e) 
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
//		System.out.println("In4");
		
//		for(String prying:alllines) {
//			if(!alllines2.contains(prying)) {
//				newlines.add(prying);				
//			}
//		}
		newlines.addAll(alllines);
//		System.out.println("In5");
//		newlines.removeAll(alllines2);
		
		while(alllines2.remove(")"));
//		System.out.println("In5.2");
		alllines2.addAll(alllines);
//		System.out.println("In5.3");
		Set<String> set2 = new LinkedHashSet<>(alllines2);
//		System.out.println("In5.4");
		alllines2.clear();
//		System.out.println("In5.5");
		alllines2.addAll(set2);
//		System.out.println("In5.6");
		alllines2.add(")");
		
//		System.out.println("In6");

		String finalee=String.join("\n", alllines2);
		BufferedWriter bw = new BufferedWriter(new FileWriter(in));
		bw.write(finalee);
		bw.close();
		
//		System.out.println("In7");
		
//		FileWriter fw=new FileWriter(in);
//		
//        fw.write(finalee);    
//        fw.close();
		String finalee2=String.join("\n", newlines);
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(out));
        bw2.write(finalee2);    
        bw2.close();
	}

}
