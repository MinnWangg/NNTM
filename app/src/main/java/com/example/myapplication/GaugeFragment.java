package com.example.myapplication;

import android.app.Dialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.anastr.speedviewlib.SpeedView;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import android.os.Handler;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GaugeFragment extends Fragment {

    private Sheets sheetsService;
    private String spreadsheetId = "1zZi8rKEgIWFsgs98PCOxiXldqpXgl7KW-69CREtL-rI";

    private SpeedView gaugeTemperature;
    private SpeedView gaugeLight;
    private SpeedView gaugeHumidity;
    private SpeedView gaugeSoilHumidity;

    private View view;

    private Handler handler = new Handler();
    private Runnable updateDataRunnable;
    private Interpreter tflite;

    public GaugeFragment(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gauge, container, false);

        // Khởi tạo các gauge
        gaugeTemperature = view.findViewById(R.id.gauge_temperature);
        gaugeHumidity = view.findViewById(R.id.gauge_humidity);
        gaugeLight = view.findViewById(R.id.gauge_light);
        gaugeSoilHumidity = view.findViewById(R.id.gauge_soil_humidity);

        // Lấy dữ liệu từ Google Sheets
        fetchDataFromGoogleSheet();
        initializeInterpreter();
        Button btnEditConditions = view.findViewById(R.id.btn_edit_conditions);
        btnEditConditions.setOnClickListener(v -> showEditConditionsPopup());

        updateDataRunnable = new Runnable() {
            @Override
            public void run() {
                fetchDataFromGoogleSheet();
                handler.postDelayed(this, 5000);
            }
        };


        handler.post(updateDataRunnable);

        Switch toggleConditions = view.findViewById(R.id.toggle_conditions);

        // Thiết lập hành vi của Switch để khóa bảng điều kiện
        toggleConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleConditions.setThumbTintList(ColorStateList.valueOf(getResources().getColor(
                    isChecked ? R.color.colorOn : R.color.colorOff)));
            setConditionsEnabled(!isChecked); // Khóa hoặc mở khóa bảng điều kiện
            updateGoogleSheetToggle(isChecked); // Cập nhật ô D2 trên Google Sheets
        });


        fetchToggleStatus();

        return view;
    }

    private void initializeInterpreter() {
        try {
            MappedByteBuffer modelFile = loadModelFile();
            tflite = new Interpreter(modelFile);
            showToast("Mô hình TensorFlow Lite đã được khởi tạo.");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Không thể tải mô hình TensorFlow Lite.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dừng việc cập nhật khi fragment bị hủy
        handler.removeCallbacks(updateDataRunnable);
        if (tflite != null) {
            tflite.close();
        }
    }

    // Hàm load mô hình TFLite từ assets
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getActivity().getAssets().openFd("conme.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void fetchDataFromGoogleSheet() {
        new Thread(() -> {
            try {
                // Lấy dữ liệu từ bảng dữ liệu tổng
                ValueRange resultData = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, "data!C2:J2")
                        .execute();

                // Lấy dữ liệu từ bảng điều kiện
                ValueRange resultConditions = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, "thaydoiDK!A2:C2")
                        .execute();

                List<List<Object>> valuesData = resultData.getValues();
                List<List<Object>> valuesConditions = resultConditions.getValues();

                if (valuesData != null && !valuesData.isEmpty() && valuesConditions != null && !valuesConditions.isEmpty()) {
                    List<Object> rowData = valuesData.get(0);
                    List<Object> rowConditions = valuesConditions.get(0);

                    if (rowData.size() >= 6 && rowConditions.size() >= 3) {
                        // Cập nhật giá trị cho các gauge từ bảng dữ liệu tổng
                        float soilHumidity = Float.parseFloat(rowData.get(3).toString()); // Ví dụ lấy từ cột D
                        float temperature = Float.parseFloat(rowData.get(0).toString()); // C2
                        float humidity = Float.parseFloat(rowData.get(1).toString());
                        float light = Float.parseFloat(rowData.get(2).toString());

                        float conditionLight = Float.parseFloat(rowConditions.get(0).toString());  // A2
                        float conditionSoilMax = Float.parseFloat(rowConditions.get(1).toString());
                        float conditionSoilStop = Float.parseFloat(rowConditions.get(2).toString());

                        // Chuẩn hóa giá trị đầu vào (không chia cho 100)
                        float normalizedTemperature = temperature ; // Giữ giá trị từ 0 đến 1
                        float normalizedHumidity = humidity; // Giữ giá trị từ 0 đến 1
                        float normalizedLight = light ; // Giữ giá trị từ 0 đến 1
                        float normalizedSoilHumidity = soilHumidity; // Giữ giá trị từ 0 đến 1

                        // Dự đoán trạng thái máy bơm
                        float predictedPumpStatus = predictPumpStatus(normalizedTemperature, normalizedHumidity, normalizedLight, normalizedSoilHumidity);
                        Log.d("Predicted Pump Status", "Giá trị dự đoán: " + predictedPumpStatus);

                        // Cập nhật UI trên MainThread
                        getActivity().runOnUiThread(() -> {
                            gaugeTemperature.speedTo(temperature);
                            gaugeHumidity.speedTo(humidity);
                            gaugeLight.speedTo(light);
                            gaugeSoilHumidity.speedTo(soilHumidity);

                            // Cập nhật trạng thái máy bơm theo giá trị dự đoán
                            String pumpStatusDisplay = predictedPumpStatus == 0 ? "Tắt" : "Bật"; // Giả định ngưỡng 0.5
                            ((EditText) view.findViewById(R.id.pump_status)).setText(pumpStatusDisplay);

                            // Cập nhật điều kiện khác
                            ((EditText) view.findViewById(R.id.condition_light)).setText(String.valueOf((int) conditionLight));
                            ((EditText) view.findViewById(R.id.condition_soil_humidity_max)).setText(String.valueOf((int) conditionSoilMax));
                            ((EditText) view.findViewById(R.id.condition_soil_humidity_min)).setText(String.valueOf((int) conditionSoilStop));
                        });
                        updateGoogleSheetToggle(predictedPumpStatus == 0 ? 0 : 1);
                    }
                } else {
                    showToast("Không có dữ liệu trong Google Sheets.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi khi lấy dữ liệu từ Google Sheets.");
            }
        }).start();
    }
    private void updateGoogleSheetToggle(int value) {
        new Thread(() -> {
            try {
                // Cập nhật ô E2 với giá trị mới
                List<List<Object>> values = new ArrayList<>();
                values.add(Arrays.asList(value)); // Giá trị cần cập nhật

                // Tạo đối tượng ValueRange
                ValueRange body = new ValueRange().setValues(values);

                // Cập nhật giá trị cho ô E2
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, "thaydoiDK!E2", body)
                        .setValueInputOption("RAW") // Cập nhật trực tiếp giá trị
                        .execute();
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi khi cập nhật dữ liệu lên Google Sheets.");
            }
        }).start();
    }
    private float predictPumpStatus(float temperature, float humidity, float light, float soilHumidity) {
        if (tflite == null) {
            showToast("Mô hình chưa được khởi tạo.");
            return -1; // Hoặc giá trị phù hợp để báo lỗi
        }

        float[] input = {temperature, humidity, light, soilHumidity};
        float[][] output = new float[1][1];

        // Log thông tin đầu vào
        Log.d("Input Data", "Temperature: " + temperature + ", Humidity: " + humidity + ", Light: " + light + ", Soil Humidity: " + soilHumidity);

        tflite.run(input, output);

        // Kiểm tra giá trị dự đoán
        return output[0][0]; // Chắc chắn giá trị này nằm trong khoảng 0 đến 1
    }

    private void showEditConditionsPopup() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.popup_edit_conditions);


        EditText editConditionLight = dialog.findViewById(R.id.edit_condition_light);
        EditText editConditionSoilHumidityMax = dialog.findViewById(R.id.edit_condition_soil_humidity_max);
        EditText editConditionSoilHumidityMin = dialog.findViewById(R.id.edit_condition_soil_humidity_min);

        // Lấy dữ liệu từ các trường đã cập nhật trước đó
        String conditionLightValue = ((EditText) view.findViewById(R.id.condition_light)).getText().toString();
        String conditionSoilHumidityMaxValue = ((EditText) view.findViewById(R.id.condition_soil_humidity_max)).getText().toString();
        String conditionSoilHumidityMinValue = ((EditText) view.findViewById(R.id.condition_soil_humidity_min)).getText().toString();


        editConditionLight.setText(conditionLightValue);
        editConditionSoilHumidityMax.setText(conditionSoilHumidityMaxValue);
        editConditionSoilHumidityMin.setText(conditionSoilHumidityMinValue);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String conditionLight = editConditionLight.getText().toString();
            String conditionSoilHumidityMax = editConditionSoilHumidityMax.getText().toString();
            String conditionSoilHumidityMin = editConditionSoilHumidityMin.getText().toString();

            saveConditionsToGoogleSheet(conditionLight, conditionSoilHumidityMax, conditionSoilHumidityMin);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void saveConditionsToGoogleSheet(String conditionLight, String conditionSoilHumidityMax, String conditionSoilHumidityMin) {

        List<List<Object>> values = List.of(
                List.of(conditionLight, conditionSoilHumidityMax, conditionSoilHumidityMin)
        );

        ValueRange body = new ValueRange().setValues(values);

        new Thread(() -> {
            try {
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, "thaydoiDK!A2:C2", body)
                        .setValueInputOption("RAW")
                        .execute();


                fetchDataFromGoogleSheet();

                showToast("Lưu thành công!");
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Lỗi khi lưu!");
            }
        }).start();
    }

    //ẩn bảng đk
    private void setConditionsEnabled(boolean enabled) {
        View conditionLayout = view.findViewById(R.id.conditionLayout);
        int color = enabled ? R.color.white : R.color.light_gray;

        conditionLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(color)));


        conditionLayout.setEnabled(enabled);

        for (int i = 0; i < ((ViewGroup) conditionLayout).getChildCount(); i++) {
            View child = ((ViewGroup) conditionLayout).getChildAt(i);
            child.setEnabled(enabled);
        }
    }

    //bật tắt auto
    private void updateGoogleSheetToggle(boolean toggleStatus) {
        List<List<Object>> values = List.of(List.of(toggleStatus ? "1" : "0"));

        ValueRange body = new ValueRange().setValues(values);

        new Thread(() -> {
            try {
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, "thaydoiDK!D2", body)
                        .setValueInputOption("RAW")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Lỗi khi cập nhật trạng thái Switch!");
            }
        }).start();
    }

    private void fetchToggleStatus() {
        new Thread(() -> {
            try {
                ValueRange result = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, "thaydoiDK!D2")
                        .execute();

                List<List<Object>> values = result.getValues();
                if (values != null && !values.isEmpty()) {
                    String toggleValue = values.get(0).get(0).toString();

                    boolean toggleStatus = toggleValue.equals("ON");

                    getActivity().runOnUiThread(() -> {
                        Switch toggleConditions = view.findViewById(R.id.toggle_conditions);
                        toggleConditions.setChecked(toggleStatus);
                        setConditionsEnabled(!toggleStatus);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Lỗi khi lấy trạng thái Switch từ Google Sheets.");
            }
        }).start();
    }






    private void showToast(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }
}

