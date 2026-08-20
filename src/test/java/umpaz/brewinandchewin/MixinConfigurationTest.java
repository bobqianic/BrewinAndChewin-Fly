package umpaz.brewinandchewin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MixinConfigurationTest {
    @Test
    void fabricMetadataReferencesPackagedResourcesAndClasses() throws IOException {
        JsonObject metadata = readJson("fabric.mod.json");
        ClassLoader loader = MixinConfigurationTest.class.getClassLoader();

        String accessWidener = metadata.get("accessWidener").getAsString();
        assertNotNull(loader.getResource(accessWidener),
                () -> "fabric.mod.json references missing class tweaker " + accessWidener);

        metadata.getAsJsonArray("mixins").forEach(element -> {
            String configName = element.getAsString();
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
        for (var element : metadata.getAsJsonArray("mixins")) {
            assertMixinClassesExist(element.getAsString());
        }
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
