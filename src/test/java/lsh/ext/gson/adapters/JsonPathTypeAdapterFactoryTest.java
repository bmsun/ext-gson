package lsh.ext.gson.adapters;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lsh.ext.gson.annotations.JsonPathExpression;
import org.junit.Test;

import static lsh.ext.gson.adapters.JsonPathTypeAdapterFactory.getJsonPathTypeAdapterFactory;
import static lsh.ext.gson.adapters.JsonPathTypeAdapterFactory.getJsonPathTypeAdapterWithGlobalDefaults;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JsonPathTypeAdapterFactoryTest {

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithDefaultConfiguration() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterFactory();
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final Wrapper wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", Wrapper.class);
		assertThat(wrapper.fooRef, is("Foo!"));
		assertThat(wrapper.barRef, is("A"));
		assertThat(wrapper.bazRef, is(ImmutableMap.of("k1", "v1")));
	}

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithDefaultConfigurationAndNotExistingPaths() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterFactory();
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final WrapperWithNotExistingPath wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", WrapperWithNotExistingPath.class);
		assertThat(wrapper.fooRef, nullValue());
	}

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithConfiguration() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterFactory(JsonPathTypeAdapterFactoryTest::getJsonPathConfiguration);
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final Wrapper wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", Wrapper.class);
		assertThat(wrapper.fooRef, is("Foo!"));
		assertThat(wrapper.barRef, is("A"));
		assertThat(wrapper.bazRef, is(ImmutableMap.of("k1", "v1")));
	}

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithConfigurationAndNotExistingPaths() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterFactory(JsonPathTypeAdapterFactoryTest::getJsonPathConfiguration);
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final WrapperWithNotExistingPath wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", WrapperWithNotExistingPath.class);
		assertThat(wrapper.fooRef, nullValue());
	}

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithGlobalDefaultsConfiguration() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterWithGlobalDefaults();
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final JsonProvider jsonProvider = new GsonJsonProvider(gson);
		final MappingProvider gsonMappingProvider = new GsonMappingProvider(gson);
		configureJsonPathGlobally(jsonProvider, gsonMappingProvider);
		final Wrapper wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", Wrapper.class);
		assertThat(wrapper.fooRef, is("Foo!"));
		assertThat(wrapper.barRef, is("A"));
		assertThat(wrapper.bazRef, is(ImmutableMap.of("k1", "v1")));
	}

	@Test
	public void testGetJsonPathTypeAdapterFactoryWithGlobalDefaultsConfigurationAndNotExistingKeys() {
		final TypeAdapterFactory unit = getJsonPathTypeAdapterWithGlobalDefaults();
		final Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(unit)
				.create();
		final JsonProvider jsonProvider = new GsonJsonProvider(gson);
		final MappingProvider gsonMappingProvider = new GsonMappingProvider(gson);
		configureJsonPathGlobally(jsonProvider, gsonMappingProvider);
		final WrapperWithNotExistingPath wrapper = gson.fromJson("{\"l1\":{\"l2\":{\"l3\":{\"foo\":\"Foo!\",\"bar\":[\"A\",\"B\",\"C\"],\"baz\":{\"k1\":\"v1\"}}}}}", WrapperWithNotExistingPath.class);
		assertThat(wrapper.fooRef, nullValue());
	}

	private static Configuration getJsonPathConfiguration(final Gson gson) {
		return Configuration.builder()
				.jsonProvider(new GsonJsonProvider(gson))
				.mappingProvider(new GsonMappingProvider(gson))
				.build();
	}

	private static final class Wrapper {

		@JsonPathExpression("$.l1.l2.l3.foo")
		private final String fooRef = null;

		@JsonPathExpression("$.l1.l2.l3.bar[0]")
		private final String barRef = null;

		@JsonPathExpression("$.l1.l2.l3.baz")
		private final Map<String, ?> bazRef = null;

	}

	private static final class WrapperWithNotExistingPath {

		@JsonPathExpression("$.nowhere")
		private final String fooRef = null;

	}

	private static void configureJsonPathGlobally(final JsonProvider jsonProvider, final MappingProvider gsonMappingProvider) {
		Configuration.setDefaults(new Configuration.Defaults() {
			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return gsonMappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}

}
