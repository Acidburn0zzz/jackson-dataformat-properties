package com.fasterxml.jackson.dataformat.javaprop;

import java.util.Map;

import org.junit.Assert;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleParsingTest extends ModuleTestBase
{
    private final ObjectMapper MAPPER = mapperForProps();

    private final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    public void testSimpleNonNested() throws Exception {
        _testSimpleNonNested(false);
        _testSimpleNonNested(true);
    }

    private void _testSimpleNonNested(boolean useBytes) throws Exception
    {
        final String INPUT = "firstName=Bob\n"
                +"lastName=Palmer\n"
                +"gender=MALE\n"
                +"verified=true\n"
                +"userImage=AQIDBA==\n";
        FiveMinuteUser result = _mapFrom(MAPPER, INPUT, FiveMinuteUser.class, useBytes);
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(4, result.getUserImage().length);

        // and then with streaming parser
        final String INPUT2 = "firstName=Bob\n"
                +"verified=true\n";
        JsonParser p = MAPPER.getFactory().createParser(INPUT2);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(3, b.length);
        // and verify it'll remain stable
        byte[] b2 = p.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(b, b2);
        p.close();
    }

    public void testSimpleRectangle() throws Exception {
        _testSimpleRectangle(false);
        _testSimpleRectangle(true);
    }

    private void _testSimpleRectangle(boolean useBytes) throws Exception
    {
        final String INPUT = "topLeft.x=1\n"
                +"topLeft.y=-2\n"
                +"bottomRight.x=5\n"
                +"bottomRight.y=10\n";
        Rectangle result = _mapFrom(MAPPER, INPUT, Rectangle.class, useBytes);
        assertEquals(5, result.bottomRight.x);
        assertEquals(10, result.bottomRight.y);
    }

    public void testNonSplittingParsing() throws Exception {
        JavaPropsSchema schema = JavaPropsSchema.emptySchema()
                .withoutPathSeparator();
        Map<?,?> result = MAPPER.readerFor(Map.class)
                .with(schema)
                .readValue("a.b.c = 3");
        assertEquals("{\"a.b.c\":\"3\"}", JSON_MAPPER.writeValueAsString(result));
    }
}
