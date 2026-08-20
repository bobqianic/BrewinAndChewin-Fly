package umpaz.brewinandchewin.common;

import org.junit.jupiter.api.Test;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BnCConfigurationTest {
    private static final Method LOAD_COMMON = loadCommonMethod();

    @Test
    void missingCommonConfigUsesDefaultsAndWritesCurrentVersion() throws IOException {
        Path path = Files.createTempDirectory("bnc-config").resolve("common.toml");

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT, common);
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void configWithoutVersionUsesDefaultsAndWritesCurrentVersion() throws IOException {
        Path path = writeConfig("""
                [root]
                levelChatScramble = 9
                levelSignScramble = 9
                levelNameScramble = 9
                """);

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT, common);
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void malformedVersionUsesDefaultsAndWritesCurrentVersion() throws IOException {
        Path path = writeConfig("""
                version = not-a-version
                [root]
                levelChatScramble = 9
                """);

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT, common);
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void unsupportedVersionUsesDefaultsAndWritesCurrentVersion() throws IOException {
        Path path = writeConfig("""
                version = 1
                [root]
                levelChatScramble = 9
                """);

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT, common);
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void supportedLegacyVersionRetainsSupportedValuesAndMigratesRecipeBook() throws IOException {
        Path path = writeConfig(fullConfig(2, false));

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(7, common.root().levelChatScramble());
        assertEquals(6, common.root().levelSignScramble());
        assertEquals(5, common.root().levelNameScramble());
        assertEquals(FluidUnit.LITER, common.keg().capacityUnit());
        assertEquals(250L, common.keg().capacity());
        assertTrue(common.recipeBook().enabled(),
                "version 2 recipe-book state should migrate to the enabled default");
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void currentVersionRetainsAllValidValuesWithoutRewriting() throws IOException {
        String content = fullConfig(BnCConfiguration.CONFIG_VERSION, false);
        Path path = writeConfig(content);

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(7, common.root().levelChatScramble());
        assertEquals(FluidUnit.LITER, common.keg().capacityUnit());
        assertEquals(250L, common.keg().capacity());
        assertFalse(common.recipeBook().enabled());
        assertEquals(content, Files.readString(path));
    }

    @Test
    void malformedFieldsFallBackSafelyAndRewriteCurrentConfig() throws IOException {
        Path path = writeConfig("""
                version = 3
                [root]
                levelChatScramble = not-an-int
                levelSignScramble = 0
                levelNameScramble = 4
                [keg]
                kegCapacityUnit = "not-a-unit"
                kegCapacity = -1
                kegCold = 2147483648
                kegChilly = 3
                kegWarm = nope
                kegHot = 4
                kegBiomeTemp = maybe
                kegDimTemp = false
                [recipe_book]
                enableRecipeBookKeg = false
                """);

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT.root().levelChatScramble(), common.root().levelChatScramble());
        assertEquals(BnCConfiguration.Common.DEFAULT.root().levelSignScramble(), common.root().levelSignScramble());
        assertEquals(4, common.root().levelNameScramble());
        assertEquals(BnCConfiguration.Common.DEFAULT.keg().capacityUnit(), common.keg().capacityUnit());
        assertEquals(BnCConfiguration.Common.DEFAULT.keg().capacity(), common.keg().capacity());
        assertEquals(BnCConfiguration.Common.DEFAULT.keg().cold(), common.keg().cold());
        assertEquals(3, common.keg().chilly());
        assertEquals(BnCConfiguration.Common.DEFAULT.keg().warm(), common.keg().warm());
        assertEquals(4, common.keg().hot());
        assertEquals(BnCConfiguration.Common.DEFAULT.keg().biomeTemp(), common.keg().biomeTemp());
        assertFalse(common.keg().dimTemp());
        assertFalse(common.recipeBook().enabled());
        assertCurrentVersionWasWritten(path);
    }

    @Test
    void unreadableCommonConfigUsesDefaultsAndIsReplaced() throws IOException {
        Path path = Files.createTempDirectory("bnc-config").resolve("common.toml");
        Files.write(path, new byte[]{(byte) 0xc3, (byte) 0x28});

        BnCConfiguration.Common common = loadCommon(path);

        assertEquals(BnCConfiguration.Common.DEFAULT, common);
        assertCurrentVersionWasWritten(path);
    }

    private static Path writeConfig(String content) throws IOException {
        Path path = Files.createTempDirectory("bnc-config").resolve("common.toml");
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static String fullConfig(int version, boolean recipeBookEnabled) {
        return """
                version = %d
                [root]
                levelChatScramble = 7
                levelSignScramble = 6
                levelNameScramble = 5
                [keg]
                kegCapacityUnit = "liters"
                kegCapacity = 250
                kegCold = 2
                kegChilly = 3
                kegWarm = 4
                kegHot = 5
                kegBiomeTemp = true
                kegDimTemp = false
                [recipe_book]
                enableRecipeBookKeg = %s
                """.formatted(version, recipeBookEnabled);
    }

    private static void assertCurrentVersionWasWritten(Path path) throws IOException {
        String saved = Files.readString(path);
        assertTrue(saved.contains("version = " + BnCConfiguration.CONFIG_VERSION));
    }

    private static BnCConfiguration.Common loadCommon(Path path) {
        try {
            return (BnCConfiguration.Common) LOAD_COMMON.invoke(null, path);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(ex.getCause());
        }
    }

    private static Method loadCommonMethod() {
        try {
            Method method = BnCConfiguration.class.getDeclaredMethod("loadCommon", Path.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
