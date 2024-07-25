# Monolizer
A Java program to allow video playback on the command line.

A demonstration can be found [here](https://youtu.be/DjXgTmQYoMc). (Note that this isn't Monolizer but Mono, another project I'm working on)

An older demonstration without instant video playback can be found [here](https://www.youtube.com/watch?v=9XG8wZvWSpc).

<div align="left"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/dynmie/monolizer-java?style=for-the-badge"> <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/dynmie/monolizer-java/gradle.yml?branch=master&logo=github&style=for-the-badge"></div>

### Notable features
- Colored video output
- Instant video playback
- Full pixel video support
- Floyd-Steinberg grayscale dithering
- Auto resizing terminal window

### What doesn't work?
- Pausing a video without audio doesn't work

## Controls
Space: Play/pause

C - Toggle colours

D - Toggle grayscale dithering

F - Toggle full pixel

Q - Quit

## Gallery
![Hollow hunger with dithering enabled](https://github.com/dynmie/monolizer-java/assets/41315732/9e8d1453-c681-4b1e-9a21-8b78b76adb49)

Hollow Hunger - Ironmouse @ 1:05 with dithering enabled

![Hollow hunger with dithering disabled](https://github.com/dynmie/monolizer-java/assets/41315732/e3c24557-5e17-46d7-8cf7-b9a9348cf80c)

Hollow Hunger - Ironmouse @ 1:05 with dithering disabled

![full pixel](https://github.com/dynmie/monolizer-java/assets/41315732/213d43ac-94cf-4e7b-ab7e-0c0b365b3f0b)

Telecaster b boy (long ver.) - Kanata Amane @ 0:26 with full pixel and color enabled

# Getting started
### Prerequisites
- [Java 21 JRE](https://adoptium.net/temurin/releases/?version=21)
- [MongoDB](https://www.mongodb.com/try/download/community)

### Playing videos
```bash
java -jar monolizer.jar
```

## Building
### Prerequisites
- [Java 21 JDK](https://adoptium.net/temurin/releases/?version=21)
- [Git](https://git-scm.com/downloads)

### Cloning the GitHub repository
```bash
git clone https://github.com/dynmie/monolizer-java.git
```
### Compiling
Windows:
```cmd
.\gradlew.bat jar
```

GNU Linux:
```bash
chmod +x ./gradlew
./gradlew jar
```

You can find the output jar at `build/libs`.
