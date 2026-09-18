package org.polydectes.jstreamlined.schema;

public record ColumnMetadata(String name, String typeName, int size, boolean nullable, boolean primaryKey) {
}
