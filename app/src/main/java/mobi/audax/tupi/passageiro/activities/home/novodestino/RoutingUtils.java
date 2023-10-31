package mobi.audax.tupi.passageiro.activities.home.novodestino;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.DashPattern;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapView;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.PedestrianOptions;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.Waypoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.dto.ViagemMotorista;

public class RoutingUtils {

    private static final String TAG = RoutingUtils.class.getName();
    private static final float POLYLINE_STROKE_WIDTH = 9f;

    private final Context context;
    private final MapView mapView;
    private final List<MapMarker> mapMarkerList = new ArrayList<>();
    private final List<MapPolyline> mapPolylines = new ArrayList<>();
    private final RoutingEngine routingEngine;

    public RoutingUtils(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
    }

    //add a rota ao mapa
    public void addRoute(@NonNull ViagemMotorista viagemMotorista) {
        clearMap();

        var waypoints = new ArrayList<Waypoint>();
        for (var listCoordenada : viagemMotorista.getListCoordenadas()) {
            var waypoint1 = new Waypoint(new GeoCoordinates(listCoordenada.getLatitude(), listCoordenada.getLongitude()));
            waypoints.add(waypoint1);
        }

        routingEngine.calculateRoute(waypoints, new CarOptions(), (routingError, routes) -> {
            if (routingError == null) {
                var route = routes.get(0);
                showRouteOnMap(route, viagemMotorista);
                for (var waypoint : waypoints) {
                    if (waypoints.get(0).equals(waypoint)) {
                        addCircleMapMarker(waypoint.coordinates, R.drawable.ic_partida);
                    } else if (waypoints.get(waypoints.size() - 1).equals(waypoint)) {
                        addCircleMapMarker(waypoint.coordinates, R.drawable.ic_destino);
                        Log.e(TAG, "addWaypoints:+route.getLengthInMeters() " + route.getLengthInMeters());
                    } else {
                        addCircleMapMarker(waypoint.coordinates, R.drawable.ic_motorista_no_mapa_passageiro);
                    }
                }
            } else {
                //Util.alert(context, context.getString(R.string.erro), "Erro calculando a rota:", routingError.toString() + viagemMotorista.getListCoordenadas().size());
            }
        });
    }

    public void addRoutePassageiro(@NonNull Viagem viagem, double latitude, double longitude, TextView textView, TextView textView2) {
        var startWaypoint = new Waypoint(new GeoCoordinates(latitude, longitude));
        var destinationWaypoint = new Waypoint(new GeoCoordinates(viagem.getLatitudeEmbarque(), viagem.getLongitudeEmbarque()));
        var waypoints = Arrays.asList(startWaypoint, destinationWaypoint);

        routingEngine.calculateRoute(waypoints, new PedestrianOptions(), (routingError, routes) -> {
            if (routingError == null) {
                var route = routes.get(0);
                showRoutePassageiroOnMap(route, viagem);
                showRouteDetails(route, textView, textView2);
            } else {
                //Util.alert(context, context.getString(R.string.erro), "Erro calculando a rota:", routingError.toString());
            }
        });
    }

    public void addRoutePassageiroDetails(@NonNull Viagem viagem, double latitude, double longitude, TextView textView, TextView textView2) {
        var startWaypoint = new Waypoint(new GeoCoordinates(latitude, longitude));
        var destinationWaypoint = new Waypoint(new GeoCoordinates(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque()));
        var waypoints = Arrays.asList(startWaypoint, destinationWaypoint);

        routingEngine.calculateRoute(waypoints, new CarOptions(), (routingError, routes) -> {
            if (routingError == null) {
                var route = routes.get(0);
                showRoutePassageiroDetails(route, textView, textView2);
            } else {
                //Util.alert(context, context.getString(R.string.erro), "Erro calculando a rota:", routingError.toString());
            }
        });
    }

    private void showRouteOnMap(@NonNull Route route, @NonNull ViagemMotorista viagemMotorista) {
        // Show route as polyline.
        var routeGeoPolyline = route.getGeometry();
        var routeMapPolyline = new MapPolyline(routeGeoPolyline, POLYLINE_STROKE_WIDTH, Color.valueOf(ContextCompat.getColor(context, R.color.primary)));
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        // Draw a circle to indicate starting point and destination.
        addCircleMapMarker(new GeoCoordinates(viagemMotorista.getCoordenadaPartida().getLatitude(), viagemMotorista.getCoordenadaPartida().getLongitude()), R.drawable.ic_partida);
        addCircleMapMarker(new GeoCoordinates(viagemMotorista.getCoordenadaDestino().getLatitude(), viagemMotorista.getCoordenadaDestino().getLongitude()), R.drawable.ic_destino);

    }

