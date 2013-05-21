/*
 * Copyright (C) the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.lucis.core;

import static org.testng.Assert.assertEquals;
import net.sf.derquinse.lucis.Page;
import net.sf.lucis.core.impl.DefaultWriter;
import net.sf.lucis.core.impl.RAMStore;
import net.sf.lucis.core.support.Queryables;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

@Test
public class LucisQueryTest {
	static final String ID = "ID";
	static final String ANALIZED = "ANALIZED";
	static final String STR = "STR";

	Store<Long> store;
	Writer writer = new DefaultWriter();
	Queryable queryable;

	private final String[] lorem = new String[] {
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas arcu neque, porta sit amet eleifend in, molestie et orci. Quisque ac elit neque, a convallis metus. Quisque et cursus justo. Nam ultrices nunc quis nulla rhoncus sodales nec at nunc. Donec quis libero quis felis facilisis condimentum. Praesent diam mauris, mattis quis scelerisque a, iaculis ac tortor. Nam vestibulum, ante tristique ultrices tristique, lorem arcu bibendum metus, at eleifend lectus dui ut eros. Donec massa nulla, congue id placerat vitae, dignissim nec augue. Pellentesque sem massa, porttitor eu suscipit nec, volutpat vel lacus. Donec sed malesuada felis.",
			"Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed pharetra dignissim neque. Nullam viverra diam non lacus egestas ac ultrices felis pretium. In tempor, purus vitae facilisis tempus, risus turpis tristique nunc, eu aliquet elit lectus et mi. Duis vitae dapibus eros. Nullam elementum eleifend leo consectetur lacinia. Suspendisse posuere vulputate porttitor. Duis vitae erat nibh. Nulla ullamcorper mi quis mauris malesuada fringilla. Pellentesque nec purus nisi. Mauris varius, velit nec consectetur tristique, turpis nibh malesuada mi, sit amet posuere magna augue nec felis. Integer ligula velit, luctus at scelerisque non, interdum eget leo. Integer ut mi diam. Mauris faucibus, dolor vitae ullamcorper hendrerit, nisi orci elementum diam, et porta elit est vel sapien. Cras imperdiet purus quis nunc lacinia ultrices. Curabitur eu neque quis neque condimentum consequat nec sed mi. Sed in ante vel mauris lacinia egestas. ",
			"Sed pellentesque nunc a dui elementum sollicitudin volutpat nisl sodales. Proin molestie purus iaculis nulla ornare at malesuada metus sagittis. Fusce vehicula, massa rhoncus blandit pulvinar, magna orci commodo mi, vel pretium augue ante vitae est. Aliquam erat volutpat. Nam placerat commodo velit sit amet pharetra. Integer vulputate condimentum sagittis. Vestibulum tincidunt viverra enim, a sodales neque ornare eget. Aliquam id velit orci. Donec ac ipsum vel ipsum aliquam adipiscing id id nunc. Etiam sed hendrerit nunc. Integer at nunc nec dui elementum sodales. Nulla rutrum, ipsum vitae blandit bibendum, eros est pellentesque arcu, sed laoreet mi eros vitae magna. Pellentesque vulputate, lacus et laoreet rutrum, dui mauris vestibulum turpis, eget sagittis urna massa id nunc. Phasellus cursus auctor vestibulum. Aliquam ac nisi sit amet arcu venenatis interdum. Maecenas dui ligula, lacinia id auctor nec, adipiscing ac nisi. ",
			"Mauris vehicula enim sed urna rutrum sit amet dapibus ipsum iaculis. Ut commodo rutrum tortor, ac venenatis libero tincidunt sit amet. Donec ante tortor, auctor vel pulvinar suscipit, dapibus vel lacus. Maecenas ut felis nunc. Morbi vitae convallis tortor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut metus elit, vulputate convallis mattis vel, ullamcorper in urna. Phasellus volutpat faucibus convallis. Nunc eu nisi eu eros bibendum condimentum quis id lectus. Integer leo orci, eleifend in vulputate eu, pharetra non mauris. Sed posuere leo non dolor laoreet sed euismod lacus tincidunt. Pellentesque eget enim et eros ullamcorper vestibulum fermentum in orci. Morbi non sollicitudin lectus. Nunc rutrum rhoncus magna, a varius mi fermentum at. In hac habitasse platea dictumst. Duis bibendum metus ut ante euismod hendrerit. ",
			"Donec tincidunt blandit pulvinar. Aliquam erat volutpat. In sodales justo a dolor feugiat fermentum. Aenean at felis ac magna euismod lobortis in nec magna. Sed pellentesque orci ac ipsum cursus dignissim. Integer non lorem dolor. Cras ornare risus non ipsum fringilla elementum. Integer pulvinar sollicitudin ipsum, id aliquam quam semper et. Mauris nunc metus, mollis sed euismod sit amet, euismod at erat. Aenean nec enim non magna porttitor euismod pretium ac justo. ",
			"Nullam accumsan pulvinar tincidunt. Maecenas vitae commodo lorem. Nam a libero metus, vel aliquam massa. Vivamus euismod imperdiet lacinia. Ut diam ipsum, tempor eget euismod vel, tempus quis nisi. Sed non nisl lectus, ut accumsan orci. Maecenas venenatis lobortis sem, fermentum consequat ligula posuere quis. Vestibulum vitae nisi nec massa mollis laoreet. Aenean arcu erat, tempor sed pulvinar aliquam, faucibus vitae tortor. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Suspendisse neque nibh, pulvinar a condimentum sit amet, cursus et orci. ",
			"Praesent consectetur dui euismod lorem accumsan ultrices. Suspendisse in sem sapien, nec accumsan urna. In hac habitasse platea dictumst. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed ipsum mi, facilisis quis ullamcorper sit amet, blandit vel augue. Donec eleifend lorem vel turpis fermentum et posuere est vestibulum. Proin at turpis diam, non tempor tellus. Aenean eu purus vitae felis porta consequat id sed nunc. Curabitur rutrum convallis elit, ac pharetra ipsum blandit sollicitudin. Sed volutpat purus id leo egestas vel sagittis mi pulvinar. In hendrerit ultrices nisl, at dictum turpis accumsan eget. Ut tempus, massa et egestas convallis, augue arcu tincidunt mi, sit amet hendrerit turpis neque id metus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nulla convallis ultrices mi at scelerisque. Aenean vestibulum pellentesque massa, eget faucibus nulla aliquet ac. Quisque tellus orci, sagittis eu rhoncus sit amet, tempus eget lorem. Quisque egestas, odio sit amet ultrices pretium, sem nulla ullamcorper felis, non congue nisl mi nec libero. Sed vitae sapien sit amet nisl lobortis mattis. Quisque convallis adipiscing aliquet. Sed molestie orci nec mauris dignissim rutrum.",
			"Aliquam nec egestas urna. Nulla ligula nisi, gravida at gravida et, suscipit id urna. Etiam cursus nisl porta leo auctor euismod. Vivamus elit risus, mollis id blandit in, cursus id augue. Morbi dapibus luctus fermentum. Duis quis sapien vitae ante scelerisque euismod eu et neque. Nunc vestibulum massa elementum turpis ultricies venenatis. Fusce sagittis nibh porta enim molestie convallis. Ut vestibulum suscipit ante quis facilisis. Nullam nulla nisi, aliquet nec congue at, aliquet non sem. Integer vestibulum dignissim sem egestas luctus.",
			"Vestibulum auctor magna ac est dictum quis mattis nisl viverra. Praesent id erat et est scelerisque porttitor eget ultrices justo. Aliquam nec urna lacus, rutrum suscipit neque. In hac habitasse platea dictumst. Suspendisse diam dolor, euismod ac porta vel, feugiat eget odio. Pellentesque nec neque dolor. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed a lorem metus. Proin sagittis convallis quam at interdum. Fusce vel pretium diam.",
			"Suspendisse cursus hendrerit nunc, quis tempus lacus gravida vitae. Nullam tincidunt placerat tellus non iaculis. Phasellus luctus eros at massa elementum tempor. Morbi eros lorem, lobortis vitae dapibus at, congue sit amet mauris. Aenean at blandit quam. Cras eu tellus eget massa congue consequat. Integer dapibus, elit in imperdiet sodales, felis dui faucibus augue, ac imperdiet libero velit tempor diam. Phasellus et facilisis ipsum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nunc eget leo sapien. Curabitur mollis, neque quis suscipit viverra, nunc nulla mollis odio, nec dapibus justo quam eget metus. Nunc accumsan, velit ac rutrum condimentum, libero lectus pulvinar odio, ut eleifend tellus tortor et odio. Quisque sit amet augue nec dolor consequat congue." };

	@Test
	public void createStore() {
		store = new RAMStore<Long>();
		Assert.assertNotNull(store);
	}

	@Test(dependsOnMethods = "createStore")
	public void add() throws InterruptedException {
		writer.write(store, add(1, 100, 1L));
		assertEquals(store.getCheckpoint(), Long.valueOf(1L));
	}

	@Test(dependsOnMethods = "add")
	public void createQueryable() {
		this.queryable = Queryables.simple(store);
		Assert.assertNotNull(queryable);
	}

	@Test(dependsOnMethods = "createQueryable")
	public void searchHighlight() {
		Term term = new Term(ANALIZED, "ipsum");
		Query query = new TermQuery(term);
		Page<Node> result = queryable.query(LucisQuery.page(query, null, null, MAPPER, 1, 10,
				Highlight.of(ImmutableMap.of(ANALIZED, 5))));
		Assert.assertNotNull(result);
	}

	private Batch<Long, Object> add(int from, int to, long cp) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		for (int i = from; i <= to; i++) {
			builder.add(document(i));
		}
		return builder.build(cp);
	}

	private Document document(int value) {
		final Document document = new Document();
		final String text = lorem[value % 10];
		document.add(new Field(ID, "ID" + value, Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(ANALIZED, text, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field(STR, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return document;
	}

	private static final DocMapper<Node> MAPPER = new DocMapper<Node>() {
		public Node map(int id, float score, Document doc, Multimap<String, String> fragments) {
			final Node node = new Node();
			node.id = doc.get(ID);
			node.analyzed = doc.get(ANALIZED);
			node.fragments = fragments;
			return node;
		}
	};

	@SuppressWarnings("unused")
	private static class Node {
		String id;
		String analyzed;
		Multimap<String, String> fragments;

		private void test(String value) {
			assertEquals(analyzed, value);
		}
	}

}
