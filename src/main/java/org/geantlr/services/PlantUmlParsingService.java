package org.geantlr.services;

import jakarta.inject.Singleton;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.abel.Entity;
import net.sourceforge.plantuml.abel.LeafType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class PlantUmlParsingService {

    private final Map<String, DomainClass> domainModelCache = new HashMap<>();

    // Matches: (class|abstract class|interface|enum) ClassName { ... }
    // Body is everything between { and either } or the next class/interface/enum/abstract keyword
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "(?:abstract\\s+class|class|interface|enum)\\s+(\\w+)(?:\\s+extends\\s+\\w+)?(?:\\s+implements\\s+\\w+)?\\s*\\{([^}]*)"
    );
    // Matches a field/method name in the form: [visibility] name : Type  or  [Type] name
    private static final Pattern FIELD_PATTERN = Pattern.compile(
        "(?:[+\\-#~]\\s*)?(\\w+)\\s*(?::\\s*\\S+|\\()"
    );
    // Pattern to strip note blocks before parsing
    private static final Pattern NOTE_PATTERN = Pattern.compile(
        "note\\s+(?:top|bottom|left|right)(?:\\s+of\\s+\\w+)?.*?end\\s+note",
        Pattern.DOTALL
    );

    private static final java.util.Set<String> COLLECTION_TYPES = java.util.Set.of(
        "List", "Set", "Collection", "Map", "Iterable", "ArrayList", "LinkedList", "HashSet"
    );

    public void parseDomainModel(String pumlContent) {
        domainModelCache.clear();
        if (pumlContent == null || pumlContent.isBlank()) return;

        // First attempt: use PlantUML library
        try {
            SourceStringReader reader = new SourceStringReader(pumlContent);
            if (!reader.getBlocks().isEmpty()) {
                var diagram = reader.getBlocks().get(0).getDiagram();
                if (diagram instanceof ClassDiagram cd) {
                    var classes = cd.getEntityFactory().root().getChildren();

                    for (var node : classes) {
                        String className = node.getName();
                        List<String> fields = new ArrayList<>();
                        var data = node.getData();

                        if (data instanceof Entity entity) {
                            LeafType type = entity.getLeafType();
                            if (type == LeafType.CLASS || type == LeafType.ENUM || type == LeafType.INTERFACE || type == LeafType.ABSTRACT_CLASS) {
                                Map<String, String> fieldTypes = new HashMap<>();
                                for (CharSequence member : entity.getBodier().getFieldsToDisplay()) {
                                    String raw = member.toString();
                                    String clean = cleanMember(raw);
                                    if (!clean.isEmpty()) {
                                        fields.add(clean);
                                        extractFieldType(raw, clean, fieldTypes);
                                    }
                                }
                                for (CharSequence member : entity.getBodier().getMethodsToDisplay()) {
                                    String clean = cleanMember(member.toString());
                                    if (!clean.isEmpty()) fields.add(clean);
                                }
                                domainModelCache.put(className, new DomainClass(className, fields, fieldTypes));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PlantUmlParsingService] PlantUML library parsing failed: " + e.getMessage());
        }

        // Fallback / supplement: regex-based parsing to catch classes the library may have missed
        // (e.g. unclosed braces, single-line format)
        parseDomainModelViaRegex(pumlContent);

        // Third pass: derive fields from UML relations (e.g. "A *-- B" → A gets field b: B)
        parseDomainModelViaRelations(pumlContent);

        // Fourth pass: add implicit "wert" field to classes that appear to be value wrappers
        // (i.e. their name contains "Wert"/"Wert"/"Value" and they still have no "wert" field)
        addImplicitWertFields();

        // Fifth pass: remove spurious fields that are class names but were never added
        // via an explicit relation (they ended up as fields due to incomplete PUML parsing
        // by the PlantUML library, e.g. when braces are missing).
        removeSpuriousClassNameFields();

        // Diagnostics – printed to stdout so the developer can verify the parsed model
        System.out.println("[PlantUmlParsingService] Parsed " + domainModelCache.size() + " classes:");
        domainModelCache.forEach((name, dc) ->
            System.out.println("  " + name + " → fields: " + dc.fields() + " | types: " + dc.fieldTypes())
        );
    }

    /**
     * Removes fields whose name (as camelCase class name) is a known domain class
     * but that were NOT added through an explicit relation (i.e. not present in fieldTypes).
     * This cleans up artefacts from incomplete PlantUML parsing where class names leak
     * into field lists (e.g. "DatenpunktKontextSammlung" appearing as a field on DatenpunktKontext).
     */
    private void removeSpuriousClassNameFields() {
        for (DomainClass dc : domainModelCache.values()) {
            dc.fields().removeIf(field -> {
                // A field is spurious if:
                // 1. Its PascalCase form is a known class name (e.g. field "datenpunktKontextSammlung"
                //    → class "DatenpunktKontextSammlung")
                // 2. It is NOT registered in fieldTypes (i.e. was not added by an explicit relation
                //    or body field with a declared type pointing to that class)
                if (field == null || field.isEmpty()) return false;
                String pascalCase = Character.toUpperCase(field.charAt(0)) + field.substring(1);
                boolean isKnownClass = domainModelCache.containsKey(pascalCase)
                    || domainModelCache.containsKey(field); // exact match (already PascalCase)
                boolean hasExplicitType = dc.fieldTypes().containsKey(field);
                return isKnownClass && !hasExplicitType;
            });
        }
    }

    /**
     * For every class whose name suggests it is a value-with-source wrapper
     * (name contains "Wert", "Value", or "Quelle") and that does not yet have a "wert" field,
     * we add an implicit "wert" field so that "someField.wert" chains resolve correctly.
     */
    private void addImplicitWertFields() {
        for (DomainClass dc : domainModelCache.values()) {
            String name = dc.name();
            boolean isWertWrapper = name.contains("Wert") || name.contains("Value") || name.contains("wert");
            if (isWertWrapper && !dc.fields().contains("wert")) {
                dc.fields().add("wert");
                // "wert" has no specific domain class type, leave out of fieldTypes
                // so it doesn't try to drill down further
            }
        }
    }

    /**
     * Parses UML relation lines and adds the target class as a field on the source class.
     *
     * Direction rules:
     *   A *-- B   →  A owns B  →  A gets field of type B  (composition, A is parent)
     *   A o-- B   →  A owns B  →  A gets field of type B  (aggregation)
     *   A --> B   →  A→B       →  A gets field of type B  (directed association)
     *   A -- B    →  undirected →  both get a field (pure association, no ownership)
     *   A --|> B  →  inheritance → IGNORED (no field added)
     *   A -up-|> B → inheritance → IGNORED
     *
     * The field name is derived from the target class name in camelCase.
     * Only adds the field if not already explicitly declared.
     */
    private void parseDomainModelViaRelations(String pumlContent) {
        String withoutNotes = NOTE_PATTERN.matcher(pumlContent).replaceAll(" ");

        // Match lines of the form:
        //   Word [mult] <arrow> [mult] Word
        // Capture groups: 1=left class, 2=arrow, 3=right class
        Pattern relationPattern = Pattern.compile(
            "^\\s*(\\w+)\\s*(?:\"[^\"]*\"\\s*)?([-.<>|o*]{2,})(?:\\s*\"[^\"]*\")?\\s+(\\w+)\\s*$",
            Pattern.MULTILINE
        );

        Matcher m = relationPattern.matcher(withoutNotes);
        while (m.find()) {
            String left  = m.group(1);
            String arrow = m.group(2);
            String right = m.group(3);

            if (left.equals(right)) continue;

            if (arrow.contains("|>") || arrow.contains("|<") || arrow.contains("<|") || arrow.contains(">|")) {
                continue;
            }

            boolean isComposition  = arrow.contains("*");
            boolean isAggregation  = arrow.contains("o");
            boolean hasRightArrow  = arrow.endsWith(">");
            boolean hasLeftArrow   = arrow.startsWith("<");
            boolean isUndirected   = !isComposition && !isAggregation && !hasRightArrow && !hasLeftArrow;

            boolean leftOwnsRight  = isComposition && !arrow.startsWith("--*")
                                   || isAggregation && !arrow.startsWith("--o")
                                   || hasRightArrow
                                   || isUndirected;

            boolean rightOwnsLeft  = isComposition && arrow.startsWith("--*")
                                   || isAggregation && arrow.startsWith("--o")
                                   || hasLeftArrow;

            // Ensure both ends exist as cache stubs BEFORE trying to add fields,
            // so tryAddRelationField finds both sides even if not declared as classes.
            ensureCacheEntry(left);
            ensureCacheEntry(right);

            if (leftOwnsRight)  tryAddRelationField(left,  right);
            if (rightOwnsLeft)  tryAddRelationField(right, left);
        }
    }

    private void ensureCacheEntry(String className) {
        if (!domainModelCache.containsKey(className)) {
            domainModelCache.put(className, new DomainClass(className, new ArrayList<>(), new HashMap<>()));
        }
    }

    /**
     * Adds a field named after targetClass (camelCase) to ownerClass if:
     * - ownerClass exists in the cache
     * - targetClass exists in the cache
     * - the field is not already present
     *
     * Also adds a shortened variant by stripping common suffixes
     * (Ref, DTO, VO, Entity, Model) so that e.g. "QuellenRef" produces
     * both "quellenRef" and "quelle" as candidate field names.
     */
    private void tryAddRelationField(String ownerClassName, String targetClassName) {
        DomainClass owner  = domainModelCache.get(ownerClassName);
        DomainClass target = domainModelCache.get(targetClassName);
        if (owner == null || target == null) return;

        // Check if any existing field already points to this target type → skip entirely
        if (owner.fieldTypes().containsValue(targetClassName)) return;

        // Check if any existing field has a raw collection type (List/Set/etc.) with no domain
        // type – this means the field was declared as e.g. "DatenpunktKontexte: List" and the
        // relation tells us the element type is targetClassName. Upgrade that field's type.
        for (String existingField : owner.fields()) {
            String existingType = owner.fieldTypes().get(existingField);
            if (existingType != null && COLLECTION_TYPES.contains(existingType)) {
                owner.fieldTypes().put(existingField, targetClassName);
                return; // type upgraded – no new field needed
            }
        }

        // No existing field covers this relation – add a camelCase field for it
        String fullFieldName = Character.toLowerCase(targetClassName.charAt(0)) + targetClassName.substring(1);
        addFieldIfAbsent(owner, fullFieldName, targetClassName);

        // Also add shortened variant (e.g. QuellenRef → quelle)
        String shortened = stripClassNameSuffix(targetClassName);
        if (!shortened.equals(targetClassName)) {
            String shortFieldName = Character.toLowerCase(shortened.charAt(0)) + shortened.substring(1);
            if (!shortFieldName.equals(fullFieldName)) {
                addFieldIfAbsent(owner, shortFieldName, targetClassName);
            }
        }
    }

    private void addFieldIfAbsent(DomainClass owner, String fieldName, String targetClassName) {
        if (!owner.fields().contains(fieldName)) {
            owner.fields().add(fieldName);
            owner.fieldTypes().put(fieldName, targetClassName);
        }
    }

    /** Strips common OOP suffix patterns: Ref, DTO, VO, Entity, Model, Service, Type */
    private String stripClassNameSuffix(String className) {
        for (String suffix : new String[]{"Ref", "DTO", "Dto", "VO", "Vo", "Entity", "Model", "Service", "Type", "Info"}) {
            if (className.endsWith(suffix) && className.length() > suffix.length()) {
                return className.substring(0, className.length() - suffix.length());
            }
        }
        return className;
    }

    /**
     * Regex-based fallback parser that can handle single-line PlantUML class definitions
     * and definitions with unclosed braces.
     */
    private void parseDomainModelViaRegex(String pumlContent) {
        // Remove note blocks first (they contain free text that may confuse field parsing)
        String withoutNotes = NOTE_PATTERN.matcher(pumlContent).replaceAll(" ");

        // Normalize: replace multiple whitespace/newlines with single space for simpler matching
        String normalized = withoutNotes.replaceAll("\\r?\\n", " ").replaceAll("\\s{2,}", " ");

        Matcher classMatcher = CLASS_PATTERN.matcher(normalized);
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String rawBody = classMatcher.group(2).trim();

            // If the body wasn't closed by "}", truncate at the next class/enum/interface/abstract keyword
            // so we don't accidentally include the content of following classes
            Pattern nextClassKeyword = Pattern.compile(
                "\\b(?:abstract\\s+class|class|interface|enum|rectangle|@enduml)\\b"
            );
            Matcher nextKw = nextClassKeyword.matcher(rawBody);
            String body = nextKw.find() ? rawBody.substring(0, nextKw.start()).trim() : rawBody;

            if (domainModelCache.containsKey(className) && !domainModelCache.get(className).fields().isEmpty()) {
                // Already parsed by PlantUML library with fields – skip
                continue;
            }

            List<String> fields = new ArrayList<>();
            Map<String, String> fieldTypes = new HashMap<>();

            // Split body by whitespace-boundaries between field declarations
            // Fields look like: "name: Type" or "+name: Type" or "name()"
            // We split on spaces and try to identify field/method names
            String[] parts = body.split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                // Skip visibility markers standalone, keywords, and type annotations
                if (part.matches("[+\\-#~]")) continue;
                // Strip leading visibility char if attached
                String cleaned = part.replaceAll("^[+\\-#~]", "");
                if (cleaned.isEmpty()) continue;

                // If this part ends with ":" it is "fieldName:" pattern
                if (cleaned.endsWith(":")) {
                    String rawName = cleaned.substring(0, cleaned.length() - 1).trim();
                    // Always start field names with lower-case (e.g. DatenpunktKontexte → datenpunktKontexte)
                    String fieldName = Character.toLowerCase(rawName.charAt(0)) + rawName.substring(1);
                    if (isValidIdentifier(fieldName) && !fields.contains(fieldName)) {
                        fields.add(fieldName);
                        if (i + 1 < parts.length) {
                            String typeName = stripGeneric(parts[i + 1]);
                            if (isValidIdentifier(typeName)) fieldTypes.put(fieldName, typeName);
                            i++;
                        }
                        continue;
                    }
                }

                // If the next part is ":" (separate token), this part is the field name
                if (i + 1 < parts.length && parts[i + 1].equals(":")) {
                    String rawName = cleaned.trim();
                    // Always start field names with lower-case
                    String fieldName = Character.toLowerCase(rawName.charAt(0)) + rawName.substring(1);
                    if (isValidIdentifier(fieldName) && !fields.contains(fieldName)) {
                        fields.add(fieldName);
                        if (i + 2 < parts.length) {
                            String typeName = stripGeneric(parts[i + 2]);
                            if (isValidIdentifier(typeName)) fieldTypes.put(fieldName, typeName);
                            i += 2;
                        } else {
                            i += 1;
                        }
                        continue;
                    }
                }

                // Method name: ends with "()" or similar
                if (cleaned.endsWith("()") || cleaned.endsWith("(")) {
                    String methodName = cleaned.replaceAll("\\(.*", "");
                    if (isValidIdentifier(methodName) && !fields.contains(methodName)) {
                        fields.add(methodName);
                    }
                }
            }

            DomainClass existing = domainModelCache.get(className);
            if (existing == null || existing.fields().isEmpty()) {
                // Library parsed nothing or an empty body – use regex result
                domainModelCache.put(className, new DomainClass(className, fields, fieldTypes));
            } else if (!fields.isEmpty()) {
                // Both have fields – supplement missing ones (regex may catch things library missed)
                for (int fi = 0; fi < fields.size(); fi++) {
                    String f = fields.get(fi);
                    if (!existing.fields().contains(f)) {
                        existing.fields().add(f);
                        String t = fieldTypes.get(f);
                        if (t != null) existing.fieldTypes().put(f, t);
                    }
                }
            }
        }
    }

    private boolean isValidIdentifier(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    /**
     * Extracts the declared type from a raw PlantUML member string like
     * "datenpunkt: Datenpunkt" or "+bezugspunkt : Bezugspunkt" and stores it in fieldTypes.
     */
    private void extractFieldType(String rawMember, String fieldName, Map<String, String> fieldTypes) {
        if (rawMember == null || fieldName == null) return;
        int colonIdx = rawMember.lastIndexOf(':');
        if (colonIdx == -1) return;
        String typePart = rawMember.substring(colonIdx + 1).trim();
        // Strip generic brackets e.g. "List<Balise>" → "Balise", "List" stays if no <
        String typeName = stripGeneric(typePart);
        if (isValidIdentifier(typeName)) {
            fieldTypes.put(fieldName, typeName);
        }
    }

    /**
     * Resolves the navigable type from a PlantUML type string.
     *
     * Rules:
     *  - "WertMitQuelle&lt;String&gt;"  → "WertMitQuelle"  (domain wrapper, keep outer type)
     *  - "List&lt;Balise&gt;"           → "Balise"          (pure collection, extract element type)
     *  - "WertMitQuelle"               → "WertMitQuelle"
     *  - "String"                      → "String"
     */
    private String stripGeneric(String typeName) {
        if (typeName == null) return "";
        typeName = typeName.trim().replaceAll("[;,]$", "");

        int open = typeName.indexOf('<');
        if (open == -1) return typeName;

        String outerType = typeName.substring(0, open).trim();

        // Only unwrap generic parameter for pure collection types (List<Balise> → Balise).
        // For domain types like WertMitQuelle<String> the outer type IS the field type.
        if (COLLECTION_TYPES.contains(outerType)) {
            int close = typeName.lastIndexOf('>');
            if (close > open) {
                String inner = typeName.substring(open + 1, close).trim();
                // Map<K,V> → take value type
                int comma = inner.indexOf(',');
                if (comma != -1) inner = inner.substring(comma + 1).trim();
                return inner;
            }
        }

        return outerType;
    }

    public Map<String, DomainClass> getDomainModelCache() {
        return domainModelCache;
    }

    private String cleanMember(String memberStr) {
        if (memberStr == null || memberStr.isBlank()) {
            return "";
        }
        String clean = memberStr.replaceAll("^[+\\-#~\\s]+", "");
        clean = clean.replaceAll("\\{.*?}\\s*", "");

        if (clean.contains("(")) {
            int openParenIndex = clean.indexOf("(");
            String beforeParen = clean.substring(0, openParenIndex).trim();

            int splitIdx = -1;
            for (int i = beforeParen.length() - 1; i >= 0; i--) {
                char c = beforeParen.charAt(i);
                if (c == ' ' || c == '>') {
                    splitIdx = i;
                    break;
                }
            }

            String methodName;
            if (splitIdx != -1 && splitIdx < beforeParen.length() - 1) {
                methodName = beforeParen.substring(splitIdx + 1).trim();
            } else {
                methodName = beforeParen;
            }

            clean = methodName + clean.substring(openParenIndex);

            int colonIndex = clean.lastIndexOf(":");
            int closeParenIndex = clean.lastIndexOf(")");

            if (colonIndex > closeParenIndex) {
                clean = clean.substring(0, colonIndex).trim();
            }
        } else if (clean.contains(":")) {
             clean = clean.substring(0, clean.indexOf(":")).trim();
        } else {
             int splitIdx = -1;
             for (int i = clean.length() - 1; i >= 0; i--) {
                char c = clean.charAt(i);
                if (c == ' ' || c == '>') {
                    splitIdx = i;
                    break;
                }
            }
             if (splitIdx != -1 && splitIdx < clean.length() - 1) {
                 clean = clean.substring(splitIdx + 1).trim();
             }
        }

        return clean.trim();
    }
}
