package com.github.anrimian.simplemusicplayer.data.utils.folders;

public class StringNode extends NodeData{

    private String data;

    StringNode(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StringNode{" +
                "data='" + data + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringNode that = (StringNode) o;

        return data != null ? data.equals(that.data) : that.data == null;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