    private void showRoutePassageiroOnMap(@NonNull Route route, @NonNull Viagem viagem) {
        // Show route as polyline.
        var routeGeoPolyline = route.getGeometry();
        var routeMapPolyline = new MapPolyline(routeGeoPolyline, POLYLINE_STROKE_WIDTH, Color.valueOf(ContextCompat.getColor(context, R.color.red)));
        var dashPattern = new DashPattern(2, 10);
        routeMapPolyline.setDashPattern(dashPattern);
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        addCircleMapMarker(new GeoCoordinates(viagem.getLatitudeEmbarque(), viagem.getLongitudeEmbarque()), R.drawable.icone_gps_01);

    }

    private void showRouteDetails(@NonNull Route route, TextView view, TextView textView2) {
        //tempo de deslocamento
        long estimatedTravelTimeInSeconds = route.getDurationInSeconds();
        if (route.getLengthInMeters() > 1000) {
            double km = route.getLengthInMeters() / 1000.0;
            String a = new DecimalFormat("#.0").format(km);
            Log.e(TAG, "showRouteDetails: route.getLengthInMeters()" + route.getLengthInMeters());
            Log.e(TAG, "showRouteDetails: route.getLengthInMeters()>1000" + km);
            view.setText(a + " Km");
        } else {
            String a = new DecimalFormat("###").format(route.getLengthInMeters());
            Log.e(TAG, "showRouteDetails: route.getLengthInMeters() menor que 1000" + a);
            view.setText(a + " Metros");
        }

        if (estimatedTravelTimeInSeconds > 60) {
            double segundosEmMinutos = estimatedTravelTimeInSeconds / 60.0;
            String a = new DecimalFormat("0").format(segundosEmMinutos);
            textView2.setText(a + " minutos");
            Log.e(TAG, "showRoutePassageiroDetails: minutos " + estimatedTravelTimeInSeconds);
        } else {
            textView2.setText(estimatedTravelTimeInSeconds + " segundos");
            Log.e(TAG, "showRoutePassageiroDetails: segundos " + estimatedTravelTimeInSeconds);
        }
    }

    private void showRoutePassageiroDetails(@NonNull Route route, TextView view, TextView view2) {
        //tempo de rota
        long estimatedTravelTimeInSeconds = route.getDurationInSeconds();

        //distancia da rota
        if (route.getLengthInMeters() > 1000) {
            double km = route.getLengthInMeters() / 1000.0;
            String a = new DecimalFormat("#.0").format(km);
            view.setText("Distância: " + a + " Km");
        } else {
            String a = new DecimalFormat("###").format(route.getLengthInMeters());
            view.setText("Distância: " + a + " Metros");
        }
        if (estimatedTravelTimeInSeconds > 60) {
            double segundosEmMinutos = estimatedTravelTimeInSeconds / 60.0;
            String a = new DecimalFormat("0").format(segundosEmMinutos);
            view2.setText("Tempo: " + a + " minutos");
        } else {
            view2.setText("Tempo: " + estimatedTravelTimeInSeconds + " segundos");
        }

    }

    public void clearMap() {
        clearWaypointMapMarker();
        clearRoute();
    }

    private void clearWaypointMapMarker() {
        for (var mapMarker : mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerList.clear();
    }

    private void clearRoute() {
        for (var mapPolyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        }
        mapPolylines.clear();
    }

    public void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        var mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        if (resourceId == R.drawable.icone_gps_01) {
            var anchor = new Anchor2D(1.0f, 0.5f);
            var mapMarker = new MapMarker(geoCoordinates, mapImage, anchor);
            mapView.getMapScene().addMapMarker(mapMarker);
            mapMarkerList.add(mapMarker);
        } else {
            var mapMarker = new MapMarker(geoCoordinates, mapImage);
            mapView.getMapScene().addMapMarker(mapMarker);
            mapMarkerList.add(mapMarker);
        }
    }

}