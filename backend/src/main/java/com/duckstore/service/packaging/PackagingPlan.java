package com.duckstore.service.packaging;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import java.util.List;

public record PackagingPlan(PackageType packageType, List<Protection> protections) {
}
