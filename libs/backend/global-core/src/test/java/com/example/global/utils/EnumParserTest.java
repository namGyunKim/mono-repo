package com.example.global.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnumParserTest {

    private enum TestColor { RED, GREEN, BLUE }

    @Test
    void toNameMap_null_returns_empty_map() {
        final Map<String, TestColor> result = EnumParser.toNameMap(null);
        assertThat(result).isEmpty();
    }

    @Test
    void toNameMap_empty_array_returns_empty_map() {
        final TestColor[] empty = new TestColor[0];
        final Map<String, TestColor> result = EnumParser.toNameMap(empty);
        assertThat(result).isEmpty();
    }

    @Test
    void toNameMap_produces_uppercase_keyed_map() {
        final Map<String, TestColor> result = EnumParser.toNameMap(TestColor.values());
        assertThat(result).containsKeys("RED", "GREEN", "BLUE");
        assertThat(result.get("RED")).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_map_exact_match() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, "RED", null);
        assertThat(result).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_map_case_insensitive() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, "green", null);
        assertThat(result).isEqualTo(TestColor.GREEN);
    }

    @Test
    void fromNameIgnoreCase_map_trimmed_input() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, "  blue  ", null);
        assertThat(result).isEqualTo(TestColor.BLUE);
    }

    @Test
    void fromNameIgnoreCase_map_null_raw_returns_default() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, null, TestColor.RED);
        assertThat(result).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_map_blank_raw_returns_default() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, "   ", TestColor.RED);
        assertThat(result).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_map_unmatched_returns_default() {
        final Map<String, TestColor> nameMap = EnumParser.toNameMap(TestColor.values());
        final TestColor result = EnumParser.fromNameIgnoreCase(nameMap, "YELLOW", TestColor.RED);
        assertThat(result).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_class_exact_match() {
        final TestColor result = EnumParser.fromNameIgnoreCase(TestColor.class, "RED");
        assertThat(result).isEqualTo(TestColor.RED);
    }

    @Test
    void fromNameIgnoreCase_class_case_insensitive() {
        final TestColor result = EnumParser.fromNameIgnoreCase(TestColor.class, "green");
        assertThat(result).isEqualTo(TestColor.GREEN);
    }

    @Test
    void fromNameIgnoreCase_class_null_raw_returns_null() {
        final TestColor result = EnumParser.fromNameIgnoreCase(TestColor.class, null);
        assertThat(result).isNull();
    }

    @Test
    void fromNameIgnoreCase_class_null_type_returns_null() {
        final TestColor result = EnumParser.fromNameIgnoreCase(null, "RED");
        assertThat(result).isNull();
    }

    @Test
    void fromNameIgnoreCase_class_blank_returns_null() {
        final TestColor result = EnumParser.fromNameIgnoreCase(TestColor.class, "   ");
        assertThat(result).isNull();
    }
}
