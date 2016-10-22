# Confluence to MD Batch Converter
### This application downloads Confluence files and converts them to MD format.

Uses XSLT templates from https://github.com/highsource/confluence-to-markdown-converter/tree/gh-pages/xslt/ .

#### To build a JAR:

```
mvn assembly:assembly -DskipTests
```

#### To run:

```
java -jar target/confluence-to-md-batch-converter-1.0-SNAPSHOT-jar-with-dependencies.jar -u=my_confluence_login -p=my_confluence_pwd -h=http://my_confluence_host:port -s=ConfluenceSPACE -t=destinationDirectory
```
