package co.elastic.otel.android.compilation.tools.utils;

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

    public String getLicenseId() {
        NodeList licenses = document.getElementsByTagName("licenses");
        if (licenses.getLength() == 0) {
            return null;
        }

        Element license = (Element) licenses.item(0);
        String name = extractItemValue(license, "name");
        String id = LicensesIdsMatcher.findId(name);
        if (id == null) {
            throw new RuntimeException("Couldn't find a license id for: " + name + " - it should be added to the 'licenses_ids.txt' file");
        }
        return id;
    }

    private String extractItemValue(Element license, String itemName) {
        return license.getElementsByTagName(itemName).item(0).getTextContent();
    }

    public String getName() {
        return getRootItemValue("name");
    }

    public String getUrl() {
        return getRootItemValue("url");
    }

    private String getRootItemValue(String itemName) {
        NodeList name = document.getElementsByTagName(itemName);
        if (name.getLength() == 0) {
            return null;
        }
        return name.item(0).getTextContent();
    }
}
