import { Point, LineString, Polygon } from 'ol/geom';
import { getTransform, transformExtent, get } from 'ol/proj';
import MultiPoint from 'ol/geom/MultiPoint';
import Feature from 'ol/Feature';
import { boundingExtent } from 'ol/extent';
import { Fill, Text, Style, Stroke, RegularShape, Circle as CircleStyle } from 'ol/style';

var lastCtrls = [];

var magenta = 'rgba(255,0,255, 0.7)';
var magentaFill = new Fill({ color: magenta });
var transparentFill = new Fill({color:'rgba(255,0,255, 0.01'});

var labelStyle = new Text({
    font: 'bold 40px san-serif',
    fill: magentaFill,
    offsetX: 30,
    offsetY: -30,
    textAlign: 'start',
    maxZoom: 10,
    rotateWithView: false 
});

var controlStyle = new Style({
    image: new CircleStyle({
        radius: 25,
        stroke: new Stroke({
            color: magenta,
            width: 5
        }),
        fill: transparentFill,
        text: labelStyle
    }),
});

var lineStyle = new Style({
    stroke: new Stroke({
        color: magenta,
        width: 5,
    }),
});

var finishStyle =  [
    new Style({
        image: new CircleStyle({
            radius: 21,
            stroke: new Stroke({
                color: magenta,
                width: 4
            }),
            fill: transparentFill,
            text: labelStyle
        }),
    }),
    new Style({
        image: new CircleStyle({
            radius: 27,
            stroke: new Stroke({
                color: magenta,
                width: 4
            }),
            fill: transparentFill,
            text: labelStyle
        }),
    })
]

var controlStyleFunction = function (feature) {
    var style = controlStyle
    style.setText(labelStyle);
    style.getText().setText(feature.get("number"));
    return style;
};

export function paintCourse(ctrls, courseSource) {
    courseSource.clear();
    lastCtrls = ctrls;

    if( ctrls.length > 1) {  //have at least got start and finish
        var angle = getAngle(ctrls[0], ctrls[1]);
        drawStart(courseSource, ctrls[0].lat, ctrls[0].lon, angle);
        const finish = ctrls[ctrls.length - 1];
        drawFinish(courseSource, finish.lat, finish.lon);
    
        for (let index = 1; index < ctrls.length - 1; index++) {
            const ctrl = ctrls[index];
            const prev = ctrls[index -1];
            drawControl(courseSource, ctrl.lat, ctrl.lon, ctrl.number);
            if( ctrl.number.indexOf("/") < 0  && prev.number.indexOf("/") < 0 ) {
                drawLine(courseSource, prev.lat, prev.lon, ctrl.lat, ctrl.lon);
            }
        };

        const last = ctrls[ctrls.length - 2];
        drawLine(courseSource, last.lat, last.lon, finish.lat, finish.lon);
    }
}

export function redrawCourse(courseSource) {
    paintCourse(lastCtrls, courseSource);
}

function getAngle(first, second) {
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var a = transform([parseFloat(first.lon), parseFloat(first.lat)]);
    var b = transform([parseFloat(second.lon), parseFloat(second.lat)]);
    var angle = Math.atan2((b[0] - a[0]), (b[1] - a[1]));
    if (angle < 0) {
        angle += (2 * Math.PI);
    }
    return angle;
}

export function zoomToLeg(view, size, leg) {

    var angle = getAngle(leg[0], leg[1]);

    //console.log(angle + ", " + view.getRotation())
    view.setRotation(-angle);

    var extent = boundingExtent([[leg[0].lon, leg[0].lat], [leg[1].lon, leg[1].lat]]);
    extent = transformExtent(extent, get('EPSG:4326'), get('EPSG:3857'));

    view.fit(extent, { size: size, maxZoom: 20, padding: [50, 50, 50, 50] })

}

function drawStart(courseSource, lat, lon, angle) {
    // we need to transform the geometries into the view's projection
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var coord = transform([parseFloat(lon), parseFloat(lat)]);
    var radius = 26 * document.streetoMap.getView().getResolution();

    var degrees120 = 2 * (Math.PI / 3);
    var width = radius;
    var x = coord[0];
    var y = coord[1];

    var x0 = x + width * Math.sin(-angle);
    var y0 = y - width * Math.cos(-angle);
    var x1 = x + (width * Math.sin(-angle + degrees120));
    var y1 = y - (width * Math.cos(-angle + degrees120));
    var x2 = x + (width * Math.sin(-angle - degrees120));
    var y2 = y - (width * Math.cos(-angle - degrees120));

    var pt1 = [x0, y0];
    var pt2 = [x1, y1];
    var pt3 = [x2, y2];
    var ring= [pt1,pt2,pt3,pt1];
    
    var feature = new Feature("Start");
    feature.setStyle(lineStyle)
    var line = new LineString(ring);
    line.rotateWithView;
    line.rotate(degrees120/2, coord);
    feature.setGeometry(line);
    courseSource.addFeature(feature);
}

function drawFinish(courseSource, lat, lon) {
    // we need to transform the geometries into the view's projection
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var feature = new Feature();
    feature.setStyle(finishStyle);
    var coordinate = transform([parseFloat(lon), parseFloat(lat)]);
    var geometry = new Point(coordinate);
    feature.setGeometry(geometry);
    courseSource.addFeature(feature);

}

function drawControl(courseSource, lat, lon, number) {
    // we need to transform the geometries into the view's projection
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var feature = new Feature({ number: number });
    feature.setStyle(controlStyleFunction);
    var coordinate = transform([parseFloat(lon), parseFloat(lat)]);
    var geometry = new Point(coordinate);
    feature.setGeometry(geometry);
    courseSource.addFeature(feature);
}

function drawLine(courseSource, aLat, aLon, bLat, bLon) {
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var feature = new Feature("line");
    feature.setStyle(lineStyle);
    var a = transform([parseFloat(aLon), parseFloat(aLat)]);
    var b = transform([parseFloat(bLon), parseFloat(bLat)]);

    var resolution = document.streetoMap.getView().getResolution();

    var distance = Math.sqrt( Math.pow(b[0] - a[0], 2) + Math.pow(b[1] - a[1], 2));
    if( distance > 60 * resolution) {
        var angle = Math.atan2((b[0] - a[0]), (b[1] - a[1]));
        if (angle < 0) {
            angle += (2 * Math.PI);
        }

        //work out 29 px at current zoom
        var offset = 29 * resolution;
        
        var deltaX = offset * Math.cos(angle);
        var deltaY = offset * Math.sin(angle);

        a[1] += deltaX, a[0] += deltaY, b[1] -= deltaX, b[0] -= deltaY;

        var line = new LineString([a, b]);
        feature.setGeometry(line);
        courseSource.addFeature(feature);
    }
}

export function handleTranslate(e, courseSource) {
    var transform = getTransform('EPSG:3857',   'EPSG:4326');
    let num = e.features.getArray()[0].values_.number;
    let coords = e.coordinate;
    lastCtrls.forEach(ctrl => {

        if(ctrl.number == num) {
            let a = transform(coords);
            ctrl.lat = a[1];
            ctrl.lon = a[0];
        }
    });
    redrawCourse(courseSource);
};


