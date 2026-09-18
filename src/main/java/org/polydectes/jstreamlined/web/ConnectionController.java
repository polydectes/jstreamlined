package org.polydectes.jstreamlined.web;

import java.sql.SQLException;
import org.polydectes.jstreamlined.config.DataSourceConfiguration.ConnectionDataSourceFactory;
import org.polydectes.jstreamlined.config.ConnectionProperties;
import org.polydectes.jstreamlined.schema.SchemaIntrospectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConnectionController {
    private final SchemaIntrospectionService schema;
    private final ConnectionProperties defaults;
    private final ConnectionDataSourceFactory dataSources;

    public ConnectionController(SchemaIntrospectionService schema, ConnectionProperties defaults,
                                ConnectionDataSourceFactory dataSources) {
        this.schema = schema;
        this.defaults = defaults;
        this.dataSources = dataSources;
    }

    @GetMapping("/")
    String dialog(Model model) {
        model.addAttribute("connection", defaults);
        return "connection";
    }

    @PostMapping("/connect")
    String connect(@RequestParam String username, @RequestParam String password,
                   @RequestParam String database, @RequestParam(defaultValue = "") String schema,
                   Model model) {
        try {
            model.addAttribute("metadata", schema.load(
                    dataSources.create(username, password, database), schema));
            return "schema";
        } catch (SQLException | IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("connection", defaults);
            return "connection";
        }
    }

}
