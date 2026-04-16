import re

with open('build.gradle', 'r') as f:
    content = f.read()

content = content.replace("sourceCompatibility = 21", "sourceCompatibility = 17")
content = content.replace("targetCompatibility = 21", "targetCompatibility = 17")
content = content.replace("jvmTarget = '21'", "jvmTarget = '17'")

with open('build.gradle', 'w') as f:
    f.write(content)
