package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Department;

import java.net.URL;
import java.util.Map;

/**
 * Created by Nikita Lazarev-Zubov on 14.12.17
 */

public final class DepartmentImpl implements Department {
    private final DepartmentOnlineStatus departmentOnlineStatus;
    private final String key;
    private final String name;
    private final int order;
    private Map<String, String> localizedNames;
    private URL logoUrl;

    DepartmentImpl(String key,
                   String name,
                   DepartmentOnlineStatus departmentOnlineStatus,
                   int order,
                   Map<String, String> localizedNames,
                   URL logoUrl) {
        this.key = key;
        this.name = name;
        this.departmentOnlineStatus = departmentOnlineStatus;
        this.order = order;
        this.localizedNames = localizedNames;
        this.logoUrl = logoUrl;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public DepartmentOnlineStatus getDepartmentOnlineStatus() {
        return departmentOnlineStatus;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Nullable
    @Override
    public Map<String, String> getLocalizedNames() {
        return localizedNames;
    }

    @Nullable
    @Override
    public URL getLogoUrl() {
        return logoUrl;
    }
}
