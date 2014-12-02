package com.locima.xml2csv.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.util.XmlUtil;

public class FixedCardinalityTests {

	private Mapping createMapping(String name, MultiValueBehaviour mvb, int min, int max) throws XMLException {
		return new Mapping(null, name, NameFormat.NO_COUNTS, 0, mvb, XmlUtil.createXPathValue(null, "."), min, max);
	}

	private MappingList createMappingList(String name, Mapping... mappings) {
		MappingList list = new MappingList();
		if (mappings != null) {
			for (Mapping mapping : mappings) {
				list.add(mapping);
			}
		}
		return list;
	}

	@Test
	public void testForMapping() throws Exception {
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 0, 0).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 1, 0).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 0, 2).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 2, 3).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 3, 3).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.LAZY, 4, 3).hasFixedOutputCardinality());

		assertEquals(true, createMapping("Test", MultiValueBehaviour.GREEDY, 2, 2).hasFixedOutputCardinality());
		assertEquals(true, createMapping("Test", MultiValueBehaviour.GREEDY, 5, 5).hasFixedOutputCardinality());

		assertEquals(false, createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0).hasFixedOutputCardinality());
		assertEquals(false, createMapping("Test", MultiValueBehaviour.GREEDY, 0, 1).hasFixedOutputCardinality());
		assertEquals(false, createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0).hasFixedOutputCardinality());
		assertEquals(false, createMapping("Test", MultiValueBehaviour.GREEDY, 5, 6).hasFixedOutputCardinality());
		assertEquals(false, createMapping("Test", MultiValueBehaviour.GREEDY, 1, 20).hasFixedOutputCardinality());

	}

	@Test
	public void testForMappingList() throws Exception {
		assertEquals(true, createMappingList("Test").hasFixedOutputCardinality());
		assertEquals(true, createMappingList("Test", createMapping("Test", MultiValueBehaviour.LAZY, 0, 0)).hasFixedOutputCardinality());

		assertEquals(true, createMappingList("Test", createMapping("Test", MultiValueBehaviour.GREEDY, 2, 2)).hasFixedOutputCardinality());

		assertEquals(false, createMappingList("Test", createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0)).hasFixedOutputCardinality());
		assertEquals(false,
						createMappingList("Test", createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0),
										createMapping("Test", MultiValueBehaviour.LAZY, 0, 0)).hasFixedOutputCardinality());
		assertEquals(false,
						createMappingList("Test", createMapping("Test", MultiValueBehaviour.LAZY, 0, 0),
										createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0)).hasFixedOutputCardinality());

		assertEquals(false,
						createMappingList("Test", createMapping("Test", MultiValueBehaviour.LAZY, 0, 0),
										createMapping("Test", MultiValueBehaviour.LAZY, 1, 1), createMapping("Test", MultiValueBehaviour.LAZY, 2, 7),
										createMapping("Test", MultiValueBehaviour.LAZY, 5, 7),
										createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0)).hasFixedOutputCardinality());

		assertEquals(true,
						createMappingList("Test", createMapping("Test", MultiValueBehaviour.LAZY, 0, 0),
										createMapping("Test", MultiValueBehaviour.LAZY, 1, 1),
										createMapping("Test", MultiValueBehaviour.GREEDY, 2, 2),
										createMapping("Test", MultiValueBehaviour.LAZY, 5, 7), createMapping("Test", MultiValueBehaviour.LAZY, 0, 0)).hasFixedOutputCardinality());

		assertEquals(false,
						createMappingList("Test", createMapping("Test", MultiValueBehaviour.LAZY, 0, 0),
										createMapping("Test", MultiValueBehaviour.LAZY, 1, 1),
										createMapping("Test", MultiValueBehaviour.GREEDY, 0, 0),
										createMapping("Test", MultiValueBehaviour.LAZY, 5, 7), createMapping("Test", MultiValueBehaviour.LAZY, 0, 0)).hasFixedOutputCardinality());

	}
}
