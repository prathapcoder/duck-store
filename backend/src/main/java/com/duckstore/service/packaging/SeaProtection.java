package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import com.duckstore.common.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule 7: any package gets moisture-absorbing beads and bubble-wrap bags. */
@Component
public class SeaProtection implements ProtectionStrategy {

    @Override
    public ShippingMode mode() {
        return ShippingMode.SEA;
    }

    @Override
    public List<Protection> protectionFor(PackageType packageType) {
        return List.of(Protection.MOISTURE_ABSORBING_BEADS, Protection.BUBBLE_WRAP_BAGS);
    }
}
