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

package org.streeto.mapping;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.jetbrains.annotations.NotNull;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.streeto.ControlSite;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class MapPrinter {

    private enum MapType {
        PDF("pdf"),
        KMZ("kmz");

        private final String key;

        MapType(String key) {
            this.key = key;
        }
    }

    //Create transformation from WS84 to WGS84 Web Mercator
    private final MathTransform wgs84ToWgs84Web;

    private final static String dummyString = "data%5Baction%5D=savemap&data%5Btitle%5D=OpenOrienteeringMap&data%5Brace_instructions%5D" +
                                              "=Race+instructions&data%5Beventdate%5D=&data%5Bclub%5D=&data%5Bstyle%5D=streeto&data%5Bscale%5D" +
                                              "=s10000&data%5Bpapersize%5D=p2970-2100&data%5Bpaperorientation%5D=portrait&data%5Bcentre_lat%5" +
                                              "D=6980976.811425837&data%5Bcentre_lon%5D=-134828.74097699366&data%5Bcentre_wgs84lat%5D=52.989072105477476&data%5B" +
                                              "centre_wgs84lon%5D=-1.2111871875822542&data%5Bcontrols%5D%5B0%5D%5Bid%5D=1&data%5Bcontrols%5D%5B0%5D%5Bnumber%5D" +
                                              "=1&data%5Bcontrols%5D%5B0%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B0%5D%5Bscore%5D=10&data%5Bcontrols%5D%5B0%5D%5Btype%5" +
                                              "D=c_regular&data%5Bcontrols%5D%5B0%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B0%5D%5Blat%5D=6980995.808714605&data%5B" +
                                              "controls%5D%5B0%5D%5Blon%5D=-134369.39101331212&data%5Bcontrols%5D%5B0%5D%5Bwgs84lat%5D=52.989174834420936&data%5Bcontrols" +
                                              "%5D%5B0%5D%5Bwgs84lon%5D=-1.2070607766509056&data%5Bcontrols%5D%5B1%5D%5Bid%5D=2&data%5Bcontrols%5D%5B1%5D%5Bnumber%5D" +
                                              "=2&data%5Bcontrols%5D%5B1%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B1%5D%5Bscore%5D=10&data%5Bcontrols%5D%5B1%5D%5Btype%5D" +
                                              "=c_regular&data%5Bcontrols%5D%5B1%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B1%5D%5Blat%5D=6981589.203398543&data%5B" +
                                              "controls%5D%5B1%5D%5Blon%5D=-134322.5136170591&data%5Bcontrols%5D%5B1%5D%5Bwgs84lat%5D=52.99238352766508&data%5Bcontrols" +
                                              "%5D%5B1%5D%5Bwgs84lon%5D=-1.2066396698355673&data%5Bcontrols%5D%5B2%5D%5Bid%5D=0&data%5Bcontrols%5D%5B2%5D%5Bnumber%5D=" +
                                              "&data%5Bcontrols%5D%5B2%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B2%5D%5Bscore%5D=0&data%5Bcontrols%5D%5B2%5D%5Btype%5D=" +
                                              "c_startfinish&data%5Bcontrols%5D%5B2%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B2%5D%5Blat%5D=6981216.554224269&data%5B" +
                                              "controls%5D%5B2%5D%5Blon%5D=-135425.9052604716&data%5Bcontrols%5D%5B2%5D%5Bwgs84lat%5D=52.990368510683766&data%5Bcontrol" +
                                              "s%5D%5B2%5D%5Bwgs84lon%5D=-1.2165516056120393";
    private final MapBox mapBox;
    private final Coordinate mapCentre;

    public MapPrinter(Envelope envelopeToMap) throws FactoryException {
        //create reference system WGS84 Web Mercator
        CoordinateReferenceSystem wgs84Web = CRS.decode("EPSG:3857", true);
        //create reference system WGS84
        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
        //transform
        wgs84ToWgs84Web = CRS.findMathTransform(wgs84, wgs84Web);
        //the map scale and orientation we are going to print
        mapBox = MapFitter.getForEnvelope(envelopeToMap).orElseThrow();
        // coords of the centre of this map
        mapCentre = envelopeToMap.centre();
    }

    private Geometry convertBack(double lon, double lat) throws TransformException {
        var jtsGf = JTSFactoryFinder.getGeometryFactory();
        var pointInWgs84Web = jtsGf.createPoint(new Coordinate(lon, lat));
        //transform point from WGS84 Web Mercator to WGS84
        return JTS.transform(pointInWgs84Web, wgs84ToWgs84Web);
    }

    public void generateMapAsPdf(String filename, String title, List<ControlSite> controls) throws TransformException, IOException {
        var pdfStream = generateArtifact(MapType.PDF, mapBox, mapCentre, title);
        var decorator = new MapDecorator(pdfStream, controls, mapBox, mapCentre);
        var doc = decorator.decorate();
        doc.save(new File(filename));
        pdfStream.close();
    }

    public void generateMapAsKmz(String filename, String title) throws TransformException, IOException {
        var kmzStream = generateArtifact(MapType.KMZ, mapBox, mapCentre, title);
        var fis = new FileOutputStream(filename);
        fis.write(kmzStream.readAllBytes());
        fis.flush();
        fis.close();
        kmzStream.close();
    }

    private InputStream generateArtifact(MapType mapType, MapBox box, Coordinate mapCentre, String title) throws IOException, TransformException{
        var orientationString =  (box.isLandscape()) ? "0.297,0.21" : "0.21,0.297";

        var centre = convertBack(mapCentre.x, mapCentre.y).getCoordinate();
        var centreLat = Math.round(centre.y);
        var centreLon = Math.round(centre.x);

        var escapedTitle = title.replaceAll(" ", "+");

        var id = requestKey();

        var url = new URL(String.format("https://tiler4.oobrien.com/%s/?style=streeto|" +
            "paper=%s" +
            "|scale=%d" +
            "|centre=%d,%d" +
            "|title=%s" +
            "|club=StreetO" +
            "|id=%s" +
            "|start=" +
            "|crosses=" +
            "|cps=" +
            "|controls=", mapType.key, orientationString, (int)box.getScale(), centreLat, centreLon, escapedTitle, id));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        var bis = new BufferedInputStream(new ByteArrayInputStream(conn.getInputStream().readAllBytes()));
        conn.disconnect();

        return bis;
    }

    private static String requestKey() {
        var dummyParameters = dummyString.getBytes(StandardCharsets.UTF_8);

        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL("https://oomap.co.uk/save.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(dummyParameters.length));
            conn.setRequestProperty("Content-Language", "en-US");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.getOutputStream().write(dummyParameters);
            conn.getOutputStream().flush();
            conn.getOutputStream().close();

             //var rd = BufferedReader(InputStreamReader(inputStream))
            var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            reader.lines().forEach(response::append);
            reader.close();
            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractKeyFromResponse(response);
     }

    @NotNull
    private static String extractKeyFromResponse(StringBuilder response) {
        var message = Arrays.stream(response.toString().split(",")).filter(it -> it.startsWith("\"message\"")).findFirst().orElseThrow();
        var code = message.split(":")[1].strip();
        code = code.substring(1, code.length() - 2);
        return code;
    }
}