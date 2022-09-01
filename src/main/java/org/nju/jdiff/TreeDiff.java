package org.nju.jdiff;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.List;

public class TreeDiff {
    public static class DiffResult{
        Node context;
        Node buggySnippet;
        Node fixedSnippet;
    }
    public static DiffResult diff(Node buggy, Node fixed){
        DiffResult result=new DiffResult();
        if(buggy.toString().equals(fixed.toString())){
            result.context=buggy;
            result.buggySnippet=null;
            result.fixedSnippet=null;
        }
        // 节点类型不同 或者 存在叶子结点
        else if(!isSameType(buggy,fixed) || isTerminal(buggy) || isTerminal(fixed)){
            result.context=null;
            result.fixedSnippet=fixed;
            result.buggySnippet=buggy;
        }else{
            int offset1=0,offset2=0;

            List<Node> children1=buggy.getChildNodes();
            List<Node> children2=fixed.getChildNodes();
            int length1=children1.size();
            int length2=children2.size();
            int minLength=Math.min(length1,length2);

            while(offset1<minLength){
                if(matches(children1.get(offset1),children2.get(offset1))){
                    offset1+=1;
                }else{
                    break;
                }
            }

            while(offset1+offset2<minLength){
                if(matches(children1.get(length1-1-offset2),children2.get(length2-1-offset2))){
                    offset2+=1;
                }else{
                    break;
                }
            }

            Node ctx=buggy.clone();

            // not 1-1
            if((length1-offset1-offset2)!=1 || (length2-offset1-offset2)!=1){
                Node bug=buggy.clone();
                Node fix=fixed.clone();
                List<Node> removeFromBuggy=new ArrayList<>();
                List<Node> removeFromFixed=new ArrayList<>();
                List<Node> removeFromCtx=new ArrayList<>();
                for(int i=0;i<offset1;i++){
                    removeFromBuggy.add(bug.getChildNodes().get(i));
                    removeFromFixed.add(fix.getChildNodes().get(i));
                }
                for(int j=0;j<offset2;j++){
                    removeFromBuggy.add(getNthChild(bug,length1-1-j));
                    removeFromFixed.add(getNthChild(fix,length2-1-j));
                }
                for(int k=offset1;k<length1-offset2;k++){
                    removeFromCtx.add(getNthChild(ctx,k));
                }
                for(Node n:removeFromBuggy){n.remove();}
                for(Node n:removeFromFixed){n.remove();}
                for(Node n:removeFromCtx){n.remove();}
                result.context=ctx;
                result.buggySnippet=bug;
                result.fixedSnippet=fix;
            }
            // 1-1
            else{
                DiffResult result1= diff(children1.get(offset1),children2.get(offset1));
                //incorporate new ctx into the old one if there is one
                if(result1.context!=null){
                    getNthChild(ctx,offset1).replace(result1.context);
                }else{
                    //replace with an empty node
                    try {
                        getNthChild(ctx,offset1).replace(getEmptyNode(getNthChild(ctx,offset1)));
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.fixedSnippet=result1.fixedSnippet;
                result.buggySnippet=result1.buggySnippet;
                result.context=ctx;
            }
        }
        return result;
    }

    private static boolean matches(Node n1,Node n2){
        return n1.toString().equals(n2.toString());
    }

    private static boolean isTerminal(Node n){
        return n.getChildNodes().size()==0;
    }

    private static boolean isSameType(Node n1,Node n2){
        return n1.getMetaModel().getTypeName().equals(n2.getMetaModel().getTypeName());
    }

    private static Node getNthChild(Node n,int nth){
        return n.getChildNodes().get(nth);
    }

    private static Node getEmptyNode(Node n) throws InstantiationException, IllegalAccessException {
        return n.getClass().newInstance();
    }
}
