package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.sheets.v4.Sheets;
import com.example.myapplication.databinding.ActivityMainBinding;

import API.SheetsServiceUtil;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Fragment currentFragment;
    private PopupManager popupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Sheets sheetsService; // Đặt sheetsService là final
        try {
            sheetsService = SheetsServiceUtil.getSheetsService(this.getResources().openRawResource(R.raw.service_account));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        popupManager = new PopupManager(this, sheetsService); // Lưu đối tượng popupManager vào biến instance

        replaceFragment(new GaugeFragment(sheetsService)); // Truyền sheetsService vào constructor
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            final int itemId = item.getItemId(); // Đặt biến itemId là final
            if (itemId == R.id.gauge) {
                replaceFragment(new GaugeFragment(sheetsService)); // Sử dụng sheetsService
                binding.floatingActionButton.hide();
            } else if (itemId == R.id.note) {
                replaceFragment(new NoteFragment());
                binding.floatingActionButton.show();
                binding.floatingActionButton.setImageResource(R.drawable.ic_plus);
            } else if (itemId == R.id.crop) {
                replaceFragment(new CropFragment());
                binding.floatingActionButton.show();
                binding.floatingActionButton.setImageResource(R.drawable.ic_plus);
            } else if (itemId == R.id.user) {
                replaceFragment(new UserFragment());
                binding.floatingActionButton.show();
                binding.floatingActionButton.setImageResource(R.drawable.ic_logout);
            }
            return true;
        });

        binding.floatingActionButton.setOnClickListener(view -> {
            if (currentFragment instanceof GaugeFragment) {

            } else if (currentFragment instanceof NoteFragment) {
                popupManager.showNotePopup();
            } else if (currentFragment instanceof CropFragment) {
                Snackbar.make(binding.getRoot(), "Crop Fragment: FAB Clicked", Snackbar.LENGTH_SHORT).show();
            } else if (currentFragment instanceof UserFragment) {
                Snackbar.make(binding.getRoot(), "User Fragment: FAB Clicked", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // Thay thế fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        currentFragment = fragment;
    }
}
