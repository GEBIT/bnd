package test;

import java.io.IOException;
import java.net.URI;

import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.OSInformation;
import aQute.bnd.osgi.Processor;
import aQute.lib.strings.Strings;
import junit.framework.TestCase;

public class ProcessorTest extends TestCase {

	public void testFixupMerge() throws IOException {
		Processor p = new Processor();
		p.setProperty("-fixupmessages.foo", "foo");
		p.setProperty("-fixupmessages.bar", "bar");
		p.error("foo");
		p.error("bar");
		assertTrue(p.check());
		p.close();
	}

	public void testFixupMacro() throws IOException {
		Processor p = new Processor();
		p.setProperty("skip", "foo");
		p.setProperty("-fixupmessages", "${skip},bar");
		p.error("foo");
		p.error("bar");
		assertTrue(p.check());
		p.close();
	}

	public void testNative() throws IOException {
		Processor p = new Processor();

		String s = p._native_capability(new String[] {
				"_native_capability"
		});
		System.out.println(s);
		assertNotNull(s);

		s = Strings.join(OSInformation.getProcessorAliases("x86-64"));
		assertEquals(s, Strings.join(OSInformation.getProcessorAliases("X86-64")));
		assertEquals(s, Strings.join(OSInformation.getProcessorAliases("x86_64")));
		assertEquals(s, Strings.join(OSInformation.getProcessorAliases("AMD64")));
		assertEquals(s, Strings.join(OSInformation.getProcessorAliases("em64t")));

		p.close();
	}

	public static void testPlugins() {

	}

