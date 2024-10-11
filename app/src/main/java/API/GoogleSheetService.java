package API;
import Data.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.oauth2.GoogleCredentials;
import java.net.HttpURLConnection; // Thêm dòng này
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.google.gson.Gson;
import java.util.List;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleSheetService {
    private Sheets sheetsService;
    private String spreadsheetId;
    private static final String SHEET_NAME = "Note"; // Tên sheet bạn đang làm việc

    public GoogleSheetService(Sheets sheetsService, String spreadsheetId) throws GeneralSecurityException, IOException {
        this.spreadsheetId = spreadsheetId;
        this.sheetsService = createSheetsService();
    }

    private Sheets createSheetsService() throws GeneralSecurityException, IOException {
        // Đường dẫn tới tệp JSON
        FileInputStream credentialStream = new FileInputStream("Downloads/nnt-438013-30f2ccae1133.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        HttpRequestInitializer requestInitializer = (HttpRequestInitializer) credentials;

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName("nnmt")
                .build();
    }

    // Phương thức để lấy tất cả cây từ Google Sheets
    public void getAllPlantsFromApi(String apiUrl, SheetsCallback callback) {
        // Tạo một yêu cầu HTTP GET tới API và xử lý phản hồi JSON
        new Thread(() -> {
            try {
                // Gọi API
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                // Kiểm tra mã trạng thái phản hồi
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Chuyển đổi phản hồi JSON thành danh sách PlantInfo
                    List<PlantInfo> plants = new Gson().fromJson(response.toString(), new TypeToken<List<PlantInfo>>() {}.getType());
                    callback.onSuccess(plants);
                } else {
                    callback.onError("Không thể lấy dữ liệu từ API. Mã phản hồi: " + responseCode);
                }
            } catch (IOException e) {
                callback.onError("Lỗi khi gọi API: " + e.getMessage());
            }
        }).start();
    }
}
