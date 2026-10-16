package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.duckstore.common.Protection;
import com.duckstore.common.ShippingMode;
import com.duckstore.common.Size;
import com.duckstore.service.packaging.AirProtection;
import com.duckstore.service.packaging.LandProtection;
import com.duckstore.service.packaging.PackageTypeSelector;
import com.duckstore.service.packaging.PackagingPlan;
import com.duckstore.service.packaging.PackagingService;
import com.duckstore.service.packaging.SeaProtection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PackagingServiceTest {

    private final PackagingService service = new PackagingService(new PackageTypeSelector(),
            List.of(new AirProtection(), new LandProtection(), new SeaProtection()));

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "XLarge|Land|Wood|Polystyrene balls",
            "XLarge|Air|Wood|Polystyrene balls",
            "XLarge|Sea|Wood|Moisture-absorbing beads + Bubble-wrap bags",
            "Large|Land|Wood|Polystyrene balls",
            "Large|Air|Wood|Polystyrene balls",
            "Large|Sea|Wood|Moisture-absorbing beads + Bubble-wrap bags",
            "Medium|Land|Cardboard|Polystyrene balls",
            "Medium|Air|Cardboard|Polystyrene balls",
            "Medium|Sea|Cardboard|Moisture-absorbing beads + Bubble-wrap bags",
            "Small|Land|Plastic|Polystyrene balls",
            "Small|Air|Plastic|Bubble-wrap bags",
            "Small|Sea|Plastic|Moisture-absorbing beads + Bubble-wrap bags",
            "XSmall|Land|Plastic|Polystyrene balls",
            "XSmall|Air|Plastic|Bubble-wrap bags",
            "XSmall|Sea|Plastic|Moisture-absorbing beads + Bubble-wrap bags"})
    void package_and_protection_follow_size_and_shipping_mode(String size, String mode, String packageType,
                                                              String protections) {
        PackagingPlan plan = service.plan(Size.fromValue(size), ShippingMode.fromValue(mode));

        assertThat(plan.packageType().label()).isEqualTo(packageType);
        assertThat(plan.protections().stream().map(Protection::label).collect(Collectors.joining(" + ")))
                .isEqualTo(protections);
    }

    @Test
    void startup_fails_if_a_shipping_mode_has_no_strategy() {
        assertThatThrownBy(() -> new PackagingService(new PackageTypeSelector(), List.of(new AirProtection())))
                .isInstanceOf(IllegalStateException.class);
    }
}
