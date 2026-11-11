package net.dehasher.hlib.controller;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import java.util.Optional;
import java.util.OptionalDouble;

public final class AttributeController {
	public static Attribute getAttributeConstant(String constantName) {
		if (constantName == null) return null;
		NamespacedKey key = NamespacedKey.fromString(constantName.toLowerCase());
		return key == null ? null : Registry.ATTRIBUTE.get(key);
	}

	public static Optional<AttributeInstance> getAttributeInstance(LivingEntity living, AttributeType type) {
		if (living == null || type == null) return Optional.empty();
		Attribute attribute = getAttributeConstant(type.getName());
		if (attribute == null) return Optional.empty();
		try {
			return Optional.ofNullable(living.getAttribute(attribute));
		} catch (RuntimeException _) {
			return Optional.empty();
		}
	}

	public static OptionalDouble getAttributeValue(LivingEntity living, AttributeType type) {
		try {
			return getAttributeInstance(living, type)
					.map(attribute -> OptionalDouble.of(attribute.getValue()))
					.orElseGet(OptionalDouble::empty);
		} catch (RuntimeException _) {
			return OptionalDouble.empty();
		}
	}

	public static boolean setAttributeBaseValue(LivingEntity living, AttributeType type, double baseValue) {
		Optional<AttributeInstance> attribute = getAttributeInstance(living, type);
		if (attribute.isEmpty()) return false;
		try {
			attribute.get().setBaseValue(baseValue);
			return true;
		} catch (RuntimeException _) {
			return false;
		}
	}

	public enum AttributeType {
		MAX_HEALTH,
		FLYING_SPEED,
		SCALE;

		@Getter
		private final String name;

		AttributeType() {
			this.name = name();
		}
	}
}