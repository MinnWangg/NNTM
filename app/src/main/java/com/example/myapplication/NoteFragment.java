package com.example.myapplication;

import androidx.fragment.app.Fragment;
import API.SheetsServiceUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;
import java.io.InputStream;
import android.widget.TextView;
import com.google.api.services.sheets.v4.Sheets;
import android.widget.EditText;
import android.widget.ImageButton;
import com.google.android.material.textfield.TextInputEditText;
import android.view.Gravity;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;



public class NoteFragment extends Fragment {

    private Sheets sheetsService;
    private String spreadsheetId = "1zZi8rKEgIWFsgs98PCOxiXldqpXgl7KW-69CREtL-rI";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note, container, false);

        try {

            InputStream credentialsStream = getResources().openRawResource(R.raw.service_account); // credentials.json trong thư mục raw
            sheetsService = SheetsServiceUtil.getSheetsService(credentialsStream);

            // Lấy dữ liệu từ Google Sheets
            new Thread(() -> {
                try {
                    List<List<Object>> data = SheetsServiceUtil.getDataFromSheet(sheetsService, spreadsheetId, "Note!A2:I");
                    getActivity().runOnUiThread(() -> {
                        displayData(data, view);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void displayData(List<List<Object>> data, View view) {
        LinearLayout plantListLayout = view.findViewById(R.id.plant_list_layout);
        plantListLayout.removeAllViews();

        for (List<Object> row : data) {
            View plantItemView = LayoutInflater.from(getContext()).inflate(R.layout.plant_item_layout, plantListLayout, false);
            TextView plantName = plantItemView.findViewById(R.id.plantName);
            ImageButton editButton = plantItemView.findViewById(R.id.editButton);
            ImageButton deleteButton = plantItemView.findViewById(R.id.deleteButton);
            LinearLayout expandableLayout = plantItemView.findViewById(R.id.expandableLayout);


            plantName.setText(row.get(0).toString());

            // Sự kiện nhấn vào tên cây
            plantName.setOnClickListener(v -> {
                if (expandableLayout.getVisibility() == View.GONE) {

                    expandableLayout.setVisibility(View.VISIBLE);

                    // Lấy lại dữ liệu mới từ Google Sheets
                    new Thread(() -> {
                        try {
                            List<List<Object>> updatedData = SheetsServiceUtil.getDataFromSheet(sheetsService, spreadsheetId, "Note!A2:I");
                            getActivity().runOnUiThread(() -> {

                                for (List<Object> updatedRow : updatedData) {
                                    if (updatedRow.get(0).toString().equals(row.get(0).toString())) {
                                        setPlantInfo(updatedRow, plantItemView);
                                        break;
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {

                    expandableLayout.setVisibility(View.GONE);
                }
            });





            editButton.setOnClickListener(v -> {

                String plantId = row.get(0).toString();
                showEditPlantPopup(plantId);
            });

            deleteButton.setOnClickListener(v -> {
                String plantId = row.get(0).toString();
                String plantNameText = row.get(2).toString();

                new AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa cây: " + plantNameText + " (ID: " + plantId + ") ?")
                        .setPositiveButton("Có", (dialog, which) -> {

                            deletePlant(plantId);
                        })
                        .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                        .show();
            });




            plantListLayout.addView(plantItemView);
        }
    }


    private void setPlantInfo(List<Object> row, View plantItemView) {
        EditText plantingDate = plantItemView.findViewById(R.id.plantingDate);
        EditText harvestDate = plantItemView.findViewById(R.id.harvestDate);
        EditText fertilizationDate = plantItemView.findViewById(R.id.fertilizationDate);
        EditText pesticideDate = plantItemView.findViewById(R.id.pesticideDate);
        TextInputEditText wateringCondition = plantItemView.findViewById(R.id.wateringCondition);
        TextInputEditText stopWateringCondition = plantItemView.findViewById(R.id.stopWateringCondition);

        // Kiểm tra và gán giá trị hoặc giá trị rỗng nếu không có thông tin
        plantingDate.setText(row.size() > 3 && row.get(3) != null ? row.get(3).toString() : "");
        harvestDate.setText(row.size() > 4 && row.get(4) != null ? row.get(4).toString() : "");
        fertilizationDate.setText(row.size() > 5 && row.get(5) != null ? row.get(5).toString() : "");
        pesticideDate.setText(row.size() > 6 && row.get(6) != null ? row.get(6).toString() : "");
        wateringCondition.setText(row.size() > 7 && row.get(7) != null ? row.get(7).toString() : "");
        stopWateringCondition.setText(row.size() > 8 && row.get(8) != null ? row.get(8).toString() : "");
    }





    private void showEditPlantPopup(String plantId) {
        updateDataDisplay();

        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_edit_plant, null);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);


        TextView plantNameSheet = popupView.findViewById(R.id.plant_name_text);
        EditText plantingDate = popupView.findViewById(R.id.planting_date);
        EditText harvestDate = popupView.findViewById(R.id.harvest_date);
        EditText fertilizationDate = popupView.findViewById(R.id.fertilization_date);
        EditText pesticideDate = popupView.findViewById(R.id.pesticide_date);
        EditText wateringCondition = popupView.findViewById(R.id.wateringCondition);
        EditText stopWateringCondition = popupView.findViewById(R.id.stopWateringCondition);

        // Lấy thông tin cây từ Google Sheets
        loadPlantData(plantId, popupWindow, plantNameSheet, plantingDate, harvestDate, fertilizationDate, pesticideDate, wateringCondition, stopWateringCondition);

        Button cancelButton = popupView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> popupWindow.dismiss());

        Button saveButton = popupView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            // Lưu thông tin vào Google Sheets
            savePlantData(plantId, plantNameSheet, plantingDate, harvestDate, fertilizationDate, pesticideDate, wateringCondition, stopWateringCondition, popupWindow);
        });

        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }


    // Phương thức để tải dữ liệu cây
    private void loadPlantData(String plantId, PopupWindow popupWindow, TextView plantNameSheet, EditText plantingDate,
                               EditText harvestDate, EditText fertilizationDate, EditText pesticideDate,
                               EditText wateringCondition, EditText stopWateringCondition) {
        new Thread(() -> {
            try {
                List<Object> row = SheetsServiceUtil.getPlantInfo(sheetsService, spreadsheetId, plantId);
                if (row != null) {
                    // Hiển thị thông tin cây trong popup
                    getActivity().runOnUiThread(() -> {
                        plantNameSheet.setText(row.size() > 2 && row.get(2) != null ? row.get(2).toString() : "");
                        plantingDate.setText(row.size() > 3 && row.get(3) != null ? row.get(3).toString() : "");
                        harvestDate.setText(row.size() > 4 && row.get(4) != null ? row.get(4).toString() : "");
                        fertilizationDate.setText(row.size() > 5 && row.get(5) != null ? row.get(5).toString() : "");
                        pesticideDate.setText(row.size() > 6 && row.get(6) != null ? row.get(6).toString() : "");
                        wateringCondition.setText(row.size() > 7 && row.get(7) != null ? row.get(7).toString() : "");
                        stopWateringCondition.setText(row.size() > 8 && row.get(8) != null ? row.get(8).toString() : "");
                    });

                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin cho cây này!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Lỗi khi lấy thông tin cây!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Phương thức để lưu dữ liệu cây
    private void savePlantData(String plantId, TextView plantNameSheet, EditText plantingDate, EditText harvestDate,
                               EditText fertilizationDate, EditText pesticideDate, EditText wateringCondition,
                               EditText stopWateringCondition, PopupWindow popupWindow) {
        new Thread(() -> {
            try {
                SheetsServiceUtil.updateGoogleSheet(sheetsService, spreadsheetId, plantId,
                        plantNameSheet.getText().toString(), plantingDate.getText().toString(),
                        harvestDate.getText().toString(), fertilizationDate.getText().toString(),
                        pesticideDate.getText().toString(), wateringCondition.getText().toString(),
                        stopWateringCondition.getText().toString());

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    updateDataDisplay();
                    popupWindow.dismiss();
                });
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }






    private void updateDataDisplay() {
        new Thread(() -> {
            try {
                List<List<Object>> data = SheetsServiceUtil.getDataFromSheet(sheetsService, spreadsheetId, "Note!A2:I");
                getActivity().runOnUiThread(() -> {

                    displayData(data, getView());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void deletePlant(String plantId) {
        new Thread(() -> {
            try {
                // Gọi phương thức deletePlantFromSheet mà không cần kiểm tra giá trị trả về
                SheetsServiceUtil.deletePlantFromSheet(sheetsService, spreadsheetId, plantId);
                getActivity().runOnUiThread(() -> {
                    // Hiển thị thông báo xóa thành công
                    Toast.makeText(getContext(), "Đã xóa cây!", Toast.LENGTH_SHORT).show();
                    updateDataDisplay();  // Cập nhật lại danh sách sau khi xóa
                });
            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    // Hiển thị thông báo lỗi
                    Toast.makeText(getContext(), "Lỗi khi xóa cây!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }





}

