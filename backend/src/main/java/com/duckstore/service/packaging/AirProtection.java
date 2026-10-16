package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import com.duckstore.common.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rules 4-5: wood or cardboard get polystyrene balls; plastic gets bubble-wrap bags. */
@Component
public class AirProtection implements ProtectionStrategy {

    @Override
    public ShippingMode mode() {
        return ShippingMode.AIR;
    }

    @Override
    public List<Protection> protectionFor(PackageType packageType) {
        return packageType == PackageType.PLASTIC
                ? List.of(Protection.BUBBLE_WRAP_BAGS)
                : List.of(Protection.POLYSTYRENE_BALLS);
    }
}
