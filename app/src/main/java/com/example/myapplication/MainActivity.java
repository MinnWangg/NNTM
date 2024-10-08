package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Fragment currentFragment;
    private PopupManager popupManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        popupManager = new PopupManager(this);
        replaceFragment(new GaugeFragment());
        binding.bottomNavigationView.setBackground(null);


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.gauge) {
                replaceFragment(new GaugeFragment());
                binding.floatingActionButton.hide();
                //binding.floatingActionButton.setImageResource(R.drawable.ic_up);
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
            // Kiểm tra Fragment hiện tại và thực hiện hành động tương ứng
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
    //thay thế fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        currentFragment = fragment;
    }
}