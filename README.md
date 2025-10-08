# MelodySky+

MelodySky+ is a DLC for [MelodySky](https://melodysky.xyz)

Learn more at our [Official Website](https://melodysky.plus/)

## Contribute Guide

We are so excited that you are interested in helping improve the mod.

Before starting your contribution, you need to do the follow steps to setup your IDE

### Preparation

- JDK 1.8 [Amazon Corretto recommended](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html)
- Java IDE [Intelli IDEA CE recommended](https://www.jetbrains.com/idea/download/)
- [Maven](https://maven.apache.org/)
- [MelodySky Jar](https://discord.gg/u8pk6aaCQ9)

### Installation

1. Make sure you have gotten the things [Preparation](#preparation) required.
2. run the following command to install melodysky in your local maven

```bash
cd ./melody
mvn install:install-file -Dfile=/path/to/your/jar -DgroupId=xyz.melody -DartifactId=melodySky -Dversion=[the version of melodysky] -Dpackaging=jar
```

make sure the **MelodySky** version is the same as [gradle.properties](./gradle.properties)