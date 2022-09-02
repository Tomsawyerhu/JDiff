package org.nju.jdiff;

import com.github.javaparser.ast.Node;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class TreeDiff {
    private int strategy;
    public static class DiffResult {
        Node context;
        Node buggySnippet;
        Node fixedSnippet;
    }

    public static class DiffStrategy{
        public static final int COMMON_PARENT=0;
        public static final int COMMON_PARENT_SIDE=1;
        public static final int COMMON_PARENT_ALL_NONRECURSIVE=2;
        public static final int COMMON_PARENT_ALL_RECURSIVE=3;

        public static final int STRATEGY_NUM=4;
    }

    public TreeDiff(int strategy){
        this.strategy=strategy;
    }
    public TreeDiff(){
        this.strategy=DiffStrategy.COMMON_PARENT;
    }

    public DiffResult diff(Node buggy, Node fixed) {
        DiffResult result = new DiffResult();
        if (buggy.toString().equals(fixed.toString())) {
            result.context = buggy;
            result.buggySnippet = null;
            result.fixedSnippet = null;
        }
        // 节点类型不同 或者 存在叶子结点
        else if (!isSameType(buggy, fixed) || isTerminal(buggy) || isTerminal(fixed)) {
            result.context = null;
            result.fixedSnippet = fixed;
            result.buggySnippet = buggy;
        } else {
            int offset1 = 0, offset2 = 0;

            List<Node> children1 = buggy.getChildNodes();
            List<Node> children2 = fixed.getChildNodes();
            int length1 = children1.size();
            int length2 = children2.size();
            int minLength = Math.min(length1, length2);

            while (offset1 < minLength) {
                if (matches(children1.get(offset1), children2.get(offset1))) {
                    offset1 += 1;
                } else {
                    break;
                }
            }

            while (offset1 + offset2 < minLength) {
                if (matches(children1.get(length1 - 1 - offset2), children2.get(length2 - 1 - offset2))) {
                    offset2 += 1;
                } else {
                    break;
                }
            }

            Node ctx = buggy.clone();

            // not 1-1
            if ((length1 - offset1 - offset2) != 1 || (length2 - offset1 - offset2) != 1) {
                Node bug = buggy.clone();
                Node fix = fixed.clone();
                List<Node> removeFromBuggy = new ArrayList<>();
                List<Node> removeFromFixed = new ArrayList<>();
                List<Node> removeFromCtx = new ArrayList<>();
                if(this.strategy==DiffStrategy.COMMON_PARENT){
                    ctx=null;
                }
                else if(this.strategy==DiffStrategy.COMMON_PARENT_SIDE) {
                    for (int i = 0; i < offset1; i++) {
                        removeFromBuggy.add(bug.getChildNodes().get(i));
                        removeFromFixed.add(fix.getChildNodes().get(i));
                    }
                    for (int j = 0; j < offset2; j++) {
                        removeFromBuggy.add(getNthChild(bug, length1 - 1 - j));
                        removeFromFixed.add(getNthChild(fix, length2 - 1 - j));
                    }
                    for (int k = offset1; k < length1 - offset2; k++) {
                        removeFromCtx.add(getNthChild(ctx, k));
                    }
                }
                else if(this.strategy==DiffStrategy.COMMON_PARENT_ALL_NONRECURSIVE){
                    for (int i = 0; i < offset1; i++) {
                        removeFromBuggy.add(bug.getChildNodes().get(i));
                        removeFromFixed.add(fix.getChildNodes().get(i));
                    }
                    for (int j = 0; j < offset2; j++) {
                        removeFromBuggy.add(getNthChild(bug, length1 - 1 - j));
                        removeFromFixed.add(getNthChild(fix, length2 - 1 - j));
                    }

                    // match from left
                    // X0 O0 X1 X2 X3
                    // O0 X1 X2 O1 O2 X3
                    // should be O0,X1 X2,X3
                    int p=offset1,q = offset1;
                    int lastP;
                    int lastQ;
                    while(true) {
                        boolean found = false;
                        lastP=p;
                        lastQ=q;
                        for (; p < length1 - offset2; p++) {
                            for (q=lastQ; q < length2 - offset2; q++) {
                                if (matches(getNthChild(bug, p), getNthChild(fix, q))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) break;
                        }

                        for (int k = lastP; k < p; k++) {
                            removeFromCtx.add(getNthChild(ctx, k));
                        }

                        while (found && p < length1 - offset2 && q < length2 - offset2) {
                            if (matches(getNthChild(bug, p), getNthChild(fix, q))) {
                                removeFromBuggy.add(getNthChild(bug, p));
                                removeFromFixed.add(getNthChild(fix, q));
                                p += 1;
                                q += 1;
                            } else {
                                break;
                            }
                        }
                        if(!found){
                            break;
                        }
                    }
                    for (int k = p; k < length1-offset2; k++) {
                        removeFromCtx.add(getNthChild(ctx, k));
                    }

                    //todo match from right
                    //todo match from both side

                }else if(this.strategy==DiffStrategy.COMMON_PARENT_ALL_RECURSIVE){
                    //todo
                    throw new NotImplementedException();
                }

                //try remove, if fail then replace with an empty node
                for (Node n : removeFromBuggy) {
                    removeOrReplaceWithEmpty(n);
                }
                for (Node n : removeFromFixed) {
                    removeOrReplaceWithEmpty(n);
                }
                for (Node n : removeFromCtx) {
                    removeOrReplaceWithEmpty(n);
                }
                result.context = ctx;
                result.buggySnippet = bug;
                result.fixedSnippet = fix;
            }
            // 1-1
            else {
                assert buggy.getChildNodes().size()==fixed.getChildNodes().size();
                DiffResult result1 = diff(children1.get(offset1), children2.get(offset1));

                if(this.strategy!=DiffStrategy.COMMON_PARENT_ALL_RECURSIVE) {
                    //incorporate new ctx into the old one if there is one
                    if (result1.context != null) {
                        getNthChild(ctx, offset1).replace(result1.context);
                    } else {
                        //try remove, if fail then replace with an empty node
                        removeOrReplaceWithEmpty(getNthChild(ctx, offset1));
                    }
                    result.fixedSnippet = result1.fixedSnippet;
                    result.buggySnippet = result1.buggySnippet;
                    result.context = ctx;
                }else{
                    //todo

                    // X X X X X
                    // X X O X X
                    // offset1=2
                    Node bug = buggy.clone();
                    Node fix = fixed.clone();
                    int length=bug.getChildNodes().size();
                    List<Node> removeFromBuggy=new ArrayList<>();
                    List<Node> removeFromFixed=new ArrayList<>();
                    for(int i=0;i<length;i++){
                        if(i!=offset1){
                            removeFromBuggy.add(getNthChild(bug,i));
                            removeFromFixed.add(getNthChild(fix,i));
                        }
                    }
                    for(Node n:removeFromBuggy){
                        removeOrReplaceWithEmpty(n);
                    }
                    for(Node n:removeFromFixed){
                        removeOrReplaceWithEmpty(n);
                    }

                    if(result1.context!=null){
                        getNthChild(ctx,offset1).replace(result1.context);
                    }else{
                        removeOrReplaceWithEmpty(getNthChild(ctx,offset1));
                    }

                    assert result1.buggySnippet!=null;
                    assert result1.fixedSnippet!=null;
                    getNthChild(bug,offset1).replace(result1.buggySnippet);
                    getNthChild(fix,offset1).replace(result1.fixedSnippet);

                    result.fixedSnippet = fix;
                    result.buggySnippet = bug;
                    result.context = ctx;

                    throw new NotImplementedException();
                }
            }
        }
        return result;
    }

    private static boolean matches(Node n1, Node n2) {
        return n1.toString().equals(n2.toString());
    }

    private static boolean isTerminal(Node n) {
        return n.getChildNodes().size() == 0;
    }

    private static boolean isSameType(Node n1, Node n2) {
        return n1.getMetaModel().getTypeName().equals(n2.getMetaModel().getTypeName());
    }

    private static Node getNthChild(Node n, int nth) {
        return n.getChildNodes().get(nth);
    }

    private static Node getEmptyNode(Node n) throws InstantiationException, IllegalAccessException {
        return n.getClass().newInstance();
    }

    private static void removeOrReplaceWithEmpty(Node n){
        boolean success=n.remove();
        if(!success){
            try {
                n.replace(getEmptyNode(n));
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
