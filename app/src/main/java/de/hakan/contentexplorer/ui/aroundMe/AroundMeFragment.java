package de.hakan.contentexplorer.ui.aroundMe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import de.hakan.contentexplorer.R;

public class AroundMeFragment extends Fragment {

    private ListView listView;
    private final ArrayList<String> itemNames = new ArrayList<>();
    private final ArrayList<Integer> imgIds = new ArrayList<>();
    private ArrayList<String> poiArrayList = new ArrayList<>();
    private Location currentLocation;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private LocationManager locationManager;

    private Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_around_me, container, false);

        listView = root.findViewById(R.id.list_view);

        if (getActivity() != null) {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            Toast.makeText(getContext(), "Please wait..", Toast.LENGTH_LONG).show();

            getCurrentLocation();

            handler = new Handler(Looper.getMainLooper());
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
            Toast.makeText(getContext(), "Refreshing your POIs...", Toast.LENGTH_LONG).show();

        }, null);

    }

    private void updateLocationInfo() {

        if (currentLocation != null) {

            // Get 10 POIs and add to ListView
            findNearbyPlaces(currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    getString(R.string.google_maps_key));

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

    public void findNearbyPlaces(double latitude, double longitude, String apiKey) {

        new Thread(() -> {
            try {
                URL url = new URL("https://places.googleapis.com/v1/places:searchNearby");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-Goog-Api-Key", apiKey);
                conn.setRequestProperty("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.types");

                @SuppressLint("DefaultLocale") String requestBody = String.format("{\"maxResultCount\": 10," +
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

                poiArrayList = new ArrayList<>(parseJSON(response.toString()));

                handler.post(() -> updateListView(poiArrayList));

                // Request to OpenAI
                String userProfileText = "You are my personal assistant. " +
                        "I only like meat. No vegan. " +
                        "I don't drink alcohol. No Cocktails. I eat fast food every Monday.\n" +
                        "Respond recommendations only in format name: ..., address: ... (line break)" +
                        " with max 3 recommendations. ";

                StringBuilder poisNearMeText = new StringBuilder();
                poisNearMeText.append("There's near me: ");

                if (!poiArrayList.isEmpty()) {

                    for (String poiItem : poiArrayList) {
                        poisNearMeText.append(poiItem).append(", ");
                    }
                }

                sendRequestToOpenAI(userProfileText, poisNearMeText.toString(), "Where to eat near me?");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateListView(ArrayList<String> poiArrayList) {

        itemNames.clear();
        imgIds.clear();

        for (String item : poiArrayList) {

            itemNames.add(item);
            imgIds.add(R.drawable.ic_dashboard_black_24dp);
        }

        initAdapter();
    }

    private void GetChatGPTAnswerForUI(ArrayList<String> poiArrayList) {

        for (String item : poiArrayList) {

            //itemNames.add(item);
            showPopup(getContext(), item);

            // To open Google Maps with an address
            // openMaps(getContext(), "123 Main Street, City, Country");
            //imgIds.add(R.drawable.ic_menu_gallery);
        }

        //initAdapter();
    }

    private void sendRequestToOpenAI(String userProfileText, String poiListText, String message) {

        new Thread(() -> {
            try {
                String apiKey = getString(R.string.open_ai_key);
                String url = "https://api.openai.com/v1/chat/completions";
                String model = "gpt-3.5-turbo";

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);

                JSONArray messagesArray = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", userProfileText + poiListText);

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", message);

                messagesArray.put(systemMessage);
                messagesArray.put(userMessage);

                requestBody.put("messages", messagesArray);
                System.out.println(requestBody);

                // Send Request
                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                writer.close();

                // Response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseBody = response.toString();

                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> jsonMap = gson.fromJson(responseBody, type);

                List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonMap.get("choices");

                String content = null;
                if (choices != null && !choices.isEmpty()) {

                    for (Map<String, Object> choice : choices) {

                        Map<String, Object> message1 = (Map<String, Object>) choice.get("message");

                        if (message1 != null) {
                            content = (String) message1.get("content");
                            if (content != null) {
                                System.out.println("Chatbot: " + content);
                            }

                        }
                    }
                }

                if (content != null) {

                    ArrayList<String> poiArrayListTemp = new ArrayList<>();
                    poiArrayListTemp.add("Recommendations: \n\n" + content);

                    // Send the answer from ChatGPT API to ListView
                    handler.post(() -> GetChatGPTAnswerForUI(poiArrayListTemp));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
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

                // Format output from Google Places API (New)
                poiItems.add("POI Name: " + displayName + "\n" + "Address: " + formattedAddress + "\n" + "POI Type: " + types);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return poiItems;
    }

    public static void showPopup(Context context, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();

        alert.show();
    }

    public static void openMaps(Context context, String address) {

        // Erstellen der URI für die Google Maps mit der Adresse
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));

        // Erstellen des Intents für die Google Maps mit der URI
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Überprüfen, ob die Google Maps App auf dem Gerät installiert ist
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            // Öffnen der Google Maps App
            context.startActivity(mapIntent);
        } else {
            // Öffnen der Google Maps Webseite im Browser als Fallback
            Uri webpage = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            context.startActivity(webIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