	public void testFixupMessages() throws IOException {
		Processor p = new Processor();
		p.setTrace(true);

		p.error("abc");
		assertFalse(p.isOk());

		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;restrict:=warning");
		assertEquals(1, p.getErrors().size());
		assertEquals(0, p.getWarnings().size());

		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc");
		assertEquals(0, p.getErrors().size());
		assertEquals(0, p.getWarnings().size());

		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;is:=error");
		assertEquals(1, p.getErrors().size());
		assertEquals(0, p.getWarnings().size());

		p.clear();
		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;is:=warning");
		assertEquals(0, p.getErrors().size());
		assertEquals(1, p.getWarnings().size());

		p.clear();
		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;replace:=def");
		assertEquals("def", p.getErrors().get(0));
		assertEquals(0, p.getWarnings().size());

		p.clear();
		p.setProperty(Constants.FIXUPMESSAGES, "'abc def\\s*ghi';is:=warning");
		p.error("abc def  \t\t   ghi");
		assertEquals(0, p.getErrors().size());
		assertEquals(1, p.getWarnings().size());

		p.error("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;replace:=def;is:=warning");
		assertEquals("def", p.getWarnings().get(0));
		assertEquals(0, p.getErrors().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;restrict:=error");
		assertEquals(0, p.getErrors().size());
		assertEquals(1, p.getWarnings().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc");
		assertEquals(0, p.getErrors().size());
		assertEquals(0, p.getWarnings().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;is:=warning");
		assertEquals(0, p.getErrors().size());
		assertEquals(1, p.getWarnings().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;is:=error");
		assertEquals(1, p.getErrors().size());
		assertEquals(0, p.getWarnings().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;replace:=def");
		assertEquals("def", p.getWarnings().get(0));
		assertEquals(0, p.getErrors().size());

		p.clear();
		p.warning("abc");
		p.setProperty(Constants.FIXUPMESSAGES, "abc;replace:=def;is:=error");
		assertEquals("def", p.getErrors().get(0));
		assertEquals(0, p.getWarnings().size());
		p.close();
	}

	public static void testDuplicates() {
		assertEquals("", Processor.removeDuplicateMarker("~"));

		assertTrue(Processor.isDuplicate("abc~"));
		assertTrue(Processor.isDuplicate("abc~~~~~~~~~"));
		assertTrue(Processor.isDuplicate("~"));
		assertFalse(Processor.isDuplicate(""));
		assertFalse(Processor.isDuplicate("abc"));
		assertFalse(Processor.isDuplicate("ab~c"));
		assertFalse(Processor.isDuplicate("~abc"));

		assertEquals("abc", Processor.removeDuplicateMarker("abc~"));
		assertEquals("abc", Processor.removeDuplicateMarker("abc~~~~~~~"));
		assertEquals("abc", Processor.removeDuplicateMarker("abc"));
		assertEquals("ab~c", Processor.removeDuplicateMarker("ab~c"));
		assertEquals("~abc", Processor.removeDuplicateMarker("~abc"));
		assertEquals("", Processor.removeDuplicateMarker(""));
		assertEquals("", Processor.removeDuplicateMarker("~~~~~~~~~~~~~~"));
	}

	public static void appendPathTest() throws Exception {
		assertEquals("a/b/c", Processor.appendPath("", "a/b/c/"));
		assertEquals("a/b/c", Processor.appendPath("", "/a/b/c"));
		assertEquals("a/b/c", Processor.appendPath("/", "/a/b/c/"));
		assertEquals("a/b/c", Processor.appendPath("a", "b/c/"));
		assertEquals("a/b/c", Processor.appendPath("a", "b", "c"));
		assertEquals("a/b/c", Processor.appendPath("a", "b", "/c/"));
		assertEquals("a/b/c", Processor.appendPath("/", "a", "b", "/c/"));
		assertEquals("a/b/c", Processor.appendPath("////////", "////a////b///c//"));

	}

	public void testUriMacro() throws Exception {
		try (Processor p = new Processor()) {
			String baseURI = p.getBaseURI().toString();
			String otherURI = new URI("file:/some/dir/").toString();
			p.setProperty("uri1", "${uri;dist/bundles}");
			p.setProperty("uri2", "${uri;/dist/bundles}");
			p.setProperty("uri3", "${uri;file:dist/bundles}");
			p.setProperty("uri4", "${uri;file:/dist/bundles}");
			p.setProperty("uri5", "${uri;dist/bundles;" + otherURI + "}");
			p.setProperty("uri6", "${uri;/dist/bundles;" + otherURI + "}");
			p.setProperty("uri7", "${uri;file:dist/bundles;" + otherURI + "}");
			p.setProperty("uri8", "${uri;file:/dist/bundles;" + otherURI + "}");
			p.setProperty("uri9", "${uri;http://foo.com/dist/bundles}");
			p.setProperty("uri10", "${uri;http://foo.com/dist/bundles;" + otherURI + "}");
			p.setProperty("uri11", "${uri;.}");
			String uri1 = p.getProperty("uri1");
			String uri2 = p.getProperty("uri2");
			String uri3 = p.getProperty("uri3");
			String uri4 = p.getProperty("uri4");
			String uri5 = p.getProperty("uri5");
			String uri6 = p.getProperty("uri6");
			String uri7 = p.getProperty("uri7");
			String uri8 = p.getProperty("uri8");
			String uri9 = p.getProperty("uri9");
			String uri10 = p.getProperty("uri10");
			String uri11 = p.getProperty("uri11");
			assertEquals(baseURI + "dist/bundles", uri1);
			assertEquals("file:/dist/bundles", uri2);
			assertEquals(baseURI + "dist/bundles", uri3);
			assertEquals("file:/dist/bundles", uri4);
			assertEquals(otherURI + "dist/bundles", uri5);
			assertEquals("file:/dist/bundles", uri6);
			assertEquals(otherURI + "dist/bundles", uri7);
			assertEquals("file:/dist/bundles", uri8);
			assertEquals("http://foo.com/dist/bundles", uri9);
			assertEquals("http://foo.com/dist/bundles", uri10);
			assertEquals(baseURI, uri11);
			assertTrue(p.check());
		}
	}

	public void testUriMacroTooFew() throws IOException {
		try (Processor p = new Processor()) {
			p.setProperty("urix", "${uri}");
			String uri = p.getProperty("urix");
			assertTrue(p.check("too few arguments", "No translation found for macro: uri"));
		}
	}

	public void testUriMacroTooMany() throws IOException {
		try (Processor p = new Processor()) {
			p.setProperty("urix", "${uri;file:/dist/bundles;file:/some/dir/;another}");
			String uri = p.getProperty("urix");
			assertTrue(p.check("too many arguments", "No translation found for macro: uri"));
		}
	}

	public void testUriMacroNoBase() throws IOException {
		try (Processor p = new Processor()) {
			p.setBase(null);
			p.setProperty("urix", "${uri;dist/bundles}");
			String uri = p.getProperty("urix");
			assertTrue(p.check("No base dir set", "No translation found for macro: uri"));
		}
	}

}
