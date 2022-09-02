# JDiff

## Introduction

> JDiff is a tool used to diff a java bug file and a repair version for the bug.  
> 
> It will output code snippets representing bug snippet and repair snippet, and common context that is shared between codes, in the form of edges & nodes, after comparing code files. 
> 
> All is done by differing asts.

## Usage

> java -jar jdiff.jar [diff_strategy] [path1] [path2] ([ctx])
+ diff_strategy: strategy used to diff tree ( support three currently )
+ path1: java bug code ( string )
+ path2: java repair code ( string )
+ ctx: need context or not ( true or false )

## Tree Diff Strategy
#### COMMON_PARENT
> split bug snippet and repair snippet by localizing node which is the common root of different sub asts corresponding to bug part and repair part.

+ diff_strategy = 0

#### COMMON_PARENT_SIDE
> on basis of *COMMON_PARENT*, also remove context from both side until meet unmatched nodes, non recursive.
+ diff_strategy = 1

#### COMMON_PARENT_ALL_NONRECURSIVE
> on basis of *COMMON_PARENT_SIDE*, also remove context from inside left, non recursive.
+ diff_strategy = 2

#### COMMON_PARENT_ALL_RECURSIVE
> on basis of *COMMON_PARENT_SIDE*, also remove context from inside, recursive, not implemented yet.
+ diff_strategy = 3

## Demo
**bug code**
```java

public class test {
  public void f() {
    int t=1;
    int a=1;
    int b=1;
    int c=1;
    int d=1;
    int p=1;
  }
}

```
**repair code**
```java

public class test {
  public void f() {
    int t=1;
    int x=1;
    int a=1;
    int b=1;
    int c=1;
    int d=1;
    int y=1;
    int p=1;
  }
}

```
#### COMMON_PARENT
**bug snippet**
```java

{
    int t = 1;
    int a = 1;
    int b = 1;
    int c = 1;
    int d = 1;
    int p = 1;
}

```

**repair snippet**
```java

{
    int t = 1;
    int x = 1;
    int a = 1;
    int b = 1;
    int c = 1;
    int d = 1;
    int y = 1;
    int p = 1;
}

```

#### COMMON_PARENT_SIDE
**bug snippet**

```java

{
    int a = 1;
    int b = 1;
    int c = 1;
    int d = 1;
}

```

**repair snippet**
```java

{
    int x = 1;
    int a = 1;
    int b = 1;
    int c = 1;
    int d = 1;
    int y = 1;
}

```

#### COMMON_PARENT_ALL_NONRECURSIVE
**bug snippet**
```java

{
}

```

**repair snippet**
```java

{
    int x = 1;
    int y = 1;
}

```