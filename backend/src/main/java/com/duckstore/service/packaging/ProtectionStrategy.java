package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import com.duckstore.common.ShippingMode;
import java.util.List;

/** Strategy: how to protect the ducks for one shipping mode. A new mode is one new implementation. */
public interface ProtectionStrategy {

    ShippingMode mode();

    List<Protection> protectionFor(PackageType packageType);
}
