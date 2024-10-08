package com.example.myapplication;

import Data.PlantInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.app.Dialog;
import android.view.View.OnClickListener;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.util.Log;
import android.view.WindowManager;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoteFragment extends Fragment {

    private LinearLayout plantListLayout;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note, container, false);
        plantListLayout = view.findViewById(R.id.plant_list_layout);

        databaseReference = FirebaseDatabase.getInstance().getReference("Plants");

        fetchPlantData();

        return view;
    }

    private void fetchPlantData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                plantListLayout.removeAllViews();

                for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    PlantInfo plantInfo = plantSnapshot.getValue(PlantInfo.class);

                    if (plantInfo != null) {

                        View plantItem = LayoutInflater.from(getContext()).inflate(R.layout.plant_item_layout, plantListLayout, false);
                        TextView plantName = plantItem.findViewById(R.id.plantName);
                        TextView customerId = plantItem.findViewById(R.id.customerId);
                        TextView plantingDate = plantItem.findViewById(R.id.plantingDate);
                        TextView harvestDate = plantItem.findViewById(R.id.harvestDate);
                        TextView fertilizationDate = plantItem.findViewById(R.id.fertilizationDate);
                        TextView pesticideDate = plantItem.findViewById(R.id.pesticideDate);
                        TextView wateringCondition = plantItem.findViewById(R.id.wateringCondition);
                        TextView stopWateringCondition = plantItem.findViewById(R.id.stopWateringCondition);
                        ImageButton editButton = plantItem.findViewById(R.id.editButton);
                        ImageButton deleteButton = plantItem.findViewById(R.id.deleteButton);
                        LinearLayout expandableLayout = plantItem.findViewById(R.id.expandableLayout);


                        plantName.setText(plantInfo.getName());
                        customerId.setText(plantInfo.getMaKhachHang());
                        plantingDate.setText(plantInfo.getNgayTrong());
                        harvestDate.setText(plantInfo.getNgayThuHoach());
                        fertilizationDate.setText(plantInfo.getNgayBonPhan());
                        pesticideDate.setText(plantInfo.getNgayPhunThuoc());
                        wateringCondition.setText(plantInfo.getDkTuoi());
                        stopWateringCondition.setText(plantInfo.getDkDungTuoi());


                        plantItem.setOnClickListener(v -> {
                            if (expandableLayout.getVisibility() == View.VISIBLE) {
                                expandableLayout.setVisibility(View.GONE);
                            } else {
                                expandableLayout.setVisibility(View.VISIBLE);
                            }
                        });


                        editButton.setOnClickListener(v -> {
                            openEditPopup(plantInfo);
                        });


                        deleteButton.setOnClickListener(v -> {
                            deletePlant(plantSnapshot.getKey());
                        });


                        plantListLayout.addView(plantItem);
                    }
                }


                if (plantListLayout.getChildCount() == 0) {
                    TextView noDataTextView = new TextView(getContext());
                    noDataTextView.setText("Không có cây nào.");
                    noDataTextView.setGravity(Gravity.CENTER);
                    plantListLayout.addView(noDataTextView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openEditPopup(PlantInfo plantInfo) {
        if (plantInfo == null) {
            Toast.makeText(getContext(), "Thông tin cây không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        String plantId = plantInfo.getId();
        if (plantId == null || plantId.isEmpty()) {
            Toast.makeText(getContext(), "ID không hợp lệ.", Toast.LENGTH_SHORT).show();
            Log.d("PlantInfo", "ID: " + plantInfo.getId());
            return;
        }

        // Tạo dialog mới
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.popup_edit_plant);


        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Chỉnh kích thước ở đây (width và height)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);


        TextView plantNameText = dialog.findViewById(R.id.plant_name_text);
        EditText plantingDate = dialog.findViewById(R.id.planting_date);
        EditText harvestDate = dialog.findViewById(R.id.harvest_date);
        EditText fertilizationDate = dialog.findViewById(R.id.fertilization_date);
        EditText pesticideDate = dialog.findViewById(R.id.pesticide_date);
        EditText wateringCondition = dialog.findViewById(R.id.wateringCondition);
        EditText stopWateringCondition = dialog.findViewById(R.id.stopWateringCondition);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);


        plantNameText.setText(plantId);
        plantingDate.setText(plantInfo.getNgayTrong());
        harvestDate.setText(plantInfo.getNgayThuHoach());
        fertilizationDate.setText(plantInfo.getNgayBonPhan());
        pesticideDate.setText(plantInfo.getNgayPhunThuoc());
        wateringCondition.setText(plantInfo.getDkTuoi());
        stopWateringCondition.setText(plantInfo.getDkDungTuoi());


        cancelButton.setOnClickListener(v -> dialog.dismiss());


        saveButton.setOnClickListener(v -> {

            plantInfo.setNgayTrong(plantingDate.getText().toString());
            plantInfo.setNgayThuHoach(harvestDate.getText().toString());
            plantInfo.setNgayBonPhan(fertilizationDate.getText().toString());
            plantInfo.setNgayPhunThuoc(pesticideDate.getText().toString());
            plantInfo.setDkTuoi(wateringCondition.getText().toString());
            plantInfo.setDkDungTuoi(stopWateringCondition.getText().toString());


            updatePlantInfo(plantInfo);
            dialog.dismiss();
        });

        dialog.show();
    }



    // Phương thức cập nhật thông tin cây vào Firebase
    private void updatePlantInfo(PlantInfo plantInfo) {

        String plantId = plantInfo.getId();


        if (plantId != null && !plantId.isEmpty()) {
            databaseReference.child(plantId).setValue(plantInfo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật cây thành công.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể cập nhật cây.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "ID không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }






    private void deletePlant(String plantId) {
        databaseReference.child(plantId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Đã xóa cây thành công.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Không thể xóa cây.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
