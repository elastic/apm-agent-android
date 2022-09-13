package co.elastic.apm.compile.tools.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PomReader {
    private final Document document;

    public PomReader(File pomFile) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            document = documentBuilderFactory.newDocumentBuilder().parse(pomFile);
            document.getDocumentElement().normalize();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLicenseName() {
        NodeList licenses = document.getElementsByTagName("licenses");
        if (licenses.getLength() == 0) {
            return null;
        }

        Element license = (Element) licenses.item(0);
        String name = extractItemValue(license, "name");
        return LicensesIdsMatcher.findId(name);
    }

    private String extractItemValue(Element license, String itemName) {
        return license.getElementsByTagName(itemName).item(0).getTextContent();
    }
}
