package com.tzh.xml.utils;

import com.tzh.xml.filter.ElementFilter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XMLParser {

    private final String ABSOLUTEPATHPREFIX = "absolutepath:";
    private final String URLPREFIX = "file:/";
    private Document document;

    public XMLParser() {}

    /**
     * 通过Document构建XMLUtils
     * @param document
     */
    public XMLParser(Document document) {
        this.document = document;
    }

    /**
     * 通过读取classPath构建XMLUtils
     * @param classPath
     * @throws FileNotFoundException
     */
    public XMLParser(String classPath) throws FileNotFoundException {
        SAXReader saxReader = new SAXReader();
        try {
            this.document = saxReader.read(getClassPathFile(classPath));
        } catch (DocumentException e) {
            throw new FileNotFoundException("未发现" + classPath + "文件！");
        }
    }

    /**
     * 获取读取到的document
     * @return
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * 获取classPath下的文件的InputStream
     * @param path
     * @return
     */
    private InputStream getClassPathFile(String path){

        //return this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            URL resource = this.getClass().getClassLoader().getResource("");
            return new FileInputStream(resource.toString().replaceFirst(URLPREFIX, "") + path);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(path + "有误！");
        }
    }

    /**
     * 遍历当前document的所有节点，保存符合filter的节点
     * @param elementFilter
     * @return
     */
    public Map<String, Element> parse(ElementFilter elementFilter) {
        Element root = this.document.getRootElement();
        Iterator<Element> iterator = root.elementIterator();
        Map<String, Element> config = new HashMap<String, Element>();
        while (iterator.hasNext()) {
            config.putAll(parseElement(iterator.next(), elementFilter));
        }
        return config;
    }

    /**
     * 获取root节点的下一级节点
     * 以  节点name:节点对象  形式保存
     * @return
     */
    public Map<String, Element> getSecondLevelElement() {

        Element root = this.document.getRootElement();
        Iterator<Element> iterator = root.elementIterator();
        Map<String, Element> elements = new HashMap<String, Element>();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            elements.put(next.getName(), next);
        }
        return elements;
    }

    /**
     * 获取获取下一级节点
     * @param element
     * @param elementFilter
     * @return
     */
    public Map<String, Element> getNextLevelElement(Element element, ElementFilter elementFilter) {

        if (element == null)
            return null;

        Iterator<Element> iterator = element.elementIterator();
        Map<String, Element> elements = new HashMap<String, Element>();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            if (elementFilter == null) {
                addUniqueConfig(elements, next.getName(), next);
            } else if (elementFilter.doFilter(next)) {
                elements.put(elementFilter.defineKey(next), next);
            }
        }
        return elements;
    }

    /**
     * 通过elementFilter获取符合filter的节点
     * @param root
     * @param elementFilter
     * @return
     */
    public Map<String, Element> parseElement(Element root, ElementFilter elementFilter){
        Map<String, Element> config = new HashMap<String, Element>();
        boolean add = false;
        if (elementFilter == null) {
            add = true;
        } else if (elementFilter.doFilter(root)) {
            add = true;
        }

        if (add) {
            addConfig(config, root, elementFilter);
        }

        Iterator<Element> iterator = root.elementIterator();
        while (iterator.hasNext()){
            config.putAll(parseElement(iterator.next(), elementFilter));
        }
        return config;
    }

    /**
     * 添加Element到config
     * @param config
     * @param element
     * @param elementFilter
     */
    private void addConfig(Map<String, Element> config, Element element, ElementFilter elementFilter) {
        // 通过用户自定义的key保存
        if (elementFilter != null) {
            String key = elementFilter.defineKey(element);
            if (key != null && key.length() > 0) {
                addUniqueConfig(config, key, element);
            } else {
                throw new IllegalArgumentException("请定义唯一的key");
            }
        } else {
            // 用户如果未指定则使用id作为key
            String id = element.attributeValue("id");
            if (id != null && id.trim().length() > 0) {
                addUniqueConfig(config, id, element);
            } else {
                // 使用标签名做key
                //addUniqueConfig(config, element.getName(), element);
                // 默认无id时的默认key
                throw new IllegalArgumentException("请设置" + element.getName() + "的id属性，或使用过滤器实现defineKey");
            }
        }
    }

    /**
     * 添加element前保证id的唯一性
     * @param config
     * @param key
     * @param element
     */
    private void addUniqueConfig(Map<String, Element> config, String key, Element element) {
        if (config.containsKey(key)) {
            throw new IllegalArgumentException(key + "不唯一！");
        } else {
            config.put(key, element);
        }
    }
}
