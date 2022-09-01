package org.nju.jdiff;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeVisit {
    public static class VisitResult{
        Map<Integer, List<Integer>> edges=new HashMap<>();
        List<AstNode> nodes=new ArrayList<>();
    }

    public static VisitResult visitTree(Node n){
        if(n==null){
            return new VisitResult();
        }
        List<Node> nodesToVisit=new ArrayList<>();
        nodesToVisit.add(n);
        Map<Integer, List<Integer>> edges=new HashMap<>();
        List<AstNode> nodes=new ArrayList<>();
        int counter=0;
        nodes.add(new AstNode(counter,n.getMetaModel().getTypeName(), n.getChildNodes().size()==0?n.toString():""));
        List<Integer> ids=new ArrayList<>();
        ids.add(counter);
        //System.out.println(n.getMetaModel().getTypeName());
        counter+=1;

        while(!nodesToVisit.isEmpty()){
            List<Node> sons=new ArrayList<>();
            List<Integer> sonsId=new ArrayList<>();
            for(int i=0;i<nodesToVisit.size();i++){
                Node current=nodesToVisit.get(i);
                int currentId=ids.get(i);
                if(!edges.containsKey(currentId)){
                    edges.put(currentId,new ArrayList<>());
                }
                for(Node child:current.getChildNodes()){
                    sons.add(child);
                    sonsId.add(counter);

                    //add edge
                    edges.get(currentId).add(counter);

                    //add node
                    nodes.add(new AstNode(counter,child.getMetaModel().getTypeName(), child.getChildNodes().size()==0?child.toString().replace("\n",""):""));
                    //System.out.println(child.getMetaModel().getTypeName()+" "+(child.getChildNodes().size()==0?child.toString():""));
                    counter+=1;
                }
            }
            nodesToVisit=sons;
            ids=sonsId;
        }
        VisitResult result=new VisitResult();
        result.edges=edges;
        result.nodes=nodes;
        return result;
    }
}
