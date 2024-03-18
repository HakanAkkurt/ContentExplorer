package de.hakan.contentexplorer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import de.hakan.contentexplorer.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CustomAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ListView listView = binding.listView;

        // Convert ArrayLists to Arrays
        String[] itemNamesArray = Objects.requireNonNull(homeViewModel.getItemNames().getValue()).toArray(new String[0]);
        Integer[] imgIdsArray = Objects.requireNonNull(homeViewModel.getImgIDs().getValue()).toArray(new Integer[0]);

        adapter = new CustomAdapter(getActivity(), itemNamesArray, imgIdsArray);
        listView.setAdapter(adapter);

        homeViewModel.getItemNames().observe(getViewLifecycleOwner(), itemNames -> {
            String[] itemNamesArrayUpdated = itemNames.toArray(new String[0]);
            adapter.setItemNames(itemNamesArrayUpdated);
            adapter.notifyDataSetChanged();
        });

        homeViewModel.getImgIDs().observe(getViewLifecycleOwner(), imgIDs -> {
            Integer[] imgIDsArrayUpdated = imgIDs.toArray(new Integer[0]);
            adapter.setImgIDs(imgIDsArrayUpdated);
            adapter.notifyDataSetChanged();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


