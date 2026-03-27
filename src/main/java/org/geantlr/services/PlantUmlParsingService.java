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

@Singleton
public class PlantUmlParsingService {

    private final Map<String, DomainClass> domainModelCache = new HashMap<>();

    public void parseDomainModel(String pumlContent) {
        domainModelCache.clear();
        try {
            SourceStringReader reader = new SourceStringReader(pumlContent);
            if (reader.getBlocks().isEmpty()) {
                return;
            }

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
                            for (CharSequence member : entity.getBodier().getFieldsToDisplay()) {
                                String clean = cleanMember(member.toString());
                                if (!clean.isEmpty()) fields.add(clean);
                            }
                            for (CharSequence member : entity.getBodier().getMethodsToDisplay()) {
                                String clean = cleanMember(member.toString());
                                if (!clean.isEmpty()) fields.add(clean);
                            }
                            domainModelCache.put(className, new DomainClass(className, fields));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, DomainClass> getDomainModelCache() {
        return domainModelCache;
    }

    private String cleanMember(String memberStr) {
        if (memberStr == null || memberStr.isBlank()) {
            return "";
        }
        String clean = memberStr.replaceAll("^[+\\-#~\\s]+", "");
        clean = clean.replaceAll("\\{.*?\\}\\s*", "");

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
