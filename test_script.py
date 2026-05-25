with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "if (currentLevel == 10 && Level10State.currentScreen == 4 && Level10State.isCrashActive) {" in line and "font" in lines[i+1]:
        for j in range(i-1, i+25):
            print(lines[j], end="")
        break
