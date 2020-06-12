package org.semanticweb.elk.cli;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

class A_Class{
	ConcurrentHashMap<String, LinkedList<String>> sc=new ConcurrentHashMap<>();
	ConcurrentHashMap<String, Integer>  indexes = new ConcurrentHashMap<>();
	
	private int index = -1;
	
	public void add (String a, String b) {
		if(sc.get(a)==null) {
			LinkedList<String> temp=new LinkedList<String>();
			temp.add(b);
			sc.put(a, temp);
			indexes.put(a, sc.size()+index);
//			indexes.put(b, sc.size()+index);
		}
		else {
			LinkedList<String> temp=new LinkedList<>(sc.get(a));
			if(!temp.contains(b)) {
				temp.add(b);				
			}			
			sc.put(a,temp);
		}
	}
	public boolean checksub(String a, String b) {
		
		if(sc.get(a)!=null) 
		{
			return this.DFS(a,b);
		}
		else 
		{
			return false;
		}		
	}
    public boolean DFS(String source,String dest){
    	ArrayList<String>temp; 
    	  
        // Mark all the vertices as not visited(By default set 
        // as false) 
    	int V = sc.size();
//    	System.out.println(V);
        boolean visited[] = new boolean[V]; 
  
        // Create a queue for BFS 
        LinkedList<String> queue = new LinkedList<String>(); 
  
        // Mark the current node as visited and enqueue it 
        visited[indexes.get(source)]=true; 
        queue.add(source); 
  
        // 'i' will be used to get all adjacent vertices of a vertex 
        Iterator<String> i; 
        while (queue.size()!=0) 
        { 
            // Dequeue a vertex from queue and print it 
            source = queue.poll(); 
  
            String n; 
            i = sc.get(source).listIterator(); 
  
            // Get all adjacent vertices of the dequeued vertex s 
            // If a adjacent has not been visited, then mark it 
            // visited and enqueue it 
            while (i.hasNext()) 
            { 
                n = i.next(); 
//                System.out.println(n);
  
                // If this adjacent node is the destination node, 
                // then return true 
                if (n.equals(dest)) 
                    return true; 
  
                // Else, continue to do BFS 
                if(indexes.get(n)!=null) 
                {
                	if (!visited[indexes.get(n)]) 
                    { 
                        visited[indexes.get(n)] = true; 
                        queue.add(n); 
                    }                 	
                }
                
            } 
        } 
        return false;
        
    }
	
}