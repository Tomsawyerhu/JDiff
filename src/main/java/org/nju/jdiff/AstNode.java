package org.nju.jdiff;

public class AstNode {
    int id;
    String type;
    String code;

    public AstNode(int id, String type, String code) {
        this.id = id;
        this.type = type;
        this.code = code;
    }

    @Override
    public String toString() {

        return String.format("id:%s type:%s code:%s", this.id,this.type,this.code.replace("\n",""));
    }
}
