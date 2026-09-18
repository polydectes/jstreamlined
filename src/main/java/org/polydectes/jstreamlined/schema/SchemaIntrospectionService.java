package org.polydectes.jstreamlined.schema;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SchemaIntrospectionService {
    public SchemaMetadata load(javax.sql.DataSource dataSource, String schema) throws SQLException {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String catalog = connection.getCatalog();
            String resolvedSchema = schema == null || schema.isBlank()
                    ? connection.getSchema() : schema;
            Map<String, MutableTable> tables = new LinkedHashMap<>();
            try (ResultSet result = metadata.getTables(catalog, resolvedSchema, "%", new String[]{"TABLE", "VIEW"})) {
                while (result.next()) {
                    String table = result.getString("TABLE_NAME");
                    tables.put(table, new MutableTable(table));
                }
            }
            for (MutableTable table : tables.values()) {
                loadColumns(metadata, catalog, resolvedSchema, table);
                loadIndexes(metadata, catalog, resolvedSchema, table);
                loadRelationships(metadata, catalog, resolvedSchema, table);
            }
            return new SchemaMetadata(catalog, resolvedSchema, tables.values().stream()
                    .map(MutableTable::toMetadata).toList());
        }
    }

    private void loadColumns(DatabaseMetaData metadata, String catalog, String schema, MutableTable table)
            throws SQLException {
        Map<String, Boolean> primaryKeys = new HashMap<>();
        try (ResultSet keys = metadata.getPrimaryKeys(catalog, schema, table.name)) {
            while (keys.next()) primaryKeys.put(keys.getString("COLUMN_NAME"), true);
        }
        try (ResultSet columns = metadata.getColumns(catalog, schema, table.name, "%")) {
            while (columns.next()) {
                table.columns.add(new ColumnMetadata(columns.getString("COLUMN_NAME"),
                        columns.getString("TYPE_NAME"), columns.getInt("COLUMN_SIZE"),
                        columns.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls,
                        primaryKeys.containsKey(columns.getString("COLUMN_NAME"))));
            }
        }
    }

    private void loadIndexes(DatabaseMetaData metadata, String catalog, String schema, MutableTable table)
            throws SQLException {
        Map<String, IndexBuilder> indexes = new LinkedHashMap<>();
        try (ResultSet result = metadata.getIndexInfo(catalog, schema, table.name, false, false)) {
            while (result.next()) {
                String name = result.getString("INDEX_NAME");
                String column = result.getString("COLUMN_NAME");
                if (name != null && column != null) {
                    indexes.computeIfAbsent(name, key -> new IndexBuilder(key, !resultRowNonUnique(result)))
                            .columns.add(column);
                }
            }
        }
        table.indexes.addAll(indexes.values().stream().map(IndexBuilder::toMetadata).toList());
    }

    private boolean resultRowNonUnique(ResultSet result) throws SQLException {
        return result.getBoolean("NON_UNIQUE");
    }

    private void loadRelationships(DatabaseMetaData metadata, String catalog, String schema, MutableTable table)
            throws SQLException {
        try (ResultSet result = metadata.getImportedKeys(catalog, schema, table.name)) {
            while (result.next()) {
                table.relationships.add(new RelationshipMetadata(
                        result.getString("FK_NAME"), result.getString("FKCOLUMN_NAME"),
                        result.getString("PKTABLE_NAME"), result.getString("PKCOLUMN_NAME")));
            }
        }
    }

    private static final class MutableTable {
        private final String name;
        private final List<ColumnMetadata> columns = new ArrayList<>();
        private final List<IndexMetadata> indexes = new ArrayList<>();
        private final List<RelationshipMetadata> relationships = new ArrayList<>();

        private MutableTable(String name) { this.name = name; }
        private TableMetadata toMetadata() { return new TableMetadata(name, columns, indexes, relationships); }
    }

    private record IndexBuilder(String name, boolean unique, List<String> columns) {
        private IndexBuilder(String name, boolean unique) { this(name, unique, new ArrayList<>()); }
        private IndexMetadata toMetadata() { return new IndexMetadata(name, unique, columns); }
    }
}
