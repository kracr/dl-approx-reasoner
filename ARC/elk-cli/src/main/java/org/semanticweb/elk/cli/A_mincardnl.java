package org.semanticweb.elk.cli;
import java.util.concurrent.ConcurrentHashMap;

class A_mincardnl{
	ConcurrentHashMap<String, Integer> sc=new ConcurrentHashMap<>();
	
	public void add (String a, Integer b) {
		if(sc.get(a)==null) 
		{
			sc.put(a, b);
		}
		else {
			if(b>sc.get(a)) 
			{
				sc.remove(a);
				sc.put(a, b);				
			}
		}
	}
	
	
}