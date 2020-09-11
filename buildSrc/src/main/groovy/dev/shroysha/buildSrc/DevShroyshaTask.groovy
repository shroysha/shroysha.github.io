package dev.shroysha.buildSrc


import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.StandardOpenOption

class DevShroyshaTask extends DefaultTask {

    static final String TASK_GROUP = "dev.shroysha"

    static Map<String, ?> makeTask(String aName, Closure aAction) {
        return makeTask(aName, aAction, [])
    }


    static Map<String, ?> makeTask(String aName, Closure aAction, List<String> aDependsOn) {
        return [
                name:aName,
                action:aAction,
                group:TASK_GROUP,
                dependsOn:aDependsOn
        ]
    }


    static void githubScript(Project project) {
        //runCommand("rm -f .gitmodules", project)
        for(Project aproject: project.getSubprojects()) {
            new File(project.getProjectDir(), ".gitmodules").createNewFile()
            if(aproject.getDepth() - 1 == project.getDepth()) {
                //runCommand("rm -rf .git", aproject)
                githubScript(aproject)
                githubSubmodule(aproject, project)
            }
        }

        githubInit(project)
        githubCommit(project)
        githubPush(project)
    }


    static boolean isGithubPrivate(Project project) {
        return project.getPath().startsWith(":private")
    }

    static String githubHomepage(Project project) {
        return project.getProjectDir().getPath().replaceFirst(System.getProperty("user.home"), "https:/")
    }

    static String hubCreateArgs(Project project) {
        String createArgs = ""

        if(isGithubPrivate(project)) {
            createArgs += " -p"
        } else {
            createArgs += " -h " + githubHomepage(project)
        }

        if(project.getDescription() != null) {
            createArgs += " -d '" + project.getDescription() + "'"
        }

        return createArgs
    }

    static void githubInit(Project project) {
        runCommand("hub remote remove origin", project)

        runCommand("hub create" + hubCreateArgs(project), project)
        runCommand("hub init", project)
        runCommand("hub add .", project)
    }

    static void githubSubmodule(Project aproject, Project project) {
        String githuburl =  "shroysha/" + aproject.getName()
        String githubpath = aproject.getProjectDir().getAbsolutePath().replace(project.getProjectDir().getAbsolutePath(), ".")
        runCommand("hub submodule add -f -b master " + githuburl + " " + githubpath, project)
    }

    static void githubCommit(Project project) {
        runCommand("hub commit -m buildAutoCommit", project)
        //runCommand("hub push origin master -v", project)
    }

    static void githubPush(Project project) {
        runCommand("hub push -u origin master --force", project)
    }

    static void runCommand(String command, Project project) {
        System.out.println(project.getPath() + " " + command)
        Process pr = Runtime.getRuntime().exec(command, null, project.getProjectDir());
        StringBuilder output = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

         reader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitVal = pr.waitFor();
        System.out.println(output + exitVal);
    }

