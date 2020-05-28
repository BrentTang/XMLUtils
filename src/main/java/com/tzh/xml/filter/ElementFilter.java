package com.tzh.xml.filter;


import org.dom4j.Element;

public interface ElementFilter {

    public boolean doFilter(Element element);
    public String defineKey(Element element);

}
