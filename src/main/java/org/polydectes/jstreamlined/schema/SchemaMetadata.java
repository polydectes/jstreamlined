package org.polydectes.jstreamlined.schema;

import java.util.List;

public record SchemaMetadata(String catalog, String schema, List<TableMetadata> tables) {
}
