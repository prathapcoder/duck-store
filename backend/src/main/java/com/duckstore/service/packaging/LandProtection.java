package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import com.duckstore.common.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule 6: any package is filled with polystyrene balls. */
@Component
public class LandProtection implements ProtectionStrategy {

    @Override
    public ShippingMode mode() {
        return ShippingMode.LAND;
    }

    @Override
    public List<Protection> protectionFor(PackageType packageType) {
        return List.of(Protection.POLYSTYRENE_BALLS);
    }
}
