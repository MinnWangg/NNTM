package API;

import java.util.List;
import Data.PlantInfo;

public interface SheetsCallback {
    void onSuccess(List<PlantInfo> plants);
    void onError(String errorMessage);
}
