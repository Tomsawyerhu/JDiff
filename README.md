# JDiff

## Introduction
> JDiff is a tool used to diff a java bug file and a repair version for the bug.  
> It will output code snippets and common context that is shared between codes, in the form of edges & nodes, after comparing code files.  
> All is done by differing asts.
## Usage
> java -jar jdiff.jar [path1] [path2] ([ctx])
+ path1: java bug code ( string )
+ path2: java fix code ( string )
+ ctx: need context or not ( true or false )