    @TaskAction
    void execute() {
        System.out.println(getProject().getGroup() + ":" + getProject().getName() + ":" + getProject().getVersion())

        final DevShroyshaTaskExtension myExt = getProject().getExtensions().create("dev.shroysha.extension", DevShroyshaTaskExtension.class, getProject())

        List<Map<String, ?>> taskMaps = new ArrayList<>()

        Map<String, List<String>> retainLines =
                DevShroyshaPlugin.readLinesFromBuildFile(getProject(), "id(", "implementation(")

        myExt.getBuildFileTemplateBinding().pluginsBlock.addAll(retainLines.get("id("))
        myExt.getBuildFileTemplateBinding().dependenciesBlock.addAll(retainLines.get("implementation("))
        myExt.addToBlock("plugins", 'id("dev.shroysha.plugin")')


        taskMaps.add(makeTask("devShroyshaBaseTask", {
            if (myExt.getFlagMap().IS_ROOT) {
                myExt.getReadmeTags().add("Base")
                myExt.addToBlock("repositories", 'mavenCentral()')
                myExt.addToBlock("repositories", 'gradlePluginPortal()')
                myExt.addToBlock("plugins", 'id("base")')
                myExt.addToBlock("plugins", 'id("idea")')
                myExt.addToBlock("plugins", 'id("org.springframework.boot") version "2.2.4.RELEASE" apply false')
                myExt.addToBlock("plugins", 'id("io.spring.dependency-management") version "1.0.9.RELEASE" apply false')
                myExt.addToBlock("plugins", 'id("io.freefair.lombok") version "5.0.0-rc4" apply false')

                if(DevShroyshaPlugin.isRunningLocally) {
                    githubScript(getProject())
                }
            }

            if (myExt.getFlagMap().IS_PYTHON) {
                myExt.getReadmeTags().add("Python")
            }

            if (myExt.getFlagMap().IS_VISUALSTUDIO) {
                myExt.getReadmeTags().add("C#")
//                ext.addToBlock("", "visualStudio {\n" +
//                        '        solutionFileLocation = file("' + getProject().getName() + '.sln")\n' +
//                        "}")
            }

            if (myExt.getFlagMap().IS_JAVA) {
                // addToBlock("repositoriesBlock", "mavenLocal()")
                myExt.addToBlock("repositories", "mavenCentral()")
                myExt.addToBlock("repositories", 'gradlePluginPortal()')
                myExt.addToBlock("plugins", 'id("java-library")')
                myExt.addToBlock("plugins", 'id("io.freefair.lombok")')
                myExt.addToBlock("", 'java { sourceCompatibility = JavaVersion.VERSION_13; targetCompatibility = JavaVersion.VERSION_13 }')
                myExt.getReadmeTags().add("Java")
            }

            if (myExt.getFlagMap().IS_GROOVY) {
                // addToBlock("repositoriesBlock", "mavenLocal()")
                myExt.addToBlock("repositories", "mavenCentral()")
                myExt.addToBlock("repositories", 'gradlePluginPortal()')
                myExt.addToBlock("plugins", 'id("groovy")')

                myExt.addToBlock("dependencies", 'implementation("org.codehaus.groovy:groovy-all:2.5.9")')
                myExt.addToBlock("plugins", 'id("io.freefair.lombok")')
                myExt.addToBlock("", 'java { sourceCompatibility = JavaVersion.VERSION_13; targetCompatibility = JavaVersion.VERSION_13 }')
                myExt.getReadmeTags().add("Groovy")
            }

            if (myExt.getFlagMap().IS_APPLICATION) {
                myExt.addToBlock("plugins", 'id("application")')
                myExt.addToBlock("", 'application { mainClassName = "App" }')
                myExt.getReadmeTags().add("Application")
            }

            if(myExt.getFlagMap().IS_JUNIT) {
                myExt.addToBlock("", 'test { useJUnitPlatform() }')
                myExt.getReadmeTags().add("JUnit")
                myExt.addToBlock("dependencies", 'testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")')
                myExt.addToBlock("dependencies", 'testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")')
            }

            if (myExt.getFlagMap().IS_EAR) {
                myExt.addToBlock("plugins", 'id("ear")')
                myExt.getReadmeTags().add("EAR")

                myExt.addToBlock("repositories", "mavenCentral()")
                myExt.addToBlock("repositories", 'gradlePluginPortal()')

                for (Project project : getProject().getSubprojects()) {
                    myExt.addToBlock("dependencies", 'deploy project(path: "' + project.getPath() + '", configuration: "archives")')
                }
            }

            if (myExt.getFlagMap().IS_SPRINGBOOT) {
                myExt.addToBlock("plugins", 'id("org.springframework.boot")')
                myExt.addToBlock("plugins", 'id("io.spring.dependency-management")')

                if (myExt.getFlagMap().IS_SPRINGBOOT_EJB) {
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-data-jpa")')
                    myExt.addToBlock("", 'bootJar { enabled = false }')
                    myExt.addToBlock("", 'jar { enabled = true }')
                    myExt.getReadmeTags().add("Spring Boot EJB")
                } else if (myExt.getFlagMap().IS_SPRINGBOOT_APPCLIENT) {
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-devtools")')
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-data-jpa")')
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-webflux")')
                    myExt.getReadmeTags().add("Spring Boot App Client")
                } else if (myExt.getFlagMap().IS_SPRINGBOOT_WAR) {
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-devtools")')
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-web")')
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-data-jpa")')
                    myExt.addToBlock("dependencies", 'implementation("org.springframework.boot:spring-boot-starter-actuator")')
                    myExt.addToBlock("dependencies", 'runtimeOnly("com.h2database:h2")')
                    myExt.getReadmeTags().add("Spring Boot WAR")
                }
            }
        }))
        taskMaps.add(makeTask("checkBuildFile", {
            myExt.getDevShroyshaGradleFile().createNewFile()

            try {
                DevShroyshaPlugin.overwriteIfDifferent(getProject().getBuildFile().toPath(), DevShroyshaPlugin.BUILD_FILE_TEMPLATE, myExt.getBuildFileTemplateBinding())
            } catch (IOException e) {
                e.printStackTrace()
            }
        }))
        taskMaps.add(makeTask("checkReadmeFile", {
            if(myExt.getGitIgnoreFile().createNewFile()) {
                Files.writeString(myExt.getGitIgnoreFile().toPath(), "/build/", StandardOpenOption.TRUNCATE_EXISTING)
            }
            try {
                DevShroyshaPlugin.overwriteIfDifferent(myExt.getReadmeFilePath(), DevShroyshaPlugin.README_TEMPLATE, myExt.getReadmeTemplateBinding())
                System.out.println(myExt.getReadmeTags().toString())
            } catch (IOException e) {
                e.printStackTrace()
            }
        }))

        for (Map<String, ?> taskMap : taskMaps) {
//            getProject().getTasks().create(taskMap)
//            getProject().getDefaultTasks().add((String)taskMap.name)
            taskMap.action()
        }
    }

}
