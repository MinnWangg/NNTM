package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.anastr.speedviewlib.SpeedView;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.List;

public class GaugeFragment extends Fragment {

    private Sheets sheetsService;
    private String spreadsheetId = "1zZi8rKEgIWFsgs98PCOxiXldqpXgl7KW-69CREtL-rI";

    private SpeedView gaugeTemperature;
    private SpeedView gaugeLight;
    private SpeedView gaugeHumidity;
    private SpeedView gaugeSoilHumidity;

    public GaugeFragment(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gauge, container, false);

        // Khởi tạo các gauge
        gaugeTemperature = view.findViewById(R.id.gauge_temperature);
        gaugeLight = view.findViewById(R.id.gauge_light);
        gaugeHumidity = view.findViewById(R.id.gauge_humidity);
        gaugeSoilHumidity = view.findViewById(R.id.gauge_soil_humidity);

        // Lấy dữ liệu từ Google Sheets
        fetchDataFromGoogleSheet();

        return view;
    }

    private void fetchDataFromGoogleSheet() {
        new Thread(() -> {
            try {
                // Lấy dữ liệu từ Google Sheets
                ValueRange result = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, "data!C2:F2")
                        .execute();

                List<List<Object>> values = result.getValues();

                if (values != null && !values.isEmpty()) {
                    // Lấy thông số từ các cột
                    List<Object> row = values.get(0);
                    if (row.size() >= 4) {
                        // Cập nhật giá trị cho các gauge
                        float temperature = Float.parseFloat(row.get(0).toString());
                        float light = Float.parseFloat(row.get(1).toString());
                        float humidity = Float.parseFloat(row.get(2).toString());
                        float soilHumidity = Float.parseFloat(row.get(3).toString());

                        // Cập nhật UI trên MainThread
                        getActivity().runOnUiThread(() -> {
                            gaugeTemperature.speedTo(temperature);
                            gaugeLight.speedTo(light);
                            gaugeHumidity.speedTo(humidity);
                            gaugeSoilHumidity.speedTo(soilHumidity);
                        });
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


    private void showToast(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }
}
