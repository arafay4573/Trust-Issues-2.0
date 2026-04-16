import re

with open('android/build.gradle', 'r') as f:
    content = f.read()

content = content.replace('sourceCompatibility JavaVersion.VERSION_1_8', 'sourceCompatibility JavaVersion.VERSION_17')
content = content.replace('targetCompatibility JavaVersion.VERSION_1_8', 'targetCompatibility JavaVersion.VERSION_17')
content = content.replace('jvmTarget = "1.8"', 'jvmTarget = "17"')

with open('android/build.gradle', 'w') as f:
    f.write(content)
