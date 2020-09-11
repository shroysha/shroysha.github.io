# $name
<% if (isRoot) out.println """
[![CC-0 license](https://img.shields.io/badge/License-CC--0-blue.svg)](https://creativecommons.org/licenses/by-nd/4.0)
![GitHub last commit](https://img.shields.io/github/last-commit/shroysha/shroysha.github.io)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/shroysha/shroysha.github.io/Gradle-Clean)
[![Documentation Status](https://readthedocs.org/projects/ansicolortags/badge/?version=latest)](http://ansicolortags.readthedocs.io/?badge=latest)
![GitHub repo size](https://img.shields.io/github/repo-size/shroysha/shroysha.github.io)

## Authors
**Shawn Shroyer**  --
<shroysha@gmail.com>
> *Shawn_Shroyer_Resume* | [PDF](docs/Shawn_Shroyer_Resume.pdf) | [DOCX](docs/Shawn_Shroyer_Resume.docx)
"""

if(!submoduleLinks.isEmpty()) {
out.println """
## Subprojects """
for(String line: submoduleLinks) out.println line
}

if (isRoot) { out.println """
## Guidelines
- [x] Standardize all projects' structure with MVC packaging and Gradle
- [x] Implement Lombok annotations when possible
- [x] Implement Spring Boot for all client-server applications
- [x] Normalize documentation with JavaDoc and Sphinx
- [x] Normalize GitHub pages with README.md and Jekyll
- [x] Conform to Eclipse code style
""" }  else {
if (javadocExists) out.println '> see JavaDoc: [JavaDoc](docs/javadoc/index.html)'
out.println """
## Description
$description

### Tags
$tags
"""
} %>


