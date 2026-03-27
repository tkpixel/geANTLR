package org.geantlr.services;

import jakarta.inject.Singleton;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.abel.Entity;

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
                        for (CharSequence member : entity.getBodier().getFieldsToDisplay()) {
                            fields.add(member.toString());
                        }
                        for (CharSequence member : entity.getBodier().getMethodsToDisplay()) {
                            fields.add(member.toString());
                        }
                        domainModelCache.put(className, new DomainClass(className, fields));
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
}
