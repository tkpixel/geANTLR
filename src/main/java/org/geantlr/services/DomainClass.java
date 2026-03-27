package org.geantlr.services;

import java.util.List;
import java.util.Map;

/**
 * Represents a domain class parsed from a PlantUML diagram.
 *
 * @param name       the class name
 * @param fields     list of field/method names (for auto-complete display)
 * @param fieldTypes map of field name → declared type name (e.g. "datenpunkt" → "Datenpunkt"),
 *                   used to resolve chained dot-accessor expressions like ctx.datenpunkt.
 */
public record DomainClass(String name, List<String> fields, Map<String, String> fieldTypes) {

    /** Convenience constructor for classes without type information (backwards compat). */
    public DomainClass(String name, List<String> fields) {
        this(name, fields, new java.util.HashMap<>());
    }
}
