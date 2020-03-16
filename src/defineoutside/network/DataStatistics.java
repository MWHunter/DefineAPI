package defineoutside.network;

import java.io.Serializable;

public class DataStatistics implements Serializable {
    double AppCPU; double SystemCPU; double appMemoryUsed; double appMemoryAvailable; double systemMemoryAvailable; double systemMemoryTotal;

    public DataStatistics(double AppCPU, double SystemCPU, double appMemoryUsed, double appMemoryAvailable, double systemMemoryAvailable, double systemMemoryTotal) {
        this.AppCPU = AppCPU;
        this.SystemCPU = SystemCPU;
        this.appMemoryUsed = appMemoryUsed;
        this.appMemoryAvailable = appMemoryAvailable;
        this.systemMemoryAvailable = systemMemoryAvailable;
        this.systemMemoryTotal = systemMemoryTotal;
    }

    public double getAppCPU() {
        return AppCPU;
    }

    public double getSystemCPU() {
        return SystemCPU;
    }

    public double getAppMemoryUsed() {
        return appMemoryUsed;
    }

    public double getAppMemoryAvailable() {
        return appMemoryAvailable;
    }

    public double getSystemMemoryAvailable() {
        return systemMemoryAvailable;
    }

    public double getSystemMemoryTotal() {
        return systemMemoryTotal;
    }

}