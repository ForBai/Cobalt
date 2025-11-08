package org.cobalt.init;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.spongepowered.asm.mixin.Mixins;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AddonPrelaunchLoader {
    
    public static void loadAddons() {
        Path addonsDir = Paths.get("config/cobalt/addons/");
        System.out.println("Scanning addons directory: " + addonsDir.toAbsolutePath());
        
        if (!Files.isDirectory(addonsDir)) {
            try {
                Files.createDirectories(addonsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jarPath : stream) {
                System.out.println("  Found addon JAR: " + jarPath);
                FabricLauncherBase.getLauncher().addToClassPath(jarPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jarPath : stream) {
                registerAddonMixins(jarPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void registerAddonMixins(Path jarPath) {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                
                if (name.endsWith(".mixins.json") && !name.equals("cobalt.mixins.json")) {
                    if (name.contains("client") && FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
                        System.out.println("    Skipping client-only mixin in non-client env: " + name);
                        continue;
                    }
                    
                    try {
                        synchronized (Mixins.class) {
                            Mixins.addConfiguration(name);
                        }
                        System.out.println("    Registered addon mixin config: " + name);
                    } catch (Exception e) {
                        System.err.println("    Failed to register mixin config: " + name);
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error scanning addon JAR for mixins: " + jarPath);
            e.printStackTrace();
        }
    }
}