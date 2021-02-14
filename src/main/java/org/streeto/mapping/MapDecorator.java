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

import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.streeto.ControlSite;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.CellText;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.streeto.utils.CollectionHelpers.last;
import static org.streeto.utils.CollectionHelpers.windowed;
import static org.streeto.utils.DistUtils.dist;


public class MapDecorator {

    private final Function<Float, Float> mm2pt = num -> num / 25.4f * 72;
    private final PDDocument doc;
    private final PDPageContentStream content;
    private final PDPageContentStream content2;
    private final List<float[]> offsetsInPts;
    private final List<ControlSite> controls;

    public MapDecorator(InputStream pdfStream, List<ControlSite> controls, MapBox box, Coordinate centre) throws IOException{
        this.controls = controls;
        this.doc = PDDocument.load(pdfStream);

        PDPage page = doc.getDocumentCatalog().getPages().get(0);
        var width = page.getMediaBox().getWidth();
        var height = page.getMediaBox().getHeight();

        var centrePage = new float[]{width / 2, height / 2};

        // the centre is the thing in the middle
        var mapCentre = new GHPoint(centre.y, centre.x); // this is what we told the tiler
        this.offsetsInPts = getControlOffsets(controls, mapCentre, box, centrePage);

        // fade the lines a bit so that you can see the map through them
        var alpha = 0.55f;
        var bold = 0.75f;
        var graphicsState = new PDExtendedGraphicsState();
        graphicsState.setStrokingAlphaConstant(alpha);
        graphicsState.setNonStrokingAlphaConstant(bold);

        this.content = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

        // Create a new font object selecting one of the PDF base fonts
        PDFont font = PDType1Font.HELVETICA_BOLD;
        content.setFont(font, 20.0f);
        content.setNonStrokingColor(Color.MAGENTA);
        content.setGraphicsStateParameters(graphicsState);
        content.setLineCapStyle(0);

        content.setLineWidth(2.0f);
        content.setStrokingColor(Color.WHITE);

        var page2 = new PDPage(PDRectangle.A4);
        doc.addPage(page2);

        this.content2 = new PDPageContentStream(doc, page2, PDPageContentStream.AppendMode.APPEND, true, true);
    }

    public PDDocument decorate() throws IOException {
        drawCourse();
        drawControlSheet();
        return doc;
    }


    private void drawCourse() throws IOException {
        content.setLineWidth(2.0f);
        content.setStrokingColor(Color.WHITE);

        drawStart();
        drawFinish();
        drawControls();

        content.setLineWidth(1.5f);
        content.setStrokingColor(Color.MAGENTA);

        drawStart();
        drawFinish();
        drawControls();

        content.close();
    }

    private void drawControlSheet() throws IOException {
        var tableBuilder = Table.builder()
                .addColumnsOfWidth(60.0f, 100.0f)
                .addRow(Row.builder()
                        .add(CellText.builder().text("Control").borderWidth(1.0f).backgroundColor(Color.LIGHT_GRAY).build())
                        .add(CellText.builder().text("Description").borderWidth(1.0f).backgroundColor(Color.LIGHT_GRAY).build())
                        .build());

        controls.forEach(control ->
                tableBuilder.addRow(Row.builder()
                        .add(CellText.builder().text(control.getNumber()).borderWidth(1.0f).horizontalAlignment(HorizontalAlignment.RIGHT).build())
                        .add(CellText.builder().text(control.getDescription()).borderWidth(1.0f).build())
                        .build())
        );
        var table = tableBuilder.build();
        var page2Box = doc.getPage(1).getMediaBox();
        var tableDrawer = TableDrawer.builder()
                .contentStream(content2)
                .startX(20f)
                .startY(page2Box.getUpperRightY() - 20f)
                .table(table)
                .build();
        tableDrawer.draw();
        content2.close();
    }

    private void drawControls() throws IOException {
        var positions = offsetsInPts.subList(1, offsetsInPts.size() - 1);
        var index = new AtomicInteger(1);
        for (float[] position : positions) {
            drawControl(controls.get(index.getAndIncrement()).getNumber(), position);
        }
        windowed(offsetsInPts, 2).forEach(it -> {
            try {
                drawLine(content, it);
            } catch (IOException e) {
                // intentionally blank
            }
        });
    }

