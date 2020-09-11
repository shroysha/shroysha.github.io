package dev.shroysha.buildSrc

import dev.shroysha.buildSrc.DevShroyshaPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

import java.nio.file.Path

class DevShroyshaTaskExtension {

    private final Project project

    private final List<String> readmeTags = new ArrayList<>()
    private final Map<String, List<String>> buildFileBinding = ["buildscriptBlock" : [],
                                                                "repositoriesBlock": [],
                                                                "pluginsBlock"     : [],
                                                                "mainBlock"        : [],
                                                                "dependenciesBlock": []]

    private final Map<String, Boolean> flagMap

    DevShroyshaTaskExtension(Project project) {
        this.project = project

        flagMap = [
                IS_ROOT                : project.getName() == DevShroyshaPlugin.ROOT_NAME,
                IS_ARCHIVE             : project.getName() == DevShroyshaPlugin.ARCHIVE_NAME,
                IS_PYTHON              : getPythonMainFile().exists(),
                IS_VISUALSTUDIO        : getVisualStudioFile().exists(),
                IS_JAVA                : getJavaSrcDir().exists(),
                IS_GROOVY              : getGroovySrcDir().exists(),
                IS_APPLICATION  : getGroovyApplicationMainFile().exists() || getJavaApplicationMainFile().exists(),
                IS_JUNIT : getTestDir().exists(),
                IS_SPRINGBOOT          : getSpringBootApplicationFile().exists(),
                IS_SPRINGBOOT_EJB      : getProject().getName().endsWith("ejb"),
                IS_SPRINGBOOT_APPCLIENT: getProject().getName().endsWith("app-client"),
                IS_SPRINGBOOT_WAR      : getProject().getName().endsWith("war"),
                IS_EAR                 : getEarSrcDir().exists()
        ]

    }

    Project getProject() {
        return project
    }

    Map<String, Boolean> getFlagMap() {
        return flagMap
    }

    List<String> getReadmeTags() {
        return readmeTags
    }

    @InputFile
    File getDevShroyshaGradleFile() {
        return new File(getProject().getProjectDir(), "dev.shroysha.gradle")
    }

    @InputFile
    File getIntellijImlFile() {
        return new File(getProject().getProjectDir(), getProject().getName() + ".iml")
    }

    @InputFile
    File getGitIgnoreFile() {
        return new File(getProject().getProjectDir(), ".gitignore")
    }

    @InputFile
    File getGroovySrcDir() {
        return new File(getProject().getProjectDir(), "/src/main/groovy")
    }

    @InputFile
    File getTestDir() {
        return new File(getProject().getProjectDir(), "/src/test")
    }

    @Input
    String getDefaultGroovyPackage() {
        return "dev.shroysha." + getProject().getName()
                .replace(":archive", "")
                .replace(":", ".")
                .replace("-", ".")
    }

    @InputFile
    File getDefaultGroovyPackageDir() {
        return new File(getGroovySrcDir(), getDefaultGroovyPackage().replace(".", "/"))
    }

    @InputFile
    File getGroovyApplicationMainFile() {
        return new File(getDefaultGroovyPackageDir(), "App.groovy")
    }


    @InputFile
    File getJavaSrcDir() {
        return new File(getProject().getProjectDir(), "/src/main/java")
    }


    @InputFile
    File getDefaultJavaPackageDir() {
        return new File(getJavaSrcDir(), getDefaultJavaPackage().replace(".", "/"))
    }

    @InputFile
    File getJavaApplicationMainFile() {
        return new File(getDefaultJavaPackageDir(), "App.java")
    }

    @InputFile
    File getPythonMainFile() {
        return new File(getProject().getProjectDir(), "__main__.py")
    }

    @InputFile
    File getVisualStudioFile() {
        return new File(getProject().getProjectDir(), getProject().getName() + ".sln")
    }

    @InputFile
    File getSpringBootApplicationFile() {
        return new File(getProject().getProjectDir(), "/src/main/resources/application.yml")
    }

    @InputFile
    File getEarSrcDir() {
        return new File(getProject().getProjectDir(), "/src/main/application")
    }


    @InputFile
    Path getReadmeFilePath() {
        return new File(getProject().getProjectDir(), "README.md").toPath()
    }


    @InputFile
    File getJavadocDir() {
        return new File(getProject().getProjectDir(), "/docs/javadoc")
    }


    @Input
    String getDefaultJavaPackage() {
        return "dev.shroysha." + getProject().getName()
                .replace(":archive", "")
                .replace(":", ".")
                .replace("-", ".")

    }

    @Input
    String getGitHubUrlPath() {
        return "https://github.com/shroysha/" + getProject().getName()
    }


    @Input
    List<String> getSubmoduleLinks() {

        Closure<Integer> sortMethod = { Project a, Project b ->
            boolean aIsArchive = a.getPath().startsWith(":archive")
            boolean bIsArchive = b.getPath().startsWith(":archive")

            if (aIsArchive && !bIsArchive)
                return 1
            else if (!aIsArchive && bIsArchive)
                return -1
            return a.getPath() <=> b.getPath()
        }
        // Collections.sort(submodules)

        List<String> links = new ArrayList<>()
        for (Project submodule : getProject().getSubprojects().sort(sortMethod)) {

            int relDepth = submodule.getDepth() - getProject().getDepth() - 1
            String spacing = new String(new char[relDepth]).replace("\0", "\t")

            String relPath = submodule.getPath().replaceFirst(getProject().getPath(), "").replace(":", "/")

            if (getProject().getPath() != ":") {
                relPath = relPath.replaceFirst("/", "")
            }

            links.add(spacing + "1. [" + submodule.getName() + "](https://github.com/shroysha/" + submodule.getName() + ")")
//            List<String> descriptionLines = DevShroyshaPlugin.readLinesFromBuildFile(submodule, "description").description;
//
//            if (!descriptionLines.isEmpty()) {
//                String description = descriptionLines.get(0)
//                        .replace("description = ", "")
//                        .replace("\"", "")
//                links.add("   - " + description);
//            }
        }
        return links
    }

    @Input
    Map getReadmeTemplateBinding() {
        Map binding = [name          : getProject().getName(),
                       githubUrl     : getGitHubUrlPath(),
                       isRoot        : flagMap.IS_ROOT,
                       submoduleLinks: getSubmoduleLinks()]
        if (!binding.isRoot) {
            binding.put("javadocExists", getJavadocDir().exists())
            binding.put("description", getProject().getDescription())
            binding.put("tags", readmeTags.toString())
        }
        return binding
    }

    @Input
    Map getBuildFileTemplateBinding() {
        return buildFileBinding
    }

    void addToBlock(String block, String contents) {
        if (block == "") block = "main"
        block += "Block"
        if (!buildFileBinding.get(block).contains(contents)) {
            buildFileBinding.get(block).add(contents)
        }
    }
}
