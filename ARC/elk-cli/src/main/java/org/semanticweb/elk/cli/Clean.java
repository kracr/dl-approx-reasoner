package org.semanticweb.elk.cli;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Clean 
{
	static ArrayList<String> alllines=new ArrayList<>();
	static ArrayList<String> outlines=new ArrayList<>();
	
	static void clean(File inout) throws IOException 
	{
		alllines.clear();
		outlines.clear();
		int lineofont=-1;
		try 
		{
			Scanner myReader = new Scanner(inout);
			int count=0;
			while (myReader.hasNextLine()) 
			{
				String data = myReader.nextLine();
				if(data.indexOf("Ontology(")==0) {
					lineofont=count;
				}
				count+=1;
				if(data.indexOf("AnnotationAssertion(")!=0) {
					alllines.add(data);
				}
			}
		    myReader.close();
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		int count=0;
		for(String h: alllines) 
		{
			if(count<=lineofont) 
			{
				outlines.add(h);
			}
			else 
			{
				while(h.contains("<") && h.contains(">"))
				{
//					System.out.println(h+" "+count);
					int start=h.indexOf('<');
					int end= h.indexOf('>');
//					if(start<=end) 
//					{
//					System.out.println(h);
					String wanted=h.substring(start+1,end);
					String intrsstr=wanted.substring(wanted.lastIndexOf('#')+1);
					h=h.substring(0,start)+":"+intrsstr+h.substring(end+1);	
//					}
					
				}
				outlines.add(h);			
			}
			count+=1;
		}
		String finalee=String.join("\n", outlines);
		FileWriter fw=new FileWriter(inout);
        fw.write(finalee);    
        System.out.println();
        fw.close(); 
        System.out.println("Ontology Cleaned!");
	
		
	}


}
