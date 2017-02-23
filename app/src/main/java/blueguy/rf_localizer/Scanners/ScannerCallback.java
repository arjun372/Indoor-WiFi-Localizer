package blueguy.rf_localizer.Scanners;

import android.util.Pair;

import java.util.List;

/**
 * Created by work on 2/22/17.
 */
public abstract class ScannerCallback {
    public abstract void onScanResult(List<Pair<Long, List<Object>>> dataList);
}
