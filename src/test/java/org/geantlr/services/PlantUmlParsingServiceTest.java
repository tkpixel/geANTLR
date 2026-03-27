package org.geantlr.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlantUmlParsingServiceTest {

    private PlantUmlParsingService service;

    // The example PUML from the user (unclosed braces, single-line format)
    private static final String EXAMPLE_PUML = "@startuml\n"
        + "skinparam classAttributeIconSize 0\n"
        + "skinparam linetype ortho\n"
        + "hide empty methods\n"
        + "title Klassendiagramm: Datenmodell QS-Tool mit Rückabbildbarkeit je Attribut\n"
        + "\n"
        + "rectangle \"Quellenmodell\" {\n"
        + "\n"
        + "abstract class QuellenRef\n"
        + "\n"
        + "class ExcelRef { datei: String\n blatt: String\n zeile: int\n spalte: String\n}\n"
        + "\n"
        + "note bottom of ExcelRef\n Spalte bezieht sich auf die Excel-Spalte\nend note\n"
        + "\n"
        + "class PlanProRef { datei: String\n objektTyp: String\n guid: String\n xpath: String\n}\n"
        + "\n"
        + "ExcelRef -up-|> QuellenRef\n"
        + "PlanProRef -up-|> QuellenRef\n"
        + "\n"
        + "class WertMitQuelle { }\n"
        + "\n"
        + "WertMitQuelle \"1\" *-- \"1\" QuellenRef\n"
        + "\n"
        + "}\n"
        + "\n"
        + "rectangle \"Fachmodell Datenpunkt-Kontext\" {\n"
        + "\n"
        + "class DatenpunktKontextSammlung { DatenpunktKontexte: List\n}\n"
        + "\n"
        + "class DatenpunktKontext { datenpunkt: Datenpunkt\n bezugspunkt: Bezugspunkt\n kmSprung: KmSprung\n bmm: BigMetalMass\n bemerkungen: WertMitQuelle\n}\n"
        + "\n"
        + "class Balise { anordnungImDP: WertMitQuelle\n}\n"
        + "\n"
        + "class Datenpunkt { id: String }\n"
        + "class Bezugspunkt { id: String }\n"
        + "\n"
        + "}\n"
        + "@enduml";

    @BeforeEach
    void setUp() {
        service = new PlantUmlParsingService();
    }

    @Test
    void testParsesClassesWithFields() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        assertFalse(cache.isEmpty(), "Domain model cache should not be empty");
        assertTrue(cache.containsKey("ExcelRef"), "Should contain ExcelRef");
        assertTrue(cache.containsKey("PlanProRef"), "Should contain PlanProRef");
        assertTrue(cache.containsKey("DatenpunktKontext"), "Should contain DatenpunktKontext");
        assertTrue(cache.containsKey("Balise"), "Should contain Balise");
    }

    @Test
    void testExcelRefHasFields() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass excelRef = cache.get("ExcelRef");
        assertNotNull(excelRef, "ExcelRef should be in cache");
        assertFalse(excelRef.fields().isEmpty(), "ExcelRef should have fields");
        assertTrue(excelRef.fields().contains("datei"), "ExcelRef should have field 'datei'");
        assertTrue(excelRef.fields().contains("blatt"), "ExcelRef should have field 'blatt'");
        assertTrue(excelRef.fields().contains("zeile"), "ExcelRef should have field 'zeile'");
        assertTrue(excelRef.fields().contains("spalte"), "ExcelRef should have field 'spalte'");
    }

    @Test
    void testDatenpunktKontextHasFields() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass ctx = cache.get("DatenpunktKontext");
        assertNotNull(ctx, "DatenpunktKontext should be in cache");
        assertFalse(ctx.fields().isEmpty(), "DatenpunktKontext should have fields");
        assertTrue(ctx.fields().contains("datenpunkt"), "DatenpunktKontext should have field 'datenpunkt'");
        assertTrue(ctx.fields().contains("bezugspunkt"), "DatenpunktKontext should have field 'bezugspunkt'");
    }

    @Test
    void testSingleLinePuml() {
        // Test with the single-line format (as received from user)
        String singleLine = "@startuml skinparam classAttributeIconSize 0 "
            + "class ExcelRef { datei: String blatt: String zeile: int spalte: String } "
            + "@enduml";
        service.parseDomainModel(singleLine);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        assertTrue(cache.containsKey("ExcelRef"), "Should parse ExcelRef from single-line format");
        DomainClass excelRef = cache.get("ExcelRef");
        assertFalse(excelRef.fields().isEmpty(), "ExcelRef should have fields in single-line format");
        assertTrue(excelRef.fields().contains("datei"), "ExcelRef should have field 'datei' in single-line format");
    }

    @Test
    void testFieldTypesAreCaptured() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass ctx = cache.get("DatenpunktKontext");
        assertNotNull(ctx);
        assertFalse(ctx.fieldTypes().isEmpty(), "DatenpunktKontext should have field type mappings");
        assertEquals("Datenpunkt", ctx.fieldTypes().get("datenpunkt"),
            "Field 'datenpunkt' should map to type 'Datenpunkt'");
        assertEquals("Bezugspunkt", ctx.fieldTypes().get("bezugspunkt"),
            "Field 'bezugspunkt' should map to type 'Bezugspunkt'");
    }

    @Test
    void testDatenpunktKontextDoesNotContainSammlungBackReference() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass ctx = cache.get("DatenpunktKontext");
        assertNotNull(ctx);
        // The parent collection must NOT appear as a field on the child –
        // DatenpunktKontextSammlung *-- DatenpunktKontext means the Sammlung owns the Kontext,
        // not the other way around.
        assertFalse(ctx.fields().contains("datenpunktKontextSammlung"),
            "DatenpunktKontext must not have a back-reference field 'datenpunktKontextSammlung', actual fields: " + ctx.fields());
        assertFalse(ctx.fields().contains("DatenpunktKontextSammlung"),
            "DatenpunktKontext must not have 'DatenpunktKontextSammlung' as field");
    }

    @Test
    void testWertMitQuelleHasImplicitField() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass wmq = cache.get("WertMitQuelle");
        assertNotNull(wmq, "WertMitQuelle should be in cache");
        assertTrue(wmq.fields().contains("wert"),
            "WertMitQuelle should have implicit 'wert' field");
    }

    @Test
    void testWertMitQuelleHasQuelleFromRelation() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        DomainClass wmq = cache.get("WertMitQuelle");
        assertNotNull(wmq, "WertMitQuelle should be in cache");
        // "QuellenRef" → shortened to "quelle" via suffix-stripping of "Ref"
        assertTrue(wmq.fields().contains("quelle") || wmq.fields().contains("quellenRef"),
            "WertMitQuelle should have 'quelle' or 'quellenRef' from UML relation to QuellenRef, fields: " + wmq.fields());
        // the relation field should point to QuellenRef
        String quelleType = wmq.fieldTypes().getOrDefault("quelle", wmq.fieldTypes().get("quellenRef"));
        assertEquals("QuellenRef", quelleType,
            "quelle/quellenRef field should resolve to type QuellenRef");
    }

    @Test
    void testChainedDotAccessResolution() {
        service.parseDomainModel(EXAMPLE_PUML);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        // Simulate: DatenpunktKontext ctx → ctx.datenpunkt. → should give fields of Datenpunkt
        DomainClass ctx = cache.get("DatenpunktKontext");
        assertNotNull(ctx);
        String datenpunktType = ctx.fieldTypes().get("datenpunkt");
        assertEquals("Datenpunkt", datenpunktType);

        DomainClass datenpunkt = cache.get("Datenpunkt");
        assertNotNull(datenpunkt, "Datenpunkt class should be in cache");
        assertFalse(datenpunkt.fields().isEmpty(), "Datenpunkt should have fields");
    }

    @Test
    void testIgnoreClassesInsideNotes() {
        String pumlWithNotes = "@startuml\n"
            + "class Foo {\n"
            + "  bar: String\n"
            + "}\n"
            + "note bottom of Foo\n"
            + "  this is a note with class keyword inside\n"
            + "  class FakeClass { fakeField: String }\n"
            + "end note\n"
            + "@enduml";
        service.parseDomainModel(pumlWithNotes);
        Map<String, DomainClass> cache = service.getDomainModelCache();

        assertFalse(cache.containsKey("FakeClass"), "Class inside note block should not be parsed");
        assertTrue(cache.containsKey("Foo"), "Foo should be parsed");
    }
}
