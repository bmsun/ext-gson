package lsh.ext.gson.stategies;

import java.lang.reflect.Field;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import lsh.ext.gson.annotations.DynamicSerializedName;

/**
 * Represents a field naming strategy where field names must be evaluated dynamically.
 *
 * @author Lyubomyr Shaydariv
 * @see DynamicSerializedName
 * @since 0-SNAPSHOT
 */
public final class DynamicSerializedNameFieldNamingStrategy
		implements FieldNamingStrategy {

	private final IFieldNamingResolver fieldNamingResolver;
	private final FieldNamingStrategy fallbackFieldNamingStrategy;

	private DynamicSerializedNameFieldNamingStrategy(final IFieldNamingResolver fieldNamingResolver, final FieldNamingStrategy fallbackFieldNamingStrategy) {
		this.fieldNamingResolver = fieldNamingResolver;
		this.fallbackFieldNamingStrategy = fallbackFieldNamingStrategy;
	}

	/**
	 * @param fieldNamingResolver A strategy to resolve JSON property names dynamically.
	 *
	 * @return A {@link DynamicSerializedNameFieldNamingStrategy} instance.
	 *
	 * @since 0-SNAPSHOT
	 */
	public static FieldNamingStrategy getDynamicSerializedNameFieldNamingStrategy(final IFieldNamingResolver fieldNamingResolver) {
		return new DynamicSerializedNameFieldNamingStrategy(fieldNamingResolver, FieldNamingPolicy.IDENTITY);
	}

	/**
	 * @param fieldNamingResolver         A strategy to resolve JSON property names dynamically.
	 * @param fallbackFieldNamingStrategy A strategy to be used if {@code fieldNamingResolver} returns {@code null}.
	 *
	 * @return A {@link DynamicSerializedNameFieldNamingStrategy} instance.
	 *
	 * @since 0-SNAPSHOT
	 */
	public static FieldNamingStrategy getDynamicSerializedNameFieldNamingStrategy(final IFieldNamingResolver fieldNamingResolver,
			final FieldNamingStrategy fallbackFieldNamingStrategy) {
		return new DynamicSerializedNameFieldNamingStrategy(fieldNamingResolver, fallbackFieldNamingStrategy);
	}

	@Override
	public String translateName(final Field field) {
		final DynamicSerializedName annotation = field.getAnnotation(DynamicSerializedName.class);
		if ( annotation == null ) {
			return fallbackFieldNamingStrategy.translateName(field);
		}
		final String resolvedName = fieldNamingResolver.resolveName(annotation.value());
		if ( resolvedName == null ) {
			return fallbackFieldNamingStrategy.translateName(field);
		}
		return resolvedName;
	}

}
