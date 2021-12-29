import { LineString } from 'ol/geom';
import { getTransform } from 'ol/proj';
import Feature from 'ol/Feature';
import { Style, Stroke } from 'ol/style';

export function paintRoute(routepoints, routeSource, colour) {
    var transform = getTransform('EPSG:4326', 'EPSG:3857');
    var feature = new Feature("line");
    var lineStyle = new Style({
        stroke: new Stroke({
            color: colour,
            width: 5,
        }),     
      });
      
    feature.setStyle(lineStyle);

    var route = routepoints.map(pt => transform([parseFloat(pt.lon), parseFloat(pt.lat)]));

    var line = new LineString(route);
    feature.setGeometry(line);
    routeSource.addFeature(feature);
}

