/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.streeto.kml;

import org.jetbrains.annotations.NotNull;
import org.streeto.ControlSite;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KmlWriter {

    private String getControlKML(ControlSite control) {
        return getKML(control);
    }

    private String getKML(ControlSite control) {
        return "<Placemark>" +
               String.format("        <name>%s</name>", control.getNumber()) +
               String.format("        <description>%s</description>", control.getDescription()) +
               "        <styleUrl>#startfinish</styleUrl>" +
               "        <Point>" +
               "            <gx:drawOrder>1</gx:drawOrder>" +
               String.format("            <coordinates>%f,%f,0</coordinates>",
                       control.getLocation().lon, control.getLocation().lat) +
               "        </Point>" +
               "</Placemark>";
    }


    public Stream<ControlSite> parseStream(InputStream xmlFile) throws Exception {
        var dbFactory = DocumentBuilderFactory.newInstance();
        var dBuilder = dbFactory.newDocumentBuilder();
        var doc = dBuilder.parse(new InputSource(xmlFile));
        var xpFactory = XPathFactory.newInstance();
        var xPath = xpFactory.newXPath();

        //<item type="T1" count="1">Value1</item>
        var xpath = "//Placemark";

        var markers = (NodeList) xPath.evaluate(xpath, doc, XPathConstants.NODESET);
        return mapIndexed(i -> (Element) markers.item(i), markers.getLength())
                .map(node -> {
                    var name = node.getElementsByTagName("name").item(0).getTextContent();
                    var description = node.getElementsByTagName("description").item(0).getTextContent();
                    var point = (Element) node.getElementsByTagName("Point").item(0);
                    var coords = point.getElementsByTagName("coordinates").item(0).getTextContent();
                    var x = coords.split(",");
                    var ret = new ControlSite(Double.parseDouble(x[1]), Double.parseDouble(x[0]), description);
                    ret.setNumber(name);
                    return ret;
                });
    }

    @NotNull
    private Stream<Element> mapIndexed(IntFunction<Element> function, int length) {
        return IntStream.range(0, length).mapToObj(function);
    }


    public String generate(List<ControlSite> controls, String mapID) {
        var kml = new StringBuilder();

        var kmlintro = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                       "<Document>\n<name>oom_" + mapID + "_controls</name>\n<open>1</open>\n" +
                       "<Style id=\"startfinish\"><IconStyle><color>ffff00ff</color><scale>1.8</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-stars.png</href></Icon><hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/></IconStyle><LabelStyle><color>ffff00ff</color></LabelStyle><BalloonStyle></BalloonStyle><ListStyle></ListStyle></Style>\n" +
                       "<Style id=\"control\"><IconStyle><color>ffff00ff</color><scale>1.0</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-blank.png</href></Icon><hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/></IconStyle><LabelStyle><color>ffff00ff</color><scale>1.0</scale></LabelStyle><BalloonStyle></BalloonStyle><ListStyle></ListStyle></Style>\n";
        var kmlheader = "<Folder>\n<name>Controls</name>\n<open>1</open>\n\n";
        var kmlfooter = "</Folder>\n</Document>\n</kml>";

        kml.append(kmlintro);
        kml.append(kmlheader);

        var body = controls.stream()
                .map(this::getControlKML).collect(Collectors.joining("\n"));

        kml.append(body);
        kml.append(kmlfooter);

        return kml.toString();
    }
}