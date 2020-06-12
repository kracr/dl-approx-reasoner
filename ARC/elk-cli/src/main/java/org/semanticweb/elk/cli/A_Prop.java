package org.semanticweb.elk.cli;
import java.util.concurrent.ConcurrentHashMap;

class A_Prop{
	ConcurrentHashMap<String, String> sc=new ConcurrentHashMap<>();
	
	public void add (String a, String b) {
		if(sc.get(a)==null) {
			sc.put(a, b);
		}
		else 
		{
			String onjintsc=new String(sc.get(a));
			String conctoadd="";
			if(onjintsc.indexOf("ObjectIntersectionOf")==0) 
			{
				String expressiontype="ObjectIntersectionOf";
				String expression= new String(onjintsc.substring(expressiontype.length()+1,onjintsc.length()-1));
				if(!expression.contains(b)) 
				{
					expression+=" "+b;
					conctoadd="ObjectIntersectionOf("+expression+")";	
					sc.remove(a);
					sc.put(a,conctoadd);
				}
								
			}
			else 
			{
				if(!onjintsc.contains(b)) {
					onjintsc+=" "+b;
					conctoadd="ObjectIntersectionOf("+onjintsc+")";	
					sc.remove(a);
					sc.put(a,conctoadd);
				}
						
			}
			
		}
	}
	
	
}