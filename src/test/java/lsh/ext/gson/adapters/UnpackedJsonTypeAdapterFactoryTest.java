package lsh.ext.gson.adapters;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class UnpackedJsonTypeAdapterFactoryTest {

	private static final Gson gson = new Gson();

	@Test
	public void testRead() {
		final Wrapper wrapper = gson.fromJson("{\"value\":\"[1,2,3]\"}", Wrapper.class);
		assertThat(wrapper.value, is(ImmutableList.of(1, 2, 3)));
	}

	@Test
	public void testWrite() {
		final Wrapper wrapper = new Wrapper(ImmutableList.of(1, 2, 3));
		assertThat(gson.toJson(wrapper), is("{\"value\":\"[1,2,3]\"}"));
	}

	private static final class Wrapper {

		@JsonAdapter(UnpackedJsonTypeAdapterFactory.class)
		final List<Integer> value;

		private Wrapper(final List<Integer> value) {
			this.value = value;
		}

	}

}
