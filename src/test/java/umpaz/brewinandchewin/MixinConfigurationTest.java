package umpaz.brewinandchewin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinConfigurationTest {
    @Test
    void fabricMetadataExistsAndIsValidJson() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");

        assertTrue(metadata.has("schemaVersion"));
        assertTrue(metadata.has("id"));
        assertTrue(metadata.has("version"));
    }

    @Test
    void fabricMetadataReferencesPackagedResourcesAndClasses() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();

        JsonElement accessTransformer = metadata.has("accessWidener")
                ? metadata.get("accessWidener")
                : metadata.get("classTweaker");
        assertNotNull(accessTransformer,
                "fabric.mod.json must declare an access widener or class tweaker");
        String accessTransformerName = accessTransformer.getAsString();
        assertNotNull(loader.getResource(accessTransformerName),
                () -> "fabric.mod.json references missing access widener/class tweaker " + accessTransformerName);

        String icon = metadata.get("icon").getAsString();
        assertNotNull(loader.getResource(icon),
                () -> "fabric.mod.json references missing icon " + icon);

        metadata.getAsJsonArray("mixins").forEach(element -> {
            String configName = mixinConfigName(element);
            assertNotNull(loader.getResource(configName),
                    () -> "fabric.mod.json references missing mixin configuration " + configName);
        });

        metadata.getAsJsonObject("entrypoints").entrySet().forEach(entry ->
                entry.getValue().getAsJsonArray().forEach(element -> {
                    String className = element.isJsonPrimitive()
                            ? element.getAsString()
                            : element.getAsJsonObject().get("value").getAsString();
                    String classResource = className.split("::", 2)[0].replace('.', '/') + ".class";
                    assertNotNull(loader.getResource(classResource),
                            () -> "fabric.mod.json references missing entrypoint " + className);
                }));
    }

    @Test
    void everyConfiguredMixinHasACompiledClass() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");
        for (JsonElement element : metadata.getAsJsonArray("mixins")) {
            assertMixinClassesExist(mixinConfigName(element));
        }
    }

    private static String mixinConfigName(JsonElement element) {
        return element.isJsonPrimitive()
                ? element.getAsString()
                : element.getAsJsonObject().get("config").getAsString();
    }

    private static void assertMixinClassesExist(String configName) throws IOException {
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();
        JsonObject config = readJson(configName);
        String packagePath = config.get("package").getAsString().replace('.', '/');

        for (String section : new String[]{"mixins", "client", "server"}) {
            JsonArray mixins = config.getAsJsonArray(section);
            if (mixins == null) {
                continue;
            }
            mixins.forEach(element -> {
                String mixin = element.getAsString();
                String classResource = packagePath + "/" + mixin.replace('.', '/') + ".class";
                assertNotNull(loader.getResource(classResource),
                        () -> configName + " references missing mixin " + mixin);
            });
        }
    }

    private static JsonObject readJson(String resourceName) throws IOException {
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourceName)) {
            assertNotNull(input, "Missing resource " + resourceName);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
