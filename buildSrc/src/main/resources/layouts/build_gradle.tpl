buildscript {
apply from: "dev.shroysha.gradle"
<% if (!buildscriptBlock.isEmpty()) for(String line: buildscriptBlock) out.println line %>
}

plugins {
<% if (!pluginsBlock.isEmpty()) for(String line: pluginsBlock) out.println line %>
}

repositories {
<% if (!repositoriesBlock.isEmpty()) for(String line: repositoriesBlock) out.println line %>
}

dependencies {
<% if (!dependenciesBlock.isEmpty()) for(String line: dependenciesBlock) out.println line %>
}

<% if (!mainBlock.isEmpty()) for(String line: mainBlock) out.println line %>
