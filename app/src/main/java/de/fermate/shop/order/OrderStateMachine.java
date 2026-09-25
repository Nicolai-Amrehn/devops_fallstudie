package de.fermate.shop.order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Erlaubte Zustandsübergänge einer Bestellung; alle anderen, auch PAID → PAID, sind verboten. */
public final class OrderStateMachine {

  private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS =
      new EnumMap<>(OrderStatus.class);

  static {
    TRANSITIONS.put(OrderStatus.NEW, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
    TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.REFUNDED));
    TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
    TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.REFUNDED));
    TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    TRANSITIONS.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
  }

  private OrderStateMachine() {}

  public static boolean canTransition(OrderStatus from, OrderStatus to) {
    return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
  }

  public static OrderStatus transition(OrderStatus from, OrderStatus to) {
    if (!canTransition(from, to)) {
      throw new IllegalStateTransitionException(from, to);
    }
    return to;
  }

  public static class IllegalStateTransitionException extends IllegalStateException {
    public IllegalStateTransitionException(OrderStatus from, OrderStatus to) {
      super("Übergang %s → %s ist nicht erlaubt".formatted(from, to));
    }
  }
}
