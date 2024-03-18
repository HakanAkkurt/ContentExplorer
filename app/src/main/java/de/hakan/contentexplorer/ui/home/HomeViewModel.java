package de.hakan.contentexplorer.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import de.hakan.contentexplorer.R;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<String>> itemNames;
    private final MutableLiveData<List<Integer>> imgIDs;

    public HomeViewModel() {
        itemNames = new MutableLiveData<>();
        imgIDs = new MutableLiveData<>();

        initItemNames();
        initImgIds();
    }

    public LiveData<List<String>> getItemNames() {
        return itemNames;
    }

    public LiveData<List<Integer>> getImgIDs() {
        return imgIDs;
    }

    private void initItemNames() {
        List<String> items = new ArrayList<>();

        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");
        items.add("Item 4");
        items.add("Item 5");
        items.add("Item 6");
        items.add("Item 7");
        items.add("Item 8");
        items.add("Item 9");
        items.add("Item 10");
        items.add("Item 11");
        items.add("Item 12");
        items.add("Item 13");
        items.add("Item 14");
        items.add("Item 15");
        items.add("Item 16");
        items.add("Item 17");
        items.add("Item 18");
        items.add("Item 19");
        items.add("Item 20");

        itemNames.setValue(items);
    }

    private void initImgIds() {
        List<Integer> imageIDs = new ArrayList<>();

        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);
        imageIDs.add(R.drawable.ic_menu_gallery);


        imgIDs.setValue(imageIDs);
    }
}
