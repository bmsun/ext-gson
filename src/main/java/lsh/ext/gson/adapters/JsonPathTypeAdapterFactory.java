package lsh.ext.gson.adapters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import lsh.ext.gson.annotations.JsonPathExpression;

import static java.util.Collections.emptyList;

import static com.jayway.jsonpath.JsonPath.compile;

/**
 * <p>
 * Represents a type adapter factory that can deserialize object fields using {@link JsonPathExpression}-annotations in JSON mappings.
 * </p>
 *
 * <p>Example of use. The following JSON:</p>
 *
 * <pre>
 *     {
 *         "l1": {
 *             "l2": {
 *                 "l3": {
 *                     "foo": "Foo!"
 *                 }
 *             }
 *         }
 *     }
 * </pre>
 *
 * <p>can be mapped to the following mapping using this type adapter factory:</p>
 *
 * <pre>
 *     public final class Foo {
 *
 *        {@code @}JsonPathExpression("$.l1.l2.l3.foo")
 *         public final String fooRef = null;
 *
 *     }
 * </pre>
 *
 * <p>So, the following code outputs {@code Foo!} to stdout:</p>
 *
 * <pre>
 *     final Gson gson = new GsonBuilder()
 *         .registerTypeAdapterFactory(getJsonPathTypeAdapterFactory())
 *         .create();
 *     final Foo foo = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\"}}}}", Foo.class);
 *     System.out.println(foo.fooRef);
 * </pre>
 *
 * <p>JSON path expressions that point to not existing paths are ignored.</p>
 *
 * @author Lyubomyr Shaydariv
 * @see #getJsonPathTypeAdapterFactory()
 * @see #getJsonPathTypeAdapterFactory(Function)
 * @see #getJsonPathTypeAdapterWithGlobalDefaults()
 * @since 0-SNAPSHOT
 */
