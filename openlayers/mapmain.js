import { paintCourse, zoomToLeg, handleTranslate, redrawCourse } from "./coursepainter";
import { paintRoute } from "./routepainter";

import 'ol/ol.css';
import Map from 'ol/Map';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import OSM from 'ol/source/OSM';
import BingMaps from 'ol/source/BingMaps';
import { Tile as TileLayer, Vector as VectorLayer } from 'ol/layer';
import { ScaleLine } from 'ol/control';
import {
  Select,
  Translate,
  DragRotateAndZoom,
  defaults as defaultInteractions,
} from 'ol/interaction';
import { getTransform } from 'ol/proj';
import { boundingExtent } from "ol/extent";

import LayerSwitcher from 'ol-layerswitcher';
import { MapBrowserEvent } from "ol";



var raster = new TileLayer({
  title: "Open Street Map",
  type: 'base',
  source: new OSM(),
});

var bing = new TileLayer({
  title: "Bing Aerial",
  type: 'base',
  visible: false,
  preload: Infinity,
  source: new BingMaps({
    key: '5zENQxb7qC96LjzF3qOz~3EmzuLTXMkKXJJzlb35FIg~Au_aUoi9kHy2Cqp4zX3CBlsyMsLjOnFidJ1c65pzOqUdM-yO5NdZ2-3HHZXyoF81',
    imagerySet: "Aerial",
    // use maxZoom 19 to see stretched tiles instead of the BingMaps
    // "no photos at this zoom level" tiles
    maxZoom: 19
  }),
});


var courseSource = new VectorSource();

var course = new VectorLayer({
  source: courseSource,
});

var routeSource = new VectorSource();
var routeLayer = new VectorLayer({
  source: routeSource,
});

var routeChoiceSource = new VectorSource();
var routeChoiceLayer = new VectorLayer({
  source: routeChoiceSource,
});

var view = new View({
  center: [-7916041.528716288, 5228379.045749711],
  zoom: 12,
});

var select = new Select({style:null});

var translate = new Translate({
  features: select.getFeatures(),
});

var map = new Map({
  layers: [bing, raster, course, routeLayer, routeChoiceLayer],
  interactions: defaultInteractions().extend([new DragRotateAndZoom(), select, translate]),
  target: document.getElementById('map'),
  view: view
});

var scaleline = new ScaleLine({ bar: true });
map.addControl(scaleline);

var layerSwitcher = new LayerSwitcher();
map.addControl(layerSwitcher);

document.streetoMap = map;
document.courseSource = courseSource;
document.routeSource = routeSource;
document.routeChoiceSource = routeChoiceSource;

map.clearCourse = function () {
  courseSource.clear();
  routeSource.clear();
  routeChoiceSource.clear();
}

map.drawCourse = paintCourse;
map.drawRoute = paintRoute;

map.zoomToFitCourse = function () {
  var extent = courseSource.getExtent();
  view.fit(extent, { size: map.getSize(), maxZoom: 16, padding: [50, 50, 50, 50] })

}

map.zoomToFitBounds = function (a, b) {
  var transform = getTransform('EPSG:4326', 'EPSG:3857');
  var ac = transform([a.lon, a.lat]);
  var bc = transform([b.lon, b.lat]);
  //[minx, miny, maxx, maxy]
  var extent = boundingExtent([ac, bc]);
  view.fit(extent, { size: map.getSize() });
  view.extent = extent
}

map.zoomToFitLeg = function (leg) {
  zoomToLeg(view, map.getSize(), leg);
}

map.zoomToLatLon = function (point, level) {
  var transform = getTransform('EPSG:4326', 'EPSG:3857');
  var lonlat = transform([point.lon, point.lat]);
  map.getView().setRotation(0);
  map.getView().setCenter(lonlat);
  map.getView().setZoom(level);
}

view.on('change:resolution', function () { redrawCourse(courseSource) });

map.currCoord = [];

// We track coordinate change each time the mouse is moved
map.on('pointermove', function (evt) {
  map.currCoord = evt.coordinate;
});

map.getMouseCoords = function () {
  var transform = getTransform('EPSG:3857', 'EPSG:4326');
  return transform(map.currCoord);
}

map.getControlUnderMouse = function() {
   var pixel = map.getPixelFromCoordinate(map.currCoord);
   map.forEachFeatureAtPixel(pixel, f => {
     if(f.values != null && f.values.number != null) {
       return f.values.number;
     }
   });
   return null;
 }



var reportControlMoved = function(e){
  var transform = getTransform('EPSG:3857', 'EPSG:4326');
  var lonlat = transform(e.coordinate);
  var number = e.features.getArray()[0].values_.number;
  theJavaFunction.controlMoved( number, lonlat[1], lonlat[0] );
};

 translate.on('translating', function(e){handleTranslate(e, courseSource);});
 translate.on('translateend',reportControlMoved );
 map.redraw = redrawCourse;



