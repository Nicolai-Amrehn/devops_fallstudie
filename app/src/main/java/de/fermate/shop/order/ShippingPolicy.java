package de.fermate.shop.order;

import java.util.EnumSet;
import java.util.Set;

/** Sperrgut im Warenkorb erzwingt die Spedition; sonst darf der Kunde wählen. */
public final class ShippingPolicy {

  private ShippingPolicy() {}

  public static Set<ShippingMethod> allowedMethods(boolean containsBulkyItem) {
    return containsBulkyItem
        ? EnumSet.of(ShippingMethod.FREIGHT)
        : EnumSet.of(ShippingMethod.PARCEL, ShippingMethod.FREIGHT);
  }

  public static void requireAllowed(ShippingMethod chosen, boolean containsBulkyItem) {
    if (!allowedMethods(containsBulkyItem).contains(chosen)) {
      throw new IllegalArgumentException(
          "Versandart %s ist für Sperrgut nicht zulässig".formatted(chosen));
    }
  }
}
