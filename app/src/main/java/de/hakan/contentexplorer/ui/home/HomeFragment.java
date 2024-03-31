package de.hakan.contentexplorer.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.hakan.contentexplorer.R;

public class HomeFragment extends Fragment {

    private ListView listView;
    private final List<String> itemNames = new ArrayList<>();
    private final List<Integer> imgIds = new ArrayList<>();
    private Location currentLocation;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private LocationManager locationManager;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        listView = root.findViewById(R.id.list_view);

        if (getActivity() != null) {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            getCurrentLocation();
        }

        return root;
    }

    private void getCurrentLocation() {

        if (getActivity() == null)
            return;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

            return;
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, location -> {
            currentLocation = location;

            updateLocationInfo();

        }, null);

    }

    private void updateLocationInfo() {

        if (currentLocation != null) {

            Thread findNearbyPlacesNewAPICallThread = new Thread(() -> {

                try {

                    // Get 10 POIs
                    ArrayList<String> poiArrayList = new ArrayList<>(
                            findNearbyPlaces(currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            getString(R.string.google_maps_key)));

                    if (!poiArrayList.isEmpty()) {
                        for (String poiItem : poiArrayList) {
                            System.out.println(poiItem);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            findNearbyPlacesNewAPICallThread.start();

            itemNames.clear();
            imgIds.clear();
            itemNames.add("Latitude: " + currentLocation.getLatitude() + "\nLongitude: " + currentLocation.getLongitude());
            imgIds.add(R.drawable.ic_menu_gallery);

            initAdapter();
        }
    }

    private void initAdapter() {
        CustomAdapter adapter = new CustomAdapter(getActivity(), itemNames, imgIds);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> getCurrentLocation());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            } else {
                Toast.makeText(getActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<String> findNearbyPlaces(double latitude, double longitude, String apiKey) {

        ArrayList<String> poiItems = new ArrayList<>();
        try {

            URL url = new URL("https://places.googleapis.com/v1/places:searchNearby");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Goog-Api-Key", apiKey);
            conn.setRequestProperty("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.types");

            @SuppressLint("DefaultLocale")
            String requestBody = String.format("{\"maxResultCount\": 10," +
                            " \"includedTypes\": ['restaurant']," +
                            " \"locationRestriction\": {\"circle\": {\"center\": {\"latitude\": %f, \"longitude\": %f}," +
                            " \"radius\": 500.0}}}",
                    latitude, longitude);

            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.getBytes());

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            poiItems.addAll(parseJSON(response.toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return poiItems;
    }

    private ArrayList<String> parseJSON(String response) {

        ArrayList<String> poiItems = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);

            JSONArray placesArray = jsonResponse.getJSONArray("places");

            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject placeObject = placesArray.getJSONObject(i);

                String nameObjectJSON = placeObject.getString("displayName");
                JSONObject displayNameJSONObject = new JSONObject(nameObjectJSON);
                String displayName = displayNameJSONObject.getString("text");

                String formattedAddress = placeObject.getString("formattedAddress");
                String types = placeObject.getString("types");

                poiItems.add(displayName + "\n" + formattedAddress + "\n" + types);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return poiItems;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
