package org.polydectes.jstreamlined.schema;

import java.util.List;

public record TableMetadata(String name, List<ColumnMetadata> columns, List<IndexMetadata> indexes,
                            List<RelationshipMetadata> relationships) {
}
