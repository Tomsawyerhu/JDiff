package org.nju.jdiff;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    public static void main(String[] args) {
        //diff_strategy bug_input_code fix_input_code (true/false)
        if(args.length<3){
            System.err.println("not enough params");
            return;
        }
        int strategy=Integer.parseInt(args[0]);
        if(strategy> TreeDiff.DiffStrategy.STRATEGY_NUM || strategy<0){
            System.err.println("no such diff strategy");
            return;
        }
        String javaFile1=args[1],javaFile2=args[2];
        if(!javaFile1.endsWith(".java") || !javaFile2.endsWith(".java")){
            System.err.println("input file must end with .java");
            return;
        }
        String code1="",code2="";
        try {
            code1=FileUtils.readFileToString(new File(javaFile1),"utf8");
            code2=FileUtils.readFileToString(new File(javaFile2),"utf8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //skip comment
        StaticJavaParser.getConfiguration().setAttributeComments(false);

        //parse
        CompilationUnit cu1 = StaticJavaParser.parse(code1);
        CompilationUnit cu2 = StaticJavaParser.parse(code2);

        //diff
        TreeDiff.DiffResult result= new TreeDiff(strategy).diff(cu1,cu2);
        System.out.println("----------------------- bug -----------------------");
        System.out.println(result.buggySnippet==null?"":result.buggySnippet.toString());
        System.out.println("----------------------- fix -----------------------");
        System.out.println(result.fixedSnippet==null?"":result.fixedSnippet.toString());

        //visit ast
        TreeVisit.VisitResult result1= TreeVisit.visitTree(result.buggySnippet);
        TreeVisit.VisitResult result2= TreeVisit.visitTree(result.fixedSnippet);
        Map<Integer, List<Integer>> edges1=result1.edges;
        Map<Integer, List<Integer>> edges2=result2.edges;
        List<AstNode> nodes1=result1.nodes;
        List<AstNode> nodes2=result2.nodes;

        //save
        String outputEdges1=javaFile1.replace(".java",".edge");
        String outputNodes1=javaFile1.replace(".java",".node");
        String outputEdges2=javaFile2.replace(".java",".edge");
        String outputNodes2=javaFile2.replace(".java",".node");

        output(edges1,outputEdges1,nodes1,outputNodes1);
        output(edges2,outputEdges2,nodes2,outputNodes2);

        //output context(optional)
        if(args.length>=4&&Boolean.parseBoolean(args[3])){
            System.out.println("----------------------- context -----------------------");
            System.out.println(result.context==null?"":result.context.toString());
            TreeVisit.VisitResult result3= TreeVisit.visitTree(result.context);
            Map<Integer, List<Integer>> edges3=result3.edges;
            List<AstNode> nodes3=result3.nodes;
            String outputEdges3= javaFile1.replace(".java",".ctx.edge");
            String outputNodes3=javaFile2.replace(".java",".ctx.node");
            output(edges3,outputEdges3,nodes3,outputNodes3);
        }
    }

    private static void output( Map<Integer, List<Integer>> edges,String outputEdges,List<AstNode> nodes,String outputNodes){
        StringBuilder edgesStr= new StringBuilder();
        for(int key:edges.keySet().stream().sorted().collect(Collectors.toList())){
            List<Integer> endNodes=edges.get(key);
            for(int endNode:endNodes){
                if(edgesStr.length()>0){
                    edgesStr.append("\n");
                }
                edgesStr.append(String.format("%d -> %d", key, endNode));
            }
        }
        //print edges
        //System.out.println(edgesStr);
        try {
            FileUtils.write(new File(outputEdges),edgesStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder nodesStr= new StringBuilder();
        for(AstNode n:nodes){
            if(nodesStr.length()>0){nodesStr.append("\n");}
            nodesStr.append(n);
        }
        //print nodes
        //System.out.println(nodesStr);
        try {
            FileUtils.write(new File(outputNodes),nodesStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
