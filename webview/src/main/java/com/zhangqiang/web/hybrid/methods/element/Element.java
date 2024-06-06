package com.zhangqiang.web.hybrid.methods.element;

public class Element {

    private String id;
    private String localName;
    private String nodeName;
    private String nodeType;

    private int clientWidth;
    private int clientHeight;
    private int clientLeft;
    private int clientTop;

    private int offsetLeft;
    private int offsetTop;
    private String baseURI;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getClientWidth() {
        return clientWidth;
    }

    public void setClientWidth(int clientWidth) {
        this.clientWidth = clientWidth;
    }

    public int getClientHeight() {
        return clientHeight;
    }

    public void setClientHeight(int clientHeight) {
        this.clientHeight = clientHeight;
    }

    public int getClientLeft() {
        return clientLeft;
    }

    public void setClientLeft(int clientLeft) {
        this.clientLeft = clientLeft;
    }

    public int getClientTop() {
        return clientTop;
    }

    public void setClientTop(int clientTop) {
        this.clientTop = clientTop;
    }

    public int getOffsetLeft() {
        return offsetLeft;
    }

    public void setOffsetLeft(int offsetLeft) {
        this.offsetLeft = offsetLeft;
    }

    public int getOffsetTop() {
        return offsetTop;
    }

    public void setOffsetTop(int offsetTop) {
        this.offsetTop = offsetTop;
    }
}
