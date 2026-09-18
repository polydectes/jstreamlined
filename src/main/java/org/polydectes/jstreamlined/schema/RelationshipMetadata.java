package org.polydectes.jstreamlined.schema;

public record RelationshipMetadata(String name, String sourceColumn, String targetTable, String targetColumn) {
}
