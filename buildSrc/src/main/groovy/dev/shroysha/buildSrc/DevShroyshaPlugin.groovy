package dev.shroysha.buildSrc


import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class DevShroyshaPlugin implements Plugin<Project> {

    static final String ROOT_NAME = "dev.shroysha", ARCHIVE_NAME = "archive"

    static final URL README_TEMPLATE_URL = DevShroyshaPlugin.class.getResource("/layouts/README_md.tpl")
    static final URL BUILD_FILE_TEMPLATE_URL = DevShroyshaPlugin.class.getResource("/layouts/build_gradle.tpl")

    static final Template README_TEMPLATE = new SimpleTemplateEngine().createTemplate(README_TEMPLATE_URL)
    static final Template BUILD_FILE_TEMPLATE = new SimpleTemplateEngine().createTemplate(BUILD_FILE_TEMPLATE_URL)

    static final boolean isRunningLocally = System.getProperty("user.home") == "/Users/Shawn"


    @Override
    void apply(final Project target) {
        try {
            target.getTasks().getByName("dev.shroysha.common")
        } catch (Exception ex) {
            target.getTasks().register("dev.shroysha.common", DevShroyshaTask.class).get().execute()
        }
    }

    static void debugFile(File file) {
        System.out.println(file.getPath() + " " + file.exists())
    }

    static Map<String, List<String>> readLinesFromBuildFile(Project project, String... args) {
        Map<String, List<String>> results = [:]
        for (String arg in args) {
            results.put(arg, new ArrayList<String>())
        }
        System.out.println(project.getBuildFile())
        List<String> lines = Files.readAllLines(project.getBuildFile().toPath())
        for (String line : lines) {
            for (String arg : args) {
                if (line.contains(arg)) {
                    results.get(arg).add(line.trim())
                }
            }
        }

        return results
    }


    static void overwriteIfDifferent(Path filePath, Template template, Map binding) throws IOException {
        List<String> contents = new ArrayList<>()
        contents.addAll(template.make(binding).toString().split("\n"))

        filePath.toFile().createNewFile()

        if (contents != Files.readAllLines(filePath)) {
            Files.write(filePath, contents, StandardOpenOption.TRUNCATE_EXISTING)
            System.out.println("Overwrote " + filePath.toString())
        } else {
            System.out.println("Kept " + filePath.toString())
        }
    }

}
