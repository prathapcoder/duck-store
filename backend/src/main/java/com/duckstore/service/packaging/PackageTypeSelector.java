package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Size;
import org.springframework.stereotype.Component;

/** Rules 1-3: the package is decided by the duck size alone. */
@Component
public class PackageTypeSelector {

    public PackageType select(Size size) {
        return switch (size) {
            case XLARGE, LARGE -> PackageType.WOOD;
            case MEDIUM -> PackageType.CARDBOARD;
            case SMALL, XSMALL -> PackageType.PLASTIC;
        };
    }
}
