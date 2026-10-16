package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.ShippingMode;
import com.duckstore.common.Size;
import com.duckstore.util.ModeRegistry;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PackagingService {

    private final PackageTypeSelector selector;
    private final ModeRegistry<ShippingMode, ProtectionStrategy> protections;

    public PackagingService(PackageTypeSelector selector, List<ProtectionStrategy> protectionStrategies) {
        this.selector = selector;
        this.protections = new ModeRegistry<>(ShippingMode.class, protectionStrategies,
                ProtectionStrategy::mode, "protection strategy");
    }

    public PackagingPlan plan(Size size, ShippingMode mode) {
        PackageType packageType = selector.select(size);
        return new PackagingPlan(packageType, protections.get(mode).protectionFor(packageType));
    }
}
