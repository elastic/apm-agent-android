package co.elastic.apm.android.sdk.internal.services.network.data;

public class CarrierInfo {
    public final String name;
    public final String mcc;
    public final String mnc;
    public final String icc;

    public CarrierInfo(String name, String mcc, String mnc, String icc) {
        this.name = name;
        this.mcc = mcc;
        this.mnc = mnc;
        this.icc = icc;
    }
}
