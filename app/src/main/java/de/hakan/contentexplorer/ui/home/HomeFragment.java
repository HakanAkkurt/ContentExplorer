package de.hakan.contentexplorer.ui.home;

import android.Manifest;
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

import java.util.ArrayList;
import java.util.List;

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

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getLocation();

        return root;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        listView.setOnItemClickListener((parent, view, position, id) -> getLocation());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation();
            } else {
                Toast.makeText(getActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
