package lsh.ext.gson.adapters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.hamcrest.Matcher;
import org.junit.Test;

import static lsh.ext.gson.adapters.AlwaysListTypeAdapterFactory.getAlwaysListTypeAdapterFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AlwaysListTypeAdapterFactoryTest {

	private static final TypeToken<List<Boolean>> booleanListTypeToken = new TypeToken<List<Boolean>>() {
	};

	private static final TypeToken<List<Integer>> integerListTypeToken = new TypeToken<List<Integer>>() {
	};

	private static final TypeToken<List<String>> stringListTypeToken = new TypeToken<List<String>>() {
	};

	private static final TypeToken<List<Map<String, Integer>>> stringToIntegerMapListTypeToken = new TypeToken<List<Map<String, Integer>>>() {
	};

	private static final Gson gson = new Gson();
	private static final TypeAdapterFactory unit = getAlwaysListTypeAdapterFactory();

	@Test
	public void testNull()
			throws IOException {
		doTest(stringListTypeToken, "null", nullValue());
	}

	@Test
	public void testSingleObject()
			throws IOException {
		doTest(stringToIntegerMapListTypeToken, "{\"foo\":1,\"bar\":2}", is(ImmutableList.of(ImmutableMap.of("foo", 1, "bar", 2))));
	}

	@Test
	public void testSingleString()
			throws IOException {
		doTest(stringListTypeToken, "\"foo\"", is(ImmutableList.of("foo")));
	}

	@Test
	public void testSingleNumber()
			throws IOException {
		doTest(integerListTypeToken, "39", is(ImmutableList.of(39)));
	}

	@Test
	public void testSingleBoolean()
			throws IOException {
		doTest(booleanListTypeToken, "true", is(ImmutableList.of(true)));
	}

	@Test
	public void testMultipleObjects()
			throws IOException {
		doTest(
				stringToIntegerMapListTypeToken,
				"[{\"foo\":1,\"bar\":2},{\"bar\":3,\"qux\":4}]",
				is(ImmutableList.of(ImmutableMap.of("foo", 1, "bar", 2), ImmutableMap.of("bar", 3, "qux", 4)))
		);
	}

	@Test
	public void testMultipleStrings()
			throws IOException {
		doTest(stringListTypeToken, "[\"foo\",\"bar\",\"baz\"]", is(ImmutableList.of("foo", "bar", "baz")));
	}

	@Test
	public void testMultipleNumbers()
			throws IOException {
		doTest(integerListTypeToken, "[39,42,100]", is(ImmutableList.of(39, 42, 100)));
	}

	@Test
	public void testMultipleBooleans()
			throws IOException {
		doTest(booleanListTypeToken, "[true,false,true]", is(ImmutableList.of(true, false, true)));
	}

	private static <T> void doTest(final TypeToken<T> typeToken, final String json, final Matcher<? super T> valueMatcher)
			throws IOException {
		final TypeAdapter<T> typeAdapter = unit.create(gson, typeToken);
		final T value = typeAdapter.fromJson(json);
		assertThat(value, valueMatcher);
		final String serializedJson = typeAdapter.toJson(value);
		assertThat(serializedJson, is(json));
	}

}
