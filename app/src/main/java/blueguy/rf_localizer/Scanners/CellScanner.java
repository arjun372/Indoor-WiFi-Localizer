package blueguy.rf_localizer.Scanners;

/**
 * Created by work on 2/22/17.
 */
class CellScanner extends Scanner {

    protected CellScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    public boolean mStartScan() {
        return false;
    }

    @Override
    public boolean mStopScan() {
        return false;
    }
}