public final class JsonPathTypeAdapterFactory
		implements TypeAdapterFactory {

	private static final TypeAdapterFactory jsonPathTypeAdapterFactory = new JsonPathTypeAdapterFactory(JsonPathTypeAdapterFactory::buildDefaultConfiguration);
	private static final TypeAdapterFactory jsonPathTypeAdapterWithGlobalDefaults = new JsonPathTypeAdapterFactory(gson -> Configuration.defaultConfiguration());

	private final Function<? super Gson, ? extends Configuration> configurationProvider;

	private JsonPathTypeAdapterFactory(final Function<? super Gson, ? extends Configuration> configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	/**
	 * @return A {@link JsonPathTypeAdapterFactory} instance that is configured with the predefined JsonPath configuration. The default JsonPath configuration
	 * used for this instance uses {@link Configuration.Defaults} with an internally defined {@link GsonJsonProvider} bound to the {@link Gson} instance
	 * provided in {@link #create(Gson, TypeToken)}, an internally defined {@link GsonMappingProvider} bound to the {@link Gson} instance provided in
	 * {@link #create(Gson, TypeToken)}, and empty options set.
	 *
	 * @see Configuration.Defaults#options()
	 * @see Configuration.Defaults#jsonProvider()
	 * @see Configuration.Defaults#mappingProvider()
	 * @since 0-SNAPSHOT
	 */
	public static TypeAdapterFactory getJsonPathTypeAdapterFactory() {
		return jsonPathTypeAdapterFactory;
	}

	/**
	 * <pre>
	 * private static void configureJsonPathGlobally(final JsonProvider gsonJsonProvider, final MappingProvider gsonMappingProvider) {
	 *     Configuration.setDefaults(new Configuration.Defaults() {
	 *        {@code @}Override
	 *         public JsonProvider jsonProvider() {
	 *             return gsonJsonProvider;
	 *         }
	 *
	 *        {@code @}Override
	 *         public MappingProvider mappingProvider() {
	 *             return gsonMappingProvider;
	 *         }
	 *
	 *        {@code @}Override
	 *         public Set&lt;Option&gt; options() {
	 *             return EnumSet.noneOf(Option.class);
	 *         }
	 *     });
	 * }
	 *
	 * </pre>
	 *
	 * @return A {@link JsonPathTypeAdapterFactory} instance that is configured with the default global JsonPath configuration.
	 *
	 * @see Configuration#setDefaults(Configuration.Defaults)
	 * @since 0-SNAPSHOT
	 */
	public static TypeAdapterFactory getJsonPathTypeAdapterWithGlobalDefaults() {
		return jsonPathTypeAdapterWithGlobalDefaults;
	}

	/**
	 * @param configurationProvider A function (strategy) to return a JsonPath {@link Configuration}.
	 *
	 * @return A {@link JsonPathTypeAdapterFactory} instance that can be configured with the given strategy.
	 *
	 * @since 0-SNAPSHOT
	 */
	public static TypeAdapterFactory getJsonPathTypeAdapterFactory(final Function<? super Gson, ? extends Configuration> configurationProvider) {
		return new JsonPathTypeAdapterFactory(configurationProvider);
	}

	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
		final TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, typeToken);
		final Collection<FieldInfo> fieldInfos = FieldInfo.of(typeToken.getRawType());
		return fieldInfos.isEmpty()
				? delegateAdapter
				: new JsonPathTypeAdapter<>(gson, delegateAdapter, gson.getAdapter(JsonElement.class), fieldInfos, configurationProvider.apply(gson));
	}

	private static final class JsonPathTypeAdapter<T>
			extends TypeAdapter<T> {

		private final Gson gson;
		private final TypeAdapter<T> delegateAdapter;
		private final TypeAdapter<JsonElement> jsonElementTypeAdapter;
		private final Collection<FieldInfo> fieldInfos;
		private final Configuration configuration;

		private JsonPathTypeAdapter(final Gson gson, final TypeAdapter<T> delegateAdapter, final TypeAdapter<JsonElement> jsonElementTypeAdapter,
				final Collection<FieldInfo> fieldInfos, final Configuration configuration) {
			this.gson = gson;
			this.delegateAdapter = delegateAdapter;
			this.jsonElementTypeAdapter = jsonElementTypeAdapter;
			this.fieldInfos = fieldInfos;
			this.configuration = configuration;
		}

		@Override
		public void write(final JsonWriter out, final T value)
				throws IOException {
			delegateAdapter.write(out, value);
		}

		@Override
		public T read(final JsonReader in)
				throws IOException {
			final JsonElement outerJsonElement = jsonElementTypeAdapter.read(in);
			final T value = delegateAdapter.fromJsonTree(outerJsonElement);
			for ( final FieldInfo fieldInfo : fieldInfos ) {
				try {
					final JsonElement innerJsonElement = fieldInfo.jsonPath.read(outerJsonElement, configuration);
					final Object innerValue = gson.fromJson(innerJsonElement, fieldInfo.field.getType());
					fieldInfo.field.set(value, innerValue);
				} catch ( final PathNotFoundException ignored ) {
				} catch ( final IllegalAccessException ex ) {
					throw new IOException(ex);
				}
			}
			return value;
		}

	}

	private static final class FieldInfo {

		private final Field field;
		private final JsonPath jsonPath;

		private FieldInfo(final Field field, final JsonPath jsonPath) {
			this.field = field;
			this.jsonPath = jsonPath;
		}

		private static Collection<FieldInfo> of(final Class<?> clazz) {
			Collection<FieldInfo> collection = emptyList();
			for ( final Field field : clazz.getDeclaredFields() ) {
				final JsonPathExpression jsonPathExpression = field.getAnnotation(JsonPathExpression.class);
				if ( jsonPathExpression != null ) {
					if ( collection.isEmpty() ) {
						collection = new ArrayList<>();
					}
					field.setAccessible(true);
					collection.add(new FieldInfo(field, compile(jsonPathExpression.value())));
				}
			}
			return collection;
		}

	}

	private static Configuration buildDefaultConfiguration(final Gson gson) {
		return Configuration.builder()
				.jsonProvider(new GsonJsonProvider(gson))
				.mappingProvider(new GsonMappingProvider(gson))
				.build();
	}

}
