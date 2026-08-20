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
    void fabricMetadataIsValidAndReferencesPackagedResourcesAndClasses() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();

        if (metadata.has("icon")) {
            String icon = metadata.get("icon").getAsString();
            assertNotNull(loader.getResource(icon), () -> "fabric.mod.json references missing icon " + icon);
        }

        String transformerKey = metadata.has("accessWidener")
                ? "accessWidener"
                : metadata.has("classTweaker") ? "classTweaker" : null;
        assertNotNull(transformerKey, "fabric.mod.json must declare an access widener or class tweaker");
        String transformer = metadata.get(transformerKey).getAsString();
        assertNotNull(loader.getResource(transformer),
                () -> "fabric.mod.json references missing " + transformerKey + " " + transformer);

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
                    assertClassExists("fabric.mod.json entrypoint " + entry.getKey(), className);
                }));
    }

    @Test
    void everyConfiguredMixinAndPluginHasACompiledClass() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");
        for (JsonElement element : metadata.getAsJsonArray("mixins")) {
            assertMixinClassesExist(mixinConfigName(element));
        }
    }

    private static void assertMixinClassesExist(String configName) throws IOException {
        JsonObject config = readJson(configName);
        String mixinPackage = config.get("package").getAsString();

        if (config.has("plugin")) {
            assertClassExists(configName + " plugin", config.get("plugin").getAsString());
        }

        for (String section : new String[]{"mixins", "client", "server"}) {
            JsonArray mixins = config.getAsJsonArray(section);
            if (mixins == null) {
                continue;
            }
            mixins.forEach(element -> assertClassExists(configName, mixinPackage + "." + element.getAsString()));
        }
    }

    private static void assertClassExists(String owner, String className) {
        String classResource = className.split("::", 2)[0].replace('.', '/') + ".class";
        assertNotNull(MixinConfigurationTest.class.getClassLoader().getResource(classResource),
                () -> owner + " references missing class " + className);
    }

    private static String mixinConfigName(JsonElement element) {
        return element.isJsonPrimitive()
                ? element.getAsString()
                : element.getAsJsonObject().get("config").getAsString();
    }

    private static JsonObject readJson(String resourceName) throws IOException {
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourceName)) {
            assertNotNull(input, "Missing resource " + resourceName);
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            assertTrue(parsed.isJsonObject(), resourceName + " must contain a JSON object");
            return parsed.getAsJsonObject();
        }
    }
}