    private void drawStart() throws IOException {
        var pos =  offsetsInPts.subList(0, 2);
        var start = pos.get(0);
        var next = pos.get(1);
        var angle = (float) atan2((next[1] - start[1]), (next[0] - start[0]));
        if (angle < 0) {
            angle += (2.0f * (float) PI);
        }

        float halfPI = (float) PI / 2;
        drawTriangle(start[0], start[1], angle + halfPI);
    }

    private void drawTriangle(float x, float y, float angle) throws IOException {
        var degrees120 = (2 * (float) PI / 3);
        float width = (float) 13.0;

        var x0 = x + width * (float) sin(angle);
        var y0 = y - width * (float) cos(angle);
        var x1 = x + (width * (float) sin(angle + degrees120));
        var y1 = y - (width * (float) cos(angle + degrees120));
        var x2 = x + (width * (float) sin(angle - degrees120));
        var y2 = y - (width * (float) cos(angle - degrees120));

        content.moveTo(x0, y0);
        content.lineTo(x1, y1);
        content.stroke();
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
        content.moveTo(x2, y2);
        content.lineTo(x0, y0);
        content.stroke();
    }

    private void drawFinish() throws IOException {
        var last = last(offsetsInPts);
        drawCircle(last, 12.0f);
        drawCircle(last, 9.5f);
    }

    private void drawControl(String number, float[] position) throws IOException {
        drawCircle(position, 1.0f);
        drawCircle(position, 12.0f);
        drawNumber(number, position);
    }

    private void drawCircle(float[] position, float r) throws IOException {
        content.moveTo(position[0], position[1]);
        var k = 0.552284749831f;
        content.moveTo(position[0] - r, position[1]);
        content.curveTo(position[0] - r, position[1] + k * r, position[0] - k * r, position[1] + r, position[0], position[1] + r);
        content.curveTo(position[0] + k * r, position[1] + r, position[0] + r, position[1] + k * r, position[0] + r, position[1]);
        content.curveTo(position[0] + r, position[1] - k * r, position[0] + k * r, position[1] - r, position[0], position[1] - r);
        content.curveTo(position[0] - k * r, position[1] - r, position[0] - r, position[1] - k * r, position[0] - r, position[1]);
        content.stroke();
    }

    private void drawNumber(String number, float[] position) throws IOException {
        content.beginText();
        content.newLineAtOffset(position[0] + 10.0F, position[1] + 10.0F);
        content.showText(number);
        content.endText();

    }

    private void drawLine(PDPageContentStream content, List<float[]> line) throws IOException {

        var line1 = line.get(1);
        var line0 = line.get(0);
        var angle = (float) atan2((line1[1] - line0[1]), (line1[0] - line0[0]));
        if (angle < 0) {
            angle += (2 * (float) PI);
        }

        var deltaX = 11.8f * (float) cos(angle);
        var deltaY = 11.8f * (float) sin(angle);

        content.moveTo(line0[0] + deltaX, line0[1] + deltaY);
        content.lineTo(line1[0] - deltaX, line1[1] - deltaY);
        content.stroke();
    }

    private List<float[]> getControlOffsets(List<ControlSite> controls, GHPoint mapCentre, MapBox box, float[] centrePage) {
        var offsetsInMetres = controls.stream().map(control -> {
            var distLat = dist(control.getLocation().lat, mapCentre.lon, mapCentre.lat, mapCentre.lon) * ((control.getLocation().lat < mapCentre.lat) ? -1.0 : 1.0);
            var distLon = dist(mapCentre.lat, control.getLocation().lon, mapCentre.lat, mapCentre.lon) * ((control.getLocation().lon < mapCentre.lon) ? -1.0 : 1.0);
            return new float[]{(float) distLon, (float) distLat}; //dists in m from the centre
        });

        return offsetsInMetres.map(p -> {
            var ratio = (float) box.getScale() / 1000.0f;
            var xPt = mm2pt.apply(p[0] / ratio); //lon
            var yPt = mm2pt.apply(p[1] / ratio); //lat
            return new float[]{centrePage[0] + xPt + 0.2f, centrePage[1] + yPt - 1.3f};
        }).collect(Collectors.toList());
    }
}